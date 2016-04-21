package se.vgregion.mvk.controller;

import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.StatusEnum;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(UtilController.class);

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

        if (date == null) {
            LOGGER.error("Date should not be null.");
            return false;
        }

        return Util.isOlderThanAYear(date);
    }

    public static String getStatusText(PrescriptionItemType expiredItem) {

        if (expiredItem.getStatus().equals(StatusEnum.LEVERERAD)) {
            return "Inga kvarvarande uttag";
        }

        if (expiredItem.getNoOfRemainingOrders() <= 0) {
            return "Max antal uttag uppnått";
        }

        if (expiredItem.getStatus().equals(StatusEnum.MAKULERAD)) {
            return "Makulerad förskrivning";
        }

        if (isOlderThanAYear(expiredItem.getLastValidDate())) {
            return "För gammal förskrivning";
        }

        if (expiredItem.getStatus().equals(StatusEnum.UTGÅTT)) {
            return "Artikel har utgått";
        }

        return WordUtils.capitalizeFully(expiredItem.getStatus().toString());
    }
}
