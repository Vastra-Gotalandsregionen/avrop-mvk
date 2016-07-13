package se._1177.lmn.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class OrderConfirmationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConfirmationController.class);

    @Autowired
    private UserProfileController userProfileController;

    public String toOrder() {
        String delegateUrlParameters = userProfileController.getDelegateUrlParameters();

        String ampOrQuestionMark = delegateUrlParameters != null && delegateUrlParameters.length() > 0 ? "&amp;" : "?";

        String result = "order" + delegateUrlParameters
                + ampOrQuestionMark
                + "faces-redirect=true&amp;includeViewParams=true";

        return result;
    }
}
