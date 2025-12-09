package com.theknife.app;

/**
 * Punto di ingresso dell'applicazione lato client.
 * <p>
 * Questa classe non contiene logica di inizializzazione diretta, ma si limita
 * ad inoltrare l'esecuzione al metodo {@link App#main(String[])}, che si occupa di:
 * </p>
 * <ul>
 *     <li>avviare il runtime JavaFX</li>
 *     <li>inizializzare la connessione con il server</li>
 *     <li>creare e verificare il file di configurazione</li>
 *     <li>caricare la scena iniziale tramite {@link com.theknife.app.SceneManager}</li>
 * </ul>
 *
 * <p>Questa classe funge principalmente da entry point standard per JVM,
 * necessaria affinché l'applicazione possa essere eseguita tramite:</p>
 * <ul>
 *     <li>console</li>
 *     <li>IDE (es. IntelliJ, Eclipse, VSCode)</li>
 *     <li>build tool con esecuzione main-class</li>
 * </ul>
 *
 * @author Mattia Sindoni 750760 VA
 * @author Erica Faccio 751654 VA
 * @author Giovanni Isgrò 753536 VA
 */
public class Main {

    /**
     * Metodo principale dell'applicazione.
     * <p>Delegato unicamente alla chiamata di {@link App#main(String[])}, che gestisce
     * l'effettiva inizializzazione dell'interfaccia JavaFX e delle componenti interne.</p>
     *
     * @param args argomenti passati da riga di comando (non utilizzati dall'applicazione)
     */
    public static void main(String[] args) {
        App.main(args);
    }
}
