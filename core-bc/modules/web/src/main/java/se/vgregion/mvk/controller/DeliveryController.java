package se.vgregion.mvk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import se.vgregion.mvk.controller.model.Cart;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.HashMap;
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

    private DeliveryMethodEnum deliveryMethod = null;//DeliveryMethodEnum.HEMLEVERANS; // Default. Will this possibly change so a user can have a personal default?
    private boolean userNeedsToChooseDeliveryMethodForEachItem;
    private Set<DeliveryMethodEnum> possibleDeliveryMethodsFittingAllItems;
    private Map<PrescriptionItemType, String> deliveryMethodForEachItem = new HashMap<>();

    public DeliveryMethodEnum getDeliveryMethod() {

        if (deliveryMethod != null && anyDeliveryMethodFitsAll() && !getDeliveryMethodForEachItem().values().contains(deliveryMethod.name())) {
            // Chosen deliveryMethod cannot be chosen or needs to be explicitly chosen.
            deliveryMethod = null;
        }

        return deliveryMethod;
    }

    public void setDeliveryMethod(DeliveryMethodEnum deliveryMethod) {
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

    public void setPossibleDeliveryMethodsFittingAllItems(Set<DeliveryMethodEnum> possibleDeliveryMethodsFittingAllItems) {
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
                deliveryMethodForEachItem.put(prescriptionItemType, null); // Null since we haven't chosen method yet.
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
