package se._1177.lmn.controller.session;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._1.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DeliveryControllerSession implements Serializable {

    private DeliveryMethodEnum deliveryMethod = null;
    private boolean userNeedsToChooseDeliveryMethodForEachItem;
    private Set<DeliveryMethodEnum> possibleDeliveryMethodsFittingAllItems;

    // Chosen delivery method for each item
    private Map<PrescriptionItemType, String> deliveryMethodForEachItem = new HashMap<>();

    public DeliveryMethodEnum getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(DeliveryMethodEnum deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public boolean isUserNeedsToChooseDeliveryMethodForEachItem() {
        return userNeedsToChooseDeliveryMethodForEachItem;
    }

    public void setUserNeedsToChooseDeliveryMethodForEachItem(boolean userNeedsToChooseDeliveryMethodForEachItem) {
        this.userNeedsToChooseDeliveryMethodForEachItem = userNeedsToChooseDeliveryMethodForEachItem;
    }

    public Set<DeliveryMethodEnum> getPossibleDeliveryMethodsFittingAllItems() {
        return possibleDeliveryMethodsFittingAllItems;
    }

    public void setPossibleDeliveryMethodsFittingAllItems(Set<DeliveryMethodEnum> possibleDeliveryMethodsFittingAllItems) {
        this.possibleDeliveryMethodsFittingAllItems = possibleDeliveryMethodsFittingAllItems;
    }

    public Map<PrescriptionItemType, String> getDeliveryMethodForEachItem() {
        return deliveryMethodForEachItem;
    }

    public void setDeliveryMethodForEachItem(Map<PrescriptionItemType, String> deliveryMethodForEachItem) {
        this.deliveryMethodForEachItem = deliveryMethodForEachItem;
    }
}
