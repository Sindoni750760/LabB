package com.theknife.app.Server;

public interface QueryResponse {

    boolean canRespond(int userId, int reviewId) throws Exception;

    String getResponse(int reviewId) throws Exception;

    boolean addResponse(int reviewId, String text) throws Exception;

    boolean editResponse(int reviewId, String text) throws Exception;

    boolean removeResponse(int reviewId) throws Exception;
}
