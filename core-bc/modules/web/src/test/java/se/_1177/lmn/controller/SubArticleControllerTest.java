package se._1177.lmn.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import riv.crm.selfservice.medicalsupply._2.ArticleType;
import riv.crm.selfservice.medicalsupply._2.OrderItemType;
import riv.crm.selfservice.medicalsupply._2.PrescriptionItemType;
import se._1177.lmn.controller.model.ArticleWithSubArticlesModel;
import se._1177.lmn.controller.model.Cart;
import se._1177.lmn.controller.model.PrescriptionItemInfo;
import se._1177.lmn.controller.model.SubArticleDto;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static se._1177.lmn.controller.util.TestUtil.getTodayPlusDays;

@RunWith(MockitoJUnitRunner.class)
public class SubArticleControllerTest {

    @Mock
    private PrescriptionItemInfo prescriptionItemInfo;

    @Mock
    private NavigationController navigationController;

    @Mock
    private UtilController utilController;

    @InjectMocks
    private SubArticleController subArticleController = new SubArticleController();

    private String prescriptionItemId1;
    private String prescriptionItemId2;
    private ArticleType article11;
    private ArticleType article12;
    private ArticleType article13;
    private ArticleType article21;
    private ArticleType article22;
    private ArticleType article23;

    @Before
    public void setup() {

        prescriptionItemId1 = "1234";
        prescriptionItemId2 = "5678";

        String articleNo11 = "11";
        String articleNo12 = "12";
        String articleNo13 = "13";

        String articleNo21 = "21";
        String articleNo22 = "22";
        String articleNo23 = "23";

        article11 = new ArticleType();
        article12 = new ArticleType();
        article13 = new ArticleType();

        article21 = new ArticleType();
        article22 = new ArticleType();
        article23 = new ArticleType();

        article11.setArticleNo(articleNo11);
        article12.setArticleNo(articleNo12);
        article13.setArticleNo(articleNo13);

        article21.setArticleNo(articleNo21);
        article22.setArticleNo(articleNo22);
        article23.setArticleNo(articleNo23);

        article11.setPackageSize(13);
        article12.setPackageSize(13);
        article13.setPackageSize(13);

        article21.setPackageSize(17);
        article22.setPackageSize(17);
        article23.setPackageSize(17);

        OrderItemType orderItemType11 = new OrderItemType();
        OrderItemType orderItemType12 = new OrderItemType();
        OrderItemType orderItemType13 = new OrderItemType();

        OrderItemType orderItemType21 = new OrderItemType();
        OrderItemType orderItemType22 = new OrderItemType();
        OrderItemType orderItemType23 = new OrderItemType();

        orderItemType11.setPrescriptionItemId(prescriptionItemId1);
        orderItemType11.setOrderDate(getTodayPlusDays(-1));
        orderItemType11.setArticle(article11);
        orderItemType11.setNoOfPcs(26); // Two packages

        orderItemType12.setPrescriptionItemId(prescriptionItemId1);
        orderItemType12.setOrderDate(getTodayPlusDays(-1));
        orderItemType12.setArticle(article12);
        orderItemType12.setNoOfPcs(39); // Three packages. Two plus three equals five which is set as noOfPackagesPerOrder below.

        orderItemType13.setPrescriptionItemId(prescriptionItemId1);
        orderItemType13.setOrderDate(getTodayPlusDays(-2)); // Note it's ordered before the other two.
        orderItemType13.setArticle(article13);
        orderItemType13.setNoOfPcs(65); // Five packages but that doesn't matter since this wasn't ordered at last order date.

        orderItemType21.setPrescriptionItemId(prescriptionItemId2);
        orderItemType21.setOrderDate(getTodayPlusDays(-1));
        orderItemType21.setArticle(article21);
        orderItemType21.setNoOfPcs(34); // Two packages

        orderItemType22.setPrescriptionItemId(prescriptionItemId2);
        orderItemType22.setOrderDate(getTodayPlusDays(-2)); // Note it's ordered before the other two.
        orderItemType22.setArticle(article22);
        orderItemType22.setNoOfPcs(69);

        orderItemType23.setPrescriptionItemId(prescriptionItemId2);
        orderItemType23.setOrderDate(getTodayPlusDays(-1));
        orderItemType23.setArticle(article23);
        orderItemType23.setNoOfPcs(85); // Five packages. Two plus five equals seven which is set as noOfPackagesPerOrder below.

        Map<String, OrderItemType> latestOrderItemsByArticleNo1 = new HashMap<>();
        Map<String, OrderItemType> latestOrderItemsByArticleNo2 = new HashMap<>();

        latestOrderItemsByArticleNo1.put(articleNo11, orderItemType11);
        latestOrderItemsByArticleNo1.put(articleNo12, orderItemType12);
//        latestOrderItemsByArticleNo1.put(articleNo13, orderItemType13); // Comment out this to be clear that this isn't expected here since it wasn't ordered at last order occasion.

        latestOrderItemsByArticleNo2.put(articleNo21, orderItemType21);
//        latestOrderItemsByArticleNo2.put(articleNo22, orderItemType22);
        latestOrderItemsByArticleNo2.put(articleNo23, orderItemType23); // Comment out this to be clear that this isn't expected here since it wasn't ordered at last order occasion.

        Map<String, Map<String, OrderItemType>> latestOrderItemsByArticleNoAndPrescriptionItem = new HashMap<>();

        latestOrderItemsByArticleNoAndPrescriptionItem.put(prescriptionItemId1, latestOrderItemsByArticleNo1);
        latestOrderItemsByArticleNoAndPrescriptionItem.put(prescriptionItemId2, latestOrderItemsByArticleNo2);

        when(prescriptionItemInfo.getLatestOrderItemsByArticleNoAndPrescriptionItem())
                .thenReturn(latestOrderItemsByArticleNoAndPrescriptionItem);
    }

