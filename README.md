# Gestione Centro (Java)

Applicazione per la gestione di un centro:
Questo progetto mira all'implementazione di uno strumento per la gestione degli interventi di manutenzione.
In particolare ci si pone lo scopo di permettere a un utente di un centro di manutenzione di visualizzare
gli interventi e lo stato dei tecnici.

Il presente progetto si propone di descrivere e sviluppare una applicazone Java che abbia le seguenti funzionalità:

1) Gestione degli interventi;
2) Gestione dei tecnici del centro;
3) Visualizzazione degli interventi e dei tecnici;
4) Salvataggio e caricamento delle informazioni;
5) Stampa della tabella degli interventi e della tabella dei tecnici.

## Gestione degli interventi
La gestione degli interventi prevede di mantenere le informazioni sugli interventi, sia quelli in corso sia quelli
chiusi. In particolare, per ogni intervento vanno mantenute le seguenti informazioni:
• Codice identificativo dell’intervento
• Descrizione del guasto o della manutenzione
• Data di apertura dell’intervento (di default quella odierna)
• Durata stimata dell’intervento in ore
• Tecnico associato
• Stato (in attesa, in lavorazione, completato)
L’utente deve avere la possibilità di aggiungere, modificare e cancellare un intervento. Si predispongano
appropriati controlli per garantire la correttezza dei dati inseriti.
Quando viene aggiunto un intervento deve essere assegnato un tecnico; l’applicazione deve mostrare
all’utente l’elenco di tutti i tecnici liberi con il relativo costo (vedi dopo). L’utente può scegliere il tecnico che
ritiene più adatto. Anche in caso di modifica di un intervento, l’utente deve avere la possibilità di scegliere
un altro tecnico da un elenco.

## Gestione dei tecnici
La gestione dei tecnici prevede di mantenere le informazioni sui tecnici. In particolare, per ogni tecnico vanno
mantenute le seguenti informazioni:
• Codice identificativo del tecnico
• Nome e cognome
• Tipo (interno, esterno)
• Stato (libero, occupato, assente)
• Intervento associato
• Costo
Si devono prevedere due tipologie di tecnici:
• Tecnico interno: caratterizzato da un costo fisso.
• Tecnico esterno (Freelance): caratterizzato da un costo orario per la trasferta.

## Visualizzazione degli interventi e dei tecnici
Le informazioni relative agli interventi e ai tecnici devono essere visualizzate in formato tabellare. L'utente
deve poter visualizzare le informazioni di tutti gli interventi. L'utente deve poter visualizzare l'elenco dei
tecnici e il loro carico di lavoro corrente.
Si implementi la possibilità di filtrare la tabella degli interventi in base al tecnico
assegnato o allo stato dell'intervento

## Salvataggio e caricamento delle prenotazioni
L’utente deve avere la possibilità di salvare in maniera persistente le informazioni (tecnici e interventi) su file
e ricaricarle all'avvio o su richiesta.
3
Nel caso in cui si tenti di salvare su un file esistente, chiedere conferma per la
sovrascrittura
Implementare un thread per il salvataggio automatico periodico in un file temporaneo

## Stampa della tabella degli interventi:
Si dia all’utente la possibilità di stampare la tabella con gli interventi. Si sfruttino le classi
di libreria Java per stampare tramite una delle stampanti configurate dal sistema operativo
