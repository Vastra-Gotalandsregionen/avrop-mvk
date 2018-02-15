package se._1177.lmn.controller;

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
public class OrderConfirmationController {

    public static final String VIEW_NAME = "Bekräftelse";

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConfirmationController.class);

}
