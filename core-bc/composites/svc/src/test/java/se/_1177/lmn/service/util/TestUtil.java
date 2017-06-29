package se._1177.lmn.service.util;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TestUtil {

    public static XMLGregorianCalendar getTodayPlusDays(int daysToAdd) {
        GregorianCalendar calendar = new GregorianCalendar();

        calendar.add(Calendar.DATE, daysToAdd);

        return toXmlGregorianCalendar(calendar);
    }

    public static XMLGregorianCalendar toXmlGregorianCalendar(GregorianCalendar calendar) {

        XMLGregorianCalendar xmlGregorianCalendar = null;
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }

        xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);

        return xmlGregorianCalendar;
    }
}
