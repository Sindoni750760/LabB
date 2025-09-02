package com.theknife.app;

import java.sql.SQLException;

/**
 * Classe utility per la gestione delle operazioni relative ai ristoranti.
 * Contiene metodi per recuperare informazioni filtrate sui ristoranti,
 * validando i parametri ricevuti prima di interrogare il database.
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class Restaurant {
        /**
     * Recupera un elenco di ristoranti filtrati in base ai parametri specificati.
     * Valida i dati ricevuti (coordinate, prezzo, stelle, ecc.) e delega la query al {@link DBHandler}.
     * Restituisce un array bidimensionale di stringhe con i risultati o eventuali codici di errore.
     *
     * @param page numero della pagina da visualizzare
     * @param latitude_string latitudine come stringa, oppure "-" se non definita
     * @param longitude_string longitudine come stringa, oppure "-" se non definita
     * @param range_km_string raggio di ricerca in km come stringa, oppure "-"
     * @param price_min_string prezzo minimo come stringa, oppure "-"
     * @param price_max_string prezzo massimo come stringa, oppure "-"
     * @param has_delivery {@code true} se si desidera filtrare per ristoranti con consegna
     * @param has_online {@code true} se si desidera filtrare per ristoranti con prenotazione online
     * @param stars_min_string valutazione minima in stelle come stringa, oppure "-"
     * @param stars_max_string valutazione massima in stelle come stringa, oppure "-"
     * @param favourite_id ID dell'utente per filtrare i preferiti, oppure 0 se non applicabile
     * @param category categoria da filtrare, oppure {@code null} se non definita
     * @param near_who ID utente per usare la sua posizione come centro di ricerca, oppure 0
     * @return array bidimensionale di stringhe con i risultati, oppure array con errore formattato
     * @throws SQLException se si verifica un errore durante la query al database
     */
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