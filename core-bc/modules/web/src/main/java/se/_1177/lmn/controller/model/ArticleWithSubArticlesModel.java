package se._1177.lmn.controller.model;

import java.util.ArrayList;
import java.util.List;

public class ArticleWithSubArticlesModel {
    private String parentArticleName;
    private String prescriptionItemId;
    private int totalOrderSize;
    private List<SubArticleDto> subArticles = new ArrayList<>();
    private String totalOrderSizeUnit;

    public String getParentArticleName() {
        return parentArticleName;
    }

    public void setParentArticleName(String parentArticleName) {
        this.parentArticleName = parentArticleName;
    }

    public String getPrescriptionItemId() {
        return prescriptionItemId;
    }

    public void setPrescriptionItemId(String prescriptionItemId) {
        this.prescriptionItemId = prescriptionItemId;
    }

    public int getTotalOrderSize() {
        return totalOrderSize;
    }

    public void setTotalOrderSize(int totalOrderSize) {
        this.totalOrderSize = totalOrderSize;
    }

    public List<SubArticleDto> getSubArticles() {
        return subArticles;
    }

    public void setSubArticles(List<SubArticleDto> subArticles) {
        this.subArticles = subArticles;
    }

    public void setTotalOrderSizeUnit(String totalOrderSizeUnit) {
        this.totalOrderSizeUnit = totalOrderSizeUnit;
    }

    public String getTotalOrderSizeUnit() {
        return totalOrderSizeUnit;
    }
}