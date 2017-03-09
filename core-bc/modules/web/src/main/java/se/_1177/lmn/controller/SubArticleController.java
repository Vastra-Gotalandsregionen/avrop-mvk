package se._1177.lmn.controller;

import org.omnifaces.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.ArticleType;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import se._1177.lmn.controller.model.ArticleWithSubArticlesModel;
import se._1177.lmn.controller.model.Cart;
import se._1177.lmn.controller.model.SubArticleDto;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SubArticleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubArticleController.class);

    @Autowired
    private Cart cart;

    @Autowired
    private UserProfileController userProfileController;

    private List<ArticleWithSubArticlesModel> articleWithSubArticlesModels;
    /*private MyObject[] myObjects = new MyObject[] {
            new MyObject("1", getNumber() + ""),
            new MyObject("2", (20 - getNumber()) + "")
    };*/

    /*private int number;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }*/

//    public MyObject[] getMyObjects() {
//        return myObjects;
//    }
    @PostConstruct
    public void init() {
        articleWithSubArticlesModels = makeDtoModel(getThoseWhereChoiceIsNeeded());
    }

    public List<ArticleWithSubArticlesModel> getPrescriptionItems() {
        return articleWithSubArticlesModels;
    }

    public String getJsonData() {
        return Json.encode(articleWithSubArticlesModels);
    }

    private List<ArticleWithSubArticlesModel> makeDtoModel(List<PrescriptionItemType> thoseWhereChoiceIsNeeded) {
        List<ArticleWithSubArticlesModel> list = new ArrayList<>();
        for (PrescriptionItemType prescriptionItemType : thoseWhereChoiceIsNeeded) {
            ArticleWithSubArticlesModel model = new ArticleWithSubArticlesModel();

            model.setParentArticleName(prescriptionItemType.getArticle().getArticleName());

            int numbersStillInNeedToDistribute = prescriptionItemType.getNoOfPackagesPerOrder();
            float numberSubArticlesLeftToDistributeTo = prescriptionItemType.getSubArticle().size();

            model.setTotalOrderSize(numbersStillInNeedToDistribute);

            int nextOrderCountNumberToDistribute;// = ((int) Math.ceil(numbersStillInNeedToDistribute / numberSubArticlesLeftToDistributeTo));
//            numbersStillInNeedToDistribute -= nextOrderCountNumberToDistribute;
//            numberSubArticlesLeftToDistributeTo -= 1;

            for (ArticleType subArticle : prescriptionItemType.getSubArticle()) {
                // Update these values for this iteration
                nextOrderCountNumberToDistribute = ((int) Math.ceil(numbersStillInNeedToDistribute / numberSubArticlesLeftToDistributeTo));
                numbersStillInNeedToDistribute -= nextOrderCountNumberToDistribute;
                numberSubArticlesLeftToDistributeTo -= 1;

                SubArticleDto subArticleDto = new SubArticleDto();
                subArticleDto.setName(subArticle.getArticleName());
                subArticleDto.setOrderCount(nextOrderCountNumberToDistribute);
                model.getSubArticles().add(subArticleDto);


            }

            list.add(model);
        }
        return list;
    }

    private List<PrescriptionItemType> getThoseWhereChoiceIsNeeded() {
        return cart.getItemsInCart().stream().filter(prescriptionItemType ->
                    prescriptionItemType.getSubArticle() != null && prescriptionItemType.getSubArticle().size() > 1
            ).collect(Collectors.toList());
    }

    public String toOrder() {
        String delegateUrlParameters = userProfileController.getDelegateUrlParameters();

        String ampOrQuestionMark = delegateUrlParameters != null && delegateUrlParameters.length() > 0 ? "&amp;" : "?";

        String result = "order" + delegateUrlParameters
                + ampOrQuestionMark
                + "faces-redirect=true&amp;includeViewParams=true";

        return result;
    }

    public String toDelivery() {

        System.out.println();

        return null;
    }
    /*public void action() {
//        System.out.println(this.number++);
//        this.myObjects[0].setValue("" + (Integer.parseInt(this.myObjects[0].getValue()) ));
    }*/
}

