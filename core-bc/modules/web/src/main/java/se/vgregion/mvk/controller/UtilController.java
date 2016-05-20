package se.vgregion.mvk.controller;

import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply._0.StatusEnum;
import se._1177.lmn.service.util.Util;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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

    public static boolean isAfterToday(XMLGregorianCalendar date) {
        return Util.isAfterToday(date);
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
            return "Max antal uttag uppnått";
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

    public static String capitalizeFully(String string) {
        return WordUtils.capitalizeFully(string);
    }

    public static <T> List<T> toList(Collection<T> collection) {
        return new ArrayList<>(collection);
    }

    public static String toProviderName(ServicePointProviderEnum provider) {
        if (provider.equals(ServicePointProviderEnum.DHL)) {
            return ServicePointProviderEnum.DHL.name();
        } else {
            return capitalizeFully(provider.name());
        }
    }

    public static String toNotificationMethodName(DeliveryNotificationMethodEnum notificationMethod) {
        switch (notificationMethod) {
            case BREV:
                return "Brev";
            case E_POST:
                return "Epost";
            case SMS:
                return "SMS";
            case TELEFON:
                return "Telefon";
            default:
                LOGGER.error("Unknown DeliveryNotificationMethodEnum: [" + notificationMethod + "]");
                return "Okänt";
        }
    }

    public static String toNotificationMethodName(String notificationMethod) {
        switch (notificationMethod) {
            case "BREV":
                return "Brev";
            case "E_POST":
                return "Epost";
            case "SMS":
                return "SMS";
            case "TELEFON":
                return "Telefon";
            default:
                LOGGER.error("Unknown DeliveryNotificationMethodEnum: [" + notificationMethod + "]");
                return "Okänt";
        }
    }
}
