package se._1177.lmn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MessageController implements Serializable {

    @Autowired
    private LocaleController localeController;

    private ResourceBundle bundle;

    @PostConstruct
    public void init() {
        bundle = getBundle();
    }

    public String getMessage(String key) {
        return bundle.getString(key);
    }

    public String getMessage(String key, Object... params  ) {
        try {
            return MessageFormat.format(bundle.getString(key), params);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    private ResourceBundle getBundle() {
        return ResourceBundle.getBundle("application", localeController.getLocale());
    }

}
