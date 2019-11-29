package se._1177.lmn.controller.session;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._1.SubjectOfCareType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._1.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._1.ObjectFactory;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;
import se._1177.lmn.service.util.JaxbUtil;

import javax.xml.bind.JAXBElement;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class OrderControllerSession implements Externalizable {

    private MedicalSupplyPrescriptionsHolder medicalSupplyPrescriptions;

    private Map<String, Boolean> chosenItemMap = new HashMap<>();

    private Map<String, PrescriptionItemType> prescriptionItemInfosToPresent = new HashMap<>();

    public MedicalSupplyPrescriptionsHolder getMedicalSupplyPrescriptions() {
        return medicalSupplyPrescriptions;
    }

    public void setMedicalSupplyPrescriptions(MedicalSupplyPrescriptionsHolder medicalSupplyPrescriptions) {
        this.medicalSupplyPrescriptions = medicalSupplyPrescriptions;
    }

    public Map<String, Boolean> getChosenItemMap() {
        return chosenItemMap;
    }

    public void setChosenItemMap(Map<String, Boolean> chosenItemMap) {
        this.chosenItemMap = chosenItemMap;
    }

    public Map<String, PrescriptionItemType> getPrescriptionItemInfosToPresent() {
        return prescriptionItemInfosToPresent;
    }

    public void setPrescriptionItemInfosToPresent(Map<String, PrescriptionItemType> prescriptionItemInfosToPresent) {
        this.prescriptionItemInfosToPresent = prescriptionItemInfosToPresent;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Map<String, String> serializableChosenPrescriptionItemInfo = new HashMap<>();

        for (Map.Entry<String, PrescriptionItemType> entry : chosenPrescriptionItemInfo.entrySet()) {
            serializableChosenPrescriptionItemInfo.put(entry.getKey(), objectToXml(entry.getValue()));
        }

        out.writeObject(serializableChosenPrescriptionItemInfo);
    }

    private String objectToXml(PrescriptionItemType value) {
        SubjectOfCareType subjectOfCareType = new SubjectOfCareType();
        subjectOfCareType.getPrescriptionItem().add(value);

        GetMedicalSupplyPrescriptionsResponseType root = new GetMedicalSupplyPrescriptionsResponseType();
        root.setSubjectOfCareType(subjectOfCareType);

        JAXBElement<GetMedicalSupplyPrescriptionsResponseType> getMedicalSupplyPrescriptionsResponse =
                new ObjectFactory().createGetMedicalSupplyPrescriptionsResponse(root);

        return JaxbUtil.objectToXML(getMedicalSupplyPrescriptionsResponse);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Map<String, String> serializableChosenPrescriptionItemInfo = (Map<String, String>) in.readObject();

        Map<String, PrescriptionItemType> chosenPrescriptionItemInfo = new HashMap<>();

        for (Map.Entry<String, String> entry : serializableChosenPrescriptionItemInfo.entrySet()) {
            chosenPrescriptionItemInfo.put(entry.getKey(), xmlToObject(entry.getValue()));
        }

        this.chosenPrescriptionItemInfo = chosenPrescriptionItemInfo;
    }

    private PrescriptionItemType xmlToObject(String value) {
        return JaxbUtil.xmlToObject(value, GetMedicalSupplyPrescriptionsResponseType.class)
                .getValue().getSubjectOfCareType().getPrescriptionItem().get(0);
    }
}
