package se._1177.lmn.controller.model;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._1.OrderItemType;
import riv.crm.selfservice.medicalsupply._1.OrderRowType;
import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._1.SubjectOfCareType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._1.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._1.ObjectFactory;
import se._1177.lmn.service.util.JaxbUtil;

import javax.xml.bind.JAXBElement;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static se._1177.lmn.service.util.JaxbUtil.readObject;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PrescriptionItemInfo implements Serializable {

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

/*    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Map<String, String> serializableChosenPrescriptionItemInfo = new HashMap<>();

        for (Map.Entry<String, PrescriptionItemType> entry : chosenPrescriptionItemInfo.entrySet()) {
            serializableChosenPrescriptionItemInfo.put(entry.getKey(), objectToXml(entry.getValue()));
        }

        out.writeObject(serializableChosenPrescriptionItemInfo);
    }*/

    /*private String objectToXml(PrescriptionItemType value) {
        SubjectOfCareType subjectOfCareType = new SubjectOfCareType();
        subjectOfCareType.getPrescriptionItem().add(value);

        GetMedicalSupplyPrescriptionsResponseType root = new GetMedicalSupplyPrescriptionsResponseType();
        root.setSubjectOfCareType(subjectOfCareType);

        JAXBElement<GetMedicalSupplyPrescriptionsResponseType> getMedicalSupplyPrescriptionsResponse =
                new ObjectFactory().createGetMedicalSupplyPrescriptionsResponse(root);

        return JaxbUtil.objectToXML(getMedicalSupplyPrescriptionsResponse);
    }*/

    /*@Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Map<String, String> serializableChosenPrescriptionItemInfo = (Map<String, String>) in.readObject();

        Map<String, PrescriptionItemType> chosenPrescriptionItemInfo = new HashMap<>();

        for (Map.Entry<String, String> entry : serializableChosenPrescriptionItemInfo.entrySet()) {
            chosenPrescriptionItemInfo.put(entry.getKey(), xmlToObject(entry.getValue()));
        }

        this.chosenPrescriptionItemInfo = chosenPrescriptionItemInfo;
    }*/

    /*private PrescriptionItemType xmlToObject(String value) {
        return JaxbUtil.xmlToObject(value, GetMedicalSupplyPrescriptionsResponseType.class)
                .getValue().getSubjectOfCareType().getPrescriptionItem().get(0);
    }*/
}
