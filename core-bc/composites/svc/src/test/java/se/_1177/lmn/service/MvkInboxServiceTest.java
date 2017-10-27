package se._1177.lmn.service;

import freemarker.core.InvalidReferenceException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import riv.crm.selfservice.medicalsupply._0.AddressType;
import riv.crm.selfservice.medicalsupply._0.ArticleType;
import riv.crm.selfservice.medicalsupply._0.CountryCodeEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._0.ObjectFactory;
import riv.crm.selfservice.medicalsupply._0.OrderRowType;
import riv.crm.selfservice.medicalsupply._0.ProductAreaEnum;

import javax.xml.bind.JAXBElement;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Patrik Björk
 */
public class MvkInboxServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MvkInboxServiceTest.class);

    private String expectedMessage;
    private String expectedMessageCollectDeliveryWithoutDeliveryPoint;
    private String expectedMessageCollectDeliveryOnlyWithoutDeliveryPoint;

    @Before
    public void init() throws IOException {
        this.expectedMessage = getContent("expectedInboxMessage.xml");
        this.expectedMessageCollectDeliveryWithoutDeliveryPoint =
                getContent("expectedInboxMessageCollectDeliveryWithoutDeliveryPoint.xml");
        this.expectedMessageCollectDeliveryOnlyWithoutDeliveryPoint =
                getContent("expectedInboxMessageCollectDeliveryOnlyWithoutDeliveryPoint.xml");
    }

    private String getContent(String fileName) throws IOException {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(fileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
        int n;
        while ((n = resourceAsStream.read(buf)) > -1) {
            baos.write(buf, 0, n);
        }

        resourceAsStream.close();

        return baos.toString("UTF-8");
    }

    @Test
    public void composeMsg() throws Exception {
        MvkInboxService mvkInboxService = new MvkInboxService(null);

        List<OrderRowType> orderRows = new ArrayList<>();
        List<DeliveryChoiceType> deliveryChoices = new ArrayList<>();

        ArticleType article1 = new ArticleType();
        ArticleType article2 = new ArticleType();

        OrderRowType item1 = new OrderRowType();
        OrderRowType item2 = new OrderRowType();

        article1.setArticleNo("1234");
        article2.setArticleNo("4321");

        article1.setArticleName("Artikalnamn1");
        article2.setArticleName("Artikelnamn2");

        article1.setProductArea(ProductAreaEnum.DIABETES);
        article2.setProductArea(ProductAreaEnum.INKONTINENS);

        item1.setArticle(article1);
        item2.setArticle(article2);

        item1.setNoOfPackages(3);
        item2.setNoOfPackages(4);

        orderRows.add(item1);
        orderRows.add(item2);

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
        choice1.setDeliveryNotificationMethod(wrapInJAXBElement(DeliveryNotificationMethodEnum.SMS));

        AddressType homeAddress = new AddressType();
        homeAddress.setStreet("Gatan 37");
        homeAddress.setPostalCode("43213");
        homeAddress.setReceiver("Kalle Karlsson");
        homeAddress.setDoorCode("4321");
        homeAddress.setCity("Bullerbyn");
        homeAddress.setPhone("031-123456");

        choice2.setHomeDeliveryAddress(homeAddress);

        deliveryChoices.add(choice1);
        deliveryChoices.add(choice2);

        String result = mvkInboxService.composeMsg(orderRows, deliveryChoices);

        assertEquals(expectedMessage, result);
    }

    private JAXBElement<DeliveryNotificationMethodEnum> wrapInJAXBElement(DeliveryNotificationMethodEnum sms) {
        return new ObjectFactory().createDeliveryChoiceTypeDeliveryNotificationMethod(sms);
    }

    @Test
    public void testNoHomeAddress() throws Exception {
        MvkInboxService mvkInboxService = new MvkInboxService(null);

        List<OrderRowType> orderRows = new ArrayList<>();
        List<DeliveryChoiceType> deliveryChoices = new ArrayList<>();

        ArticleType article1 = new ArticleType();
        ArticleType article2 = new ArticleType();

        OrderRowType item1 = new OrderRowType();
        OrderRowType item2 = new OrderRowType();

        article1.setArticleNo("1234");
        article2.setArticleNo("4321");

        article1.setArticleName("Artikalnamn1");
        article2.setArticleName("Artikelnamn2");

        article1.setProductArea(ProductAreaEnum.DIABETES);
        article2.setProductArea(ProductAreaEnum.INKONTINENS);

        item1.setArticle(article1);
        item2.setArticle(article2);

        item1.setNoOfPackages(3);
        item2.setNoOfPackages(4);

        orderRows.add(item1);
        orderRows.add(item2);

        DeliveryChoiceType choice1 = new DeliveryChoiceType();
        DeliveryChoiceType choice2 = new DeliveryChoiceType();

        choice1.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        choice2.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);

        DeliveryPointType deliveryPoint = new DeliveryPointType();
        deliveryPoint.setDeliveryPointAddress("Gatan 1");
        deliveryPoint.setDeliveryPointName("Matnära");
        deliveryPoint.setDeliveryPointPostalCode("12345");
        deliveryPoint.setDeliveryPointCity("Ankeborg");
        deliveryPoint.setCountryCode(CountryCodeEnum.SE);

        choice1.setDeliveryPoint(deliveryPoint);
        choice1.setDeliveryNotificationReceiver("070-2345678");
        choice1.setDeliveryNotificationMethod(wrapInJAXBElement(DeliveryNotificationMethodEnum.BREV));

        choice2.setDeliveryPoint(deliveryPoint);
        choice2.setDeliveryNotificationReceiver("0731234234");
        choice2.setDeliveryNotificationMethod(null);

        deliveryChoices.add(choice1);
        deliveryChoices.add(choice2);

        try {
            String result = mvkInboxService.composeMsg(orderRows, deliveryChoices);
            fail();
        } catch (InvalidReferenceException e) {
            // choice2 has BREV as delivery notification method and therefore must have a home address.
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCollectDeliveryWithoutDeliveryPoint() throws Exception {
        MvkInboxService mvkInboxService = new MvkInboxService(null);

        List<OrderRowType> orderRows = new ArrayList<>();
        List<DeliveryChoiceType> deliveryChoices = new ArrayList<>();

        ArticleType article1 = new ArticleType();
        ArticleType article2 = new ArticleType();

        OrderRowType item1 = new OrderRowType();
        OrderRowType item2 = new OrderRowType();

        article1.setArticleNo("1234");
        article2.setArticleNo("4321");

        article1.setArticleName("Artikalnamn1");
        article2.setArticleName("Artikelnamn2");

        article1.setProductArea(ProductAreaEnum.DIABETES);
        article2.setProductArea(ProductAreaEnum.INKONTINENS);

        item1.setArticle(article1);
        item2.setArticle(article2);

        item1.setNoOfPackages(3);
        item2.setNoOfPackages(4);

        orderRows.add(item1);
        orderRows.add(item2);

        DeliveryChoiceType choice1 = new DeliveryChoiceType();
        DeliveryChoiceType choice2 = new DeliveryChoiceType();

        choice1.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        choice2.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);

        AddressType homeAddress = new AddressType();
        homeAddress.setStreet("Gatan 37");
        homeAddress.setPostalCode("43213");
        homeAddress.setReceiver("Kalle Karlsson");
        homeAddress.setDoorCode("4321");
        homeAddress.setCity("Bullerbyn");
        homeAddress.setPhone("031-123456");

        choice2.setHomeDeliveryAddress(homeAddress);

        DeliveryPointType deliveryPoint = new DeliveryPointType();
        deliveryPoint.setDeliveryPointAddress("Gatan 1");
        deliveryPoint.setDeliveryPointName("Matnära");
        deliveryPoint.setDeliveryPointPostalCode("12345");
        deliveryPoint.setDeliveryPointCity("Ankeborg");
        deliveryPoint.setCountryCode(CountryCodeEnum.SE);

        choice1.setDeliveryPoint(deliveryPoint);
        choice1.setDeliveryNotificationReceiver("070-2345678");
        choice1.setDeliveryNotificationMethod(wrapInJAXBElement(DeliveryNotificationMethodEnum.SMS));

        choice2.setDeliveryNotificationReceiver("0731234234");
        choice2.setDeliveryNotificationMethod(null);

        deliveryChoices.add(choice1);
        deliveryChoices.add(choice2);

        String result = mvkInboxService.composeMsg(orderRows, deliveryChoices);

        assertEquals(expectedMessageCollectDeliveryWithoutDeliveryPoint, result);
    }

    @Test
    public void testCollectDeliveryOnlyWithoutDeliveryPoint() throws Exception {
        MvkInboxService mvkInboxService = new MvkInboxService(null);

        List<OrderRowType> orderRows = new ArrayList<>();
        List<DeliveryChoiceType> deliveryChoices = new ArrayList<>();

        ArticleType article1 = new ArticleType();
        ArticleType article2 = new ArticleType();

        OrderRowType item1 = new OrderRowType();
        OrderRowType item2 = new OrderRowType();

        article1.setArticleNo("1234");
        article2.setArticleNo("4321");

        article1.setArticleName("Artikalnamn1");
        article2.setArticleName("Artikelnamn2");

        article1.setProductArea(ProductAreaEnum.DIABETES);
        article2.setProductArea(ProductAreaEnum.INKONTINENS);

        item1.setArticle(article1);
        item2.setArticle(article2);

        item1.setNoOfPackages(3);
        item2.setNoOfPackages(4);

        orderRows.add(item1);
        orderRows.add(item2);

        DeliveryChoiceType choice1 = new DeliveryChoiceType();

        choice1.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);

        AddressType homeAddress = new AddressType();
        homeAddress.setStreet("Gatan 37");
        homeAddress.setPostalCode("43213");
        homeAddress.setReceiver("Kalle Karlsson");
        homeAddress.setDoorCode("4321");
        homeAddress.setCity("Bullerbyn");
        homeAddress.setPhone("031-123456");

        choice1.setHomeDeliveryAddress(homeAddress);

        DeliveryPointType deliveryPoint = new DeliveryPointType();
        deliveryPoint.setDeliveryPointAddress("Gatan 1");
        deliveryPoint.setDeliveryPointName("Matnära");
        deliveryPoint.setDeliveryPointPostalCode("12345");
        deliveryPoint.setDeliveryPointCity("Ankeborg");
        deliveryPoint.setCountryCode(CountryCodeEnum.SE);

        choice1.setDeliveryNotificationReceiver("070-2345678");
        choice1.setDeliveryNotificationMethod(wrapInJAXBElement(DeliveryNotificationMethodEnum.SMS));

        deliveryChoices.add(choice1);

        String result = mvkInboxService.composeMsg(orderRows, deliveryChoices);

        assertEquals(expectedMessageCollectDeliveryOnlyWithoutDeliveryPoint, result);
    }

    @Test
    public void testAllAreSame() throws Exception {

        DeliveryPointType dp1 = new DeliveryPointType();
        DeliveryPointType dp2 = new DeliveryPointType();
        DeliveryPointType dp3 = new DeliveryPointType();
        DeliveryPointType dp4 = new DeliveryPointType();

        dp1.setDeliveryPointId("same");
        dp2.setDeliveryPointId("same");
        dp3.setDeliveryPointId("same");
        dp4.setDeliveryPointId("same");

        DeliveryChoiceType d1 = new DeliveryChoiceType();
        DeliveryChoiceType d2 = new DeliveryChoiceType();
        DeliveryChoiceType d3 = new DeliveryChoiceType();
        DeliveryChoiceType d4 = new DeliveryChoiceType();

        d1.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        d2.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        d3.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        d4.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);

        d1.setDeliveryPoint(dp1);
        d2.setDeliveryPoint(dp2);
        d3.setDeliveryPoint(dp3);
        d4.setDeliveryPoint(dp4);

        List<DeliveryChoiceType> deliveryChoices = Arrays.asList(d1, d2, d3, d4);

        boolean result = MvkInboxService.allAreSame(deliveryChoices);

        assertTrue(result);
    }

    @Test
    public void testAllAreSameNotSame() throws Exception {

        DeliveryPointType dp1 = new DeliveryPointType();
        DeliveryPointType dp2 = new DeliveryPointType();
        DeliveryPointType dp4 = new DeliveryPointType();

        dp1.setDeliveryPointId("same");
        dp2.setDeliveryPointId("same");
        dp4.setDeliveryPointId("same");

        DeliveryChoiceType d1 = new DeliveryChoiceType();
        DeliveryChoiceType d2 = new DeliveryChoiceType();
        DeliveryChoiceType d3 = new DeliveryChoiceType();
        DeliveryChoiceType d4 = new DeliveryChoiceType();

        d1.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        d2.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        d3.setDeliveryMethod(DeliveryMethodEnum.HEMLEVERANS);
        d4.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);

        d1.setDeliveryPoint(dp1);
        d2.setDeliveryPoint(dp2);
        d4.setDeliveryPoint(dp4);

        List<DeliveryChoiceType> deliveryChoices = Arrays.asList(d1, d2, d3, d4);

        boolean result = MvkInboxService.allAreSame(deliveryChoices);

        assertFalse(result);
    }
}