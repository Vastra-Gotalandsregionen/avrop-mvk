package se.vgregion.mvk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import static se._1177.lmn.service.util.Constants.ACTION_SUFFIX;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HomeDeliveryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeDeliveryController.class);

    private String doorCode;
    private boolean nextViewIsCollectDelivery;

    public String toVerifyDelivery() {
        if (nextViewIsCollectDelivery) {
            return "collectDelivery" + ACTION_SUFFIX;
        } else {
            return "verifyDelivery" + ACTION_SUFFIX;
        }
    }

    public void setDoorCode(String doorCode) {
        this.doorCode = doorCode;
    }

    public String getDoorCode() {
        return doorCode;
    }

    public void setNextViewIsCollectDelivery(boolean nextViewIsCollectDelivery) {
        this.nextViewIsCollectDelivery = nextViewIsCollectDelivery;
    }
}
