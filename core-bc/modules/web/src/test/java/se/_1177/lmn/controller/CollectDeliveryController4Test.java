package se._1177.lmn.controller;

import org.junit.Before;
import org.junit.Test;
import riv.crm.selfservice.medicalsupply._2.ArticleType;
import riv.crm.selfservice.medicalsupply._2.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._2.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._2.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._2.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._2.OrderRowType;
import riv.crm.selfservice.medicalsupply._2.PrescriptionItemType;
import se._1177.lmn.controller.model.Cart;
import se._1177.lmn.controller.model.PrescriptionItemInfo;
import se._1177.lmn.model.ServicePointProvider;
import se._1177.lmn.service.mock.MockServicePointProviderEnum;
import se._1177.lmn.service.mock.MockUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static se._1177.lmn.service.util.CartUtil.createOrderRow;

/**
 * @author Patrik Björk
 */
public class CollectDeliveryController4Test {

    private CollectDeliveryController collectDeliveryController;
    private PrescriptionItemInfo prescriptionItemInfo;
    private Cart cart;
    private DeliveryAlternativeType alternative1;

    @Before
    public void setup() throws Exception {

        collectDeliveryController = new CollectDeliveryController();
        prescriptionItemInfo = new PrescriptionItemInfo();

        Field preferredDeliveryNotificationMethod = collectDeliveryController.sessionData.getClass()
                .getDeclaredField("preferredDeliveryNotificationMethod");
        preferredDeliveryNotificationMethod.setAccessible(true);
        preferredDeliveryNotificationMethod.set(collectDeliveryController.sessionData, DeliveryNotificationMethodEnum.SMS);

        Field prescriptionItemInfoField = collectDeliveryController.getClass()
                .getDeclaredField("prescriptionItemInfo");
        prescriptionItemInfoField.setAccessible(true);
        prescriptionItemInfoField.set(collectDeliveryController, prescriptionItemInfo);

        cart = new Cart();

        // We want to make one item with two delivery alternatives; one home delivery with notifications, and one
        // collect delivery without notifications; and one item with only collect delivery without notifications. The
        // bug that should be corrected lead to the notifications from the home delivery alternative "slipped through"
        // to the collect delivery choices if they have the same provider. Also given that both prescriptions were
        // chosen collect delivery.
        alternative1 = new DeliveryAlternativeType();

        DeliveryAlternativeType alternative2 = new DeliveryAlternativeType();

        alternative1.setServicePointProvider(MockUtil.toCvType(MockServicePointProviderEnum.INGEN));
        alternative2.setServicePointProvider(MockUtil.toCvType(MockServicePointProviderEnum.INGEN));

        alternative1.setDeliveryMethod(DeliveryMethodEnum.HEMLEVERANS);
        alternative2.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);

        alternative1.setAllowChioceOfDeliveryPoints(false);
        alternative2.setAllowChioceOfDeliveryPoints(false);

        alternative1.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.E_POST);
        alternative1.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.BREV);
        alternative1.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.SMS);

        PrescriptionItemType item1 = new PrescriptionItemType();
        PrescriptionItemType item2 = new PrescriptionItemType();

        item1.setPrescriptionItemId("1");
        item2.setPrescriptionItemId("2");

        ArticleType article = new ArticleType();
        article.setArticleName("doesn't matter here");
        item1.setArticle(article);
        item2.setArticle(article);

        item1.getDeliveryAlternative().add(alternative1);
        item1.getDeliveryAlternative().add(alternative2);
        item2.getDeliveryAlternative().add(alternative2);

        cart.getOrderRows().add(createOrderRow(item1).get());
        cart.getOrderRows().add(createOrderRow(item2).get());

        for (OrderRowType orderRowType : cart.getOrderRows()) {
            DeliveryChoiceType deliveryChoice = new DeliveryChoiceType();
            deliveryChoice.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
            orderRowType.setDeliveryChoice(deliveryChoice);
        }

        prescriptionItemInfo.getChosenPrescriptionItemInfo().put(item1.getPrescriptionItemId(), item1);
        prescriptionItemInfo.getChosenPrescriptionItemInfo().put(item2.getPrescriptionItemId(), item2);

        Field cartField = collectDeliveryController.getClass().getDeclaredField("cart");

        cartField.setAccessible(true);
        cartField.set(collectDeliveryController, cart);

        Map<PrescriptionItemType, String> deliveryMethodForEachItem = new HashMap<>();
        deliveryMethodForEachItem.put(item1, "UTLÄMNINGSSTÄLLE");
        deliveryMethodForEachItem.put(item2, "UTLÄMNINGSSTÄLLE");

        DeliveryController deliveryController = new DeliveryController();
        Field deliveryMethodForEachItemField = deliveryController.sessionData.getClass()
                .getDeclaredField("deliveryMethodForEachItem");
        deliveryMethodForEachItemField.setAccessible(true);
        deliveryMethodForEachItemField.set(deliveryController.sessionData, deliveryMethodForEachItem);

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

        List<String> ingen = deliveryNotificationMethodsPerProvider.get(ServicePointProvider.INGEN);

        // We want to assert that no delivery notification methods from the HEMLEVERANS alternative pass through.
        assertEquals(null, ingen);
    }


}
