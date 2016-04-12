package se._1177.lmn.service.util;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.ZonedDateTime;
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

}
