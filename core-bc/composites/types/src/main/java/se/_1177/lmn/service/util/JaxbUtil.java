package se._1177.lmn.service.util;

import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._1.SubjectOfCareType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._1.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._1.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.StringReader;
import java.io.StringWriter;

public class JaxbUtil {

    public static <T> T readObject(ObjectInput in, Class<T> clazz) throws ClassNotFoundException, IOException {
        String xml = (String) in.readObject();
        JAXBElement<T> jaxbElement = xmlToObject(xml, clazz);

        if (jaxbElement == null) {
            return null;
        }

        return jaxbElement.getValue();
    }

    public static <T> String objectToXML(JAXBElement<T> object) {

        if (object.getValue() == null) {
            return null;
        }

        try {
            //Create JAXB Context
            JAXBContext jaxbContext = JAXBContext.newInstance(object.getValue().getClass());

            //Create Marshaller
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            //Required formatting??
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            //Print XML String to Console
            StringWriter sw = new StringWriter();

            //Write XML to StringWriter
            jaxbMarshaller.marshal(object, sw);

            return sw.toString();
            //Verify XML Content
//            String xmlContent = sw.toString();
//            System.out.println( xmlContent );

        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> JAXBElement<T> xmlToObject(String xml, Class<T> clazz) {

        if (xml == null) {
            return null;
        }

        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (JAXBElement<T>) jaxbUnmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String objectToXml(PrescriptionItemType value) {
        SubjectOfCareType subjectOfCareType = new SubjectOfCareType();
        subjectOfCareType.getPrescriptionItem().add(value);

        GetMedicalSupplyPrescriptionsResponseType root = new GetMedicalSupplyPrescriptionsResponseType();
        root.setSubjectOfCareType(subjectOfCareType);

        JAXBElement<GetMedicalSupplyPrescriptionsResponseType> getMedicalSupplyPrescriptionsResponse =
                new ObjectFactory().createGetMedicalSupplyPrescriptionsResponse(root);

        return JaxbUtil.objectToXML(getMedicalSupplyPrescriptionsResponse);
    }

    public static PrescriptionItemType xmlToPrescriptionItemType(String value) {
        return JaxbUtil.xmlToObject(value, GetMedicalSupplyPrescriptionsResponseType.class)
                .getValue().getSubjectOfCareType().getPrescriptionItem().get(0);
    }
}
