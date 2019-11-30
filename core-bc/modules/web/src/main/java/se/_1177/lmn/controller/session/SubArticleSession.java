package se._1177.lmn.controller.session;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._1.ArticleType;
import se._1177.lmn.controller.model.ArticleWithSubArticlesModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SubArticleSession implements Serializable {

    private List<ArticleWithSubArticlesModel> articleWithSubArticlesModels;

    private Map<String, ArticleType> subArticleIdToArticle = new HashMap<>();

    public List<ArticleWithSubArticlesModel> getArticleWithSubArticlesModels() {
        return articleWithSubArticlesModels;
    }

    public void setArticleWithSubArticlesModels(List<ArticleWithSubArticlesModel> articleWithSubArticlesModels) {
        this.articleWithSubArticlesModels = articleWithSubArticlesModels;
    }

    public Map<String, ArticleType> getSubArticleIdToArticle() {
        return subArticleIdToArticle;
    }

    public void setSubArticleIdToArticle(Map<String, ArticleType> subArticleIdToArticle) {
        this.subArticleIdToArticle = subArticleIdToArticle;
    }
}
