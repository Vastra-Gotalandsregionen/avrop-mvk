package se._1177.lmn.controller;

import org.junit.Test;
import riv.crm.selfservice.medicalsupply._0.ArticleType;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * @author Patrik Björk
 */
public class OrderControllerTest {

    @Test
    public void choiceForSubArticlesIsNeededNoSubArticles() throws Exception {
        OrderController orderController = new OrderController();

        ArrayList<PrescriptionItemType> itemsInCart = new ArrayList<>();

        PrescriptionItemType prescriptionItemType = new PrescriptionItemType();
        prescriptionItemType.setArticle(new ArticleType());

        itemsInCart.add(prescriptionItemType);

        assertFalse(orderController.choiceForSubArticlesIsNeeded(itemsInCart));
    }

    @Test
    public void choiceForSubArticlesIsNeededWithOneSubArticle() throws Exception {
        OrderController orderController = new OrderController();

        ArrayList<PrescriptionItemType> itemsInCart = new ArrayList<>();

        PrescriptionItemType prescriptionItemType = new PrescriptionItemType();
        prescriptionItemType.setArticle(new ArticleType());

        prescriptionItemType.getSubArticle().add(new ArticleType());

        itemsInCart.add(prescriptionItemType);

        assertFalse(orderController.choiceForSubArticlesIsNeeded(itemsInCart));
    }

    @Test
    public void choiceForSubArticlesIsNeededWithTwoSubArticles() throws Exception {
        OrderController orderController = new OrderController();

        ArrayList<PrescriptionItemType> itemsInCart = new ArrayList<>();

        PrescriptionItemType prescriptionItemType = new PrescriptionItemType();
        prescriptionItemType.setArticle(new ArticleType());

        prescriptionItemType.getSubArticle().add(new ArticleType());
        prescriptionItemType.getSubArticle().add(new ArticleType());

        itemsInCart.add(prescriptionItemType);

        assertTrue(orderController.choiceForSubArticlesIsNeeded(itemsInCart));
    }

}