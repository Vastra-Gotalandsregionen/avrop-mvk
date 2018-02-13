package se._1177.lmn.controller;

import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._1.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._1.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._1.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply._1.StatusEnum;
import se._1177.lmn.service.util.Util;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static se._1177.lmn.service.util.Constants.CUSTOMER_SERVICE_INFO;

/**
 * Utility controller mainly called from the views.
 *
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

    @Autowired
    private MessageController messageController;

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

        if (Util.isBeforeToday(expiredItem.getLastValidDate())) {
            return "Utgången förskrivning";
        }

        if (expiredItem.getStatus().equals(StatusEnum.UTGÅTT)) {
            return "Artikel har utgått";
        }

        return WordUtils.capitalizeFully(expiredItem.getStatus().toString());
    }

    public static String capitalizeFully(String string) {
        return WordUtils.capitalizeFully(string);
    }

    public static String mapToText(String deliveryMethod) {
        DeliveryMethodEnum methodEnum = DeliveryMethodEnum.fromValue(deliveryMethod);

        switch (methodEnum) {
            case UTLÄMNINGSSTÄLLE:
                return "Utlämningsställe";
            case HEMLEVERANS:
                return "Leveransadress";
            default:
                throw new IllegalArgumentException("Unknown delivery method: " + deliveryMethod);
        }
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
        return messageController.getMessage(CUSTOMER_SERVICE_INFO);
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

    public void addErrorMessageWithCustomerServiceInfo(String text) {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        if (facesContext != null) {

            Iterator<FacesMessage> messages = facesContext.getMessages();

            String customerServiceInfo = getCustomerServiceInfo();

            // Remove the last message if it is equal to the customerServiceInfo message. It will be added last in this
            // method.
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
