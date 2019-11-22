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
@Scope(value = "session")
//@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TestController implements Serializable {
    private Integer number = 0;

    public Integer getNumber() {
        return number++;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}
