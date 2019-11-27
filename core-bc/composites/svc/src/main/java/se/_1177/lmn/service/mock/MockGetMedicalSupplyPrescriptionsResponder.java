package se._1177.lmn.service.mock;

import riv.crm.selfservice.medicalsupply._2.ArticleType;
import riv.crm.selfservice.medicalsupply._2.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._2.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._2.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._2.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._2.ImageType;
import riv.crm.selfservice.medicalsupply._2.OrderItemType;
import riv.crm.selfservice.medicalsupply._2.PrescriberType;
import riv.crm.selfservice.medicalsupply._2.PrescribingOrganizationType;
import riv.crm.selfservice.medicalsupply._2.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._2.ResultCodeEnum;
import riv.crm.selfservice.medicalsupply._2.StatusEnum;
import riv.crm.selfservice.medicalsupply._2.SubjectOfCareType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptions._2.rivtabp21.GetMedicalSupplyPrescriptionsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._2.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._2.GetMedicalSupplyPrescriptionsType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._2.ObjectFactory;

import javax.jws.WebService;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import static riv.crm.selfservice.medicalsupply._2.DeliveryMethodEnum.HEMLEVERANS;
import static se._1177.lmn.service.mock.MockUtil.toCvType;

/**
 * @author Patrik Björk
 */
