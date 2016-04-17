package se.vgregion.mvk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HomeDeliveryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeDeliveryController.class);

    private String doorCode;

    public String toVerifyDelivery() {
        return null;
    }

    public void setDoorCode(String doorCode) {
        this.doorCode = doorCode;
    }

    public String getDoorCode() {
        return doorCode;
    }
}
