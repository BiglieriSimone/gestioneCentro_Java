package model;

/**
 * Rappresenta i possibili stati di un tecnico nel centro di manutenzione.
 */
public enum StatoTecnico {
    /** Tecnico libero da incarichi*/
    LIBERO, 
    /** Tecnico occupato in un incarico */
    OCCUPATO, 
    /**Tecnico non disponibile */
    ASSENTE 
}
