package model;
import java.io.Serializable;

/**
    * Classe astratta che rappresenta un Tecnico.
    * Implementa Serializable per permettere il salvataggio su file e ripristino
    * dello stato del file su file binario
*/
public abstract class Tecnico implements Serializable {

    // Attributi privati per incapsulare i dati
    /** identificativo del tecnico */
    private String id;

    /** nome del tecnico*/
    private String nome;

    /** cognome del tecnico */
    private String cognome;

    /** stato del tecnico (Occupato, Libero, Assente) */
    private StatoTecnico stato; // stato attuale del tecnico

    /** Intervento associato se il tecnico è occupato */
    private Intervento interventoAssociato; // Associazione 1:1 in relazione all'intervento

/**
    * Costruttore per la classe Tecnico.
    * @param id Identificativo del tecnico.
    * @param nome Nome del tecnico.
    * @param cognome Cognome del tecnico.
    * @throws IllegalArgumentException se i parametri sono nulli o vuoti.
*/
    public Tecnico(String id, String nome, String cognome) {
        setId(id);
        setNome(nome);
        setCognome(cognome);

        // Default alla creazione
        setStato(StatoTecnico.LIBERO);
        setInterventoAssociato(null); 
    }

    /**
     * ritorna l'ID del tecnico
     * @return identificativo del tecnico
     */
    public String getId() { return id; }

    /**
     * Imposta l'ID del tecnico verificando che non sia nullo o vuoto.
     * @param id identificativo del tecnico
     */
    public void setId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID non può essere vuoto.");
        }
        if(!id.startsWith("T")){
            throw new IllegalArgumentException("Formato ID non valido");
        }
        this.id = id;
    };

    /**
     * Ritorna il nome del tecnico
     * @return nome del tecnico
     */
    public String getNome() { return nome; }

    /**
     * imposta nome del tecnico verificando che non sia nullo
     * @param nome nome del tecnico
     */
    public void setNome(String nome) {
        if (nome == null) {
            throw new IllegalArgumentException("Il nome non può essere vuoto.");
        }
        this.nome = nome;
    }

    /**
     * Ritorna il cognome del tecnico
     * @return cognome del tecnico
     */
    public String getCognome() { return cognome; }


    /**
     * Imposta cognome del tecnico verificando che non sia nullo
     * @param cognome cognome del tecnico
     */
    public void setCognome(String cognome) {
        if (cognome == null) {
            throw new IllegalArgumentException("Il cognome non può essere vuoto.");
        }
        this.cognome = cognome;
    }

    /**
     * Ritorna lo stato attuale del tecnico
     * @return lo stato attuale del tecnico
     */
    public StatoTecnico getStato() { return stato; }

    /**
     * Imposta lo stato del tecnico verificando che non sia nullo.
     * Controlla che il tecnico occupato in un intervento non possa essere segnato assente
     * @param stato stato del tecnico (Libero, Occupato, Assente)
     */
    public void setStato(StatoTecnico stato) {
        if (stato == null) {
            throw new IllegalArgumentException("Lo stato non può essere nullo. (occupato, libero, assente");
        }
        if(stato == StatoTecnico.ASSENTE){
            if(this.stato == StatoTecnico.OCCUPATO){
                throw new IllegalStateException("Impossibile segnare come assente un tecnico occupato in un intervento");
            }
            this.interventoAssociato = null;
        }
        this.stato = stato;
    }

    /**
     * Ritorna l'intervento associato al tecnico
     * @return intervento assegnato al tecnico, o null se il tecnico è libero/assente
     */
    public Intervento getInterventoAssociato() { return interventoAssociato; }

    /**
     * Associa un intervento al tecnico
     * @param intervento intervento da associare
     */
    public void setInterventoAssociato(Intervento intervento) { 
        this.interventoAssociato = intervento; 
    }

    /** Metodo polimorfico che le sottoclassi dovranno implementare separatamente */
    /**
     * Metodo polimorfico che le sottoclassi dovranno implementare separatamente
     * @param oreStimate ore stimate per il calcolo del costo preventivo
     * @return ritorna il costo preventivo per la manutenzione
     */
    public abstract double calcolaCosto(double oreStimate);
    

    /**
     * verifica se è disponibile il tecnico
     * @return vero se è disponibile, falso se non lo è
     */
    public boolean isDisponibile(){
        return stato == StatoTecnico.LIBERO;
    }

    /**
     * Se è libero, è possibile assegnare ad un tecnico un intervento
     * @param intervento intervento preso in carico
     */
    public void assegnaIntervento(Intervento intervento){
        if(intervento == null){
            throw new IllegalArgumentException("Intervento nullo (assegnagli un tecnico");
        }
        if(!isDisponibile()){
            throw new IllegalStateException("Tecnico non disponibile");
        }

        this.interventoAssociato = intervento;
        this.stato = StatoTecnico.OCCUPATO;
    }

    /**Libera un tecnico dall'incarico corrente */
    public void libera(){
        this.interventoAssociato = null;
        this.stato = StatoTecnico.LIBERO;
    }

    /** Fornisce una stringa di base dei dati del tecnico */
    @Override
    public String toString() {
        return nome + " " + cognome + " (" + id + ")";
    }
}