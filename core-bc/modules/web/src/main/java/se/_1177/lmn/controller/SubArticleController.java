package se._1177.lmn.controller;

import org.apache.commons.lang3.StringUtils;
import org.omnifaces.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._1.ArticleType;
import riv.crm.selfservice.medicalsupply._1.ImageType;
import riv.crm.selfservice.medicalsupply._1.OrderItemType;
import riv.crm.selfservice.medicalsupply._1.OrderRowType;
import riv.crm.selfservice.medicalsupply._1.PrescriptionItemType;
import se._1177.lmn.controller.model.ArticleWithSubArticlesModel;
import se._1177.lmn.controller.model.Cart;
import se._1177.lmn.controller.model.PrescriptionItemInfo;
import se._1177.lmn.controller.model.SubArticleDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static se._1177.lmn.service.util.Constants.ACTION_SUFFIX;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SubArticleController {

    public static final String VIEW_NAME = "Fördela underartiklar";

    private static final Logger LOGGER = LoggerFactory.getLogger(SubArticleController.class);

    @Autowired
    private Cart cart;

    @Autowired
    private PrescriptionItemInfo prescriptionItemInfo;

    @Autowired
    private NavigationController navigationController;

    @Autowired
    private UtilController utilController;

    private List<ArticleWithSubArticlesModel> articleWithSubArticlesModels;

    private Map<String, ArticleType> subArticleIdToArticle = new HashMap<>();

    public void init() {
        articleWithSubArticlesModels = makeDtoModel(getThoseWhereChoiceIsNeeded());
    }

    public List<ArticleWithSubArticlesModel> getArticleWithSubArticlesModels() {
        return articleWithSubArticlesModels;
    }

    public String getJsonData() {
        return jsonEncode(articleWithSubArticlesModels);
    }

    String jsonEncode(List<ArticleWithSubArticlesModel> articleWithSubArticlesModels) {
        return Json.encode(articleWithSubArticlesModels);
    }

    List<ArticleWithSubArticlesModel> makeDtoModel(List<PrescriptionItemType> thoseWhereChoiceIsNeeded) {
        List<ArticleWithSubArticlesModel> list = new ArrayList<>();
        for (PrescriptionItemType prescriptionItemType : thoseWhereChoiceIsNeeded) {
            ArticleWithSubArticlesModel model = new ArticleWithSubArticlesModel();

            model.setParentArticleName(prescriptionItemType.getArticle().getArticleName());

            model.setPrescriptionItemId(prescriptionItemType.getPrescriptionItemId());

            model.setTotalOrderSize(
                    prescriptionItemType.getNoOfArticlesPerOrder()
                    / prescriptionItemType.getArticle().getPackageSize());

            // Zero packages per order means we're dealing with articles and not packages
            model.setTotalOrderSizeUnit(prescriptionItemType.getNoOfPackagesPerOrder() == 0
                    ? "artiklar" : "förpackningar");

            if (articleWithSubArticlesModels != null && articleWithSubArticlesModels.contains(model)) {
                // The model is present from previous user interactions. We then want to keep the distribution.
                model = articleWithSubArticlesModels.get(articleWithSubArticlesModels.indexOf(model));
                list.add(model);
                continue;
            }

            Map<String, Map<String, OrderItemType>> latestOrderItemsByArticleNoAndPrescriptionItem =
                    prescriptionItemInfo.getLatestOrderItemsByArticleNoAndPrescriptionItem();

            Map<String, OrderItemType> latestOrderedForThisPrescriptionItem =
                    latestOrderItemsByArticleNoAndPrescriptionItem.get(prescriptionItemType.getPrescriptionItemId());

            int numberDistributedForPrescriptionItem = 0;

            for (ArticleType subArticle : prescriptionItemType.getSubArticle()) {
                // Update these values for this iteration

                int orderCountThisSubArticle = 0;

                if (latestOrderedForThisPrescriptionItem != null
                        && latestOrderedForThisPrescriptionItem.get(subArticle.getArticleNo()) != null) {

                    Integer previouslyOrderedPcs = latestOrderedForThisPrescriptionItem
                            .get(subArticle.getArticleNo()).getNoOfPcs();

                    Integer previouslyOrderedPackages = previouslyOrderedPcs / subArticle.getPackageSize();

                    orderCountThisSubArticle = previouslyOrderedPackages;

                }

                String variety = subArticle.getVariety();

                SubArticleDto subArticleDto = new SubArticleDto();
                subArticleDto.setName(!isEmpty(variety) ? variety : subArticle.getArticleName());
                subArticleDto.setArticleNo(subArticle.getArticleNo());
                subArticleDto.setOrderCount(orderCountThisSubArticle);

                numberDistributedForPrescriptionItem += orderCountThisSubArticle;

                ImageType articleImage = subArticle.getArticleImage();
                if (articleImage != null) {
                    subArticleDto.setThumbnailUrl(articleImage.getThumbnail());
                    subArticleDto.setImageUrl(articleImage.getOriginal());
                }

                model.getSubArticles().add(subArticleDto);

                subArticleIdToArticle.put(subArticle.getArticleNo(), subArticle);
            }

            model.setDistributedNumber(numberDistributedForPrescriptionItem);

            list.add(model);
        }

        return list;
    }

    private List<PrescriptionItemType> getThoseWhereChoiceIsNeeded() {
        List<PrescriptionItemType> prescriptionItemsInCart = prescriptionItemInfo
                .getChosenPrescriptionItemInfoList();

        List<PrescriptionItemType> result = prescriptionItemsInCart.stream().filter(prescriptionItemType ->
                prescriptionItemType.getSubArticle() != null && prescriptionItemType.getSubArticle().size() > 0
        ).collect(Collectors.toList());

        result.sort(Comparator.comparing(o -> o.getArticle().getArticleName()));

        return result;
    }

    public String toOrder() {
        return navigationController.goBack();
    }

    public String toDelivery() {

        // Validate distributed numbers
        List<String> invalidDistributedNumbers = articleWithSubArticlesModels.stream()
                .filter(model -> model.getTotalOrderSize() != model.getDistributedNumber())
                .map(model -> model.getParentArticleName())
                .collect(Collectors.toList());

        if (invalidDistributedNumbers.size() > 0) {
            String text = "Följande har fel antal fördelade artiklar:<br/>"
                    + StringUtils.join(invalidDistributedNumbers, "<br/>");

            utilController.addErrorMessageWithCustomerServiceInfo(text);

            return "subArticle";
        }

        // The sub articles were not added in the previous step since more information was needed.
        complementCartWithSubArticles();

        return navigationController.gotoView("delivery" + ACTION_SUFFIX, DeliveryController.VIEW_NAME);
    }

    private void complementCartWithSubArticles() {
        for (ArticleWithSubArticlesModel articleWithSubArticlesModel : articleWithSubArticlesModels) {

            List<SubArticleDto> subArticlesToOrder = articleWithSubArticlesModel.getSubArticles()
                    .stream()
                    .filter(subArticleDto -> subArticleDto.getOrderCount() > 0)
                    .collect(Collectors.toList());

            // We'll remove all orderRows with the same prescriptionItemId and then re-add.
            cart.getOrderRows().removeIf(orderRowType -> orderRowType.getPrescriptionItemId()
                            .equals(articleWithSubArticlesModel.getPrescriptionItemId()));

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

    public String getViewName() {
        return VIEW_NAME;
    }
}