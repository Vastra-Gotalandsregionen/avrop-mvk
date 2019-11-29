package se._1177.lmn.model;

import riv.crm.selfservice.medicalsupply._1.OrderItemType;
import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._1.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._1.ObjectFactory;
import se._1177.lmn.service.util.JaxbUtil;

import javax.xml.bind.JAXBElement;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class which aggregates orderable {@link PrescriptionItemType}s, noLongerOrderable {@link PrescriptionItemType}s, and
 * the {@link GetMedicalSupplyPrescriptionsResponseType} which the two former are based from.
 *
 * @author Patrik Björk
 */
public class MedicalSupplyPrescriptionsHolder implements Externalizable {

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

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        List<String> orderables = this.orderable.stream().map(JaxbUtil::objectToXml).collect(Collectors.toList());

        List<String> noLongerOrderables = this.noLongerOrderable.stream().map(JaxbUtil::objectToXml).collect(Collectors.toList());

        JAXBElement<GetMedicalSupplyPrescriptionsResponseType> jaxbElement = new ObjectFactory()
                .createGetMedicalSupplyPrescriptionsResponse(supplyPrescriptionsResponse);

        out.writeObject(orderables);
        out.writeObject(noLongerOrderables);
        out.writeObject(JaxbUtil.objectToXML(jaxbElement));
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }
}
