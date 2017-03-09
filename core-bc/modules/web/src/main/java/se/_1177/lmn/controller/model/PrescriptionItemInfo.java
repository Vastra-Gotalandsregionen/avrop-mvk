package se._1177.lmn.controller.model;

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
public class PrescriptionItemInfo {

    private Map<String, PrescriptionItemType> prescriptionItemInfo = new HashMap<>();

    /**
     * Use this method to store the prescription item to fetch the info later.
     *
     * @param prescriptionItemId
     * @param prescriptionItem
     */
    public void addPrescriptionItemForInfo(String prescriptionItemId, PrescriptionItemType prescriptionItem) {
        this.prescriptionItemInfo.put(prescriptionItemId, prescriptionItem);
    }

    public Map<String, PrescriptionItemType> getPrescriptionItemInfo() {
        return prescriptionItemInfo;
    }

    public void emptyCart() {
        prescriptionItemInfo = new HashMap<>();
    }
}
