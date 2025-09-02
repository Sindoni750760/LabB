package com.theknife.app;

/**
 * Classe di avvio dell'applicazione.
 * Delega l'esecuzione al metodo {@code main} della classe {@link App},
 * che gestisce l'inizializzazione JavaFX e la configurazione dell'app.
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class Main {
    /**
     * Metodo principale dell'applicazione.
     * Avvia l'applicazione JavaFX tramite {@link App}.
     *
     * @param args argomenti da riga di comando (non utilizzati)
     */
    public static void main(String[] args) {
        App.main(args);
    }
}