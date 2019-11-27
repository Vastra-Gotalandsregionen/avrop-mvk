package se._1177.lmn.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._2.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._2.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._2.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._2.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._2.PrescriptionItemType;
import se._1177.lmn.controller.model.Cart;
import se._1177.lmn.controller.model.PrescriptionItemInfo;
import se._1177.lmn.model.ServicePointProvider;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static se._1177.lmn.service.util.Constants.ACTION_SUFFIX;

/**
 * Controller class which handles the model for the view where delivery type is chosen. Either
 * DeliveryMethodEnum.HEMLEVERANS or DeliveryMethodEnum.UTLÄMNINGSSTÄLLE is chosen for all or, if none of them are
 * available for all {@link PrescriptionItemType}s, a {@link DeliveryMethodEnum} is chosen for each
 * {@link PrescriptionItemType}.
 *
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DeliveryController {

    public static final String VIEW_NAME = "Leveranssätt";

    private static final Logger LOGGER = LoggerFactory.getLogger(DeliveryController.class);

    @Autowired
    private Cart cart;

    @Autowired
    private PrescriptionItemInfo prescriptionItemInfo;

    @Autowired
    private HomeDeliveryController homeDeliveryController;

    @Autowired
    private CollectDeliveryController collectDeliveryController;

    @Autowired
    private UserProfileController userProfileController;

    @Autowired
    private UtilController utilController;

    @Autowired
    private NavigationController navigationController;

    private DeliveryMethodEnum deliveryMethod = null;
    private boolean userNeedsToChooseDeliveryMethodForEachItem;
    private Set<DeliveryMethodEnum> possibleDeliveryMethodsFittingAllItems;

    // Chosen delivery method for each item
    private Map<PrescriptionItemType, String> deliveryMethodForEachItem = new HashMap<>();

    /**
     * If both methods are available, choose UTLÄMNINGSSTÄLLE since that is preferred by design. If only one method is
     * available, choose that. If none are available, return null.
     *
     * @return the {@link DeliveryMethodEnum} according to the logic above
     */
    public DeliveryMethodEnum getDeliveryMethod() {

        // First, check if only one choice is possible. If so, choose that.
        if (anyDeliveryMethodFitsAll()) {
            if (possibleDeliveryMethodsFittingAllItems.contains(DeliveryMethodEnum.HEMLEVERANS) &&
                    !possibleDeliveryMethodsFittingAllItems.contains(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)) {
                return DeliveryMethodEnum.HEMLEVERANS;
            } else if (possibleDeliveryMethodsFittingAllItems.contains(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE) &&
                    !possibleDeliveryMethodsFittingAllItems.contains(DeliveryMethodEnum.HEMLEVERANS)) {
                return DeliveryMethodEnum.UTLÄMNINGSSTÄLLE;
            } else if (deliveryMethod == null
                    && possibleDeliveryMethodsFittingAllItems.contains(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)) {
                // If no deliveryMethod is chosen yet and UTLÄMNINGSSTÄLLE is available, make that default choice.
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

    public String toOrder() {
        return navigationController.goBack();
    }

    /**
     * This method validates the user has made all necessary choices or it will add error messages to be presented to
     * the user. It also has the side-effects that it resets state for {@link HomeDeliveryController} and
     * {@link CollectDeliveryController}. It also decides outcome; either the user should stay at the delivery view, or
     * be taken to homeDelivery view, or be taken to collectDelivery view.
     *
     * @return the action outcome
     */
    public String toDeliveryMethod() {

        if (anyDeliveryMethodFitsAll()) {

            if (deliveryMethod == null) {
                String msg = "Du måste välja leveranssätt.";
                FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg,
                        msg));

                return "delivery";
            }

            homeDeliveryController.initNotificationGroups();
            collectDeliveryController.resetChoices();

            setDeliveryMethodForAllItems(deliveryMethod);

            if (deliveryMethod.equals(DeliveryMethodEnum.HEMLEVERANS)) {

                homeDeliveryController.setNextViewIsCollectDelivery(false);

                return navigationController.gotoView("homeDelivery" + ACTION_SUFFIX, HomeDeliveryController.VIEW_NAME);
            } else if (deliveryMethod.equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)) {
                return navigationController.gotoView("collectDelivery" + ACTION_SUFFIX, CollectDeliveryController.VIEW_NAME);
            } else {
                throw new RuntimeException("Unexpected " + DeliveryMethodEnum.class.getCanonicalName());
            }
        } else {
            // The user must have chosen both delivery methods and thus needs to go through both home delivery and
            // collect delivery views.

            // Validate delivery method is chosen for all items
            Map<PrescriptionItemType, String> deliveryMethodForEachItem = getDeliveryMethodForEachItem();
            for (String deliveryMethod : deliveryMethodForEachItem.values()) {
                if (deliveryMethod == null) {
                    String msg = "Du måste välja leveranssätt för alla produkter.";
                    FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg,
                            msg));

                    return "delivery";
                }
            }

            homeDeliveryController.initNotificationGroups();
            collectDeliveryController.resetChoices();

            // Validation passed. Set what we know for each order row, i.e. delivery method for the delivery choice of
            // the order row.
            cart.getOrderRows().forEach(orderRowType -> {
                DeliveryChoiceType deliveryChoice = new DeliveryChoiceType();

                PrescriptionItemType prescriptionItem = prescriptionItemInfo.getPrescriptionItem(orderRowType);
                String deliveryMethodString = deliveryMethodForEachItem.get(prescriptionItem);

                deliveryChoice.setDeliveryMethod(DeliveryMethodEnum.fromValue(deliveryMethodString));
                orderRowType.setDeliveryChoice(deliveryChoice);
            });

            homeDeliveryController.setNextViewIsCollectDelivery(true);
            return navigationController.gotoView("homeDelivery" + ACTION_SUFFIX, HomeDeliveryController.VIEW_NAME);
        }
    }

    List<DeliveryAlternativeType> getPossibleDeliveryAlternatives(PrescriptionItemType prescriptionItem) {
        return getDeliveryAlternativeMatchingDeliveryMethod(
                prescriptionItem, getDeliveryMethodForEachItem().get(prescriptionItem));
    }

    private Map<ServicePointProvider, Set<DeliveryNotificationMethodEnum>>
    getAllCombinationsOfProvidersAndNotificationMethods(
            Set<DeliveryNotificationMethodEnum> allDeliveryNotificationMethods,
            Set<ServicePointProvider> allServicePointProviders) {

        Map<ServicePointProvider, Set<DeliveryNotificationMethodEnum>> allCollectCombinations = new HashMap<>();

        for (ServicePointProvider servicePointProvider : allServicePointProviders) {
            allCollectCombinations.put(servicePointProvider, new HashSet<>());

            for (DeliveryNotificationMethodEnum deliveryNotificationMethod : allDeliveryNotificationMethods) {
                allCollectCombinations.get(servicePointProvider).add(deliveryNotificationMethod);
            }
        }

        return allCollectCombinations;
    }

    private List<DeliveryAlternativeType> getDeliveryAlternativeMatchingDeliveryMethod(
            PrescriptionItemType prescriptionItem,
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
        cart.getOrderRows().forEach(orderRowType -> {
            DeliveryChoiceType deliveryChoice = new DeliveryChoiceType();

            deliveryChoice.setDeliveryMethod(deliveryMethod);

            orderRowType.setDeliveryChoice(deliveryChoice);
        });
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
        List<PrescriptionItemType> prescriptionItemsInCart = prescriptionItemInfo
                .getPrescriptionItems(cart.getOrderRows());

        deliveryMethodForEachItem.keySet().retainAll(prescriptionItemsInCart);

        // Make sure chosen items are in the map. Otherwise add them.
        for (PrescriptionItemType prescriptionItemType : prescriptionItemsInCart) {
            if (!deliveryMethodForEachItem.containsKey(prescriptionItemType)) {
                String decidedDeliveryMethod = decideOnDeliveryMethod(prescriptionItemType);

                deliveryMethodForEachItem.put(prescriptionItemType, decidedDeliveryMethod);
            } else {

                // Check if the current choice is allowed. Otherwise reset.
                List<String> allowedMethods = new ArrayList<>();

                prescriptionItemType.getDeliveryAlternative().forEach(
                        d -> allowedMethods.add(d.getDeliveryMethod().name()));

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
            utilController.addErrorMessageWithCustomerServiceInfo(msg);
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

    public List<PrescriptionItemType> sortByNumberDeliveryMethodChoices(List<PrescriptionItemType> items) {
        List<PrescriptionItemType> newList = new ArrayList<>(items);

        newList.sort((o1, o2) -> {
            List<DeliveryAlternativeType> alternatives1 = o1.getDeliveryAlternative();
            boolean hasBothDeliveryMethods1 =
                    anyDeliveryAlternativeHasDeliveryMethod(alternatives1, DeliveryMethodEnum.HEMLEVERANS)
                            &&
                            anyDeliveryAlternativeHasDeliveryMethod(alternatives1, DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);

            List<DeliveryAlternativeType> alternatives2 = o2.getDeliveryAlternative();
            boolean hasBothDeliveryMethods2 =
                    anyDeliveryAlternativeHasDeliveryMethod(alternatives2, DeliveryMethodEnum.HEMLEVERANS)
                            &&
                            anyDeliveryAlternativeHasDeliveryMethod(alternatives2, DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);


            if (hasBothDeliveryMethods1 && !hasBothDeliveryMethods2) {
                return -1;
            } else if (!hasBothDeliveryMethods1 && hasBothDeliveryMethods2) {
                return 1;
            } else {
                return 0;
            }
        });

        return newList;
    }

    public String getViewName() {
        return VIEW_NAME;
    }
}
