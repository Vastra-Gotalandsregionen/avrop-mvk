package se._1177.lmn.controller;

import org.junit.Before;
import org.junit.Test;
import riv.crm.selfservice.medicalsupply._2.ArticleType;
import riv.crm.selfservice.medicalsupply._2.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._2.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._2.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._2.PrescriptionItemType;
import se._1177.lmn.controller.model.Cart;
import se._1177.lmn.controller.model.PrescriptionItemInfo;
import se._1177.lmn.model.ServicePointProvider;
import se._1177.lmn.service.mock.MockServicePointProviderEnum;
import se._1177.lmn.service.mock.MockUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static riv.crm.selfservice.medicalsupply._2.DeliveryNotificationMethodEnum.*;
import static se._1177.lmn.service.util.CartUtil.createOrderRow;

/**
 * @author Patrik Björk
 */
public class CollectDeliveryController3Test {

    private CollectDeliveryController collectDeliveryController;
    private PrescriptionItemInfo prescriptionItemInfo;

    private PrescriptionItemType item1, item2, item3;

    @Before
    public void setup() throws Exception {

        collectDeliveryController = new CollectDeliveryController();
        prescriptionItemInfo = new PrescriptionItemInfo();

        Field preferredDeliveryNotificationMethod = collectDeliveryController.getClass()
                .getDeclaredField("preferredDeliveryNotificationMethod");
        preferredDeliveryNotificationMethod.setAccessible(true);
        preferredDeliveryNotificationMethod.set(collectDeliveryController, SMS);

        Field prescriptionItemInfoField = collectDeliveryController.getClass()
                .getDeclaredField("prescriptionItemInfo");
        prescriptionItemInfoField.setAccessible(true);
        prescriptionItemInfoField.set(collectDeliveryController, prescriptionItemInfo);

        Cart cart = new Cart();

        DeliveryAlternativeType alternative1 = new DeliveryAlternativeType();
//        DeliveryAlternativeType alternative2 = new DeliveryAlternativeType();
        DeliveryAlternativeType alternative3 = new DeliveryAlternativeType();
        DeliveryAlternativeType alternative4 = new DeliveryAlternativeType();
        DeliveryAlternativeType alternative5 = new DeliveryAlternativeType();
        DeliveryAlternativeType alternative6 = new DeliveryAlternativeType();

        alternative1.setServicePointProvider(MockUtil.toCvType(MockServicePointProviderEnum.SCHENKER));
//        alternative2.setServicePointProvider(ServicePointProviderEnum.SCHENKER);
        alternative3.setServicePointProvider(MockUtil.toCvType(MockServicePointProviderEnum.POSTNORD));
        alternative4.setServicePointProvider(MockUtil.toCvType(MockServicePointProviderEnum.POSTNORD));
        alternative5.setServicePointProvider(MockUtil.toCvType(MockServicePointProviderEnum.DHL));
        alternative6.setServicePointProvider(MockUtil.toCvType(MockServicePointProviderEnum.INGEN));

        alternative1.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
//        alternative2.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        alternative3.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        alternative4.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        alternative5.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        alternative6.setDeliveryMethod(DeliveryMethodEnum.HEMLEVERANS);

        alternative1.getDeliveryNotificationMethod().add(E_POST);
        alternative1.getDeliveryNotificationMethod().add(BREV);
        alternative1.getDeliveryNotificationMethod().add(SMS);

//        alternative2.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.BREV); // Only BREV is overlapping for SCHENKER, BUT for possible delivery alternatives

        alternative3.getDeliveryNotificationMethod().add(BREV);
        alternative3.getDeliveryNotificationMethod().add(SMS);

//        alternative4.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.BREV);
        alternative4.getDeliveryNotificationMethod().add(SMS); // Only SMS is overlapping for POSTNORD

        // These two are available for DHL
        alternative5.getDeliveryNotificationMethod().add(E_POST);
        alternative5.getDeliveryNotificationMethod().add(BREV);

        alternative1.setAllowChioceOfDeliveryPoints(true);
        alternative3.setAllowChioceOfDeliveryPoints(true);
        alternative4.setAllowChioceOfDeliveryPoints(true);
        alternative5.setAllowChioceOfDeliveryPoints(true);
        alternative6.setAllowChioceOfDeliveryPoints(true);

        item1 = new PrescriptionItemType();
        item2 = new PrescriptionItemType();
        item3 = new PrescriptionItemType();

        item1.setPrescriptionItemId("1");
        item2.setPrescriptionItemId("2");
        item3.setPrescriptionItemId("3");

        ArticleType article = new ArticleType();
        article.setArticleName("doesn't matter here");
        item1.setArticle(article);
        item2.setArticle(article);
        item3.setArticle(article);

        // Which delivery alternatives that are added to each item doesn't matter as long as all delivery alternatives
        // are added to any item.
        item1.getDeliveryAlternative().add(alternative1); // UTLÄMNINGSSTÄLLE, SCHENKER
        item1.getDeliveryAlternative().add(alternative6); // HEMLEVERANS

//        item2.getDeliveryAlternative().add(alternative1); // UTLÄMNINGSSTÄLLE, SCHENKER
//        item2.getDeliveryAlternative().add(alternative2); // UTLÄMNINGSSTÄLLE, SCHENKER
        item2.getDeliveryAlternative().add(alternative3); // UTLÄMNINGSSTÄLLE, POSTNORD
//        item2.getDeliveryAlternative().add(alternative4); // UTLÄMNINGSSTÄLLE, POSTNORD
//        item2.getDeliveryAlternative().add(alternative5); // UTLÄMNINGSSTÄLLE, DHL

        item3.getDeliveryAlternative().add(alternative6); // HEMLEVERANS

        // Now no delivery method is common to all items, and of those with UTLÄMNINGSSTÄLLE, SCHENKER is the common
        // denominator.

        cart.getOrderRows().add(createOrderRow(item1).get());
        cart.getOrderRows().add(createOrderRow(item2).get());
        cart.getOrderRows().add(createOrderRow(item3).get());

        DeliveryChoiceType deliveryChoice1 = new DeliveryChoiceType();
        DeliveryChoiceType deliveryChoice2 = new DeliveryChoiceType();
        deliveryChoice1.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        deliveryChoice2.setDeliveryMethod(DeliveryMethodEnum.HEMLEVERANS);

        cart.getOrderRows().get(0).setDeliveryChoice(deliveryChoice1);
        cart.getOrderRows().get(1).setDeliveryChoice(deliveryChoice1);
        cart.getOrderRows().get(2).setDeliveryChoice(deliveryChoice2);

        prescriptionItemInfo.getChosenPrescriptionItemInfo().put(item1.getPrescriptionItemId(), item1);
        prescriptionItemInfo.getChosenPrescriptionItemInfo().put(item2.getPrescriptionItemId(), item2);
        prescriptionItemInfo.getChosenPrescriptionItemInfo().put(item3.getPrescriptionItemId(), item3);

        Field cartField = collectDeliveryController.getClass().getDeclaredField("cart");

        cartField.setAccessible(true);
        cartField.set(collectDeliveryController, cart);

        Map<PrescriptionItemType, String> deliveryMethodForEachItem = new HashMap<>();
        deliveryMethodForEachItem.put(item1, "UTLÄMNINGSSTÄLLE");
        deliveryMethodForEachItem.put(item2, "UTLÄMNINGSSTÄLLE");
        deliveryMethodForEachItem.put(item3, "HEMLEVERANS");

        DeliveryController deliveryController = new DeliveryController();
        Field deliveryMethodForEachItemField = deliveryController.getClass()
                .getDeclaredField("deliveryMethodForEachItem");
        deliveryMethodForEachItemField.setAccessible(true);
        deliveryMethodForEachItemField.set(deliveryController, deliveryMethodForEachItem);

        Field cartFieldOnDeliveryController = deliveryController.getClass().getDeclaredField("cart");
        cartFieldOnDeliveryController.setAccessible(true);
        cartFieldOnDeliveryController.set(deliveryController, cart);

        Field prescriptionItemInfoFieldOnDeliveryController = deliveryController.getClass().getDeclaredField("prescriptionItemInfo");
        prescriptionItemInfoFieldOnDeliveryController.setAccessible(true);
        prescriptionItemInfoFieldOnDeliveryController.set(deliveryController, prescriptionItemInfo);

        OrderController orderController = new OrderController();

        Field deliveryControllerField = orderController.getClass().getDeclaredField("deliveryController");
        deliveryControllerField.setAccessible(true);
        deliveryControllerField.set(orderController, deliveryController);
//        deliveryControllerField.set(orderController, mock(DeliveryController.class));

        Field collectDeliveryControllerField = orderController.getClass().getDeclaredField("collectDeliveryController");
        collectDeliveryControllerField.setAccessible(true);
        collectDeliveryControllerField.set(orderController, collectDeliveryController);

        Field deliveryController2 = collectDeliveryController.getClass().getDeclaredField("deliveryController");
        deliveryController2.setAccessible(true);
        deliveryController2.set(collectDeliveryController, deliveryController);

        // This is an important preparatory step.
        orderController.prepareDeliveryOptions(prescriptionItemInfo.getPrescriptionItems(cart.getOrderRows()));
    }

