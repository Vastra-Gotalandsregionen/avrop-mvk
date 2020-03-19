package se._1177.lmn.controller.session;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._1.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;
import se._1177.lmn.controller.InvoiceAddressController;
import se._1177.lmn.controller.model.AddressModel;
import se._1177.lmn.controller.model.HomeDeliveryNotificationModel;
import se._1177.lmn.model.NotificationOrDoorDelivery;

import java.io.Serializable;
import java.util.List;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Data
public class InvoiceAddressControllerSession implements Serializable {

    private InvoiceAddressController.SameOrDifferent sameOrDifferent;

    private AddressModel addressModel;

}
