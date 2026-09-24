package view;
import javax.swing.table.*;
import logic.*;
import model.*;

/**
 * Modello che permette alla JTable di visualizzare la lista dei tecnici
 * Implementa AbstractTableModel per collegare la lista alla vista in modo dinamico
 * tra la lista di oggetti e le celle delle tabelle
 */
public class TecnicoTableModel extends AbstractTableModel {
    /**Array di stringhe contenente le etichette per l'intestazioni della tabella */
    private String[] nomiColonne = {"Codice","Nome","Cognome","Tipo","Stato", "Costo/h"};

    /**Riferimento alla lista dei tecnici gestita dal controller*/
    private GestioneDati<Tecnico> listaTecnici;

    /**
     * Costruttore del modello di tabella
     * @param lista lista dei tecnici da visualizzare nella tabella
     */
    public TecnicoTableModel(GestioneDati<Tecnico> lista) {
        this.listaTecnici = lista;
    }

    /**
     * Determina il numero di righe della tabella in base alla dimensione della lista
     * @return Numero di tecnici presenti 
     */
    @Override
    public int getRowCount(){return listaTecnici.size();}

    /**
     * Determina il numero di colonne della tabella
     * @return numerodi colonne definite nell'array nomiColonne
     */
    @Override
    public int getColumnCount(){return nomiColonne.length;}

    /**Restituisce il nome della colonna da visualizzare nell'intestazione
     * @param col L'indice della colonna
     * @return intestazione corrispondente (es. "Codice","Nome", ecc.)
     */
    @Override
    public String getColumnName(int col){return nomiColonne[col];}

    /**Estrae il valore da un oggetto Tecnico
     * e le converte in un formato visualizzabile nella singola cella:
     * @param rowIndex Indice di riga
     * @param columnIndex Indice di colonna
     * @return oggetto da visualizzare nella cella
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex){
        /* Recupero del tecnico della lista tramite indice di riga */
        Tecnico t = listaTecnici.get(rowIndex);

        /* recupera il costo fisso dei tecnici interni e il costo orario degli esterni  */
        String costo = "0.00 €";
        if(t instanceof TecnicoInterno){
            costo = String.format("%.2f €", TecnicoInterno.getCostoFisso());
        }else if (t instanceof TecnicoEsterno){
            costo = String.format("%.2f €", ((TecnicoEsterno) t).getCostoOrario());
        }

        /*
        Switch per mappare gli indici delle colonne agli attributi 
        Più sicuro e compatto rispetto allo switch tradizionale, non serve il break
        */
        return switch (columnIndex) {
            case 0 -> t.getId();    //codice
            case 1 -> t.getNome();  //nome
            case 2 -> t.getCognome(); //cognome
            //controlliamo il tipo di t, se è interno seleziona la prima opzione, seconda altrimenti
            // controlla se t (Tecnico generico) è presente tra i diversi tecnici esistenti
            case 3 -> (t instanceof model.TecnicoInterno) ? "Interno" : "Esterno"; //tipo di tecnico
            case 4 -> t.getStato(); //stato
            case 5 -> String.format("%.2f €", //costo economico associato
                    (t instanceof TecnicoInterno)
                    ? TecnicoInterno.getCostoFisso()
                    : ((TecnicoEsterno) t).getCostoOrario());
                    default -> null;
        };
    } 
    /**
     * Notifica tutti i listener (JTable) che i dati sottostanti sono stati modificati
     * Questo metodo effettua il ridisegno completo della tabella per riflettere
     *  aggiunte, rimozioni o modifiche all'istanza della tabella
    */

   public void aggiornaTabella(){
    /* 
    Metodo ereditato da AbstractTableModel
    Comunica alla vista di aggiornare tutte le celle visibili
    */
       fireTableDataChanged();
    }
}
