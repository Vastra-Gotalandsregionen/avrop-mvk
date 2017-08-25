package se._1177.lmn.controller.model;

public class SubArticleDto {
    private String name;
    private int orderCount;
    private String articleNo;
    private String thumbnailUrl;
    private String imageUrl;

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

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}