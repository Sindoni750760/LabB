package com.theknife.app.Server;

public interface QueryReview {

    int getReviewsPageCount(int restId) throws Exception;

    String[][] getReviews(int restId, int page) throws Exception;

    String[] getMyReview(int userId, int restId) throws Exception;

    boolean addReview(int userId, int restId, int stars, String text) throws Exception;

    boolean editReview(int userId, int restId, int stars, String text) throws Exception;

    boolean removeReview(int userId, int restId) throws Exception;
}
