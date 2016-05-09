package se.vgregion.mvk.controller;

import mvk.itintegration.userprofile._2.UserProfileType;
import mvk.itintegration.userprofile.getuserprofileresponder._2.GetUserProfileResponseType;
import org.junit.Before;
import org.junit.Test;
import riv.crm.selfservice.medicalsupply._0.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.ServicePointProviderEnum;
import se.vgregion.mvk.controller.model.Cart;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Patrik Björk
 */
public class VerifyDeliveryControllerTest {

    private VerifyDeliveryController verifyDeliveryController;

    @Before
    public void init() throws Exception {

        DeliveryAlternativeType deliveryAlternative1 = new DeliveryAlternativeType();
        DeliveryAlternativeType deliveryAlternative2 = new DeliveryAlternativeType();
        DeliveryAlternativeType deliveryAlternative3 = new DeliveryAlternativeType();

        deliveryAlternative1.setDeliveryMethod(DeliveryMethodEnum.HEMLEVERANS);
        deliveryAlternative1.setServicePointProvider(ServicePointProviderEnum.INGEN);
        deliveryAlternative1.setDeliveryMethodId("da1");

        deliveryAlternative2.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        deliveryAlternative2.setServicePointProvider(ServicePointProviderEnum.DHL);
        deliveryAlternative2.setDeliveryMethodId("da2");

        deliveryAlternative3.setDeliveryMethod(DeliveryMethodEnum.UTLÄMNINGSSTÄLLE);
        deliveryAlternative3.setServicePointProvider(ServicePointProviderEnum.POSTNORD);
        deliveryAlternative3.setDeliveryMethodId("da3");

        PrescriptionItemType item1 = new PrescriptionItemType();
        PrescriptionItemType item2 = new PrescriptionItemType();
        PrescriptionItemType item3 = new PrescriptionItemType();

        item1.getDeliveryAlternative().add(deliveryAlternative1); // HEMLEVERANS
        item1.getDeliveryAlternative().add(deliveryAlternative2); // UTLÄMNINGSSTÄLLE, DHL

        item2.getDeliveryAlternative().add(deliveryAlternative2); // UTLÄMNINGSSTÄLLE, DHL
        item2.getDeliveryAlternative().add(deliveryAlternative3); // UTLÄMNINGSSTÄLLE, POSTNORD

        item3.getDeliveryAlternative().add(deliveryAlternative1); // HEMLEVERANS

        // UserProfileController
        UserProfileController userProfileController = mock(UserProfileController.class);

        UserProfileType userProfileType = new UserProfileType();
        userProfileType.setSubjectOfCareId("190808080808");
        userProfileType.setCity("Staden");
        userProfileType.setPhoneNumber("012345678");
        userProfileType.setZip("12345");
        userProfileType.setFirstName("Åttan");
        userProfileType.setLastName("Åttansson");
        userProfileType.setStreetAddress("Gatan 1");

        GetUserProfileResponseType profileResponseType = mock(GetUserProfileResponseType.class);
        when(profileResponseType.getUserProfile()).thenReturn(userProfileType);

        when(userProfileController.getUserProfile()).thenReturn(profileResponseType);

        // DeliveryController
        DeliveryController deliveryController = mock(DeliveryController.class);

        HashMap<PrescriptionItemType, String> deliveryMethodForEachItem = new HashMap();
        deliveryMethodForEachItem.put(item1, DeliveryMethodEnum.UTLÄMNINGSSTÄLLE.name());
        deliveryMethodForEachItem.put(item2, DeliveryMethodEnum.UTLÄMNINGSSTÄLLE.name());
        deliveryMethodForEachItem.put(item3, DeliveryMethodEnum.HEMLEVERANS.name());

        // The items, with their chosen deliveryAlternative, have no common delivery method and of those with
        // UTLÄMNINGSSTÄLLE, DHL is the common denominator.

        when(deliveryController.getDeliveryMethodForEachItem()).thenReturn(deliveryMethodForEachItem);

        // CollectDeliveryController
        CollectDeliveryController collectDeliveryController = mock(CollectDeliveryController.class);
//        when(collectDeliveryController.getServicePointProviderForItem())

        // The cart
        Cart cart = new Cart();
        cart.getItemsInCart().add(item1);
        cart.getItemsInCart().add(item2);
        cart.getItemsInCart().add(item3);

        // Inject
        verifyDeliveryController = new VerifyDeliveryController();
        Class<? extends VerifyDeliveryController> clazz = verifyDeliveryController.getClass();

        Field userProfileControllerField = clazz.getDeclaredField("userProfileController");
        userProfileControllerField.setAccessible(true);
        userProfileControllerField.set(verifyDeliveryController, userProfileController);

        Field deliveryControllerField = clazz.getDeclaredField("deliveryController");
        deliveryControllerField.setAccessible(true);
        deliveryControllerField.set(verifyDeliveryController, deliveryController);

    }

    @Test
    public void confirmOrder() throws Exception {

    }

}