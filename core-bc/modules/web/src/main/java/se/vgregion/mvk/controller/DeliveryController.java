package se.vgregion.mvk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.ServicePointProviderEnum;
import se.vgregion.mvk.controller.model.Cart;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static se._1177.lmn.service.util.Constants.ACTION_SUFFIX;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DeliveryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeliveryController.class);

    @Autowired
    private Cart cart;

    @Autowired
    private HomeDeliveryController homeDeliveryController;

    @Autowired
    private CollectDeliveryController collectDeliveryController;

    private DeliveryMethodEnum deliveryMethod = null;//DeliveryMethodEnum.HEMLEVERANS; // Default. Will this possibly change so a user can have a personal default?
    private boolean userNeedsToChooseDeliveryMethodForEachItem;
    private Set<DeliveryMethodEnum> possibleDeliveryMethodsFittingAllItems;

    // Chosen delivery method for each item
    private Map<PrescriptionItemType, String> deliveryMethodForEachItem = new HashMap<>();

    public DeliveryMethodEnum getDeliveryMethod() {

        // First, check if only one choice is possible. If so, choose that.
        if (anyDeliveryMethodFitsAll()) {
            if (possibleDeliveryMethodsFittingAllItems.contains(DeliveryMethodEnum.HEMLEVERANS) &&
                    !possibleDeliveryMethodsFittingAllItems.contains(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)) {
                return DeliveryMethodEnum.HEMLEVERANS;
            } else if (possibleDeliveryMethodsFittingAllItems.contains(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE) &&
                    !possibleDeliveryMethodsFittingAllItems.contains(DeliveryMethodEnum.HEMLEVERANS)) {
                return DeliveryMethodEnum.UTLÄMNINGSSTÄLLE;
            } else {
                return deliveryMethod;
            }
        }

        return null;
    }

    public void setDeliveryMethod(DeliveryMethodEnum deliveryMethod) {
        if (anyDeliveryMethodFitsAll() && deliveryMethod != null) {
            // This is set for all items.
            Map<PrescriptionItemType, String> deliveryMethodForEachItem = getDeliveryMethodForEachItem();

            for (Map.Entry<PrescriptionItemType, String> entry : deliveryMethodForEachItem.entrySet()) {
                entry.setValue(deliveryMethod.name());
            }
        }
        this.deliveryMethod = deliveryMethod;
    }

    public DeliveryMethodEnum getUtlamningsstalleEnum() {
        return DeliveryMethodEnum.UTLÄMNINGSSTÄLLE;
    }

    public String getUtlamningsstalleValue() {
        return DeliveryMethodEnum.UTLÄMNINGSSTÄLLE.name();
    }

    public DeliveryMethodEnum getHemleveransEnum() {
        return DeliveryMethodEnum.HEMLEVERANS;
    }

    public String getHemleveransValue() {
        return DeliveryMethodEnum.HEMLEVERANS.name();
    }

    public String toDeliveryMethod() {

        homeDeliveryController.resetChoices();
        collectDeliveryController.resetChoices();

        if (anyDeliveryMethodFitsAll()) {

            if (deliveryMethod == null) {
                String msg = "Du måste välja leveranssätt.";
                FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg,
                        msg));

                return "delivery";
            }

            setDeliveryMethodForAllItems(deliveryMethod);

            if (deliveryMethod.equals(DeliveryMethodEnum.HEMLEVERANS)) {

                homeDeliveryController.setNextViewIsCollectDelivery(false);

                return "homeDelivery" + ACTION_SUFFIX;
            } else if (deliveryMethod.equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)) {
                return "collectDelivery" + ACTION_SUFFIX;
            } else {
                throw new RuntimeException("Unexpected " + DeliveryMethodEnum.class.getCanonicalName());
            }
        } else {
            // The user must have chosen both delivery methods and thus needs to go through both views.

            // Validate delivery method is chosen for all items
            for (String deliveryMethod : getDeliveryMethodForEachItem().values()) {
                if (deliveryMethod == null) {
                    String msg = "Du måste välja leveranssätt för alla produkter.";
                    FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg,
                            msg));

                    return "delivery";
                }
            }

            homeDeliveryController.setNextViewIsCollectDelivery(true);
            return "homeDelivery" + ACTION_SUFFIX;
        }
    }

    List<DeliveryAlternativeType> getPossibleDeliveryAlternatives(PrescriptionItemType prescriptionItem) {
        return getDeliveryAlternativeMatchingDeliveryMethod(
                prescriptionItem, getDeliveryMethodForEachItem().get(prescriptionItem));
    }

    private Map<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>>
    getAllCombinationsOfProvidersAndNotificationMethods(
            Set<DeliveryNotificationMethodEnum> allDeliveryNotificationMethods,
            Set<ServicePointProviderEnum> allServicePointProviders) {

        Map<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>> allCollectCombinations = new HashMap<>();

        for (ServicePointProviderEnum servicePointProvider : allServicePointProviders) {
            allCollectCombinations.put(servicePointProvider, new HashSet<>());

            for (DeliveryNotificationMethodEnum deliveryNotificationMethod : allDeliveryNotificationMethods) {
                allCollectCombinations.get(servicePointProvider).add(deliveryNotificationMethod);
            }
        }

        return allCollectCombinations;
    }

    private List<DeliveryAlternativeType> getDeliveryAlternativeMatchingDeliveryMethod(PrescriptionItemType prescriptionItem,
                                                                                       String deliveryMethodName) {

        List<DeliveryAlternativeType> matching = new ArrayList<>();
        for (DeliveryAlternativeType deliveryAlternativeType : prescriptionItem.getDeliveryAlternative()) {
            if (deliveryAlternativeType.getDeliveryMethod().name().equals(deliveryMethodName)) {
                matching.add(deliveryAlternativeType);
            }
        }

        if (matching.size() == 0) {
            throw new RuntimeException("No matching delivery method was found.");
        }

        return matching;
    }

    private void setDeliveryMethodForAllItems(DeliveryMethodEnum deliveryMethod) {
        Map<PrescriptionItemType, String> deliveryMethodForEachItem = getDeliveryMethodForEachItem();

        Iterator<PrescriptionItemType> iterator = deliveryMethodForEachItem.keySet().iterator();

        while (iterator.hasNext()) {
            deliveryMethodForEachItem.put(iterator.next(), deliveryMethod.name());
        }
    }

    public boolean isUserNeedsToChooseDeliveryMethodForEachItem() {
        return userNeedsToChooseDeliveryMethodForEachItem;
    }

    public void setUserNeedsToChooseDeliveryMethodForEachItem(boolean userNeedsToChooseDeliveryMethodForEachItem) {
        this.userNeedsToChooseDeliveryMethodForEachItem = userNeedsToChooseDeliveryMethodForEachItem;
    }

    public void setPossibleDeliveryMethodsFittingAllItems(
            Set<DeliveryMethodEnum> possibleDeliveryMethodsFittingAllItems) {

        this.possibleDeliveryMethodsFittingAllItems = possibleDeliveryMethodsFittingAllItems;
    }

    public Set<DeliveryMethodEnum> getPossibleDeliveryMethodsFittingAllItems() {
        return possibleDeliveryMethodsFittingAllItems;
    }

    public Map<PrescriptionItemType, String> getDeliveryMethodForEachItem() {

        // Remove all which don't exist in cart since there may be remaining items from previous choices.
        deliveryMethodForEachItem.keySet().retainAll(cart.getItemsInCart());

        // Make sure chosen items are in the map. Otherwise add them.
        for (PrescriptionItemType prescriptionItemType : cart.getItemsInCart()) {
            if (!deliveryMethodForEachItem.containsKey(prescriptionItemType)) {
                String decidedDeliveryMethod = decideOnDeliveryMethod(prescriptionItemType);
                
                deliveryMethodForEachItem.put(prescriptionItemType, decidedDeliveryMethod);
            } else {

                // Check if the current choice is allowed. Otherwise reset.
                List<String> allowedMethods = new ArrayList<>();

                prescriptionItemType.getDeliveryAlternative().forEach(d -> allowedMethods.add(d.getDeliveryMethod().name()));

                if (!allowedMethods.contains(deliveryMethodForEachItem.get(prescriptionItemType))) {
                    deliveryMethodForEachItem.put(prescriptionItemType, null);
                }
            }
        }

        return deliveryMethodForEachItem;
    }

    private String decideOnDeliveryMethod(PrescriptionItemType prescriptionItem) {

        List<DeliveryAlternativeType> deliveryAlternatives = prescriptionItem.getDeliveryAlternative();

        Boolean homeDeliveryPossible = anyDeliveryAlternativeHasDeliveryMethod(deliveryAlternatives,
                DeliveryMethodEnum.HEMLEVERANS);

        Boolean collectDeliveryPossible = anyDeliveryAlternativeHasDeliveryMethod(deliveryAlternatives,
                DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);

        if (homeDeliveryPossible && collectDeliveryPossible) {
            return null;
        } else if (homeDeliveryPossible && !collectDeliveryPossible) {
            return getHemleveransValue();
        } else if (!homeDeliveryPossible && collectDeliveryPossible) {
            return getUtlamningsstalleValue();
        } else {
            String msg = prescriptionItem.getArticle().getArticleName() + " kan inte beställas.";
            FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
            return null;
        }
    }

    public boolean deliveryMethodUsedForAnyItem(DeliveryMethodEnum deliveryMethod) {
        return deliveryMethodForEachItem.values().contains(deliveryMethod.name());
    }

    public Boolean anyDeliveryAlternativeHasDeliveryMethod(List<DeliveryAlternativeType> deliveryAlternatives,
                                                           DeliveryMethodEnum deliveryMethod) {
        for (DeliveryAlternativeType deliveryAlternative : deliveryAlternatives) {
            if (deliveryAlternative.getDeliveryMethod().equals(deliveryMethod)) {
                return true;
            }
        }

        return false;
    }

    public boolean anyDeliveryMethodFitsAll() {
        return possibleDeliveryMethodsFittingAllItems.contains(DeliveryMethodEnum.HEMLEVERANS)
                ||
                possibleDeliveryMethodsFittingAllItems.contains(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
    }
}
