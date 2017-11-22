package se._1177.lmn.controller;

import org.junit.Test;
import riv.crm.selfservice.medicalsupply._1.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._1.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._1.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;

import static org.junit.Assert.*;

public class HomeDeliveryControllerTest {

    @Test
    public void hasWithAndWithoutNotificationForHomeDeliveryBoth() throws Exception {
        HomeDeliveryController controller = new HomeDeliveryController();

        // With notification method
        DeliveryAlternativeType da1 = new DeliveryAlternativeType();
        da1.setDeliveryMethod(DeliveryMethodEnum.HEMLEVERANS);
        da1.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.E_POST);

        // Without notification
        DeliveryAlternativeType da2 = new DeliveryAlternativeType();
        da2.setDeliveryMethod(DeliveryMethodEnum.HEMLEVERANS);

        PrescriptionItemType item = new PrescriptionItemType();
        item.getDeliveryAlternative().add(da1);
        item.getDeliveryAlternative().add(da2);

        assertEquals(HomeDeliveryController.NotificationVariant.BOTH_WITH_AND_WITHOUT_NOTIFICATION,
                controller.hasWithAndWithoutNotificationForHomeDelivery(item));
    }

    @Test
    public void hasWithAndWithoutNotificationForHomeDeliveryWithout() throws Exception {
        HomeDeliveryController controller = new HomeDeliveryController();

        // Without notification
        DeliveryAlternativeType da2 = new DeliveryAlternativeType();
        da2.setDeliveryMethod(DeliveryMethodEnum.HEMLEVERANS);

        PrescriptionItemType item = new PrescriptionItemType();
        item.getDeliveryAlternative().add(da2);

        assertEquals(HomeDeliveryController.NotificationVariant.WITHOUT_NOTIFICATION,
                controller.hasWithAndWithoutNotificationForHomeDelivery(item));
    }

    @Test
    public void hasWithAndWithoutNotificationForHomeDeliveryWith() throws Exception {
        HomeDeliveryController controller = new HomeDeliveryController();

        // With notification method
        DeliveryAlternativeType da1 = new DeliveryAlternativeType();
        da1.setDeliveryMethod(DeliveryMethodEnum.HEMLEVERANS);
        da1.getDeliveryNotificationMethod().add(DeliveryNotificationMethodEnum.E_POST);

        PrescriptionItemType item = new PrescriptionItemType();
        item.getDeliveryAlternative().add(da1);

        assertEquals(HomeDeliveryController.NotificationVariant.WITH_NOTIFICATION,
                controller.hasWithAndWithoutNotificationForHomeDelivery(item));
    }

}