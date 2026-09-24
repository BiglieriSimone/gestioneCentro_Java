package model;

/**
 * Rappresenta un tecnico dipendente esterno al centro
 * Estende la classe astratta Tecnico
 * La paga oraria varia in base al tecnico
 */
public class TecnicoEsterno extends Tecnico {
    /** Attributo che memorizza il costo orario di un tecnico*/
    private double costoOrario;


    /**
     * Costruttore della classe TecnicoEsterno
     * richiama la superclasse e assegna il costo orario definito
     * @param codice ID del tecnico (formato "T...")
     * @param nome Nome del tecnico
     * @param cognome Cognome del tecnico
     * @param costoOrario costo orario (€/h) applicato dal tecnico
     */
    public TecnicoEsterno(String codice, String nome, String cognome, double costoOrario){
        super(codice, nome, cognome);
        setCostoOrario(costoOrario);
    }
    
    /**
     * ritorna il costo orario del tecnico
     * @return costo orario del tecnico
     */
    public double getCostoOrario(){return costoOrario;}

    /**
     * Imposta il costo orario del tecnico esterno
     * @param costoOrario costo orario variabile del tecnico
     */
    public void setCostoOrario(double costoOrario){
        if(costoOrario < 0){
            throw new IllegalArgumentException("Il costo orario non può essere negativo.");
        }
        this.costoOrario = costoOrario;
    }

    /**
     * Polimorfismo: il costo dipende dalla durata
     * @param oreStimate numero ore necessarie al completamento dell'intervento
     * @return costo totale (in base al costo orario)
     */
    @Override
    public double calcolaCosto(double oreStimate){
        return costoOrario * oreStimate;
    }

    /**
     * Ritorna una stringa personalizzata che verrà sfruttata
     * dalla comboBox nella vista per mostrare le informazioni dei tecnici
     */
    @Override
    public String toString() {
        // Sfrutta il toString() della classe base e aggiunge la paga oraria specifica di questo professionista
        return super.toString() + ". costo: " +this.costoOrario + " €/h";
    }
}
