package se.vgregion.mvk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;

import static se._1177.lmn.service.util.Constants.ACTION_SUFFIX;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DeliveryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeliveryController.class);

    private DeliveryMethodEnum deliveryMethod = DeliveryMethodEnum.HEMLEVERANS; // Default. Will this possibly change so a user can have a personal default?

    public String getDeliveryMethod() {
        return deliveryMethod.value();
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = DeliveryMethodEnum.fromValue(deliveryMethod);
    }

    public String getUtlamningsstalleValue() {
        return DeliveryMethodEnum.UTLÄMNINGSSTÄLLE.value();
    }

    public String getHemleveransValue() {
        return DeliveryMethodEnum.HEMLEVERANS.value();
    }

    public String toDeliveryMethod() {
        if (deliveryMethod.equals(DeliveryMethodEnum.HEMLEVERANS)) {
            return "homeDelivery" + ACTION_SUFFIX;
        } else if (deliveryMethod.equals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)) {
            return "collectDelivery" + ACTION_SUFFIX;
        } else {
            throw new RuntimeException("Unexpected " + DeliveryMethodEnum.class.getCanonicalName());
        }
    }
}
