package se._1177.lmn.controller.model;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._2.OrderRowType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Cart implements Serializable {

    private List<OrderRowType> orderRows = new ArrayList<>();

    public List<OrderRowType> getOrderRows() {
        return orderRows;
    }

    public void setOrderRows(List<OrderRowType> itemsInCart) {
        this.orderRows = itemsInCart;
    }

    public void emptyCart() {
        orderRows = new ArrayList<>();
    }

    public Integer getOrderCountForSubArticle(String articleNo) {
        for (OrderRowType orderRowType : orderRows) {

            if (orderRowType.getArticle() == null || orderRowType.getArticle().getArticleNo() == null) {
                throw new IllegalStateException("An order row should have an article set.");
            }

            if (orderRowType.getArticle().getArticleNo().equals(articleNo)) {
                return orderRowType.getNoOfPackages();
            }
        }

        return null;
    }

    /*@Override
    public void writeExternal(ObjectOutput out) throws IOException {
        RegisterMedicalSupplyOrderType registerMedicalSupplyOrderType = new riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._1.ObjectFactory().createRegisterMedicalSupplyOrderType();
        OrderType order = new OrderType();
        order.getOrderRow().addAll(orderRows);
        registerMedicalSupplyOrderType.setOrder(order);

        JAXBElement<RegisterMedicalSupplyOrderType> registerMedicalSupplyOrder = new riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._1.ObjectFactory().createRegisterMedicalSupplyOrder(registerMedicalSupplyOrderType);

       *//* registerMedicalSupplyOrder.

        new ObjectFactory().
        OrderType orderType = new JAXBElement<OrderType>()new ObjectFactory().createOrderType();
        orderType.getOrderRow().addAll(orderRows);*//*
        out.writeObject(objectToXML(registerMedicalSupplyOrder));
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        RegisterMedicalSupplyOrderType registerMedicalSupplyOrderType = readObject(in, RegisterMedicalSupplyOrderType.class);

        if (registerMedicalSupplyOrderType != null) {
            orderRows = registerMedicalSupplyOrderType.getOrder().getOrderRow();
        }
    }*/
}
