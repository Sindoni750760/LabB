package com.theknife.app.Server;

public interface QueryFavourite {

    boolean isFavourite(int userId, int restId) throws Exception;

    boolean addFavourite(int userId, int restId) throws Exception;

    boolean removeFavourite(int userId, int restId) throws Exception;
}
