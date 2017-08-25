package se._1177.lmn.controller;

import org.junit.Test;
import riv.crm.selfservice.medicalsupply._1.DeliveryMethodEnum;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * @author Patrik Björk
 */
public class DeliveryControllerTest {

    @Test
    public void getDeliveryMethodBothAvailable() throws Exception {
        DeliveryController controller = new DeliveryController();

        controller.setPossibleDeliveryMethodsFittingAllItems(
                new HashSet<>(Arrays.asList(DeliveryMethodEnum.HEMLEVERANS, DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)));

        DeliveryMethodEnum deliveryMethod = controller.getDeliveryMethod();

        assertEquals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE, deliveryMethod);
    }

    @Test
    public void getDeliveryMethodUtlämningsställeAvailable() throws Exception {
        DeliveryController controller = new DeliveryController();

        controller.setPossibleDeliveryMethodsFittingAllItems(
                new HashSet<>(Arrays.asList(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE)));

        DeliveryMethodEnum deliveryMethod = controller.getDeliveryMethod();

        assertEquals(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE, deliveryMethod);
    }

    @Test
    public void getDeliveryMethodHemleveransAvailable() throws Exception {
        DeliveryController controller = new DeliveryController();

        controller.setPossibleDeliveryMethodsFittingAllItems(
                new HashSet<>(Arrays.asList(DeliveryMethodEnum.HEMLEVERANS)));

        DeliveryMethodEnum deliveryMethod = controller.getDeliveryMethod();

        assertEquals(DeliveryMethodEnum.HEMLEVERANS, deliveryMethod);
    }

    @Test
    public void getDeliveryMethodNoneAvailable() throws Exception {
        DeliveryController controller = new DeliveryController();

        controller.setPossibleDeliveryMethodsFittingAllItems(new HashSet<>());

        DeliveryMethodEnum deliveryMethod = controller.getDeliveryMethod();

        assertNull(deliveryMethod);
    }

}