package se.vgregion.mvk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.ServicePointProviderEnum;
import se._1177.lmn.service.LmnService;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;
import se.vgregion.mvk.controller.model.Cart;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static se._1177.lmn.service.util.Constants.ACTION_SUFFIX;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class OrderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private UserProfileController userProfileController;

    @Autowired
    private CollectDeliveryController collectDeliveryController;

    @Autowired
    private LmnService lmnService;

    @Autowired
    private Cart cart;

    private MedicalSupplyPrescriptionsHolder medicalSupplyPrescriptions;

    private Map<String, Boolean> chosenItemMap = new HashMap<>();

    private ExecutorService executorService;

    public ExecutorService getExecutor() {
        if (executorService != null) {
            return executorService;
        }
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory();

        threadFactory.setDaemon(true);
        threadFactory.setThreadGroupName("backgroundTasksGroup");
        threadFactory.setThreadNamePrefix("backgroundTask");

        executorService = Executors.newCachedThreadPool(threadFactory);

        return executorService;
    }

    @PostConstruct
    public void  init() {
        try {
            this.medicalSupplyPrescriptions = lmnService.getMedicalSupplyPrescriptionsHolder(
                    userProfileController.getUserProfile().getUserProfile().getSubjectOfCareId());



            for (PrescriptionItemType prescriptionItem : medicalSupplyPrescriptions.orderable) {
                String prescriptionId = prescriptionItem.getPrescriptionId();
                chosenItemMap.put(prescriptionId, cart.getItemsInCart().contains(prescriptionId));
                cart.addPrescriptionItemForInfo(prescriptionId, prescriptionItem);
            }

            getExecutor().submit(() -> {
                // Just calling any method will init the bean. We do this to load the delivery points.
                collectDeliveryController.loadDeliveryPointsForAllSuppliers();


            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            String msg = "Internt kommunikationsfel. Dina produkter kunde inte hämtas. Försök senare.";

            FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
        }
    }

    public List<PrescriptionItemType> getMedicalSupplyPrescriptions() {
        return medicalSupplyPrescriptions.orderable;
    }

    public List<PrescriptionItemType> getNoLongerOrderableMedicalSupplyPrescriptions() {
        return medicalSupplyPrescriptions.noLongerOrderable;
    }

    public Map<String, Boolean> getChosenItemMap() {
        return chosenItemMap;
    }

    public String toDelivery() {

        List<String> toCart = new ArrayList<>();
        List<PrescriptionItemType> chosenPrescriptionItems = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : chosenItemMap.entrySet()) {
            if (entry.getValue()) {
                toCart.add(entry.getKey());
                chosenPrescriptionItems.add(cart.getPrescriptionItemInfo()entry.getKey());
            }
        }

        cart.setItemsInCart(toCart);

        if (cart.getItemsInCart().size() == 0) {
            String msg = "Du har inte valt någon produkt. Välj minst en för att fortsätta.";
            FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_WARN, msg, msg));

            return "order";
        } else {
            prepareInCaseOfCollectDelivery(chosenPrescriptionItems);

            return "delivery" + ACTION_SUFFIX;
        }

    }

    private void prepareInCaseOfCollectDelivery(final List<PrescriptionItemType> chosenPrescriptionItems) {

        final Set<DeliveryMethodEnum>               remainingDeliveryMethods =
                new HashSet<>(Arrays.asList(DeliveryMethodEnum.values()));

        final Set<DeliveryNotificationMethodEnum>   allDeliveryNotificationMethods =
                new HashSet<>(Arrays.asList(DeliveryNotificationMethodEnum.values()));

        final Set<ServicePointProviderEnum>         allServicePointProviders =
                new HashSet<>(Arrays.asList(ServicePointProviderEnum.values()));

        Map<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>> allCollectCombinations =
                getAllCombinationsOfProvidersAndNotificationMethods(
                        allDeliveryNotificationMethods, allServicePointProviders);

        /*for (DeliveryNotificationMethodEnum deliveryNotificationMethod : allDeliveryNotificationMethods) {
            for (ServicePointProviderEnum servicePointProvider : allServicePointProviders) {
                allCollectCombinations.add(servicePointProvider.value() + ":" + deliveryNotificationMethod.value());
            }
        }*/

        final Map<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>> remainingCollectCombinations =
                allCollectCombinations;

//        final Set<DeliveryMethodEnum> remainingDeliveryMethods

        for (PrescriptionItemType prescriptionItem : chosenPrescriptionItems) {

            // Find out which deliveryNotificationMethod(s) that are available for all items.
            // Also find out which ServicePointProvider that are available for all items.

            Set<DeliveryMethodEnum> deliveryMethodsForItem = new HashSet<>();
//            Set<String> allCollectCombinationsForItem = new HashSet<>();
            Map<ServicePointProviderEnum, List<DeliveryNotificationMethodEnum>> combinationsForItem =
                    new HashMap<>();

            for (DeliveryAlternativeType deliveryAlternative : prescriptionItem.getDeliveryAlternative()) {
                deliveryMethodsForItem.add(deliveryAlternative.getDeliveryMethod());

                // Sum possible combinations
                ServicePointProviderEnum itemServiceProvider = deliveryAlternative.getServicePointProvider();

                combinationsForItem.put(itemServiceProvider, new ArrayList<>());

                for (DeliveryNotificationMethodEnum notificationMethod :
                        deliveryAlternative.getDeliveryNotificationMethod()) {

                    combinationsForItem.get(itemServiceProvider).add(notificationMethod);
                }

            }

            remainingDeliveryMethods.retainAll(deliveryMethodsForItem);

            // First remove all providers which aren't options...
            remainingCollectCombinations.keySet().retainAll(combinationsForItem.keySet());

            // ... then, for each provider, remove the notification methods which aren't options.
            for (Map.Entry<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>> remaining : remainingCollectCombinations.entrySet()) {
                remaining.getValue().retainAll(combinationsForItem.get(remaining.getKey()));
            }

        }
            /*remainingDeliveryNotificationMethods.retainAll(
                    deliveryAlternative.getDeliveryNotificationMethod());

            remainingServicePointProviders.retainAll(
                    Arrays.asList(deliveryAlternative.getServicePointProvider()));*/

//        getExecutor().submit(() -> {

//                if (remainingDeliveryNotificationMethods.size() > 0) {
        collectDeliveryController.setPossibleCollectCombinations(remainingCollectCombinations);

        collectDeliveryController.setPossibleDeliveryMethods(remainingDeliveryMethods);
//                }

            /*if (allServicePointProviders.size() == 0) {
                // We have a challenge grouping the items which can be ordered together

                Map<ServicePointProviderEnum, List<PrescriptionItemType>> groupedItems = new HashMap<>();

                for (PrescriptionItemType item : chosenPrescriptionItems) {

                }
            }*/

//        });
    }

    private Map<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>> getAllCombinationsOfProvidersAndNotificationMethods(Set<DeliveryNotificationMethodEnum> allDeliveryNotificationMethods, Set<ServicePointProviderEnum> allServicePointProviders) {
        Map<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>> allCollectCombinations = new HashMap<>();

        for (ServicePointProviderEnum servicePointProvider : allServicePointProviders) {
            allCollectCombinations.put(servicePointProvider, new HashSet<>());

            for (DeliveryNotificationMethodEnum deliveryNotificationMethod : allDeliveryNotificationMethods) {
                allCollectCombinations.get(servicePointProvider).add(deliveryNotificationMethod);
            }
        } return allCollectCombinations;
    }
}
