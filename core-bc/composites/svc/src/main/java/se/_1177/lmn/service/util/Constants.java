package se._1177.lmn.service.util;

/**
 * @author Patrik Björk
 */
public class Constants {

    // This suffix is appended to JSF action in order to make redirect after post request and keep potential query
    // string parameters.
    public static final String ACTION_SUFFIX = "?faces-redirect=true&amp;includeViewParams=true";

    // Messages
    public static final String CUSTOMER_SERVICE_INFO = "customer.service.info";
    public static final String CUSTOMER_SERVICE_PHONE = "customer.service.phone";

    public static final String PRODUCTS_FETCH_DEFAULT_ERROR = "products.fetch.default.error";
    public static final String PRODUCTS_FETCH_NONE_FOUND = "products.fetch.none.found";

    public static final String ORDER_CONFIRMATION = "order.confirmation";
}
