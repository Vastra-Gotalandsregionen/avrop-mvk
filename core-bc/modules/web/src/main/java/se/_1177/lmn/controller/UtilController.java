package se._1177.lmn.controller;

import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply._0.StatusEnum;
import se._1177.lmn.service.util.Util;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Patrik Björk
 */
@Component
public class UtilController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UtilController.class);

    @Value("${backToOwnProfileLink}")
    private String backToOwnProfileLink;

    @Value("${logoutLink}")
    private String logoutLink;

    @Value("${settingsLink}")
    private String settingsLink;

    @Value("${mvkStartPageLink}")
    private String mvkStartPageLink;

    @Value("${mvkInboxLink}")
    private String mvkInboxLink;

    @Value("${mvkOtherServicesLink}")
    private String mvkOtherServicesLink;

    @Value("${customerServiceInfo}")
    private String customerServiceInfo;

    @Value("${customerServicePhoneNumber}")
    private String customerServicePhoneNumber;

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

    public static String toSubjectOfCareIdWithHyphen(String subjectOfCareId) {
        if (subjectOfCareId == null) {
            return null;
        }

        if (subjectOfCareId.length() == 10) {
            return subjectOfCareId.substring(0, 6) + "-" + subjectOfCareId.substring(6, 10);
        } else if (subjectOfCareId.length() == 12) {
            return subjectOfCareId.substring(0, 8) + "-" + subjectOfCareId.substring(8, 12);
        } else if (subjectOfCareId.contains("-")) {
            return subjectOfCareId;
        } else {
            LOGGER.warn("Could not read and insert hyphen in subjectOfCareId=" + subjectOfCareId);
            return subjectOfCareId;
        }
    }

    public String getCustomerServiceInfo() {
        return customerServiceInfo;
    }

    public String getBackToOwnProfileLink() {
        return backToOwnProfileLink;
    }

    public String getLogoutLink() {
        return logoutLink;
    }

    public String getSettingsLink() {
        return settingsLink;
    }

    public String getMvkStartPageLink() {
        return mvkStartPageLink;
    }

    public String getMvkInboxLink() {
        return mvkInboxLink;
    }

    public String getMvkOtherServicesLink() {
        return mvkOtherServicesLink;
    }

    public String getCustomerServicePhoneNumber() {
        return customerServicePhoneNumber;
    }

    public void addErrorMessageWithCustomerServiceInfo(String text) {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        if (facesContext != null) {

            Iterator<FacesMessage> messages = facesContext.getMessages();

            while (messages.hasNext()) {
                FacesMessage next = messages.next();

                if (!messages.hasNext()) {
                    // The last message
                    if (next.getSummary().equals(customerServiceInfo)) {
                        messages.remove();
                    }
                }
            }

            boolean add = true;
            // Don't add duplicate error messages
            List<FacesMessage> messageList = facesContext.getMessageList();
            for (FacesMessage facesMessage : messageList) {
                if (facesMessage.getSummary().equals(text)) {
                    add = false;
                }
            }

            if (add) {
                facesContext.addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, text, text));
            }

            facesContext.addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, customerServiceInfo,
                    customerServiceInfo));
        }
    }

}
