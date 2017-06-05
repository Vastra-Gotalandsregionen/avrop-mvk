package se._1177.lmn.controller.model;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.OrderRowType;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PrescriptionItemInfo {

    private Map<String, PrescriptionItemType> chosenPrescriptionItemInfo = new HashMap<>();

    public Map<String, PrescriptionItemType> getChosenPrescriptionItemInfo() {
        return chosenPrescriptionItemInfo;
    }

    // todo Should be called when same method is called in Cart
    public void emptyChosenPrescriptionItems() {
        chosenPrescriptionItemInfo = new HashMap<>();
    }

    public PrescriptionItemType getPrescriptionItem(String prescriptionItemId) {
        return getChosenPrescriptionItemInfo().get(prescriptionItemId);
    }

    public PrescriptionItemType getPrescriptionItem(OrderRowType orderRow) {
        return getChosenPrescriptionItemInfo().get(orderRow.getPrescriptionItemId());
    }

    public List<PrescriptionItemType> getPrescriptionItems(List<OrderRowType> orderRows) {
        Set<PrescriptionItemType> prescriptionItems = new HashSet<>();

        orderRows.forEach(orderRow -> prescriptionItems.add(getPrescriptionItem(orderRow)));

        return new ArrayList<>(prescriptionItems);
    }

    public List<PrescriptionItemType> getChosenPrescriptionItemInfoList() {
        return new ArrayList<>(chosenPrescriptionItemInfo.values());
    }
}
