package se._1177.lmn.model;

import riv.crm.selfservice.medicalsupply._2.OrderItemType;
import riv.crm.selfservice.medicalsupply._2.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._2.GetMedicalSupplyPrescriptionsResponseType;

import java.util.List;
import java.util.Map;

/**
 * Class which aggregates orderable {@link PrescriptionItemType}s, noLongerOrderable {@link PrescriptionItemType}s, and
 * the {@link GetMedicalSupplyPrescriptionsResponseType} which the two former are based from.
 *
 * @author Patrik Björk
 */
public class MedicalSupplyPrescriptionsHolder {

    public List<PrescriptionItemType> orderable;
    public List<PrescriptionItemType> noLongerOrderable;
    public GetMedicalSupplyPrescriptionsResponseType supplyPrescriptionsResponse;
    public Map<String, Map<String, OrderItemType>> latestOrderItemsByArticleNoAndPrescriptionItem;

    public List<PrescriptionItemType> getOrderable() {
        return orderable;
    }

    public void setOrderable(List<PrescriptionItemType> orderable) {
        this.orderable = orderable;
    }

    public List<PrescriptionItemType> getNoLongerOrderable() {
        return noLongerOrderable;
    }

    public void setNoLongerOrderable(List<PrescriptionItemType> noLongerOrderable) {
        this.noLongerOrderable = noLongerOrderable;
    }

    public GetMedicalSupplyPrescriptionsResponseType getSupplyPrescriptionsResponse() {
        return supplyPrescriptionsResponse;
    }

    public void setSupplyPrescriptionsResponse(GetMedicalSupplyPrescriptionsResponseType supplyPrescriptionsResponse) {
        this.supplyPrescriptionsResponse = supplyPrescriptionsResponse;
    }

    public Map<String, Map<String, OrderItemType>> getLatestOrderedNumbersByArticleNo() {
        return latestOrderItemsByArticleNoAndPrescriptionItem;
    }

    public void setLatestOrderItemsByArticleNoAndPrescriptionItem(
            Map<String, Map<String, OrderItemType>> latestOrderItemsByArticleNoAndPrescriptionItem) {

        this.latestOrderItemsByArticleNoAndPrescriptionItem = latestOrderItemsByArticleNoAndPrescriptionItem;
    }
}
