package se._1177.lmn.controller;

import org.omnifaces.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._0.ArticleType;
import riv.crm.selfservice.medicalsupply._0.OrderRowType;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import se._1177.lmn.controller.model.ArticleWithSubArticlesModel;
import se._1177.lmn.controller.model.Cart;
import se._1177.lmn.controller.model.PrescriptionItemInfo;
import se._1177.lmn.controller.model.SubArticleDto;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static se._1177.lmn.service.util.Constants.ACTION_SUFFIX;

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
    private PrescriptionItemInfo prescriptionItemInfo;

    @Autowired
    private UserProfileController userProfileController;

    private List<ArticleWithSubArticlesModel> articleWithSubArticlesModels;

    private Map<String, ArticleType> subArticleIdToArticle = new HashMap<>();

    public void init() {
        articleWithSubArticlesModels = makeDtoModel(getThoseWhereChoiceIsNeeded());
    }

    public List<ArticleWithSubArticlesModel> getArticleWithSubArticlesModels() {
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

            model.setPrescriptionItemId(prescriptionItemType.getPrescriptionItemId());

            int numbersStillInNeedToDistribute = prescriptionItemType.getNoOfPackagesPerOrder();
            float numberSubArticlesLeftToDistributeTo = prescriptionItemType.getSubArticle().size();

            model.setTotalOrderSize(numbersStillInNeedToDistribute);

            int nextOrderCountNumberToDistribute;

            for (ArticleType subArticle : prescriptionItemType.getSubArticle()) {
                // Update these values for this iteration
                nextOrderCountNumberToDistribute = ((int) Math.ceil(numbersStillInNeedToDistribute
                        / numberSubArticlesLeftToDistributeTo));

                numbersStillInNeedToDistribute -= nextOrderCountNumberToDistribute;
                numberSubArticlesLeftToDistributeTo -= 1;

                SubArticleDto subArticleDto = new SubArticleDto();
                subArticleDto.setName(subArticle.getArticleName());
                subArticleDto.setArticleNo(subArticle.getArticleNo());
                subArticleDto.setOrderCount(nextOrderCountNumberToDistribute);
                model.getSubArticles().add(subArticleDto);

                subArticleIdToArticle.put(subArticle.getArticleNo(), subArticle);
            }

            list.add(model);
        }

        return list;
    }

    private List<PrescriptionItemType> getThoseWhereChoiceIsNeeded() {
        List<PrescriptionItemType> prescriptionItemsInCart = prescriptionItemInfo
                .getChosenPrescriptionItemInfoList();

        return prescriptionItemsInCart.stream().filter(prescriptionItemType ->
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
        // The sub articles were not added in the previous step since more information was needed.
        complementCartWithSubArticles();

        return "delivery" + ACTION_SUFFIX;
    }

    private void complementCartWithSubArticles() {
        for (ArticleWithSubArticlesModel articleWithSubArticlesModel : articleWithSubArticlesModels) {

            List<SubArticleDto> subArticlesToOrder = articleWithSubArticlesModel.getSubArticles()
                    .stream()
                    .filter(subArticleDto -> subArticleDto.getOrderCount() > 0)
                    .collect(Collectors.toList());

            for (SubArticleDto subArticleDto : subArticlesToOrder) {
                ArticleType subArticle = subArticleIdToArticle.get(subArticleDto.getArticleNo());

                OrderRowType orderRowType = new OrderRowType();

                orderRowType.setArticle(subArticle);
                orderRowType.setNoOfPackages(subArticleDto.getOrderCount());
                orderRowType.setNoOfPcs(subArticleDto.getOrderCount() * subArticle.getPackageSize());

                PrescriptionItemType prescriptionItem = prescriptionItemInfo.getPrescriptionItem(
                        articleWithSubArticlesModel.getPrescriptionItemId());

                orderRowType.setPrescriptionId(prescriptionItem.getPrescriptionId());
                orderRowType.setPrescriptionItemId(prescriptionItem.getPrescriptionItemId());
                orderRowType.setSource(prescriptionItem.getSource());

                cart.getOrderRows().add(orderRowType);
            }
        }
    }
}