    @Test
    public void makeDtoModel() throws Exception {

        // Given

        // These two have earlier OrderItemTypes
        PrescriptionItemType prescriptionItemType1 = new PrescriptionItemType();
        PrescriptionItemType prescriptionItemType2 = new PrescriptionItemType();

        // This does not have any earlier OrderItemType.
        PrescriptionItemType prescriptionItemType3 = new PrescriptionItemType();

        prescriptionItemType1.setPrescriptionItemId(this.prescriptionItemId1);
        prescriptionItemType2.setPrescriptionItemId(this.prescriptionItemId2);
        prescriptionItemType3.setPrescriptionItemId("something that doesn't match earlier OrderItemTypes");

        ArticleType parentArticle1 = new ArticleType();
        ArticleType parentArticle2 = new ArticleType();
        ArticleType parentArticle3 = new ArticleType();

        parentArticle1.setArticleName("a1");
        parentArticle2.setArticleName("a2");
        parentArticle3.setArticleName("a3");

        parentArticle1.setPackageSize(1);
        parentArticle2.setPackageSize(2);
        parentArticle3.setPackageSize(3);

        prescriptionItemType1.setArticle(parentArticle1);
        prescriptionItemType2.setArticle(parentArticle2);
        prescriptionItemType3.setArticle(parentArticle3);

        prescriptionItemType1.getSubArticle().add(article11);
        prescriptionItemType1.getSubArticle().add(article12);
        prescriptionItemType1.getSubArticle().add(article13);

        prescriptionItemType2.getSubArticle().add(article21);
        prescriptionItemType2.getSubArticle().add(article22);
        prescriptionItemType2.getSubArticle().add(article23);

        // Just reuse the articles here, there won't be any conflict.
        prescriptionItemType3.getSubArticle().add(article21);
        prescriptionItemType3.getSubArticle().add(article22);
        prescriptionItemType3.getSubArticle().add(article23);

        prescriptionItemType1.setNoOfArticlesPerOrder(5);
        prescriptionItemType2.setNoOfArticlesPerOrder(14);
        prescriptionItemType3.setNoOfArticlesPerOrder(33);

        prescriptionItemType1.setNoOfPackagesPerOrder(5);
        prescriptionItemType2.setNoOfPackagesPerOrder(7);
        prescriptionItemType3.setNoOfPackagesPerOrder(11);

        List<PrescriptionItemType> prescriptionItems = new ArrayList<>();

        prescriptionItems.add(prescriptionItemType1);
        prescriptionItems.add(prescriptionItemType2);
        prescriptionItems.add(prescriptionItemType3);

        // When
        List<ArticleWithSubArticlesModel> result = subArticleController.makeDtoModel(prescriptionItems);

        // Then
        assertEquals(5,result.get(0).getTotalOrderSize());
        assertEquals(3, result.get(0).getSubArticles().size());
        assertEquals("11", result.get(0).getSubArticles().get(0).getArticleNo());
        assertEquals(2, result.get(0).getSubArticles().get(0).getOrderCount()); // Two package as in setup.
        assertEquals(3, result.get(0).getSubArticles().get(1).getOrderCount()); // Three packages as in setup.
        assertEquals(0, result.get(0).getSubArticles().get(2).getOrderCount()); // Wasn't included in last order so zero.

        assertEquals(7, result.get(1).getTotalOrderSize());
        assertEquals(3, result.get(1).getSubArticles().size());
        assertEquals("21", result.get(1).getSubArticles().get(0).getArticleNo());
        assertEquals(2, result.get(1).getSubArticles().get(0).getOrderCount()); // Two package as in setup.
        assertEquals(0, result.get(1).getSubArticles().get(1).getOrderCount()); // Wasn't included in last order so zero.
        assertEquals(5, result.get(1).getSubArticles().get(2).getOrderCount()); // Five packages as in setup.

        // The last one should be distributed evenly. Eleven packages means 4+4+3.
        assertEquals(11, result.get(2).getTotalOrderSize());
        assertEquals(3, result.get(2).getSubArticles().size());
        assertEquals("21", result.get(2).getSubArticles().get(0).getArticleNo());
        assertEquals(0, result.get(2).getSubArticles().get(0).getOrderCount()); // Hasn't been ordered before.
        assertEquals(0, result.get(2).getSubArticles().get(1).getOrderCount()); // Hasn't been ordered before.
        assertEquals(0, result.get(2).getSubArticles().get(2).getOrderCount()); // Hasn't been ordered before.
    }

