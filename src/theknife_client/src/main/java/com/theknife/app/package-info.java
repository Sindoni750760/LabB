/**
 * Package principale del client TheKnife.
 * Gestisce lifecycle JavaFX, routing tra schermate e comunicazione TCP con il server.
 *
 * <h2>Componenti principali</h2>
 *
 * <ul>
 *     <li>{@link com.theknife.app.App} — entrypoint JavaFX</li>
 *     <li>{@link com.theknife.app.Main} — entrypoint CLI</li>
 *     <li>{@link com.theknife.app.SceneManager} — navigazione scene</li>
 *     <li>{@link com.theknife.app.Communicator} — comunicazione socket</li>
 *     <li>{@link com.theknife.app.User} — info utente loggato</li>
 * </ul>
 *
 * <h2>Responsabilità</h2>
 * <ul>
 *     <li>Mostrare le UI richieste</li>
 *     <li>Inviare istruzioni al server</li>
 *     <li>Validare input lato client</li>
 *     <li>Gestire stato sessione</li>
 * </ul>
 */
package com.theknife.app;
