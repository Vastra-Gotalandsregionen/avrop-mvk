package se._1177.lmn.service;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import riv.crm.selfservice.medicalsupply._0.AdressType;
import riv.crm.selfservice.medicalsupply._0.ArticleType;
import riv.crm.selfservice.medicalsupply._0.CountryCodeEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.ProductAreaEnum;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Patrik Björk
 */
public class MvkInboxServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MvkInboxServiceTest.class);

    @Test @Ignore // TODO: 2016-05-09 Run this when schema is updated when spelling corrections.
    public void composeMsg() throws Exception {
        MvkInboxService mvkInboxService = new MvkInboxService(null);

        List<PrescriptionItemType> prescriptionItems = new ArrayList<>();
        List<DeliveryChoiceType> deliveryChoices = new ArrayList<>();

        ArticleType article1 = new ArticleType();
        ArticleType article2 = new ArticleType();

        PrescriptionItemType item1 = new PrescriptionItemType();
        PrescriptionItemType item2 = new PrescriptionItemType();

        article1.setArticleNo("1234");
        article2.setArticleNo("4321");

        article1.setArticleName("Artikalnamn1");
        article2.setArticleName("Artikelnamn2");

        article1.setProductArea(ProductAreaEnum.DIABETES);
        article2.setProductArea(ProductAreaEnum.INKONTINENS);

        item1.setArticle(article1);
        item2.setArticle(article2);

        prescriptionItems.add(item1);
        prescriptionItems.add(item2);

        DeliveryChoiceType choice1 = new DeliveryChoiceType();
        DeliveryChoiceType choice2 = new DeliveryChoiceType();

        choice1.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        choice2.setDeliveryMethod(DeliveryMethodEnum.HEMLEVERANS);

        DeliveryPointType deliveryPoint = new DeliveryPointType();
        deliveryPoint.setDeliveryPointAddress("Gatan 1");
        deliveryPoint.setDeliveryPointName("Matnära");
        deliveryPoint.setDeliveryPointPostalCode("12345");
        deliveryPoint.setDeliveryPointCity("Ankeborg");
        deliveryPoint.setCountryCode(CountryCodeEnum.SE);

        choice1.setDeliveryPoint(deliveryPoint);

        choice1.setDeliveryNotificationReceiver("070-2345678");
        choice1.setDeliveryNotificationMethod(DeliveryNotificationMethodEnum.SMS);

        AdressType homeAddress = new AdressType();
        homeAddress.setStreet("Gatan 37");
        homeAddress.setPostalCode("43213");
        homeAddress.setReciever("Kalle Karlsson");
        homeAddress.setDoorCode("4321");
        homeAddress.setCity("Bullerbyn");
        homeAddress.setPhone("031-123456");

        choice2.setHomeDeliveryAdress(homeAddress);

        deliveryChoices.add(choice1);
        deliveryChoices.add(choice2);

        String result = mvkInboxService.composeMsg(prescriptionItems, deliveryChoices);

        LOGGER.info(result);
    }

}