# Laboratorio Interdisciplinare B
Progetto Java svolto per il Laboratorio interdisciplinare composto da due moduli:
- theknife_client: interfaccia utente
- theknife_server: backend e gestione dei dati

## Struttura del progetto
- LabB/
- pom.xml                                                      <- POM aggregatore del client e server
- src/
- theknife_client/                                      <- Path del client
- theknife_server/                                      <- Path del server
- src/main/resources/init-db.sql    <- Script SQL per l'inizializzazione del DB

## Prerequisit
- Java 17 o superiore;
- Maven 3.8 +
- PostgreSQL installato e attivo sulla porta 5432
- Utente PostgreSQL: postgres
- Password: da inserire in base al vostro account
- Database: verrà creato automaticamente col nome 'theknife'

## Installazione e compilazione
1. Clona la repository:
   git clone https://github.com/Sindoni750760/LabB.git
2. Entra nella directory del progetto:
   cd LabB
3. Compila e installa tutti i moduli
   mvn clean install
4. Genera la documentazione Java (JavaDoc):
   mvn javadoc:javadoc
## Setup del Database
Il progetto include uno script SQL per la creazione automatica del database e delle relative tabelle
Per eseguire il setup:
1. Andare sulla cartella principale dove si è scaricata la cartella 'LabB' ed entrarci
2. Una volta dentro, imposta la variabile d'ambiente per la password:
   (solo per powershell)
   $env:PGPASSWORD = "La_tua_password"
3. Esegui il profilo Maven dedicato
   mvn -P init-db validate
Attraverso questo comando:
- Viene creato il database 'theknife', qualora non esistesse
- Esegue lo script 'init-db.sql per creare tutte le tabelle necessarie

### Note:
- Lo script SQL si trova in src/theknife_server/src/main/resources/init-db.sql

## Moduli singoli
per poter compilare un singolo modulo, ricorrere ai seguenti comandi:
- Client
  mvn -pl src/theknife_client install
- Server
  mvn -pl src/theknife_server install

# Contatti del team di sviluppo
- Autore: Mattia Sindoni
  Email: mattia.sindoni@studenti.uninsubria.it
- Autore: Erica Faccio
  Email: erica.faccio@studenti.uninsubria.it
- Autore: Giovanni
  Email: giovanni.isgrò@studenti.uninsubria.it
