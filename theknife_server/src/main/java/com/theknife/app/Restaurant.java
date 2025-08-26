package com.theknife.app;

import java.sql.SQLException;

public class Restaurant {
    //function used to get restaurants info using filters
    public static String[][] getRestaurantsWithFilter(int page, String latitude_string, String longitude_string, String range_km_string, String price_min_string, String price_max_string, boolean has_delivery, boolean has_online, String stars_min_string, String stars_max_string, int favourite_id, String category, int near_who) throws SQLException {
        double latitude, longitude, range_km;
        if(latitude_string.equals("-") && longitude_string.equals("-"))
            latitude = longitude = -1;
        else try {
            latitude = Double.parseDouble(latitude_string);
            longitude = Double.parseDouble(longitude_string);
        } catch(NumberFormatException e) {
            return new String[][]{{"error","coordinates"}};
        }

        if(range_km_string.equals("-"))
            range_km = -1;
        else try {
            range_km = Double.parseDouble(range_km_string);
        } catch(NumberFormatException e) {
            return new String[][]{{"error","coordinates"}};
        }

        if(near_who > 0) {
            double[] user_coordinates = DBHandler.getUserPosition(near_who);
            if(user_coordinates == null)
                return new String[][]{{"error","unauthorized"}};
            latitude = user_coordinates[0];
            longitude = user_coordinates[1];
        }

        //everything defined or everything undefined, else error in format
        if((latitude != -1 || longitude != -1 || range_km != -1) &&
        (latitude == -1 || longitude == -1 || range_km == -1)) {
            return new String[][]{{"error","coordinates"}};
        }

        int price_min, price_max;
        if(price_min_string.equals("-"))
            price_min = -1;
        else try {
            price_min = Integer.parseInt(price_min_string);
        } catch(NumberFormatException e) {
            return new String[][]{{"error","price"}};
        }
        if(price_max_string.equals("-"))
            price_max = -1;
        else try {
            price_max = Integer.parseInt(price_max_string);
        } catch(NumberFormatException e) {
            return new String[][]{{"error","price"}};
        }

        double stars_min, stars_max;
        if(stars_min_string.equals("-"))
            stars_min = -1;
        else try {
            stars_min = Double.parseDouble(stars_min_string);
        } catch(NumberFormatException e) {
            return new String[][]{{"error","stars"}};
        }
        if(stars_max_string.equals("-"))
            stars_max = -1;
        else try {
            stars_max = Double.parseDouble(stars_max_string);
        } catch(NumberFormatException e) {
            return new String[][]{{"error","stars"}};
        }

        return DBHandler.getRestaurantsWithFilter(page, latitude, longitude, range_km, price_min, price_max, has_delivery, has_online, stars_min, stars_max, favourite_id, category);
    }
}