package se._1177.lmn.controller.model;

import java.util.ArrayList;
import java.util.List;

public class ArticleWithSubArticlesModel {
    private String parentArticleName;
    private int totalOrderSize;
    private List<SubArticleDto> subArticles = new ArrayList<>();

    public String getParentArticleName() {
        return parentArticleName;
    }

    public void setParentArticleName(String parentArticleName) {
        this.parentArticleName = parentArticleName;
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
}
