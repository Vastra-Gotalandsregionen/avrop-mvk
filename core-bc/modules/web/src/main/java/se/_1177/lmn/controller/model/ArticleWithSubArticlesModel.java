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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArticleWithSubArticlesModel that = (ArticleWithSubArticlesModel) o;

        if (totalOrderSize != that.totalOrderSize) return false;
        if (parentArticleName != null ? !parentArticleName.equals(that.parentArticleName) : that.parentArticleName != null)
            return false;
        if (prescriptionItemId != null ? !prescriptionItemId.equals(that.prescriptionItemId) : that.prescriptionItemId != null)
            return false;
        return totalOrderSizeUnit != null ? totalOrderSizeUnit.equals(that.totalOrderSizeUnit) : that.totalOrderSizeUnit == null;
    }

    @Override
    public int hashCode() {
        int result = parentArticleName != null ? parentArticleName.hashCode() : 0;
        result = 31 * result + (prescriptionItemId != null ? prescriptionItemId.hashCode() : 0);
        result = 31 * result + totalOrderSize;
        result = 31 * result + (totalOrderSizeUnit != null ? totalOrderSizeUnit.hashCode() : 0);
        return result;
    }
}