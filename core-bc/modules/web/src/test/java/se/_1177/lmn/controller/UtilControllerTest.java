package se._1177.lmn.controller;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import riv.crm.selfservice.medicalsupply._2.DeliveryMethodEnum;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Patrik Björk
 */
public class UtilControllerTest {

    private UtilController utilController = new UtilController();

    @Before
    public void setup() {
        LocaleController localeController = mock(LocaleController.class);
        when(localeController.getLocale()).thenReturn(new Locale("sv", "SE", "14"));

        MessageController messageController = new MessageController();
        ReflectionTestUtils.setField(messageController, "localeController", localeController);

        messageController.init();

        ReflectionTestUtils.setField(utilController, "messageController", messageController);
    }

    @Test
    public void toDate() throws Exception {
        XMLGregorianCalendar xmlGregorianCalendar = null;
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }

        // Test today
        GregorianCalendar calendar = new GregorianCalendar();

        xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);

        Date date = utilController.toDate(xmlGregorianCalendar);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // Verify we get the same day date string.
        assertEquals(sdf.format(new Date()), sdf.format(date));
    }

    @Test
    public void isAfterToday() throws Exception {

        XMLGregorianCalendar xmlGregorianCalendar = null;
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }

        // Test today
        GregorianCalendar calendar = new GregorianCalendar();
        xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);

        XMLGregorianCalendar today = xmlGregorianCalendar;

        assertFalse(utilController.isAfterToday(today));

        calendar.add(Calendar.DATE, 1);

        // Test tomorrow
        xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);

        XMLGregorianCalendar tomorrow = xmlGregorianCalendar;

        assertTrue(utilController.isAfterToday(tomorrow));
    }

    @Test
    public void toSubjectOfCareIdWithHyphen() {
        assertEquals("121212-1212",     utilController.toSubjectOfCareIdWithHyphen("1212121212"));
        assertEquals("19121212-1212",   utilController.toSubjectOfCareIdWithHyphen("191212121212"));
        assertEquals("121212-1212",     utilController.toSubjectOfCareIdWithHyphen("121212-1212"));
        assertEquals("19121212-1212",   utilController.toSubjectOfCareIdWithHyphen("19121212-1212"));
        assertEquals("asdfjal",         utilController.toSubjectOfCareIdWithHyphen("asdfjal"));
        assertEquals(null,              utilController.toSubjectOfCareIdWithHyphen(null));
    }

    @Test
    public void getCustomerServiceInfo() throws IOException {
        Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("application_sv_SE_14.properties"));

        assertEquals(properties.getProperty("customer.service.info"), utilController.getCustomerServiceInfo());
    }

    @Test
    public void mapToTextCollectDelivery() {
        String value = DeliveryMethodEnum.UTLÄMNINGSSTÄLLE.value();
        String capitalized = UtilController.capitalizeFully(value);
        String result = UtilController.mapToText(value);

        assertEquals(capitalized, result);
    }

    @Test
    public void mapToTextHomeDelivery() {
        String value = DeliveryMethodEnum.HEMLEVERANS.value();
        String result = UtilController.mapToText(value);

        assertEquals("Leveransadress", result);
    }

    @Test
    public void mapToTextIllegalArgument() {
        String value = "asdfaea";
        try {
            String result = UtilController.mapToText(value);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        }
    }
}
