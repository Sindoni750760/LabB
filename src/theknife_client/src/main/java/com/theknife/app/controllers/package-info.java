/**
 * Package contenente i controller JavaFX per la GUI del client.
 * Ogni classe mappa una specifica schermata FXML e gestisce input/output utente.
 *
 * <h2>Responsabilità dei controller</h2>
 * <ul>
 *     <li>Raccogliere dati dalla UI</li>
 *     <li>Validare input</li>
 *     <li>Effettuare richieste al server</li>
 *     <li>Aggiornare la UI al risultato ottenuto</li>
 * </ul>
 *
 * <h2>Esempi di schermate</h2>
 * <ul>
 *     <li>LoginController</li>
 *     <li>RegisterController</li>
 *     <li>ViewRestaurants</li>
 *     <li>ViewRestaurantInfo</li>
 *     <li>MyReviews</li>
 *     <li>WriteReview</li>
 * </ul>
 *
 * Inoltre ogni controller implementa l’interfaccia {@code OnlineChecker}
 * per reagire alla perdita di connessione.
 */
package com.theknife.app.controllers;
