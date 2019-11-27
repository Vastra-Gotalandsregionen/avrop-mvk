package se._1177.lmn.controller.model;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._2.OrderItemType;
import riv.crm.selfservice.medicalsupply._2.OrderRowType;
import riv.crm.selfservice.medicalsupply._2.PrescriptionItemType;

import java.util.ArrayList;
import java.util.Comparator;
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

    private Map<String, Map<String, OrderItemType>> latestOrderItemsByArticleNoAndPrescriptionItem;

    public Map<String, PrescriptionItemType> getChosenPrescriptionItemInfo() {
        return chosenPrescriptionItemInfo;
    }

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

        List<PrescriptionItemType> result = new ArrayList<>(prescriptionItems);

        result.sort(Comparator.comparing(o -> o.getArticle().getArticleName()));

        return new ArrayList<>(result);
    }

    public List<PrescriptionItemType> getChosenPrescriptionItemInfoList() {
        return new ArrayList<>(chosenPrescriptionItemInfo.values());
    }

    public Map<String, Map<String, OrderItemType>> getLatestOrderItemsByArticleNoAndPrescriptionItem() {
        return latestOrderItemsByArticleNoAndPrescriptionItem;
    }

    public void setLatestOrderItemsByArticleNo(
            Map<String, Map<String, OrderItemType>> latestOrderItemsByArticleNoAndPrescriptionItem) {
         this.latestOrderItemsByArticleNoAndPrescriptionItem = latestOrderItemsByArticleNoAndPrescriptionItem;
    }
}
