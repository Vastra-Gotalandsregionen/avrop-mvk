package se._1177.lmn.service.util;

import riv.crm.selfservice.medicalsupply._1.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._1.ObjectFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Patrik Björk
 */
public class Util {

    /**
     * Checks whether the date (not timestamp) is before a year ago.
     *
     * @param xmlGregorianCalendar
     * @return
     */
    public static boolean isOlderThanAYear(XMLGregorianCalendar xmlGregorianCalendar) {
        GregorianCalendar lastValidDateGregorian = xmlGregorianCalendar.toGregorianCalendar();

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

    public static boolean isBeforeToday(XMLGregorianCalendar date) {

        if (date == null) {
            return false;
        }

        Calendar firstMilliSecondToday = Calendar.getInstance();

        firstMilliSecondToday.set(Calendar.HOUR_OF_DAY, 0);
        firstMilliSecondToday.set(Calendar.MINUTE, 0);
        firstMilliSecondToday.set(Calendar.SECOND, 0);
        firstMilliSecondToday.set(Calendar.MILLISECOND, 0);

        Date toGregorian = new GregorianCalendar(date.getYear(), date.getMonth() - 1, date.getDay()).getTime();

        return toGregorian.before(firstMilliSecondToday.getTime());
    }

    public static boolean isBetween13And18(String subjectOfCareId) {

        String birthDate = subjectOfCareId.substring(0, 8);

        LocalDate birthLocalDate = stringToLocalDate(birthDate);

        LocalDate now = LocalDate.now(ZoneId.of("Europe/Stockholm"));

        return isBetween13And18YearsBetween(birthLocalDate, now);
    }

    static LocalDate stringToLocalDate(String birthDate) {

        if (birthDate.length() != 8) {
            throw new IllegalArgumentException("Birth date must be of form yyyyMMdd, i.e. eight characters long.");
        }

        return LocalDate.of(
                Integer.parseInt(birthDate.substring(0, 4)),
                Integer.parseInt(birthDate.substring(4, 6)),
                Integer.parseInt(birthDate.substring(6, 8))
                );
    }

    static boolean isBetween13And18YearsBetween(LocalDate birthLocalDate, LocalDate now) {
        int years = Period.between(birthLocalDate, now).getYears();

        return years >= 13 && years < 18;
    }

    public static boolean isValidEmailAddress(String email) {
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    public static JAXBElement<DeliveryNotificationMethodEnum> wrapInJAXBElement(
            DeliveryNotificationMethodEnum notificationMethod) {

        return new ObjectFactory().createDeliveryChoiceTypeDeliveryNotificationMethod(notificationMethod);
    }
}
