package se._1177.lmn.service.util;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Patrik Björk
 */
public class Util {

    public static boolean isOlderThanAYear(XMLGregorianCalendar lastValidDate1) {
        GregorianCalendar lastValidDateGregorian = lastValidDate1.toGregorianCalendar();

        ZonedDateTime lastValidDateTime = lastValidDateGregorian.toZonedDateTime();

        LocalDate lastValidDate = lastValidDateTime.toLocalDate();

        LocalDate aYearAgo = LocalDate.now().minusYears(1);

        return lastValidDate.isBefore(aYearAgo);
    }

    public static XMLGregorianCalendar toXmlGregorianCalendar(GregorianCalendar calendar) {
        XMLGregorianCalendar xmlGregorianCalendar;
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }

        xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);

        return xmlGregorianCalendar;
    }

    public static boolean isAfterToday(XMLGregorianCalendar date) {

        if (date == null) {
            return false;
        }

        Calendar lastMilliSecondToday = Calendar.getInstance();

        lastMilliSecondToday.set(Calendar.HOUR_OF_DAY, 23);
        lastMilliSecondToday.set(Calendar.MINUTE, 59);
        lastMilliSecondToday.set(Calendar.SECOND, 59);
        lastMilliSecondToday.set(Calendar.MILLISECOND, 999);

        Date toGregorian = new GregorianCalendar(date.getYear(), date.getMonth() - 1, date.getDay()).getTime();

        return toGregorian.after(lastMilliSecondToday.getTime());
    }
}
