package se.vgregion.mvk.controller;

import org.junit.Before;
import org.junit.Test;
import riv.crm.selfservice.medicalsupply._0.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.ServicePointProviderEnum;
import se.vgregion.mvk.controller.model.Cart;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Patrik Björk
 */
public class CollectDeliveryController2Test {

    private CollectDeliveryController collectDeliveryController;

    private PrescriptionItemType item1, item2, item3;

    @Before
    public void setup() throws Exception {

        collectDeliveryController = new CollectDeliveryController();

        Field preferredDeliveryNotificationMethod = collectDeliveryController.getClass()
                .getDeclaredField("preferredDeliveryNotificationMethod");
        preferredDeliveryNotificationMethod.setAccessible(true);
        preferredDeliveryNotificationMethod.set(collectDeliveryController, DeliveryNotificationMethodEnum.SMS);

        Cart cart = new Cart();

        DeliveryAlternativeType alternative1 = new DeliveryAlternativeType();
        DeliveryAlternativeType alternative2 = new DeliveryAlternativeType();
        DeliveryAlternativeType alternative3 = new DeliveryAlternativeType();
        DeliveryAlternativeType alternative4 = new DeliveryAlternativeType();
        DeliveryAlternativeType alternative5 = new DeliveryAlternativeType();
        DeliveryAlternativeType alternative6 = new DeliveryAlternativeType();

        alternative1.setServicePointProvider(ServicePointProviderEnum.SCHENKER);
        alternative2.setServicePointProvider(ServicePointProviderEnum.SCHENKER);
        alternative3.setServicePointProvider(ServicePointProviderEnum.POSTNORD);
        alternative4.setServicePointProvider(ServicePointProviderEnum.POSTNORD);
        alternative5.setServicePointProvider(ServicePointProviderEnum.DHL);
        alternative6.setServicePointProvider(ServicePointProviderEnum.INGEN);

        alternative1.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        alternative2.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        alternative3.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        alternative4.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        alternative5.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        alternative6.setDeliveryMethod(DeliveryMethodEnum.HEMLEVERANS);

        alternative1.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.E_POST);
        alternative1.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.BREV);
        alternative1.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.SMS);

        alternative2.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.BREV); // Only BREV is overlapping for SCHENKER

        alternative3.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.BREV);
        alternative3.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.SMS);

        // These two are available for DHL
        alternative4.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.BREV);
        alternative4.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.SMS);

        // These two are available for DHL
        alternative5.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.E_POST);
        alternative5.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.BREV);

        item1 = new PrescriptionItemType();
        item2 = new PrescriptionItemType();
        item3 = new PrescriptionItemType();

        // Which delivery alternatives that are added to each item doesn't matter as long as all delivery alternatives
        // are added to any item.
        item1.getDeliveryAlternative().add(alternative1); // UTLÄMNINGSSTÄLLE, SCHENKER
        item1.getDeliveryAlternative().add(alternative6); // HEMLEVERANS

        item2.getDeliveryAlternative().add(alternative1); // UTLÄMNINGSSTÄLLE, SCHENKER
        item2.getDeliveryAlternative().add(alternative2); // UTLÄMNINGSSTÄLLE, SCHENKER
        item2.getDeliveryAlternative().add(alternative3); // UTLÄMNINGSSTÄLLE, POSTNORD
        item2.getDeliveryAlternative().add(alternative4); // UTLÄMNINGSSTÄLLE, POSTNORD
        item2.getDeliveryAlternative().add(alternative5); // UTLÄMNINGSSTÄLLE, DHL

        item3.getDeliveryAlternative().add(alternative6); // HEMLEVERANS

        // Now no delivery method is common to all items, and of those with UTLÄMNINGSSTÄLLE, SCHENKER is the common
        // denominator.

        cart.getItemsInCart().add(item1);
        cart.getItemsInCart().add(item2);
        cart.getItemsInCart().add(item3);

        Field cartField = collectDeliveryController.getClass().getDeclaredField("cart");

        cartField.setAccessible(true);
        cartField.set(collectDeliveryController, cart);

        OrderController orderController = new OrderController();

        Field deliveryControllerField = orderController.getClass().getDeclaredField("deliveryController");
        deliveryControllerField.setAccessible(true);
        deliveryControllerField.set(orderController, mock(DeliveryController.class));

        Field collectDeliveryControllerField = orderController.getClass().getDeclaredField("collectDeliveryController");
        collectDeliveryControllerField.setAccessible(true);
        collectDeliveryControllerField.set(orderController, collectDeliveryController);

        // This is an important preparatory step.
        orderController.prepareDeliveryOptions(cart.getItemsInCart());
    }

    @Test
    public void getDeliveryNotificationMethodsPerProvider() throws Exception {

        Map<ServicePointProviderEnum, List<String>> deliveryNotificationMethodsPerProvider = collectDeliveryController
                .getDeliveryNotificationMethodsPerProvider();

        List<String> schenker = deliveryNotificationMethodsPerProvider.get(ServicePointProviderEnum.SCHENKER);
        List<String> postnord = deliveryNotificationMethodsPerProvider.get(ServicePointProviderEnum.POSTNORD);
        List<String> dhl = deliveryNotificationMethodsPerProvider.get(ServicePointProviderEnum.DHL);

        // Only POSTNORD is available for all items so only POSTNORD will have any notification methods.
        assertEquals(Arrays.asList("BREV"), schenker);
        assertEquals(null, postnord);
        assertEquals(null, dhl);
    }

    @Test
    public void initChosenDeliveryNotificationMethod() {

        collectDeliveryController.initChosenDeliveryNotificationMethod();

        Map<ServicePointProviderEnum, String> chosenDeliveryNotificationMethod = collectDeliveryController
                .getChosenDeliveryNotificationMethod();

        String postnord = chosenDeliveryNotificationMethod.get(ServicePointProviderEnum.POSTNORD);
        String schenker = chosenDeliveryNotificationMethod.get(ServicePointProviderEnum.SCHENKER);
        String dhl = chosenDeliveryNotificationMethod.get(ServicePointProviderEnum.DHL);

        // Only POSTNORD is available for all items and SMS is the preferred method according to setup().
        assertEquals(null, postnord);
        assertEquals("BREV", schenker);
        assertEquals(null, dhl);
    }

    @Test
    public void getRelevantServicePointProviders() {

        List<ServicePointProviderEnum> relevantServicePointProviders = collectDeliveryController
                .getRelevantServicePointProviders();

        assertEquals(Arrays.asList(ServicePointProviderEnum.SCHENKER), relevantServicePointProviders);
    }

    @Test
    public void getServicePointProviderForItem() {
        ServicePointProviderEnum providerItem1 = collectDeliveryController.getServicePointProviderForItem(item1);


    }

}