package se.vgregion.mvk.controller;

import org.springframework.stereotype.Component;
import se._1177.lmn.service.util.Util;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Patrik Björk
 */
@Component
public class UtilController {

    public Date toDate(XMLGregorianCalendar calendar) {
        if (calendar == null) {
            return null;
        }

        return calendar.toGregorianCalendar().getTime();
    }

    public boolean isAfterToday(XMLGregorianCalendar date) {

        if (date == null) {
            return false;
        }

        Calendar lastMilliSecondToday = Calendar.getInstance();

        lastMilliSecondToday.set(Calendar.HOUR_OF_DAY, 23);
        lastMilliSecondToday.set(Calendar.MINUTE, 59);
        lastMilliSecondToday.set(Calendar.SECOND, 59);
        lastMilliSecondToday.set(Calendar.MILLISECOND, 999);

        Date toGregorian = new GregorianCalendar(date.getYear(), date.getMonth() -1, date.getDay()).getTime();

        return toGregorian.after(lastMilliSecondToday.getTime());
    }

    public static boolean isOlderThanAYear(XMLGregorianCalendar date) {
        return Util.isOlderThanAYear(date);
    }

}
