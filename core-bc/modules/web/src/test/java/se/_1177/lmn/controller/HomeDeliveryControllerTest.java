package se._1177.lmn.controller;

import org.junit.Test;
import riv.crm.selfservice.medicalsupply._2.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._2.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._2.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._2.PrescriptionItemType;
import se._1177.lmn.model.NotificationVariant;

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

        assertEquals(NotificationVariant.BOTH_WITH_AND_WITHOUT_NOTIFICATION,
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

        assertEquals(NotificationVariant.WITHOUT_NOTIFICATION,
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

        assertEquals(NotificationVariant.WITH_NOTIFICATION,
                controller.hasWithAndWithoutNotificationForHomeDelivery(item));
    }

}
