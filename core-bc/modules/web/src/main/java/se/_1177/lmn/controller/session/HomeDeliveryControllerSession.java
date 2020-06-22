package se._1177.lmn.controller.session;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._2.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._2.PrescriptionItemType;
import se._1177.lmn.controller.model.AddressModel;
import se._1177.lmn.controller.model.HomeDeliveryNotificationModel;
import se._1177.lmn.model.NotificationOrDoorDelivery;

import java.io.Serializable;
import java.util.List;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Data
public class HomeDeliveryControllerSession extends AbstractSessionData implements Serializable {

    private AddressModel addressModel;

    private boolean nextViewIsCollectDelivery;

    private DeliveryNotificationMethodEnum preferredDeliveryNotificationMethod;

    private String smsNumber;
    private String email;

    private String deliveryComment;

    private NotificationOrDoorDelivery notificationOrDoorDelivery;

    private List<PrescriptionItemType> notificationOptional;
    private List<PrescriptionItemType> notificationMandatory;
    private List<PrescriptionItemType> notificationUnavailable;

    private HomeDeliveryNotificationModel notificationOptionalModel;
    private HomeDeliveryNotificationModel notificationMandatoryModel;


}
