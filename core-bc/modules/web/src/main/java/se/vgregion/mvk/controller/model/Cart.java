package se.vgregion.mvk.controller.model;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Cart {

    private List<String> itemsInCart = new ArrayList<>();

    public List<String> getItemsInCart() {
        return itemsInCart;
    }

    public void setItemsInCart(List<String> itemsInCart) {
        this.itemsInCart = itemsInCart;
    }
}
