package se._1177.lmn.controller;

import org.junit.Test;
import se._1177.lmn.service.util.Constants;

import static org.junit.Assert.*;

public class NavigationControllerTest {

    private NavigationController navigationController = new NavigationController();

    @Test
    public void init() {
        String value = "order" + Constants.ACTION_SUFFIX;
        navigationController.init(value, OrderController.VIEW_NAME);

        assertEquals(1, navigationController.getViews().size());
        assertEquals(value, navigationController.getViews().get(0).value);
    }

    @Test
    public void gotoDeliveryView() {
        // Init (to make realistic).
        this.init();

        String value = "delivery" + Constants.ACTION_SUFFIX;
        navigationController.gotoView(value, DeliveryController.VIEW_NAME);

        assertEquals(2, navigationController.getViews().size());
        assertEquals(value, navigationController.getViews().get(1).value);
    }

    @Test
    public void goBack() {
        // Prepare by going to delivery view.
        this.gotoDeliveryView();

        String value = "order" + Constants.ACTION_SUFFIX;

        // Then go back
        navigationController.goBack();

        // Then
        assertEquals(1, navigationController.getViews().size());
        assertEquals(value, navigationController.getViews().get(0).value);
    }

    @Test
    public void getViews() {
        // E.g. prepare by navigation to delivery view.
        this.gotoDeliveryView();

        String value = "delivery" + Constants.ACTION_SUFFIX;

        assertEquals(2, navigationController.getViews().size());
        assertEquals(value, navigationController.getViews().get(1).value);
    }

    @Test
    public void hasVisitedCollectDeliveryPositive() {
        // Start by navigating to delivery view and then add more views.
        this.gotoDeliveryView();
        navigationController.gotoView("collectDelivery" + Constants.ACTION_SUFFIX, CollectDeliveryController.VIEW_NAME);

        // Then
        assertTrue(navigationController.hasVisitedCollectDelivery());

        // We add one more view just to see it's still true.
        navigationController.gotoView("verifyDelivery" + Constants.ACTION_SUFFIX, VerifyDeliveryController.VIEW_NAME);

        // Then
        assertTrue(navigationController.hasVisitedCollectDelivery());
    }

    @Test
    public void hasVisitedCollectDeliveryNegative() {
        // Start by navigating to delivery view and then add more views.
        this.gotoDeliveryView();

        // Navigate to other view than collectDelivery
        navigationController.gotoView("verifyDelivery" + Constants.ACTION_SUFFIX, VerifyDeliveryController.VIEW_NAME);

        // Then
        assertFalse(navigationController.hasVisitedCollectDelivery());
    }

    @Test
    public void goBackTo() {
        // Prepare by making a number of navigations.
        this.hasVisitedCollectDeliveryPositive();

        String value = "delivery" + Constants.ACTION_SUFFIX;
        navigationController.goBackTo(value, DeliveryController.VIEW_NAME);

        // Then
        assertEquals(2, navigationController.getViews().size());
        assertEquals(value, navigationController.getViews().get(1).value);
    }

    @Test
    public void goBackTo1() {
        // Prepare by making a number of navigations.
        this.hasVisitedCollectDeliveryPositive();

        String value = "delivery" + Constants.ACTION_SUFFIX;
        navigationController.goBackTo(NavigationController.View.from(value, DeliveryController.VIEW_NAME));

        // Then
        assertEquals(2, navigationController.getViews().size());
        assertEquals(value, navigationController.getViews().get(1).value);
    }

    @Test
    public void getActionSuffix() {
        assertEquals(Constants.ACTION_SUFFIX, navigationController.getActionSuffix());
    }

    @Test
    public void ensureLastViewIs() {
        // Prepare by making a number of navigations.
        this.hasVisitedCollectDeliveryPositive();

        // We know we should have passed collectDelivery.
        String viewWithoutSuffix = "collectDelivery";
        navigationController.ensureLastViewIs(viewWithoutSuffix, CollectDeliveryController.VIEW_NAME);

        assertEquals(viewWithoutSuffix + Constants.ACTION_SUFFIX, navigationController.getViews().lastElement().value);
        assertEquals(CollectDeliveryController.VIEW_NAME, navigationController.getViews().lastElement().label);
    }

    @Test
    public void ensureLastViewIsAddView() {
        // This tests that a view also can be added if it doesn't already exist.

        // Prepare by making a number of navigations.
        this.hasVisitedCollectDeliveryPositive();

        int sizeBeforeEnsure = navigationController.getViews().size();

        // Ensure last view is a view which isn't stored.
        String viewWithoutSuffix = "invoiceAddress";
        navigationController.ensureLastViewIs(viewWithoutSuffix, InvoiceAddressController.VIEW_NAME);

        int sizeAfterEnsure = navigationController.getViews().size();

        assertEquals(viewWithoutSuffix + Constants.ACTION_SUFFIX, navigationController.getViews().lastElement().value);
        assertEquals(InvoiceAddressController.VIEW_NAME, navigationController.getViews().lastElement().label);
        assertEquals(sizeBeforeEnsure + 1, sizeAfterEnsure);
    }
}