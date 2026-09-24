package view;
import javax.swing.table.*;
import logic.*;
import model.*;

/**
 * Modello che permette alla JTable di visualizzare la lista degli interventi
 * Implementa AbstractTableModel per collegare la lista alla vista
 */

public class InterventoTableModel extends AbstractTableModel {
/** Intestazioni delle colonne della tabella */
    private String[] nomiColonne = {"Codice","Data","Descrizione","Stato","Tecnico", "Costo Preventivo"};

    /** Riferimento alla lista dei dati gestita dal controller*/
    private GestioneDati<Intervento> listaInterventi;

    /**
     * Costruttore del modello
     * @param lista lista degli interventi da visualizzare nella tabella
     */
    public InterventoTableModel(GestioneDati<Intervento> lista) {
        this.listaInterventi = lista;
    }

    /** Ritorna il numero di interventi presenti
     * @return Numero di interventi presenti */
    @Override
    public int getRowCount(){return listaInterventi.size();}

    /**Ritorna il numero di attributi definiti per ogni intervento
     * @return numerodi attributi definite per ogni intervento */
    @Override
    public int getColumnCount(){return nomiColonne.length;}

    /**Ritorna il nome della colonna da visualizzaere
     * @return nome della colonna da visualizzare nell'intestazione in base all'indice*/
    @Override
    public String getColumnName(int col){return nomiColonne[col];}

    /**Estrae il valore da visualizzare in una cella specifica:
     * @param rowIndex Indice di un intervento specifico
     * @param columnIndex attributo specifico dell'intervento
     * @return oggetto da visualizzare nella cella
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex){
        //Intervento i = listaInterventi.getElementi().get(rowIndex);
        /* Recupero dell'intervento dalla lista thread-safe tramite l'indice di riga */
        Intervento i = listaInterventi.get(rowIndex);
    switch (columnIndex) {
            case 0:
                return i.getCodice(); // Colonna Codice
            case 1:
                return i.getDataApertura(); // Colonna data di apertura
            case 2:
                return i.getDescrizione(); // Colonna descrizione testuale

            case 3: return i.getStato(); // Colonna stato (enum StatoIntervento)
            case 4: //Colonna Tecnico (ID o messaggio di assenza)
                if (i.getTecnicoAssociato() != null) {
                    return i.getTecnicoAssociato().getId();
                } else {
                    //Gestisco in Intervento.java, ma lo mantengo
                    return "Non assegnato";
                }
            case 5: // Colonna costo preventivo con logica dinamica
                if (i.getStato() == StatoIntervento.IN_LAVORAZIONE || i.getStato() == StatoIntervento.IN_ATTESA) {
                    double preventivo = i.calcolaPreventivo();
                    return String.format("%.1f h -> %.2f €", i.getStimaDurata(), preventivo);
                } 
                // se completato, mostra il saldo finale
                else if (i.getStato() == StatoIntervento.COMPLETATO) {
                    return String.format("TOTALE: %.2f €", i.calcolaPreventivo());
                } 
                //se in attesa, mostriamo solo le ore previste
                else {
                    return i.getStimaDurata() + " h previste";
                }
            default:
                return null;
        }
        } 

        /**
         * Determina se una cella è modificabile dall'utente:
         * @param row Indice della riga
         * @param column Indice della colonna
         * @return true se la cella appartiene alla colonna "Stato" e l'intervento non è chiuso
         */
    @Override
    public boolean isCellEditable(int row, int column){
        /* rende modificabile solo la colonna 3 */
        if(column == 3){
            Intervento i = listaInterventi.get(row);
            return i.getStato() != StatoIntervento.COMPLETATO;
        }
        return false;
    }

    /**
     * Salva il valore modificato dall'utente tramite l'editor della tabella:
     * @param aValue il nuovo valore (StatoIntervento) inserito
     * @param row riga interessata
     * @param column colonna interessata
     */
    @Override
    public void setValueAt(Object aValue, int row, int column){
        /* Verifica che il valore provenga dalla colonna 3 e sia del tipo corretto */
        if(column == 3 && aValue instanceof StatoIntervento){
            Intervento i = listaInterventi.get(row);
            StatoIntervento nuovoStato = (StatoIntervento) aValue;
            /* modifica all'oggetto del modello */
            i.setStato(nuovoStato);

            /* Notifica alla tabella che la riga è stata aggiornata
            Questo farà il ridisegno della riga e l'aggiornamento dei colori */
            fireTableRowsUpdated(row, row);
            //o passa il riferimento
            //il refresh del PanelTecnici avviene tramite il TableModelListener
            //MainFrame main = (MainFrame) SwingUtilities.getWindowAncestor(null); 
        }
    }

    /**
     * Notifica alla vista che l'intera struttura dei dati è cambiata
     * Da utilizzare dopo aggiunte o rimozioni di più righe di interventi
     */
     
    /* 
    Rilegge tutti i dati del modello, si ridisegna 
    completamente, aggiorna tutte le celle visibili
    serve quando i dati cambiano, non sempre necessario con DefaultTableModel
    */
    public void aggiornaTabella(){
       fireTableDataChanged();
    }
}
