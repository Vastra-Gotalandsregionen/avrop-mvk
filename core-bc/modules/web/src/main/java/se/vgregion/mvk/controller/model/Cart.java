package se.vgregion.mvk.controller.model;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Cart {

    private List<PrescriptionItemType> itemsInCart = new ArrayList<>();
    private Map<String, PrescriptionItemType> prescriptionItemInfo = new HashMap<>();

    public List<PrescriptionItemType> getItemsInCart() {
        return itemsInCart;
    }

    public void setItemsInCart(List<PrescriptionItemType> itemsInCart) {
        this.itemsInCart = itemsInCart;
    }

    /**
     * Use this method to store the prescription item to fetch the info later.
     *
     * @param prescriptionId
     * @param prescriptionItem
     */
    public void addPrescriptionItemForInfo(String prescriptionId, PrescriptionItemType prescriptionItem) {
        this.prescriptionItemInfo.put(prescriptionId, prescriptionItem);
    }

    public Map<String, PrescriptionItemType> getPrescriptionItemInfo() {
        return prescriptionItemInfo;
    }

    public void emptyCart() {
        itemsInCart = new ArrayList<>();
        prescriptionItemInfo = new HashMap<>();
    }
}
