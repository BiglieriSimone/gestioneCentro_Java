package logic;
import java.io.Serializable;
import java.util.*;
import model.*;

/**
 * Classe centrale che rappresenta il controller.
 * Implementa Serializable per permettere il salvataggio totale dello stato su file.
 * Coordina le interazioni tra i modelli dei tecnici e interventi.
 */
public class GestioneCentro implements Serializable{

    //Strutture dati generiche incapsulate per la memorizzazione dei record
    /** struttura dati generica che memorizza i record dei tecnici, gestisce i tecnici e i suoi attributi */
    private GestioneDati<Tecnico> gestioneTecnici;  //Gestione dei tecnici
    /** struttura dati generica che memorizza i record degli interventi, gestisce gli interventi e i suoi attributi */
    private GestioneDati<Intervento> gestioneInterventi; //Gestione degli interventi

    /** rileva ultimo id del tecnico per aggiornamento */
    private int ultimoIdTecnico = 0;
    /** rileva ultimo id dell'intervento per aggiornamento */
    private int ultimoIdIntervento = 0;

    /**
     * Costruttore della classe:
     * Inizializza i contenitori generici per la gestione dei dati
     */
    public GestioneCentro(){
        this.gestioneTecnici = new GestioneDati<>();
        this.gestioneInterventi = new GestioneDati<>();
    }

    /**
     * VISUALIZZA:
     * Restituisce l'ID che verrà asseggnato al prossimo tecnico.
     * Non altera lo stato del sistema nè incrementa il contatore interno,
     * in quanto viene utilizzato nel costruttore del dialogo per tecnici per compilare da solo il campo in sola lettura.
     * @return Stringa formattata del prossimo ID
     */
    public String visualizzaProssimoIdTecnico() {
        //String.format("%04d", ...) aggiunge gli zeri di riempimento a sinistra fino a 4 cifre
        return "T" + String.format("%04d", ultimoIdTecnico + 1);
    }

    /**
     * CONFERMA:
     * incrementa il contatore interno e restituisce l'ID definitivo.
     * è marcato come "synchronized" per evitare che due thread creino contemporaneamente un tecnico con lo stesso ID
     * @return nuovo identificativo
     */
    public synchronized String generaProssimoIdTecnico() {
        ultimoIdTecnico++; // Incremento reale
        return "T" + String.format("%04d", ultimoIdTecnico);
    }


    /**
     * VISUALIZZA:
     * Restituisce l'ID che verrà asseggnato al prossimo tecnico.
     * Non altera lo stato del sistema nè incrementa il contatore interno,
     * in quanto viene utilizzato nel costruttore del dialogo per interventi per compilare da solo il campo in sola lettura.
     * @return Stringa formattata del prossimo ID
     */
    public String visualizzaProssimoIdIntervento() {
        return "I" + String.format("%04d", ultimoIdIntervento + 1);
    }

    /**
     * CONFERMA:
     * incrementa il contatore interno e restituisce l'ID definitivo.
     * è marcato come "synchronized" per evitare che due thread creino contemporaneamente un tecnico con lo stesso ID
     * @return nuovo identificativo
     */
    public synchronized String generaProssimoIdIntervento() {
        ultimoIdIntervento++; // Incremento reale
        return "I" + String.format("%04d", ultimoIdIntervento);
    }

    /**
     * Sincronizza lo stato corrente sostituendo i dati in memoria con quelli letti da un file di salvataggio
     * è sincronizzato per bloccare l'interfaccia durante il ripristino
     * @param sorgente nuova istanza di GestioneCentro
     */
    public synchronized void importaDati(GestioneCentro sorgente) {
        if (sorgente != null) {

            //prima di caricare, bisogna fare spazio, se non si pulisse prima, la lista verrebbe moltiplicata
            this.gestioneTecnici.pulisci();
            this.gestioneInterventi.pulisci();
            
            // Re-inseriamo usando i metodi per garantire i controlli
            for (Tecnico t : sorgente.getGestioneTecnici().getElementi()) {
                this.gestioneTecnici.aggiungi(t);
            }
            for (Intervento i : sorgente.getGestioneInterventi().getElementi()) {
                this.gestioneInterventi.aggiungi(i);
            }
            /**Allinea i contatori ai dati caricati */
            this.ultimoIdIntervento = sorgente.ultimoIdIntervento;
            this.ultimoIdTecnico = sorgente.ultimoIdTecnico;
        }
    }