    @Test
    public void getDeliveryNotificationMethodsPerProvider() throws Exception {

        Map<ServicePointProvider, List<String>> deliveryNotificationMethodsPerProvider = collectDeliveryController
                .getDeliveryNotificationMethodsPerProvider();

        List<String> schenker = deliveryNotificationMethodsPerProvider.get(ServicePointProvider.from("SCHENKER"));
        List<String> postnord = deliveryNotificationMethodsPerProvider.get(ServicePointProvider.from("POSTNORD"));
        List<String> dhl = deliveryNotificationMethodsPerProvider.get(ServicePointProvider.from("DHL"));

        // Only POSTNORD is available for all items so only POSTNORD will have any notification methods.
        assertEquals(Arrays.asList("E_POST", "BREV", "SMS"), schenker);
        assertEquals(Arrays.asList("BREV", "SMS"), postnord);
        assertEquals(null, dhl); // No one has DHL
    }

    @Test
    public void initChosenDeliveryNotificationMethod() {

        collectDeliveryController.initChosenDeliveryNotificationMethod();

        Map<ServicePointProvider, String> chosenDeliveryNotificationMethod = collectDeliveryController
                .getChosenDeliveryNotificationMethod();

        String postnord = chosenDeliveryNotificationMethod.get(ServicePointProvider.from("POSTNORD"));
        String schenker = chosenDeliveryNotificationMethod.get(ServicePointProvider.from("SCHENKER"));
        String dhl = chosenDeliveryNotificationMethod.get(ServicePointProvider.from("DHL"));

        // Only POSTNORD is available for all items and SMS is the preferred method according to setup(). // TODO: 2016-05-17 not true anymore
        assertEquals("SMS", postnord);
        assertEquals("SMS", schenker);
        assertEquals(null, dhl);
    }

    @Test
    public void getRelevantServicePointProviders() {

        List<ServicePointProvider> relevantServicePointProviders = new ArrayList<>(collectDeliveryController
                .getServicePointProvidersForDeliveryPointChoice().keySet());

        assertEquals(Arrays.asList(ServicePointProvider.from("POSTNORD"), ServicePointProvider.from("SCHENKER")),
                relevantServicePointProviders); // No single provider is common to all.
    }

    @Test
    public void getServicePointProviderForItem() {
        ServicePointProvider providerItem1 = collectDeliveryController.getServicePointProviderForItem(item1);

        assertEquals(ServicePointProvider.from("SCHENKER"), providerItem1);
    }

}
