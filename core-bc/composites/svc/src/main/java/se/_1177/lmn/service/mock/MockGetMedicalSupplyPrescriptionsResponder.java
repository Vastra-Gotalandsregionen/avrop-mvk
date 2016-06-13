package se._1177.lmn.service.mock;

import riv.crm.selfservice.medicalsupply._0.ArticleType;
import riv.crm.selfservice.medicalsupply._0.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._0.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.OrderItemType;
import riv.crm.selfservice.medicalsupply._0.PrescriberType;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.ProductAreaEnum;
import riv.crm.selfservice.medicalsupply._0.ResultCodeEnum;
import riv.crm.selfservice.medicalsupply._0.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply._0.StatusEnum;
import riv.crm.selfservice.medicalsupply._0.SubjectOfCareType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptions._0.rivtabp21.GetMedicalSupplyPrescriptionsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.ObjectFactory;

import javax.jws.WebService;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Period;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

/**
 * @author Patrik Björk
 */
@WebService(targetNamespace = "urn:riv:crm:selfservice:medicalsupply:GetMedicalSupplyPrescriptions:0:rivtabp21")
public class MockGetMedicalSupplyPrescriptionsResponder
        implements GetMedicalSupplyPrescriptionsResponderInterface {

    private final ObjectFactory objectFactory = new ObjectFactory();

    public GetMedicalSupplyPrescriptionsResponseType getMedicalSupplyPrescriptions(
            String logicalAddress,
            GetMedicalSupplyPrescriptionsType parameters) {

        /*try {
            Thread.sleep(11000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        Random random = new Random(4);

        GetMedicalSupplyPrescriptionsResponseType response = objectFactory
                .createGetMedicalSupplyPrescriptionsResponseType();

        response.setResultCode(ResultCodeEnum.OK);

        SubjectOfCareType subjectOfCare = new SubjectOfCareType();
        subjectOfCare.setSubjectOfCareId(random.nextInt(1000) + "");

        for (int i = 0; i <= 300; i++) {
            addPrescriptionItem(random, subjectOfCare, random.nextBoolean());
        }

        response.setSubjectOfCareType(subjectOfCare);

        response.setComment("Kommentar" + random.nextInt(100));

        return response;
    }

    private void addPrescriptionItem(Random random, SubjectOfCareType subjectOfCare, boolean nextPossibleOrderDateInFuture) {
        OrderItemType orderItem = new OrderItemType();

        ArticleType article = new ArticleType();
        article.setArticleName("Artikelnamn" + random.nextInt(100));
        article.setArticleNo(random.nextInt(100000) + "");
        article.setIsOrderable(random.nextBoolean());
        article.setPackageSize(random.nextInt(100));
        article.setPackageSizeUnit("Enhet" + random.nextInt(100));
        article.setProductArea(ProductAreaEnum.values()[random.nextInt(ProductAreaEnum.values().length)]);

        orderItem.setArticle(article);

        orderItem.setDeliveredDate(wrapInJaxBElement(getRandomCalendar(random)));
        DeliveryChoiceType deliveryChoice = new DeliveryChoiceType();
        deliveryChoice.setDeliveryMethod(DeliveryMethodEnum.HEMLEVERANS);
//            deliveryChoice.setDeliveryPoint(); kanske todo utveckla mer på deliveryChoice...
        orderItem.setDeliveryChoice(deliveryChoice);

        subjectOfCare.getOrderItem().add(orderItem);

        PrescriptionItemType prescriptionItem = new PrescriptionItemType();

        ArticleType article2 = new ArticleType();
        article2.setArticleName("Artikelnamn" + random.nextInt(100));
        article2.setArticleNo(random.nextInt(100000) + "");
        article2.setIsOrderable(random.nextBoolean());
        article2.setPackageSize(random.nextInt(100));
        article2.setPackageSizeUnit("Enhet" + random.nextInt(100));
        article2.setProductArea(ProductAreaEnum.values()[random.nextInt(ProductAreaEnum.values().length)]);

        prescriptionItem.setArticle(article2);

        prescriptionItem.setNoOfOrders(random.nextInt(10));
        prescriptionItem.setNoOfRemainingOrders(random.nextInt(5));

        DeliveryAlternativeType deliveryAlternative = getRandomDeliveryAlternativeType(random);
        prescriptionItem.getDeliveryAlternative().add(deliveryAlternative);

        DeliveryAlternativeType deliveryAlternative2 = getRandomDeliveryAlternativeType(random);
        prescriptionItem.getDeliveryAlternative().add(deliveryAlternative2);

        if (!nextPossibleOrderDateInFuture) {
            prescriptionItem.setNextEarliestOrderDate(getRandomCalendar(random));
        } else {
            Calendar c = Calendar.getInstance();

            c.add(Calendar.MONTH, 1);

            XMLGregorianCalendar randomCalendar = getRandomCalendar(random);
            randomCalendar.setYear(c.get(Calendar.YEAR) + 1);
            randomCalendar.setDay(random.nextInt(20) + 1); // To avoid invalid dates.

            prescriptionItem.setNextEarliestOrderDate(randomCalendar);
        }
        prescriptionItem.setPrescriptionId(random.nextInt(100000) + "");
        prescriptionItem.setPrescriptionItemId(random.nextInt(100000) + "");
        PrescriberType prescriber = new PrescriberType();
        prescriber.setPrescriberName("Kalle Karlsson");
        prescriber.setPrescriberCode(random.nextInt(1000) + "");
        prescriber.setPrescriberId(random.nextInt(1000) + "");
        prescriber.setPrescriberTitle("Läkare");
        prescriptionItem.setPrescriber(prescriber);
        prescriptionItem.setLastValidDate(getRandomCalendar(random));
        prescriptionItem.setNoOfArticlesPerOrder(random.nextInt(5) * 1000 + 1000);
        prescriptionItem.setNoOfPackagesPerOrder(random.nextInt(5) * 50 + 50);
        prescriptionItem.setStatus(StatusEnum.values()[random.nextInt(StatusEnum.values().length)]);

        subjectOfCare.getPrescriptionItem().add(prescriptionItem);
    }

    private JAXBElement<XMLGregorianCalendar> wrapInJaxBElement(XMLGregorianCalendar calendar) {
        return new riv.crm.selfservice.medicalsupply._0.ObjectFactory().createOrderItemTypeDeliveredDate(calendar);
    }

    private DeliveryAlternativeType getRandomDeliveryAlternativeType(Random random) {
        DeliveryAlternativeType deliveryAlternative = new DeliveryAlternativeType();
        deliveryAlternative.setDeliveryMethodId(random.nextInt(10000) + "");
        deliveryAlternative.setDeliveryMethod(
                DeliveryMethodEnum.values()[random.nextInt(DeliveryMethodEnum.values().length)]);

        if (deliveryAlternative.getDeliveryMethod().equals(DeliveryMethodEnum.HEMLEVERANS)) {
            deliveryAlternative.setServicePointProvider(ServicePointProviderEnum.INGEN);
        } else {
            deliveryAlternative.setServicePointProvider(
                    ServicePointProviderEnum.values()[random.nextInt(ServicePointProviderEnum.values().length)]);

            // Inefficient but this is a mock...
            while (deliveryAlternative.getServicePointProvider().equals(ServicePointProviderEnum.INGEN)) {
                // Continue until something other than ServicePointProviderEnum.INGEN gets set.
                deliveryAlternative.setServicePointProvider(
                        ServicePointProviderEnum.values()[random.nextInt(ServicePointProviderEnum.values().length)]);

            }
        }

        deliveryAlternative.setAllowChioceOfDeliveryPoints(random.nextBoolean());
        deliveryAlternative.setDeliveryMethodName(Character.getName(random.nextInt(1000)));

        // When HEMLEVERANS we set notification methods sometimes.
        if (deliveryAlternative.getDeliveryMethod().equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)
                || random.nextBoolean()) {

            DeliveryNotificationMethodEnum first = DeliveryNotificationMethodEnum.values()
                    [random.nextInt(DeliveryNotificationMethodEnum.values().length)];
            deliveryAlternative.getDeliveryNotificationMethod().add(first);

            DeliveryNotificationMethodEnum second = DeliveryNotificationMethodEnum.values()
                    [random.nextInt(DeliveryNotificationMethodEnum.values().length)];

            while (second == first) {
                second = DeliveryNotificationMethodEnum.values()
                        [random.nextInt(DeliveryNotificationMethodEnum.values().length)];
            }

            deliveryAlternative.getDeliveryNotificationMethod().add(second);

        }
        return deliveryAlternative;
    }

    private XMLGregorianCalendar getRandomCalendar(Random random) {
        XMLGregorianCalendar xmlGregorianCalendar = null;
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }

        xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar());

        Period period = Period.ofDays(random.nextInt(730));

        long timeToSubtract = -period.getDays() * 24L * 60L * 60L * 1000L;

        Duration duration = datatypeFactory.newDuration(timeToSubtract);

        xmlGregorianCalendar.add(duration);

        return xmlGregorianCalendar;
    }

}