@WebService(targetNamespace = "urn:riv:crm:selfservice:medicalsupply:GetMedicalSupplyPrescriptions:2:rivtabp21")
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

        /*int j = 1;
        if (j == 1) {
            response.setResultCode(ResultCodeEnum.ERROR);
            response.setComment("Felmeddelandet");
            return response;
        }*/

        response.setResultCode(ResultCodeEnum.OK);

        SubjectOfCareType subjectOfCare = new SubjectOfCareType();
        subjectOfCare.setSubjectOfCareId(random.nextInt(1000) + "");

        for (int i = 0; i <= 80; i++) {
            addPrescriptionItem(random, subjectOfCare, random.nextBoolean());
        }

        // Add determined item
        addSpecificPrescriptionItemSärnärWithSubArticles(random, subjectOfCare);
        addSpecificPrescriptionItemSärnärWithSubArticlesAndHomeDeliveryWithNotification(random, subjectOfCare);
        addSpecificPrescriptionItemZeroPackages(random, subjectOfCare);
        addSpecificPrescriptionItemWithTwoHomeDeliveryOptions(random, subjectOfCare);
        addSpecificPrescriptionItemWithTwoHomeDeliveryOptionsWithOtherNotifications(random, subjectOfCare);
        addSpecificPrescriptionItemWithTwoHomeDeliveryOptionsWithOverlappingNotifications(random, subjectOfCare);
        addSpecificPrescriptionItemWithCollectDeliveryWithIngenProvider(random, subjectOfCare);

        response.setSubjectOfCareType(subjectOfCare);

        response.setComment("Kommentar" + random.nextInt(100));

        return response;
    }

    private void addPrescriptionItem(Random random, SubjectOfCareType subjectOfCare, boolean nextPossibleOrderDateInFuture) {

        String articleNo = "100" + random.nextInt(20);

        ArticleType article = new ArticleType();
        article.setArticleName("Artikelnamn" + random.nextInt(100));
        article.setArticleNo(articleNo);
        article.setIsOrderable(random.nextBoolean());
        article.setPackageSize(random.nextInt(100));
        article.setPackageSizeUnit("Enhet" + random.nextInt(100));
        MockProductAreaEnum value = MockProductAreaEnum.values()[random.nextInt(MockProductAreaEnum.values().length)];
        article.setProductArea(toCvType(value));

        PrescriptionItemType prescriptionItem = new PrescriptionItemType();

        ArticleType article2 = new ArticleType();
        article2.setArticleName("Artikelnamn" + random.nextInt(100));
        article2.setArticleNo(articleNo);
        article2.setIsOrderable(random.nextBoolean());
        article2.setPackageSize(random.nextInt(100));
        article2.setPackageSizeUnit("Enhet" + random.nextInt(100));
        MockProductAreaEnum value1 = MockProductAreaEnum.values()[random.nextInt(MockProductAreaEnum.values().length)];
        article2.setProductArea(toCvType(value1));

        prescriptionItem.setArticle(article2);

        prescriptionItem.setNoOfOrders(random.nextInt(10));
        prescriptionItem.setNoOfRemainingOrders(random.nextInt(5));

        DeliveryAlternativeType deliveryAlternative = getRandomDeliveryAlternativeType(random);
        prescriptionItem.getDeliveryAlternative().add(deliveryAlternative);

        DeliveryAlternativeType deliveryAlternative2 = getRandomDeliveryAlternativeType(random);

        if (!(deliveryAlternative.getDeliveryMethod().equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)
                && deliveryAlternative2.getDeliveryMethod().equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE))) {
            // We don't want multiple alternatives all having UTLÄMNINGSSTÄLLE as method.
            prescriptionItem.getDeliveryAlternative().add(deliveryAlternative2);
        }

        if (!nextPossibleOrderDateInFuture) {
            prescriptionItem.setNextEarliestOrderDate(getRandomCalendar(random, -365));
        } else {
            Calendar c = Calendar.getInstance();

            c.add(Calendar.MONTH, 1);

            XMLGregorianCalendar randomCalendar = getRandomCalendar(random, 0);
            randomCalendar.setYear(c.get(Calendar.YEAR) + 1);
            randomCalendar.setDay(random.nextInt(20) + 1); // To avoid invalid dates.

//            prescriptionItem.setNextEarliestOrderDate(null);
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

        PrescribingOrganizationType prescribingOrganization = new PrescribingOrganizationType();
        prescribingOrganization.setPrescribingOrganizationName("Organisation " + random.nextInt());
        prescriptionItem.setPrescribingOrganization(prescribingOrganization);

        prescriptionItem.setLastValidDate(getRandomCalendar(random, 665L));
        prescriptionItem.setNoOfArticlesPerOrder(random.nextInt(5) * 1000 + 1000);
        prescriptionItem.setNoOfPackagesPerOrder(random.nextInt(5) * 50 + 50);
        prescriptionItem.setStatus(StatusEnum.values()[random.nextInt(StatusEnum.values().length)]);

        prescriptionItem.setAllowOtherInvoiceAddress(random.nextBoolean());

        subjectOfCare.getPrescriptionItem().add(prescriptionItem);

        OrderItemType orderItem = new OrderItemType();

        orderItem.setArticle(article);

        orderItem.setDeliveredDate(wrapInJaxBElement(getRandomCalendar(random, 0)));
        orderItem.setOrderDate(getRandomCalendar(random, 0));
        DeliveryChoiceType deliveryChoice = new DeliveryChoiceType();
        deliveryChoice.setDeliveryMethod(HEMLEVERANS);
        orderItem.setDeliveryChoice(deliveryChoice);
        orderItem.setPrescriptionItemId(prescriptionItem.getPrescriptionItemId());
        orderItem.setOrderDate(getRandomCalendar(random, -365)); // Todo To make more realistic take from a set of order dates so some items were ordered at the same time.

        subjectOfCare.getOrderItem().add(orderItem);
    }

    private void addSpecificPrescriptionItemSärnärWithSubArticles(Random random, SubjectOfCareType subjectOfCare) {

        String prescriptionItemId = random.nextInt(100000) + "";

        List<ArticleType> subArticles = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            ArticleType subArticle = new ArticleType();
            subArticle.setArticleName("Artikelnamn" + " - Särskild näring - Smak " + i);
            subArticle.setArticleNo(random.nextInt(100000) + "");
            subArticle.setIsOrderable(true);
            subArticle.setPackageSize(4);
            subArticle.setPackageSizeUnit("st");
            subArticle.setProductArea(toCvType(MockProductAreaEnum.SÄRNÄR));
            if (random.nextBoolean()) {
                subArticle.setVariety("Smak " + i);
            }

            subArticles.add(subArticle);

            OrderItemType orderItem = new OrderItemType();
            orderItem.setDeliveredDate(wrapInJaxBElement(getRandomCalendar(random, 0)));
            orderItem.setOrderDate(getRandomCalendar(random, 0));
            DeliveryChoiceType deliveryChoice = new DeliveryChoiceType();
            deliveryChoice.setDeliveryMethod(HEMLEVERANS);
            orderItem.setDeliveryChoice(deliveryChoice);
            orderItem.setArticle(subArticle);
            orderItem.setNoOfPcs(random.nextInt(100) * subArticle.getPackageSize());
            orderItem.setPrescriptionItemId(prescriptionItemId);
            orderItem.setOrderDate(getRandomCalendar(random, -365)); // Todo To make more realistic take from a set of order dates so some items were ordered at the same time.

            subjectOfCare.getOrderItem().add(orderItem);
        }

        PrescriptionItemType prescriptionItem = new PrescriptionItemType();

        ArticleType article2 = new ArticleType();
        article2.setArticleName("Artikelnamn" + " - Särskild näring2");
        article2.setArticleNo(random.nextInt(100000) + "");
        article2.setIsOrderable(true);
        article2.setPackageSize(2);
        article2.setPackageSizeUnit("st");
        article2.setProductArea(toCvType(MockProductAreaEnum.SÄRNÄR));

        prescriptionItem.getSubArticle().addAll(subArticles);

        prescriptionItem.setArticle(article2);

        prescriptionItem.setNoOfOrders(4);
        prescriptionItem.setNoOfRemainingOrders(3);

        DeliveryAlternativeType deliveryAlternative = getRandomDeliveryAlternativeType(random);
        prescriptionItem.getDeliveryAlternative().add(deliveryAlternative);

        DeliveryAlternativeType deliveryAlternative2 = getRandomDeliveryAlternativeType(random);

        if (!(deliveryAlternative.getDeliveryMethod().equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)
                && deliveryAlternative2.getDeliveryMethod().equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE))) {
            // We don't want multiple alternatives all having UTLÄMNINGSSTÄLLE as method.
            prescriptionItem.getDeliveryAlternative().add(deliveryAlternative2);
        }

        prescriptionItem.setNextEarliestOrderDate(getRandomCalendar(random, -365));

        prescriptionItem.setPrescriptionId(random.nextInt(100000) + "");
        prescriptionItem.setPrescriptionItemId(prescriptionItemId);
        PrescriberType prescriber = new PrescriberType();
        prescriber.setPrescriberName("Kalle Karlsson");
        prescriber.setPrescriberCode(random.nextInt(1000) + "");
        prescriber.setPrescriberId(random.nextInt(1000) + "");
        prescriber.setPrescriberTitle("Läkare");
        prescriptionItem.setPrescriber(prescriber);
        prescriptionItem.setLastValidDate(getRandomCalendar(random, 665L));
        prescriptionItem.setNoOfArticlesPerOrder(320);
        prescriptionItem.setNoOfPackagesPerOrder(160);
        prescriptionItem.setStatus(StatusEnum.AKTIV);

        subjectOfCare.getPrescriptionItem().add(prescriptionItem);

        OrderItemType orderItem = new OrderItemType();
        orderItem.setDeliveredDate(wrapInJaxBElement(getRandomCalendar(random, 0)));
        DeliveryChoiceType deliveryChoice = new DeliveryChoiceType();
        deliveryChoice.setDeliveryMethod(HEMLEVERANS);
        orderItem.setDeliveryChoice(deliveryChoice);
        orderItem.setArticle(article2);
        orderItem.setNoOfPcs(article2.getPackageSize() * prescriptionItem.getNoOfPackagesPerOrder());
        orderItem.setPrescriptionItemId(prescriptionItem.getPrescriptionItemId());
        orderItem.setOrderDate(getRandomCalendar(random, -365)); // Todo To make more realistic take from a set of order dates so some items were ordered at the same time.

        subjectOfCare.getOrderItem().add(orderItem);
    }

    private void addSpecificPrescriptionItemSärnärWithSubArticlesAndHomeDeliveryWithNotification(Random random, SubjectOfCareType subjectOfCare) {

        String prescriptionItemId = random.nextInt(100000) + "";

        List<ArticleType> subArticles = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            ArticleType subArticle = new ArticleType();
            subArticle.setArticleName("Artikelnamn" + " - Särskild näring - Smak " + i);
            subArticle.setArticleNo(random.nextInt(100000) + "");
            subArticle.setIsOrderable(true);
            subArticle.setPackageSize(4);
            subArticle.setPackageSizeUnit("st");
            subArticle.setProductArea(toCvType(MockProductAreaEnum.SÄRNÄR));
            if (random.nextBoolean()) {
                subArticle.setVariety("Smak " + i);
            }

            subArticles.add(subArticle);

            OrderItemType orderItem = new OrderItemType();
            orderItem.setDeliveredDate(wrapInJaxBElement(getRandomCalendar(random, 0)));
            orderItem.setOrderDate(getRandomCalendar(random, 0));
            DeliveryChoiceType deliveryChoice = new DeliveryChoiceType();
            deliveryChoice.setDeliveryMethod(HEMLEVERANS);
            orderItem.setDeliveryChoice(deliveryChoice);
            orderItem.setArticle(subArticle);
            orderItem.setNoOfPcs(random.nextInt(8) * subArticle.getPackageSize());
            orderItem.setPrescriptionItemId(prescriptionItemId);
            orderItem.setOrderDate(getRandomCalendar(random, -365)); // Todo To make more realistic take from a set of order dates so some items were ordered at the same time.

            subjectOfCare.getOrderItem().add(orderItem);
        }

        PrescriptionItemType prescriptionItem = new PrescriptionItemType();

        ArticleType article2 = new ArticleType();
        article2.setArticleName("Artikelnamn" + " - Särskild näring med hemleverans och notifiering");
        article2.setArticleNo(random.nextInt(100000) + "");
        article2.setIsOrderable(true);
        article2.setPackageSize(2);
        article2.setPackageSizeUnit("st");
        article2.setProductArea(toCvType(MockProductAreaEnum.SÄRNÄR));

        prescriptionItem.getSubArticle().addAll(subArticles);

        prescriptionItem.setArticle(article2);

        prescriptionItem.setNoOfOrders(4);
        prescriptionItem.setNoOfRemainingOrders(3);

        DeliveryAlternativeType deliveryAlternativeType1 = new DeliveryAlternativeType();
        deliveryAlternativeType1.setServicePointProvider(toCvType(MockServicePointProviderEnum.INGEN));
        deliveryAlternativeType1.setDeliveryMethod(HEMLEVERANS);
        deliveryAlternativeType1.setDeliveryMethodId("1234");
        deliveryAlternativeType1.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.TELEFON);
        deliveryAlternativeType1.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.BREV);

        prescriptionItem.getDeliveryAlternative().add(deliveryAlternativeType1);

        DeliveryAlternativeType deliveryAlternative = getRandomDeliveryAlternativeType(random);
        prescriptionItem.getDeliveryAlternative().add(deliveryAlternative);

        DeliveryAlternativeType deliveryAlternative2 = getRandomDeliveryAlternativeType(random);

        if (!(deliveryAlternative.getDeliveryMethod().equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)
                && deliveryAlternative2.getDeliveryMethod().equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE))) {
            // We don't want multiple alternatives all having UTLÄMNINGSSTÄLLE as method.
            prescriptionItem.getDeliveryAlternative().add(deliveryAlternative2);
        }

        prescriptionItem.setNextEarliestOrderDate(getRandomCalendar(random, -365));

        prescriptionItem.setPrescriptionId(random.nextInt(100000) + "");
        prescriptionItem.setPrescriptionItemId(prescriptionItemId);
        PrescriberType prescriber = new PrescriberType();
        prescriber.setPrescriberName("Kalle Karlsson");
        prescriber.setPrescriberCode(random.nextInt(1000) + "");
        prescriber.setPrescriberId(random.nextInt(1000) + "");
        prescriber.setPrescriberTitle("Läkare");
        prescriptionItem.setPrescriber(prescriber);
        prescriptionItem.setLastValidDate(getRandomCalendar(random, 665L));
        prescriptionItem.setNoOfArticlesPerOrder(32);
        prescriptionItem.setNoOfPackagesPerOrder(16);
        prescriptionItem.setStatus(StatusEnum.AKTIV);

        subjectOfCare.getPrescriptionItem().add(prescriptionItem);

        OrderItemType orderItem = new OrderItemType();
        orderItem.setDeliveredDate(wrapInJaxBElement(getRandomCalendar(random, 0)));
        DeliveryChoiceType deliveryChoice = new DeliveryChoiceType();
        deliveryChoice.setDeliveryMethod(HEMLEVERANS);
        orderItem.setDeliveryChoice(deliveryChoice);
        orderItem.setArticle(article2);
        orderItem.setNoOfPcs(article2.getPackageSize() * prescriptionItem.getNoOfPackagesPerOrder());
        orderItem.setPrescriptionItemId(prescriptionItem.getPrescriptionItemId());
        orderItem.setOrderDate(getRandomCalendar(random, -365)); // Todo To make more realistic take from a set of order dates so some items were ordered at the same time.

        subjectOfCare.getOrderItem().add(orderItem);
    }

    /*private CVType toCvType(MockServicePointProviderEnum providerEnum) {
        CVType cvType = new CVType();
        cvType.setDisplayName(providerEnum.name());
        return cvType;
    }*/

    private void addSpecificPrescriptionItemZeroPackages(Random random, SubjectOfCareType subjectOfCare) {

        String prescriptionItemId = random.nextInt(100000) + "";
        random.nextInt(1234);
        XMLGregorianCalendar randomCalendar1 = getRandomCalendar(random, -365);
        XMLGregorianCalendar randomCalendar2 = getRandomCalendar(random, -365);
        XMLGregorianCalendar randomCalendar3 = getRandomCalendar(random, -365);

        List<ArticleType> subArticles = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            ArticleType subArticle = new ArticleType();
            subArticle.setArticleName("Artikelnamn" + " - Särskild näring - Smak " + i);
            subArticle.setArticleNo(random.nextInt(100000) + "");
            subArticle.setIsOrderable(true);
            subArticle.setPackageSize(1); // Important
            subArticle.setPackageSizeUnit("st");
            subArticle.setProductArea(toCvType(MockProductAreaEnum.DIABETES));
            if (random.nextBoolean()) {
                subArticle.setVariety("Smak " + i);
            }

            subArticles.add(subArticle);

            XMLGregorianCalendar calendarForThisOrderItem;
            switch (random.nextInt(2)) {
                case 0:
                    calendarForThisOrderItem = randomCalendar1;
                    break;
                case 1:
                    calendarForThisOrderItem = randomCalendar2;
                    break;
                case 2:
                    calendarForThisOrderItem = randomCalendar3;
                    break;
                default:
                    throw new RuntimeException();
            }

            OrderItemType orderItem = new OrderItemType();
            orderItem.setDeliveredDate(wrapInJaxBElement(getRandomCalendar(random, 0)));
            orderItem.setOrderDate(getRandomCalendar(random, 0));
            DeliveryChoiceType deliveryChoice = new DeliveryChoiceType();
            deliveryChoice.setDeliveryMethod(HEMLEVERANS);
            orderItem.setDeliveryChoice(deliveryChoice);
            orderItem.setArticle(subArticle);
            orderItem.setNoOfPcs(random.nextInt(8) * subArticle.getPackageSize());
            orderItem.setPrescriptionItemId(prescriptionItemId);
            orderItem.setOrderDate(calendarForThisOrderItem); // Todo To make more realistic take from a set of order dates so some items were ordered at the same time.

            subjectOfCare.getOrderItem().add(orderItem);
        }

        PrescriptionItemType prescriptionItem = new PrescriptionItemType();

        ImageType articleImage = new ImageType();
        articleImage.setOriginal("http://www.sll.se/Global/Verksamhet/Pressbilder%20Sommarkampanj%202017/1177_1280x720_cykel.png");
        articleImage.setThumbnail("http://www.sll.se/Global/Verksamhet/Pressbilder%20Sommarkampanj%202017/1177_1280x720_cykel.png");

        ArticleType article2 = new ArticleType();
        article2.setArticleName("Artikelnamn" + " - Diabetes med 0 förpackningar och förpackningsstorlek 1");
        article2.setArticleNo(random.nextInt(100000) + "");
        article2.setIsOrderable(true);
        article2.setPackageSize(1); // Important
        article2.setPackageSizeUnit("st");
        article2.setProductArea(toCvType(MockProductAreaEnum.SÄRNÄR));
