package model;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Rappresenta un intervento di manutenzione.
 * Implementa Serializable per consentire il salvataggio persistente su file
 * Contiene i dettagli di un intervento e il tecnico assegnato
 */
public class Intervento implements Serializable {

    // attributi per incapsulare le informazioni dell'intervento
    /** codice dell'intervento ("es. I0001") */
    private String codice;
    /** descrizione di un intervento */
    private String descrizione;
    /** data di inizio di un intervento */
    private LocalDate dataApertura;
    /** durata di un intervento stimata*/
    private double stimaDurata;
    /** tecnico associato all'intervento */
    private Tecnico tecnicoAssociato;
    /** stato dell'intervento (In_Attesa, In_Lavorazione, Completato) */
    private StatoIntervento stato;


    /**
     * Costruttore della classe Intervento:
     * @param codice Codice dell'intervento
     * @param descrizione Dettaglio della manutenzione
     * @param stimaDurata durata prevista in ore (>0).
     * @param tecnico Tecnico assegnato
     */
    public Intervento(String codice, String descrizione, double stimaDurata, Tecnico tecnico){
        setCodice(codice);
        setDescrizione(descrizione);
        setStimaDurata(stimaDurata);

        //un intervento deve avere un tecnico associato.
        setTecnicoAssociato(tecnico);

        //Default
        this.dataApertura = LocalDate.now(); //.plusDays(1) per verificare se lo aggiorna al giorno dopo
        this.stato = StatoIntervento.IN_ATTESA;
    }

    /**
     * Ritorna l'ID di un intervento
     * @return codice dell'intervento
     */
    public String getCodice(){return codice;}

    /**
     * imposta il codice verificando che il prefisso sia conforme
     * @param codice identificativo dell'intervento
     */
    public void setCodice(String codice){
        if (codice == null || codice.trim().isEmpty())throw new IllegalArgumentException("Il codice ID non può essere vuoto.");
        if(!codice.startsWith("I")) throw new IllegalArgumentException("Formato codice intervento non valido");
        this.codice = codice;
    }

    /**
     * Ritorna la descrizione di un intervento
     * @return descrizione dell'intervento
     */
    public String getDescrizione(){return descrizione;}

    /**
     * Aggiorna la descrizione controllando che non ci siano campi vuoti
     * @param descrizione descrizione dell'intervento
     */
    public void setDescrizione(String descrizione){
        if (descrizione == null || descrizione.trim().isEmpty()){
            throw new IllegalArgumentException("La descrizione non può essere vuota.");
        }
        this.descrizione = descrizione;
    }


    /**
     * Ritorna la durata stimata dell'intervento
     * @return durata stimata dell'intervento
     */
    public double getStimaDurata(){return stimaDurata;}

    /**
     * Imposta la durata prevista verificando che il tempo inserito sia non negativo
     * @param stimaDurata durata prevista per completare l'incarico
     */
    public void setStimaDurata(double stimaDurata){
        if (stimaDurata <= 0){
            throw new IllegalArgumentException("La durata stimata deve essere maggiore di 0 minuti");
        }
        this.stimaDurata = stimaDurata;
    }

    /**
     * Ritorna il tecnico associato all'intervento
     * @return tecnico associato all'intervento
     */
    public Tecnico getTecnicoAssociato(){return tecnicoAssociato;}

    
    /**
     * Associa un tecnico all'intervento
     * il tecnico deve essere nello stato libero per poter essere assegnato
     * Valido anche per riassegnamenti
     * @param nuovoTecnico associa un nuovo tecnico all'intervento
     */
    public void setTecnicoAssociato(Tecnico nuovoTecnico) {
        if (nuovoTecnico == null) throw new IllegalArgumentException("Tecnico nullo");

        // se il tecnico è lo stesso, non fare niente
        if (this.tecnicoAssociato == nuovoTecnico) {
            return; 
        }

        // se è diverso, libera il vecchio e occupa il nuovo
        if (this.tecnicoAssociato != null) {
            this.tecnicoAssociato.libera();
        }

        if (!nuovoTecnico.isDisponibile()) {
            throw new IllegalStateException("Tecnico non disponibile");
        }

        this.tecnicoAssociato = nuovoTecnico;
        nuovoTecnico.assegnaIntervento(this);
    }   

    /**
     * Ritorna lo stato del tecnico associato
     * @return ritorna lo stato del tecnico associato
     */
    public StatoIntervento getStato(){return stato;}

    /**
     * imposta lo stato dell'intervento in base alle operazioni presenti.
     * @param nuovoStato nuovo stato dell'intervento (in attesa, completato, in lavorazione)
     */
    public void setStato(StatoIntervento nuovoStato){
        if(nuovoStato == null) return;

        // se passiamo a COMPLETATO, libera il tecnico associato facendolo tornare libero
        if(nuovoStato == StatoIntervento.COMPLETATO && this.stato != StatoIntervento.COMPLETATO){
            if(this.tecnicoAssociato != null){
                //Libera il tecnico rendendolo libero e null  ma mantiene il riferimento dell'incarico per lo storico
                this.tecnicoAssociato.libera();
            }
        }
        this.stato = nuovoStato;
    }

    /**
     * Ritorna la data di inizio lavorazione dell'intervento
     * @return data di inizio lavorazione dell'intervento in formato YYYY/MM/DD
     */
    public LocalDate getDataApertura(){ return dataApertura;}

    /**
     * Calcola il costo stimato sulla base delle ore stimate e paga oraria del tecnico interno o esterno
     * @return costo totale del preventivo calcolato a runtime oppure 0.0 se non c'è personale associato
     */
    public double calcolaPreventivo(){
        if(tecnicoAssociato == null) return 0.0;
        return tecnicoAssociato.calcolaCosto(this.stimaDurata);
    }
    
    /**
     * Forza la chiusura dell'intervento e libera il tecnico dall'incarico
     * ma mantiene il suo storico
     */
    public void completato(){
        if(this.stato == StatoIntervento.COMPLETATO) return;
        this.stato = StatoIntervento.COMPLETATO;
        if(tecnicoAssociato != null){
            this.tecnicoAssociato.libera(); //imposta il tecnico a LIBERO
        }
    }
    
    /**Permette di scollegare un tecnico dall'intervento assegnato, ma l'intervento rimane in IN_ATTESA */
     //alternativa: mantenere lo stato intervento IN_LAVORAZIONE aprendo un dialogo che chiede di selezionare
     //un altro tecnico libero
    public void scollegaTecnico(){
        this.tecnicoAssociato = null;
        this.stato = StatoIntervento.IN_ATTESA;
    }

    /** Fornisce una stringa di base dei dati dell'intervento */
    @Override
    public String toString(){
        String nomeTec = (tecnicoAssociato != null) ? tecnicoAssociato.getCognome() : "Non assegnato";
        return "Intervento: " +codice+ ", "+descrizione+", Stato: "+ stato+", Tecnico Associato: "+nomeTec;
    }
}
