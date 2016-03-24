package se.vgregion.mvk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Patrik Björk
 */
@Component
@Scope("session")
public class FirstController {

    private final String POST_REDIRECT_SUFFIX = "?faces-redirect=true&amp;includeViewParams=true";

    @Autowired
    private CounterController counterController;

    private int count;

    public String getText(String name) {
        return "Hello " + name + "! " + ++count + " " + counterController.getCount();
    }

    public String action() {
        return "step" + POST_REDIRECT_SUFFIX;
    }

    public String home() {
        return "index" + POST_REDIRECT_SUFFIX;
    }
}
