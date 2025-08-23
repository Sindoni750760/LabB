CREATE TABLE utenti (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cognome VARCHAR(100) NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password CHAR(60) NOT NULL,
    data_nascita DATE,
    --luogo_domicilio,
    is_ristoratore BOOLEAN NOT NULL
);

CREATE TABLE "RistorantiTheKnife" (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    nazione VARCHAR(100) NOT NULL,
    citta VARCHAR(100) NOT NULL,
    indirizzo VARCHAR(100) NOT NULL,
    latitudine DOUBLE PRECISION NOT NULL,
    longitudine DOUBLE PRECISION NOT NULL,
    fascia_prezzo INTEGER NOT NULL,
    servizio_delivery BOOLEAN NOT NULL,
    prenotazione_online BOOLEAN NOT NULL,
    --tipo_cucina,
    proprietario INTEGER REFERENCES utenti(id)
);

CREATE TABLE recensioni (
    id SERIAL PRIMARY KEY,
    id_utente INTEGER REFERENCES utenti(id),
    id_ristorante INTEGER REFERENCES "RistorantiTheKnife"(id),
    stelle INTEGER NOT NULL,
    testo VARCHAR(255) NOT NULL
);

CREATE TABLE risposte (
    id_recensione INTEGER REFERENCES recensioni(id) PRIMARY KEY,
    testo VARCHAR(255) NOT NULL
);

CREATE TABLE preferiti (
    id_utente INTEGER REFERENCES utenti(id),
    id_ristorante INTEGER REFERENCES "RistorantiTheKnife"(id)
)