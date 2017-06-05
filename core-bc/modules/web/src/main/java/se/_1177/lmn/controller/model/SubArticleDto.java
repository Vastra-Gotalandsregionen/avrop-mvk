package se._1177.lmn.controller.model;

public class SubArticleDto {
    private String name;
    private int orderCount;
    private String articleNo;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public void setArticleNo(String articleNo) {
        this.articleNo = articleNo;
    }

    public String getArticleNo() {
        return articleNo;
    }
}