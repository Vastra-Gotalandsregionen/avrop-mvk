package se._1177.lmn.service;

import freemarker.core.InvalidReferenceException;
import mvk.crm.casemanagement.inbox.addmessage._2.rivtabp21.AddMessageResponderInterface;
import mvk.crm.casemanagement.inbox.addmessageresponder._2.AddMessageType;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import riv.crm.selfservice.medicalsupply._1.AddressType;
import riv.crm.selfservice.medicalsupply._1.ArticleType;
import riv.crm.selfservice.medicalsupply._1.CountryCodeEnum;
import riv.crm.selfservice.medicalsupply._1.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._1.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._1.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._1.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._1.ObjectFactory;
import riv.crm.selfservice.medicalsupply._1.OrderRowType;
import riv.crm.selfservice.medicalsupply._1.ProductAreaEnum;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._1.RegisterMedicalSupplyOrderType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    public void complexMessage() throws Exception {

        // Given
        RegisterMedicalSupplyOrderType order = getComplexRegisterMedicalSupplyOrderType();

        AddMessageResponderInterface mock = mock(AddMessageResponderInterface.class);

        MvkInboxService mvkInboxService = new MvkInboxService(mock);

        // When
        String msg = mvkInboxService.composeMsg(order.getOrder().getOrderRow());

        // Then
        assertEquals(getContent("expectedInboxMessageComplexExample.xml"), msg);
    }

    @Test
    public void sendInboxMessage() throws Exception {

        // Given
        AddMessageResponderInterface mock = mock(AddMessageResponderInterface.class);

        MvkInboxService mvkInboxService = new MvkInboxService(mock);

        List<OrderRowType> orderRows = new ArrayList<>();

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

        item1.setDeliveryChoice(choice1);
        item2.setDeliveryChoice(choice2);

        // When
        mvkInboxService.sendInboxMessage("191212121212", orderRows, "SE00000000000-0001");

        // Then
        verify(mock, times(1)).addMessage(any(AddMessageType.class));
    }

    @Test
    public void composeMsg() throws Exception {
        MvkInboxService mvkInboxService = new MvkInboxService(null);

        List<OrderRowType> orderRows = new ArrayList<>();

        ArticleType article1 = new ArticleType();
        ArticleType article2 = new ArticleType();
        ArticleType article3 = new ArticleType();

        OrderRowType item1 = new OrderRowType();
        OrderRowType item2 = new OrderRowType();
        OrderRowType item3 = new OrderRowType();

        article1.setArticleNo("1234");
        article2.setArticleNo("4321");
        article3.setArticleNo("5678");

        article1.setArticleName("Artikalnamn1");
        article2.setArticleName("Artikelnamn2");
        article3.setArticleName("Artikelnamn3");

        article1.setProductArea(ProductAreaEnum.DIABETES);
        article2.setProductArea(ProductAreaEnum.INKONTINENS);
        article3.setProductArea(ProductAreaEnum.INKONTINENS);

        item1.setArticle(article1);
        item2.setArticle(article2);
        item3.setArticle(article3);

        item1.setNoOfPackages(3);
        item2.setNoOfPackages(4);
        item2.setNoOfPackages(4);

        orderRows.add(item1);
        orderRows.add(item2);
        orderRows.add(item3);

        DeliveryChoiceType choice1 = new DeliveryChoiceType();
        DeliveryChoiceType choice2 = new DeliveryChoiceType();
        DeliveryChoiceType choice3 = new DeliveryChoiceType();

        choice1.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        choice2.setDeliveryMethod(DeliveryMethodEnum.HEMLEVERANS);
        choice3.setDeliveryMethod(DeliveryMethodEnum.HEMLEVERANS);

        DeliveryPointType deliveryPoint = new DeliveryPointType();
        deliveryPoint.setDeliveryPointAddress("Gatan 1");
        deliveryPoint.setDeliveryPointName("Matnära");
        deliveryPoint.setDeliveryPointPostalCode("12345");
        deliveryPoint.setDeliveryPointCity("Ankeborg");
        deliveryPoint.setCountryCode(CountryCodeEnum.SE);

        choice1.setDeliveryPoint(deliveryPoint);

        choice1.setDeliveryNotificationReceiver("070-2345678");
        choice1.setDeliveryNotificationMethod(wrapInJAXBElement(DeliveryNotificationMethodEnum.SMS));

        AddressType address = new AddressType();
        address.setReceiver("receiver 1");
        address.setCareOfAddress("careof 1");
        address.setStreet("street 1");
        address.setPostalCode("12345");
        address.setCity("city 1");
        choice1.setInvoiceAddress(address);

        AddressType homeAddress = new AddressType();
        homeAddress.setStreet("Gatan 37");
        homeAddress.setPostalCode("43213");
        homeAddress.setReceiver("Kalle Karlsson");
        homeAddress.setDoorCode("4321");
        homeAddress.setCity("Bullerbyn");
        homeAddress.setPhone("031-123456");

        choice2.setHomeDeliveryAddress(homeAddress);
        choice2.setDeliveryComment("Delivery comment 1.");

        choice3.setHomeDeliveryAddress(homeAddress);
        choice3.setDeliveryComment("Delivery comment 1.");

        item1.setDeliveryChoice(choice1);
        item2.setDeliveryChoice(choice2);
        item3.setDeliveryChoice(choice3);

        String result = mvkInboxService.composeMsg(orderRows);

        assertEquals(expectedMessage, result);
    }

    @Test
    public void testNoHomeAddress() throws Exception {
        MvkInboxService mvkInboxService = new MvkInboxService(null);

        List<OrderRowType> orderRows = new ArrayList<>();

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
        choice1.setDeliveryNotificationMethod(wrapInJAXBElement(DeliveryNotificationMethodEnum.BREV));

        choice2.setDeliveryPoint(deliveryPoint);
        choice2.setDeliveryNotificationMethod(null);

        item1.setDeliveryChoice(choice1);
        item2.setDeliveryChoice(choice2);

        try {
            String result = mvkInboxService.composeMsg(orderRows);
            fail();
        } catch (InvalidReferenceException e) {
            // choice1 has BREV as delivery notification method and therefore must have a home address.
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCollectDeliveryWithoutDeliveryPoint() throws Exception {
        MvkInboxService mvkInboxService = new MvkInboxService(null);

        List<OrderRowType> orderRows = new ArrayList<>();

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
        choice2.setContactPerson("The contact person");

        item1.setDeliveryChoice(choice1);
        item2.setDeliveryChoice(choice2);

        String result = mvkInboxService.composeMsg(orderRows);

        assertEquals(expectedMessageCollectDeliveryWithoutDeliveryPoint, result);
    }

    @Test
    public void testCollectDeliveryOnlyWithoutDeliveryPoint() throws Exception {
        MvkInboxService mvkInboxService = new MvkInboxService(null);

        List<OrderRowType> orderRows = new ArrayList<>();

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

        choice1.setContactPerson("The contact person");

        item1.setDeliveryChoice(choice1);
        item2.setDeliveryChoice(choice1);

        String result = mvkInboxService.composeMsg(orderRows);

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

    private RegisterMedicalSupplyOrderType getComplexRegisterMedicalSupplyOrderType() throws JAXBException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("registerMedicalSupplyOrderExample.xml");

        JAXBContext jaxbContext = JAXBContext.newInstance(RegisterMedicalSupplyOrderType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        JAXBElement<RegisterMedicalSupplyOrderType> orderType = unmarshaller.unmarshal(new StreamSource(inputStream), RegisterMedicalSupplyOrderType.class);

        return orderType.getValue();
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

    private JAXBElement<DeliveryNotificationMethodEnum> wrapInJAXBElement(DeliveryNotificationMethodEnum sms) {
        return new ObjectFactory().createDeliveryChoiceTypeDeliveryNotificationMethod(sms);
    }

}