//        article2.setGrossPrice("1 200 kr");
        article2.setArticleImage(articleImage);

        prescriptionItem.setArticle(article2);

        prescriptionItem.getSubArticle().addAll(subArticles);

        prescriptionItem.setNoOfOrders(4);
        prescriptionItem.setNoOfRemainingOrders(3);

        prescriptionItem.setGrossPrice("1 200 kr");

        DeliveryAlternativeType deliveryAlternative = getRandomDeliveryAlternativeType(random);
        prescriptionItem.getDeliveryAlternative().add(deliveryAlternative);

        DeliveryAlternativeType deliveryAlternative2 = getRandomDeliveryAlternativeType(random);

        if (!(deliveryAlternative.getDeliveryMethod().equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)
                && deliveryAlternative2.getDeliveryMethod().equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE))) {
            // We don't want multiple alternatives all having UTLÄMNINGSSTÄLLE as method.
            prescriptionItem.getDeliveryAlternative().add(deliveryAlternative2);
        }

        prescriptionItem.setNextEarliestOrderDate(getRandomCalendar(random, -365));

        PrescribingOrganizationType prescribingOrganization = new PrescribingOrganizationType();
        prescribingOrganization.setPrescribingOrganizationName("Vårdenhet XXX");
        prescribingOrganization.setPrescribingOrganizationId("asdf");

        prescriptionItem.setPrescriptionId(random.nextInt(100000) + "");
        prescriptionItem.setPrescriptionItemId(prescriptionItemId);
        PrescriberType prescriber = new PrescriberType();
        prescriber.setPrescriberName("Kalle Karlsson");
        prescriber.setPrescriberCode(random.nextInt(1000) + "");
        prescriber.setPrescriberId(random.nextInt(1000) + "");
        prescriber.setPrescriberTitle("Läkare");
        prescriptionItem.setPrescriber(prescriber);
        prescriptionItem.setLastValidDate(getRandomCalendar(random, 665L));
        prescriptionItem.setNoOfArticlesPerOrder(32);
        prescriptionItem.setNoOfPackagesPerOrder(0);
        prescriptionItem.setStatus(StatusEnum.AKTIV);
        prescriptionItem.setPrescribingOrganization(prescribingOrganization);

        subjectOfCare.getPrescriptionItem().add(prescriptionItem);

        /*OrderItemType orderItem = new OrderItemType();
        orderItem.setDeliveredDate(wrapInJaxBElement(getRandomCalendar(random, 0)));
        DeliveryChoiceType deliveryChoice = new DeliveryChoiceType();
        deliveryChoice.setDeliveryMethod(DeliveryMethodEnum.HEMLEVERANS);
        orderItem.setDeliveryChoice(deliveryChoice);
        orderItem.setArticle(article2);
        orderItem.setNoOfPcs(article2.getPackageSize() * prescriptionItem.getNoOfPackagesPerOrder());
        orderItem.setPrescriptionItemId(prescriptionItem.getPrescriptionItemId());
        orderItem.setOrderDate(getRandomCalendar(random, -365)); // Todo To make more realistic take from a set of order dates so some items were ordered at the same time.

        subjectOfCare.getOrderItem().add(orderItem);*/
    }

    private void addSpecificPrescriptionItemWithTwoHomeDeliveryOptions(Random random, SubjectOfCareType subjectOfCare) {

        String prescriptionItemId = random.nextInt(100000) + "";
        random.nextInt(1234);
        XMLGregorianCalendar randomCalendar1 = getRandomCalendar(random, -365);
        XMLGregorianCalendar randomCalendar2 = getRandomCalendar(random, -365);
        XMLGregorianCalendar randomCalendar3 = getRandomCalendar(random, -365);

        PrescriptionItemType prescriptionItem = new PrescriptionItemType();

        ImageType articleImage = new ImageType();
        articleImage.setOriginal("https://www.w3schools.com/css/trolltunga.jpg");
        articleImage.setThumbnail("https://www.w3schools.com/css/trolltunga.jpg");

        ArticleType article2 = new ArticleType();
        article2.setArticleName("Diabetes med två hemleveransalternativ");
        article2.setArticleNo(random.nextInt(100000) + "");
        article2.setIsOrderable(true);
        article2.setPackageSize(1); // Important
        article2.setPackageSizeUnit("st");
        article2.setProductArea(toCvType(MockProductAreaEnum.DIABETES));
        article2.setArticleImage(articleImage);

        prescriptionItem.setArticle(article2);

        prescriptionItem.setNoOfOrders(4);
        prescriptionItem.setNoOfRemainingOrders(3);

        DeliveryAlternativeType deliveryAlternativeType1 = new DeliveryAlternativeType();
        deliveryAlternativeType1.setServicePointProvider(toCvType(MockServicePointProviderEnum.INGEN));
        deliveryAlternativeType1.setDeliveryMethod(HEMLEVERANS);
        deliveryAlternativeType1.setDeliveryMethodId("1234");
        deliveryAlternativeType1.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.TELEFON);
        deliveryAlternativeType1.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.SMS);

        prescriptionItem.getDeliveryAlternative().add(deliveryAlternativeType1);

        DeliveryAlternativeType deliveryAlternativetype2 = new DeliveryAlternativeType();
        deliveryAlternativetype2.setServicePointProvider(toCvType(MockServicePointProviderEnum.INGEN));
        deliveryAlternativetype2.setDeliveryMethod(HEMLEVERANS);
        deliveryAlternativetype2.setDeliveryMethodId("1234");

        prescriptionItem.getDeliveryAlternative().add(deliveryAlternativetype2);

        prescriptionItem.setNextEarliestOrderDate(getRandomCalendar(random, -365));

        prescriptionItem.setPrescriptionId(random.nextInt(100000) + "");
        prescriptionItem.setPrescriptionItemId(prescriptionItemId);
        PrescriberType prescriber = new PrescriberType();
        prescriber.setPrescriberName("Kalle Karlsson");
        prescriber.setPrescriberCode(random.nextInt(1000) + "");
        prescriber.setPrescriberId(random.nextInt(1000) + "");
        prescriber.setPrescriberTitle("Läkare");
        prescriptionItem.setPrescriber(prescriber);
        prescriptionItem.setLastValidDate(getRandomCalendar(random, 665L));
        prescriptionItem.setNoOfArticlesPerOrder(32);
        prescriptionItem.setNoOfPackagesPerOrder(0);
        prescriptionItem.setStatus(StatusEnum.AKTIV);

        subjectOfCare.getPrescriptionItem().add(prescriptionItem);
    }

    private void addSpecificPrescriptionItemWithTwoHomeDeliveryOptionsWithOtherNotifications(Random random, SubjectOfCareType subjectOfCare) {

        String prescriptionItemId = random.nextInt(100000) + "";
        random.nextInt(1234);

        PrescriptionItemType prescriptionItem = new PrescriptionItemType();

        ArticleType article2 = new ArticleType();
        article2.setArticleName("Diabetes med två andra hemleveransalternativ");
        article2.setArticleNo(random.nextInt(100000) + "");
        article2.setIsOrderable(true);
        article2.setPackageSize(1); // Important
        article2.setPackageSizeUnit("st");
        article2.setProductArea(toCvType(MockProductAreaEnum.DIABETES));

        prescriptionItem.setArticle(article2);

        prescriptionItem.setNoOfOrders(4);
        prescriptionItem.setNoOfRemainingOrders(3);

        DeliveryAlternativeType deliveryAlternativeType1 = new DeliveryAlternativeType();
        deliveryAlternativeType1.setServicePointProvider(toCvType(MockServicePointProviderEnum.INGEN));
        deliveryAlternativeType1.setDeliveryMethod(HEMLEVERANS);
        deliveryAlternativeType1.setDeliveryMethodId("1234");
        deliveryAlternativeType1.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.BREV);
        deliveryAlternativeType1.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.E_POST);

        prescriptionItem.getDeliveryAlternative().add(deliveryAlternativeType1);

        DeliveryAlternativeType deliveryAlternativetype2 = new DeliveryAlternativeType();
        deliveryAlternativetype2.setServicePointProvider(toCvType(MockServicePointProviderEnum.INGEN));
        deliveryAlternativetype2.setDeliveryMethod(HEMLEVERANS);
        deliveryAlternativetype2.setDeliveryMethodId("1234");

        prescriptionItem.getDeliveryAlternative().add(deliveryAlternativetype2);

        prescriptionItem.setNextEarliestOrderDate(getRandomCalendar(random, -365));

        prescriptionItem.setPrescriptionId(random.nextInt(100000) + "");
        prescriptionItem.setPrescriptionItemId(prescriptionItemId);
        PrescriberType prescriber = new PrescriberType();
        prescriber.setPrescriberName("Kalle Karlsson");
        prescriber.setPrescriberCode(random.nextInt(1000) + "");
        prescriber.setPrescriberId(random.nextInt(1000) + "");
        prescriber.setPrescriberTitle("Läkare");
        prescriptionItem.setPrescriber(prescriber);
        prescriptionItem.setLastValidDate(getRandomCalendar(random, 665L));
        prescriptionItem.setNoOfArticlesPerOrder(32);
        prescriptionItem.setNoOfPackagesPerOrder(0);
        prescriptionItem.setStatus(StatusEnum.AKTIV);

        subjectOfCare.getPrescriptionItem().add(prescriptionItem);
    }

    private void addSpecificPrescriptionItemWithTwoHomeDeliveryOptionsWithOverlappingNotifications(Random random, SubjectOfCareType subjectOfCare) {

        String prescriptionItemId = random.nextInt(100000) + "";
        random.nextInt(1234);

        PrescriptionItemType prescriptionItem = new PrescriptionItemType();

        ArticleType article2 = new ArticleType();
        article2.setArticleName("Diabetes med två överlappande hemleveransalternativ");
        article2.setArticleNo(random.nextInt(100000) + "");
        article2.setIsOrderable(true);
        article2.setPackageSize(1); // Important
        article2.setPackageSizeUnit("st");
        article2.setProductArea(toCvType(MockProductAreaEnum.DIABETES));

        prescriptionItem.setArticle(article2);

        prescriptionItem.setNoOfOrders(4);
        prescriptionItem.setNoOfRemainingOrders(3);

        DeliveryAlternativeType deliveryAlternativeType1 = new DeliveryAlternativeType();
        deliveryAlternativeType1.setServicePointProvider(toCvType(MockServicePointProviderEnum.INGEN));
        deliveryAlternativeType1.setDeliveryMethod(HEMLEVERANS);
        deliveryAlternativeType1.setDeliveryMethodId("1234");
        deliveryAlternativeType1.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.TELEFON);
        deliveryAlternativeType1.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.E_POST);

        prescriptionItem.getDeliveryAlternative().add(deliveryAlternativeType1);

        DeliveryAlternativeType deliveryAlternativetype2 = new DeliveryAlternativeType();
        deliveryAlternativetype2.setServicePointProvider(toCvType(MockServicePointProviderEnum.INGEN));
        deliveryAlternativetype2.setDeliveryMethod(HEMLEVERANS);
        deliveryAlternativetype2.setDeliveryMethodId("1234");

        prescriptionItem.getDeliveryAlternative().add(deliveryAlternativetype2);

        prescriptionItem.setNextEarliestOrderDate(getRandomCalendar(random, -365));

        prescriptionItem.setPrescriptionId(random.nextInt(100000) + "");
        prescriptionItem.setPrescriptionItemId(prescriptionItemId);
        PrescriberType prescriber = new PrescriberType();
        prescriber.setPrescriberName("Kalle Karlsson");
        prescriber.setPrescriberCode(random.nextInt(1000) + "");
        prescriber.setPrescriberId(random.nextInt(1000) + "");
        prescriber.setPrescriberTitle("Läkare");
        prescriptionItem.setPrescriber(prescriber);
        prescriptionItem.setLastValidDate(getRandomCalendar(random, 665L));
        prescriptionItem.setNoOfArticlesPerOrder(32);
        prescriptionItem.setNoOfPackagesPerOrder(0);
        prescriptionItem.setStatus(StatusEnum.AKTIV);

        subjectOfCare.getPrescriptionItem().add(prescriptionItem);
    }

    private void addSpecificPrescriptionItemWithCollectDeliveryWithIngenProvider(Random random, SubjectOfCareType subjectOfCare) {

        String prescriptionItemId = random.nextInt(100000) + "";
        random.nextInt(1234);

        PrescriptionItemType prescriptionItem = new PrescriptionItemType();

        ArticleType article2 = new ArticleType();
        article2.setArticleName("Diabetes med utlämningsställe, INGEN leverantör");
        article2.setArticleNo(random.nextInt(100000) + "");
        article2.setIsOrderable(true);
        article2.setPackageSize(1); // Important
        article2.setPackageSizeUnit("st");
        article2.setProductArea(toCvType(MockProductAreaEnum.DIABETES));

        prescriptionItem.setArticle(article2);

        prescriptionItem.setNoOfOrders(4);
        prescriptionItem.setNoOfRemainingOrders(3);

        DeliveryAlternativeType deliveryAlternativeType1 = new DeliveryAlternativeType();
        deliveryAlternativeType1.setServicePointProvider(toCvType(MockServicePointProviderEnum.INGEN));
        deliveryAlternativeType1.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        deliveryAlternativeType1.setDeliveryMethodId("1234");

        prescriptionItem.getDeliveryAlternative().add(deliveryAlternativeType1);

        prescriptionItem.setNextEarliestOrderDate(getRandomCalendar(random, -365));

        prescriptionItem.setPrescriptionId(random.nextInt(100000) + "");
        prescriptionItem.setPrescriptionItemId(prescriptionItemId);
        PrescriberType prescriber = new PrescriberType();
        prescriber.setPrescriberName("Kalle Karlsson");
        prescriber.setPrescriberCode(random.nextInt(1000) + "");
        prescriber.setPrescriberId(random.nextInt(1000) + "");
        prescriber.setPrescriberTitle("Läkare");
        prescriptionItem.setPrescriber(prescriber);
        prescriptionItem.setLastValidDate(getRandomCalendar(random, 730L));
        prescriptionItem.setNoOfArticlesPerOrder(32);
        prescriptionItem.setNoOfPackagesPerOrder(0);
        prescriptionItem.setStatus(StatusEnum.AKTIV);

        subjectOfCare.getPrescriptionItem().add(prescriptionItem);
    }

    private JAXBElement<XMLGregorianCalendar> wrapInJaxBElement(XMLGregorianCalendar calendar) {
        return new riv.crm.selfservice.medicalsupply._2.ObjectFactory().createOrderItemTypeDeliveredDate(calendar);
    }

    private DeliveryAlternativeType getRandomDeliveryAlternativeType(Random random) {
        DeliveryAlternativeType deliveryAlternative = new DeliveryAlternativeType();
        deliveryAlternative.setDeliveryMethodId(random.nextInt(10000) + "");
        deliveryAlternative.setDeliveryMethod(
                DeliveryMethodEnum.values()[random.nextInt(DeliveryMethodEnum.values().length)]);

        deliveryAlternative.setAllowDeliveryComment(random.nextBoolean());
        deliveryAlternative.setAllowContactPerson(true);

        if (deliveryAlternative.getDeliveryMethod().equals(HEMLEVERANS)) {
            deliveryAlternative.setServicePointProvider(toCvType(MockServicePointProviderEnum.INGEN));
        } else {
            deliveryAlternative.setServicePointProvider(
                    toCvType(MockServicePointProviderEnum.values()[random.nextInt(MockServicePointProviderEnum.values().length)]));

            // Inefficient but this is a mock...
            while (MockServicePointProviderEnum.valueOf(deliveryAlternative.getServicePointProvider().getOriginalText())
                    .equals(MockServicePointProviderEnum.INGEN)) {
                // Continue until something other than MockServicePointProviderEnum.INGEN gets set.
                deliveryAlternative.setServicePointProvider(
                        toCvType(MockServicePointProviderEnum.values()[random.nextInt(MockServicePointProviderEnum.values().length)]));

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

            DeliveryNotificationMethodEnum third = DeliveryNotificationMethodEnum.values()
                    [random.nextInt(DeliveryNotificationMethodEnum.values().length)];

            while (third == first || third == second) {
                third = DeliveryNotificationMethodEnum.values()
                        [random.nextInt(DeliveryNotificationMethodEnum.values().length)];
            }

            deliveryAlternative.getDeliveryNotificationMethod().add(third);

        }
        return deliveryAlternative;
    }

    private XMLGregorianCalendar getRandomCalendar(Random random, long offset) {
        XMLGregorianCalendar xmlGregorianCalendar = null;
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }

        xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar());

        Period period = Period.ofDays(random.nextInt(730));

        long timeToSubtract = (offset - period.getDays()) * 24L * 60L * 60L * 1000L;

        Duration duration = datatypeFactory.newDuration(timeToSubtract);

        xmlGregorianCalendar.add(duration);

        return xmlGregorianCalendar;
    }

}
