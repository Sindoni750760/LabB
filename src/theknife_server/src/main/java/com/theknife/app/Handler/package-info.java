/**
 * Package responsabile della gestione dei comandi provenienti dal client,
 * instradati tramite {@link com.theknife.app.ClientThread}.
 *
 * Ogni handler implementa {@link com.theknife.app.Handler.CommandHandler} e
 * gestisce un gruppo specifico di comandi.
 *
 * <h2>Handler presenti</h2>
 * <ul>
 *     <li>{@link com.theknife.app.Handler.AuthHandler} — login/registrazione</li>
 *     <li>{@link com.theknife.app.Handler.RestaurantHandler} — gestione ristoranti</li>
 *     <li>{@link com.theknife.app.Handler.DisconnectHandler} — logout/disconnessione</li>
 * </ul>
 *
 * <h2>Responsabilità</h2>
 * <ul>
 *     <li>Lettura, validazione e risposta ai comandi</li>
 *     <li>Utilizzo dei servizi del DB</li>
 *     <li>Invio feedback al Client tramite ClientContext</li>
 * </ul>
 *
 * Questo package rappresenta il punto centrale tra protocollo TCP e logica applicativa.
 */
package com.theknife.app.Handler;
