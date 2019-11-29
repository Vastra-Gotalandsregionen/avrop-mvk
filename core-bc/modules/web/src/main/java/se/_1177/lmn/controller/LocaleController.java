package se._1177.lmn.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Locale;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LocaleController implements Serializable {

    private static final String LANGUAGE = "sv";
    private static final String COUNTRY = "SE";

    private Locale locale;

    @PostConstruct
    public void init() {
        locale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLanguageVariant(String variant) {
        locale = new Locale(LANGUAGE, COUNTRY, variant);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    }

}
