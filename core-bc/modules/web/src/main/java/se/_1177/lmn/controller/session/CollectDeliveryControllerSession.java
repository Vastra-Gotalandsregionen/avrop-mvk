package se._1177.lmn.controller.session;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._1.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._1.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._1.ServicePointProviderEnum;
import se._1177.lmn.controller.InvoiceAddressController;
import se._1177.lmn.controller.model.AddressModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Data
public class CollectDeliveryControllerSession extends AbstractSessionData implements Serializable {

    private AddressModel addressModel;
    private String zip;
    private Map<ServicePointProviderEnum, String> deliveryPointIdsMap = new HashMap<>();
    private DeliveryNotificationMethodEnum preferredDeliveryNotificationMethod; // These gets stored in session memory
    private String email;
    private String smsNumber;
    private Map<ServicePointProviderEnum, List<DeliveryPointType>> deliveryPointsPerProvider = new HashMap<>();
    private Map<ServicePointProviderEnum, Set<DeliveryNotificationMethodEnum>>
            possibleCollectCombinationsFittingAllWithNotificationMethods;
    private Map<ServicePointProviderEnum, String> chosenDeliveryNotificationMethod;
    private String phoneNumber;
    private String contactPerson;

}
