package model;

/**
 * Rappresenta i possibili stati di un intervento nel centro di manutenzione.
 */
public enum StatoIntervento {
    /** Incarico preso in carica */
    IN_ATTESA, 
    /** Incarico completato */
    COMPLETATO, 
    /** Iniziate le lavorazioni */
    IN_LAVORAZIONE 
}