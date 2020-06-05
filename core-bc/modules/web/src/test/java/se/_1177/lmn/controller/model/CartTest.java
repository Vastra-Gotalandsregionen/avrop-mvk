package se._1177.lmn.controller.model;

import org.junit.Test;
import riv.crm.selfservice.medicalsupply._1.CountryCodeEnum;
import riv.crm.selfservice.medicalsupply._1.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._1.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._1.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._1.OrderRowType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class CartTest {

    @Test
    public void testSerializeDeserialize() throws IOException, ClassNotFoundException {
        String comment = "comment...";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        Cart cart = new Cart();

        DeliveryPointType deliveryPoint = new DeliveryPointType();
        deliveryPoint.setDeliveryPointId("1");
        deliveryPoint.setCountryCode(CountryCodeEnum.SE);

        DeliveryChoiceType deliveryChoice = new DeliveryChoiceType();
        deliveryChoice.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        deliveryChoice.setDeliveryPoint(deliveryPoint);
        deliveryChoice.setDeliveryComment(comment);

        OrderRowType orderRowType = new OrderRowType();
        orderRowType.setDeliveryChoice(deliveryChoice);
        orderRowType.setNoOfPcs(5);

        cart.setOrderRows(Collections.singletonList(orderRowType));

        // Serialize
        oos.writeObject(cart);

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));

        Cart deserialized = (Cart) ois.readObject();

        assertEquals(comment, deserialized.getOrderRows().get(0).getDeliveryChoice().getDeliveryComment());
    }

}
