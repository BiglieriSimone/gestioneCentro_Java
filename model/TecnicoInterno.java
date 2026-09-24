package model;
/**
 * Rappresenta un tecnico dipendente interno al centro
 * Estende la classe astratta Tecnico
 * la paga oraria è fissa
 */
public class TecnicoInterno extends Tecnico {
    /** la paga oraria del tecnico interno è fissa */
    private static double PAGA_ORARIA = 25.0;

    /**
     * Costruttore della classe: 
     * passa gli attributi base del tecnico.
     * @param codice identificativo del tecnico
     * @param nome nome del tecnico
     * @param cognome cognome del tecnico
     */
    public TecnicoInterno(String codice, String nome, String cognome){
        super(codice,nome,cognome);
    }

    /**
     * Metodo get che fornisce l'accesso alla paga oraria di un tecnico interno
     * @return ritorna la paga oraria fissa
     */
    public static double getCostoFisso(){return PAGA_ORARIA;}
    
    /**
     * Calcola il costo complessivo dell'intervento per i tecnici interni
     * Polimorfismo: il costo non dipende dalla durata di manutenzione, ma è costante
     * @param oreStimate numero ore necessarie al completamento dell'intervento
     * @return costo totale (in base al costo orario fisso)
    */
    @Override
    public double calcolaCosto(double oreStimate){
       return oreStimate * PAGA_ORARIA;
    }

    /**
     * Ritorna una stringa personalizzata che verrà sfruttata
     * dalla comboBox nella vista per mostrare le informazioni dei tecnici
     */
    @Override
    public String toString() {
        // Sfrutta il toString() della classe base e aggiunge la paga oraria specifica di questo tecnico
        return super.toString() + ". costo: " +getCostoFisso()+ " €/h";
    }
}