    @Test
    public void jsonEncode() throws Exception {
        ArrayList<ArticleWithSubArticlesModel> model = new ArrayList<>();

        ArticleWithSubArticlesModel model1 = new ArticleWithSubArticlesModel();
        ArticleWithSubArticlesModel model2 = new ArticleWithSubArticlesModel();

        model1.setParentArticleName("pan1");
        model2.setParentArticleName("pan2");

        SubArticleDto subArticleDto = new SubArticleDto();
        subArticleDto.setName("sub");
        model2.getSubArticles().add(subArticleDto);

        model.add(model1);
        model.add(model2);

        String json = subArticleController.jsonEncode(model);

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.eval("" +
                "var lengthOfObject = function(json) {" +
                "print('JSON input: ' + json);" +
                "return JSON.parse(json).length;" +
                "};" +
                "" +
                "var nameOfSubArticleInSecondModel = function(json) {" +
                "return JSON.parse(json)[1].subArticles[0].name;" +
                "};");

        Invocable invocable = (Invocable) engine;

        Number lengthOfObject = (Number) invocable.invokeFunction("lengthOfObject", json);
        String nameOfSubArticleInSecondModel = (String) invocable.invokeFunction("nameOfSubArticleInSecondModel", json);

        assertEquals(2, lengthOfObject.intValue());
        assertEquals("sub", nameOfSubArticleInSecondModel);
    }

    @Test
    public void init() throws Exception {

        PrescriptionItemType p1 = new PrescriptionItemType();
        PrescriptionItemType p2 = new PrescriptionItemType();
        PrescriptionItemType p3 = new PrescriptionItemType();

        p1.setPrescriptionId("p1");
        p2.setPrescriptionId("p2");
        p3.setPrescriptionId("p3");

        ArticleType a1 = new ArticleType();
        ArticleType a2 = new ArticleType();
        ArticleType a3 = new ArticleType();

        a1.setArticleName("a1");
        a2.setArticleName("a2");
        a3.setArticleName("a3");

        a1.setPackageSize(1);
        a2.setPackageSize(2);
        a3.setPackageSize(3);

        p1.setArticle(a1);
        p2.setArticle(a2);
        p3.setArticle(a3);

        ArticleType subArticle = new ArticleType();

        // p2 is the one with subarticle.
        p2.getSubArticle().add(subArticle);
        p2.setNoOfPackagesPerOrder(1);
        p2.setNoOfArticlesPerOrder(2);

        List<PrescriptionItemType> prescriptionItemTypes = new ArrayList<>();

        prescriptionItemTypes.add(p1);
        prescriptionItemTypes.add(p2);
        prescriptionItemTypes.add(p3);

        when(prescriptionItemInfo.getChosenPrescriptionItemInfoList()).thenReturn(prescriptionItemTypes);

        subArticleController.init();

        assertEquals(1, subArticleController.getArticleWithSubArticlesModels().size());
    }

    @Test
    public void toDelivery() throws Exception {
        Cart cart = new Cart();

        Field cartField = subArticleController.getClass().getDeclaredField("cart");
        cartField.setAccessible(true);
        cartField.set(subArticleController, cart);

        // Prepare by executing that test.
        init();

        // Given
        subArticleController.getArticleWithSubArticlesModels().get(0).getSubArticles().get(0).setOrderCount(1);
        subArticleController.getArticleWithSubArticlesModels().get(0).setPrescriptionItemId("p2");

        PrescriptionItemType any = new PrescriptionItemType();

        when(prescriptionItemInfo.getPrescriptionItem(eq("p2"))).thenReturn(any);

        // When
        subArticleController.toDelivery();

        // Then
        assertEquals(1, cart.getOrderRows().size());
    }

}