    /**
     * Registra un intervento nel sistema
     * Verifica che non sia un duplicato e controlla che il codice non sia già esistente
     * @param nuovo nuovo record di intervento da inserire in lista
     */

    public synchronized void registraNuovoIntervento(Intervento nuovo){
        //Scansiono la lista per verificare che il codice non esista già tra gli interventi
        for(Intervento i : gestioneInterventi.getElementi()){
            if(i.getCodice().equals(nuovo.getCodice())){
                throw new IllegalArgumentException("Esiste già un intervento con codice "+nuovo.getCodice());
            }
        }
        //Se il ciclo finisce senza trovare un codice uguale, aggiungo l'intervento
        gestioneInterventi.aggiungi(nuovo);
    }

    /**
     * Registra un tecnico nel sistema
     * Verifica che non sia un duplicato e controlla che il codice non sia già esistente
     * @param nuovo nuovo record di tecnico da inserire in lista
     */
    public synchronized void registraNuovoTecnico(Tecnico nuovo){
        //Scansiono la lista per verificare che l'ID non esista già tra i tecnici
        for(Tecnico t : gestioneTecnici.getElementi()){
            if(t.getId().equals(nuovo.getId())){
                throw new IllegalArgumentException("Esiste già un tecnico con ID "+nuovo.getId());
            }
        }
        //Se il ciclo finisce senza trovare un codice uguale, aggiungi il tecnico
        gestioneTecnici.aggiungi(nuovo);
    }

    /**
     * Filtra i tecnici liberi.
     * @return una lista di tecnici il cui stato è pari a LIBERO
     */
    public List<Tecnico> getTecniciLiberi(){
        // NON NECESSITA DI SYNCHRONIZED POICHE' LEGGE LE COPIE IMMUTABILI RESTITUITE DA GESTIONEDATI
        List<Tecnico> liberi = new ArrayList<>();
        for (Tecnico t : gestioneTecnici.getElementi()){
            if(t.getStato() == StatoTecnico.LIBERO){
                liberi.add(t);
            }
        }
        return liberi;
    }

    /**
     * Rimuove un intervento dal sistema e libera il tecnico se era ancora occupato.
     * @param i oggetto Intervento da eliminare
     */
    public synchronized void cancellaIntervento(Intervento i){
        if(i.getStato() != StatoIntervento.COMPLETATO && i.getTecnicoAssociato() != null){
            i.getTecnicoAssociato().libera();
        }
        gestioneInterventi.rimuovi(i);
    }
    
    /**Gestione dello stato del tecnico da libero ad assene e viceversa
     * @param t tecnico a cui viene cambiato lo stato
     */
    public synchronized void commutaAssenzaTecnico(Tecnico t) {
        // Se è ASSENTE, lo riportiamo a LIBERO
        if (t.getStato() == StatoTecnico.ASSENTE) {
            t.setStato(StatoTecnico.LIBERO);
            return;
        }

        // Se è OCCUPATO, gestiamo il distacco dall'intervento
        if (t.getStato() == StatoTecnico.OCCUPATO) {
            Intervento inter = t.getInterventoAssociato();
            if (inter != null) {
                inter.scollegaTecnico(); 
            }
        }
        // pulisce anagrafica tecnico e rende libero
        t.libera(); 
        //cambiamo lo stato in ASSENTE
        t.setStato(StatoTecnico.ASSENTE);
    }

    
    /**
     * Esegue la modifica dei dati di un intervento esistente
     * @param originale istanza dell'intervento già esistente da aggiornare
     * @param nuovaDesc nuovo testo 
     * @param nuovaDurata nuovo valore stimato della durata
     * @param nuovoTecnico nuovo tecnico da associare
     */
    public synchronized void aggiornaIntervento(Intervento originale, String nuovaDesc, double nuovaDurata, Tecnico nuovoTecnico){
    //Aggiorna i dati
        originale.setDescrizione(nuovaDesc);
        originale.setStimaDurata(nuovaDurata);
        
        //Aggiorna il tecnico
        originale.setTecnicoAssociato(nuovoTecnico);
    }

    /**
     * Fornisce l'accesso ai dati dei tecnici
     * @return GestioneDati tipizzato per i dati dei tecnici
     */
    public GestioneDati<Tecnico> getGestioneTecnici(){return gestioneTecnici;}

    /**
     * Fornisce l'accesso ai dati degli interventi
     * @return GestioneDati tipizzato per l'elenco degli interventi
     */
    public GestioneDati<Intervento> getGestioneInterventi(){return gestioneInterventi;}
}
