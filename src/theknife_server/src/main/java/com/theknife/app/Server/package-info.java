/**
 * Package dedicato all'accesso strutturato al database del progetto TheKnife.
 * Contiene la stratificazione CRUD per la gestione delle principali entità:
 * <ul>
 *     <li>Utenti</li>
 *     <li>Ristoratori</li>
 *     <li>Ristoranti</li>
 *     <li>Recensioni e risposte</li>
 *     <li>Preferiti</li>
 * </ul>
 *
 * <h2>Architettura</h2>
 * I livelli sono così composti:
 *
 * <ol>
 *     <li>{@link com.theknife.app.Server.GenericCRUD}
 *         — funzioni DB comuni</li>
 *
 *     <li>{@link com.theknife.app.Server.UserCRUD}
 *         — operazioni sugli utenti</li>
 *
 *     <li>{@link com.theknife.app.Server.RestaurateurCRUD}
 *         — operazioni sui ristoranti dell’utente loggato</li>
 *
 *     <li>{@link com.theknife.app.Server.RestaurantCRUD}
 *         — filtro ristoranti, recensioni, risposte, preferiti</li>
 *
 *     <li>{@link com.theknife.app.Server.DBHandler}
 *         — punto di accesso centrale</li>
 * </ol>
 *
 * <h2>Scopo</h2>
 * Fornire un layer di accesso riusabile, pulito e thread-safe,
 * separando il protocollo TCP dalla logica SQL.
 */
package com.theknife.app.Server;
