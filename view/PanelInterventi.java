package view;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import logic.*;
import model.*;

/**
 * Pannello per la visualizzazione e gestione degli interventi di manutenzioni.
 * Implementa una visualizzazione tabellare con renderer in base allo stato
 */
public class PanelInterventi extends JPanel {
    /** Riferimento al controller per l'accesso alla logica degli interventi */
    private GestioneCentro controller;
    /** Componente tabella per mostrare gli interventi */
    private JTable tabellaInterventi;
    /** Modello dati per la gestione degli interventi nella tabella */
    private InterventoTableModel tableModel;

    /** Gestore per l'ordinamento e il filtraggio delle righe nella tabella */
    private TableRowSorter<InterventoTableModel> sorter;
    /** Menu a tendina per il filtraggio degli interventi in base al tecnico assegnato */
    private JComboBox<Object> comboFiltroTecnico;
    /** Menu a tendina per il filtraggio degli interventi in base allo stato */
    private JComboBox<Object> comboFiltroStato;

    /**
     * Costruttore del pannello interventi
     * Inizializza il modello, imposta il layout e costruisce l'interfaccia
     * @param controller riferimento alla gestione delle tabelle degli interventi
     */
    public PanelInterventi(GestioneCentro controller) {
        /* Salva il riferimento al controller */
        this.controller = controller;
        /* Inizializza il modello con la lista degli interventi, viene ricavato dal controller (che prende i dati dal centro) */
        this.tableModel = new InterventoTableModel(controller.getGestioneInterventi());
        /* Imposta il layout principale come BorderLayout */
        setLayout(new BorderLayout());
        /* Chiama il metodo per configurare gli elementi grafici */
        setComponenti();
    }

    /**
     * Restituisce il riferimento alla tabella degli interventi
     * @return l'oggetto tabellaInterventi degli interventi
     */
    public JTable getTabella(){
        return this.tabellaInterventi;
    }

    /**
     * Configura la struttura del pannello: 
     * - tabella
     * - filtri
     * - pulsanti
     * - renderer
     */
    private void setComponenti() {
        /* Inizializzazione Tabella e Renderer */
        tabellaInterventi = new JTable(tableModel) {
            /**
             * Sovrascrive il renderer per colorare le righe in base allo stato dell'intervento
             * @param renderer utilizzato per disegnare la cella
             * @param row indice della riga corrente da renderizzare
             * @param column indice della colonna corrente da renderizzare
             * @return componente grafico personalizzato con colori associati allo stato d'intervento
             */
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                /* Ottiene il componente di base */
                Component c = super.prepareRenderer(renderer, row, column);
                /* Applica il colore solo se la riga non è selezionata */
                if (!isRowSelected(row)) {
                    // Recupera lo stato dell'intervento in colonna 3 per la decisione del colore
                    StatoIntervento stato = (StatoIntervento) getValueAt(row, 3);
                    if (stato != null) {
                        switch (stato) {
                            /* Bianco per interventi IN_ATTESA */
                            case IN_ATTESA -> c.setBackground(Color.WHITE);
                            /* Giallo chiaro per interventi IN_LAVORAZIONE */
                            case IN_LAVORAZIONE -> c.setBackground(new Color(255, 255, 204));
                            case COMPLETATO -> {
                                /* Testo grigio e sfondo grigio per interventi COMPLETATI */
                                c.setBackground(new Color(230, 230, 230));
                                c.setForeground(Color.GRAY);
                            }
                        }
                    }
                }
                /* ritorna il componente */
                return c;
            }
        };

        /* Configurazione Sorter per i Filtri:
        ha la funzione da ponte tra l'utente e il modello 
        per nascondere le righe filtrate */
        sorter = new TableRowSorter<>(tableModel);
        tabellaInterventi.setRowSorter(sorter);
        
        /* Configurazione colonna "stato" (JComboBox interna alla cella): */
        /**Recupera la colonna "Stato" dalla tabella con indice 3 */
        TableColumn colonnaStato = tabellaInterventi.getColumnModel().getColumn(3);

        /** Crea JComboBox utilizzata come editor interno della cella:
        la combo contiene automaticamente tutti i valori dell'enumerazione StatoIntervento */
        JComboBox<StatoIntervento> comboStatoCell = new JComboBox<>(StatoIntervento.values());

        /* Imposta la COmboBox come celleditor della colonna:
        In questo modo l'utente può modificare lo stato direttamente dalla tabella
        selezionando un valore dal menu a tendina */
        colonnaStato.setCellEditor(new DefaultCellEditor(comboStatoCell));

        /* Imposta la larghezza preferita della colonna 1 (Descrizione)
        Serve per migliorare la leggibilità e ordine del testo */
        tabellaInterventi.getColumnModel().getColumn(1).setPreferredWidth(100);

        /** Configurazione Pannello Filtri (NORTH) */
        JPanel panelFiltri = new JPanel(new FlowLayout(FlowLayout.LEFT));
        /* bordo con titolo al pannello per separare l'area filtri dal resto della grafica */
        panelFiltri.setBorder(BorderFactory.createTitledBorder("Filtra Interventi"));

        panelFiltri.add(new JLabel("Tecnico:"));
        comboFiltroTecnico = new JComboBox<>();
        aggiornaComboTecniciFiltro(); // Carica gli ID dei tecnici esistenti nella combo
        panelFiltri.add(comboFiltroTecnico);

        panelFiltri.add(new JLabel("Stato:"));
        comboFiltroStato = new JComboBox<>();
        comboFiltroStato.addItem("Tutti");//Rimuove il filtro stato
        /*Inserisce nella combo tutti i valori disponibili dell'enum scelto */
        for (StatoIntervento s : StatoIntervento.values()) {
            comboFiltroStato.addItem(s);
        }
        /* Aggiunge la combobox al pannello dei filtri */
        panelFiltri.add(comboFiltroStato);

        /* Listener per i filtri: */
        ActionListener filtroListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applicaFiltri(); // Ogni volta che cambia una combo, riapplica il filtro RowFilter
            }
        };
        comboFiltroTecnico.addActionListener(filtroListener);
        comboFiltroStato.addActionListener(filtroListener);

        add(panelFiltri, BorderLayout.NORTH);

        /** ScrollPane Centrale (Vista con scorrimento) */
        JScrollPane scrollPane = new JScrollPane(tabellaInterventi);
        add(scrollPane, BorderLayout.CENTER);

        /** Pannello Azioni (SOUTH) */
        JPanel panelAzioni = new JPanel(new FlowLayout(FlowLayout.LEFT));
        /**Bottone per nuovo intervento */
        JButton btnNuovo = new JButton("Nuovo Intervento");
        /** Bottone per eliminare un intervento */
        JButton btnElimina = new JButton("Elimina Intervento");
        /** Bottone per modificare un intervento */
        JButton btnModifica = new JButton("Modifica Intervento");

        /*
            Listener per la creazione di un nuovo intervento
            Apre il dialogo e registra l'oggetto nel controller se confermato
        */
        btnNuovo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogoIntervento dialog = new DialogoIntervento((Frame) SwingUtilities.getWindowAncestor(PanelInterventi.this), controller);
                dialog.setVisible(true); //apre il dialogo 
                if (dialog.isConfermato()) {
                    try {
                        controller.registraNuovoIntervento(dialog.getIntervento());
                        ricarica(); //aggiorna la tabella
                        JOptionPane.showMessageDialog(PanelInterventi.this, "Intervento registrato");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(PanelInterventi.this, ex.getMessage());
                    }
                }
            }
        });

        /*
            Listener per l'eliminazione di un intervento
        */
        btnElimina.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int riga = tabellaInterventi.getSelectedRow(); //Ottiene la riga selezionata
                if (riga == -1) {
                    JOptionPane.showMessageDialog(PanelInterventi.this, "Seleziona un intervento.");
                    return;
                }
                /* Trasforma l'indice della vista in quello reale del modello
                Serve nel caso ci siano filtri attivi, in modo che garantisca la consistenza dei record
                (l'ordine delle righe può essere diverso dall'ordine reale dei dati nel modello)
                */
                int modelRow = tabellaInterventi.convertRowIndexToModel(riga);
                /* Recupera l'oggetto Intervento reale del modello dati 
                utilizzando l'indice corretto ottenuto dalla conversione */
                Intervento inter = controller.getGestioneInterventi().get(modelRow);

                /** Chiede la conferma se si vuole eliminare un intervento */
                int conferma = JOptionPane.showConfirmDialog(PanelInterventi.this, "Eliminare " + inter.getCodice() + "?", "Conferma", JOptionPane.YES_NO_OPTION);
                if (conferma == JOptionPane.YES_OPTION) {
                    controller.cancellaIntervento(inter); //esegue cancellazione
                    ricarica(); //aggiorna vista
                    refreshMainFrame(); //sincronizza altri pannelli (es. stato tecnici)
                    JOptionPane.showMessageDialog(PanelInterventi.this, "Intervento rimosso");
                }
            }
        });

        /*
            Listener per la modifica di un intervento esistente.
        */
        btnModifica.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int riga = tabellaInterventi.getSelectedRow();
                if (riga == -1) {
                    JOptionPane.showMessageDialog(PanelInterventi.this, "Seleziona un intervento.");
                    return;
                }
                /** Converte l'indice della riga cliccata visivamente dall'utente */
                //cambia se applichi un filtro
                int modelRow = tabellaInterventi.convertRowIndexToModel(riga);
                /**Usa quell'indice reale appena trovato per andare a prendere l'oggetto Intervento che corrisponde all'indice reale */
                Intervento inter = controller.getGestioneInterventi().get(modelRow);

                /* Impedisce modifiche a interventi già chiusi */
                if (inter.getStato() == StatoIntervento.COMPLETATO) {
                    JOptionPane.showMessageDialog(PanelInterventi.this, "Impossibile modificare un intervento completato.");
                    return;
                }
                /* Crea il dialogo per la modifica:
                la finestra principale viene recuperata con getWindowAncestor(...) e passata
                come parent del dialogo */
                /**Crea il dialogo per la modifica */
                DialogoIntervento dialog = new DialogoIntervento((Frame) SwingUtilities.getWindowAncestor(PanelInterventi.this), controller);

                /* Carica i dati dell'intervento selezionato per permettere la modifica dall'utente */
                dialog.caricaDati(inter);
                /* dialogo visibile ed esecuzione bloccata fino a chiusura o conferma */
                dialog.setVisible(true);

                if (dialog.isConfermato()) {
                    /* Aggiorna i dati tramite controller (in modo che sia sincronizzato con i dati del centro) */
                    controller.aggiornaIntervento(inter, dialog.getNuovaDescrizione(), dialog.getNuovaDurata(), dialog.getNuovoTecnico());
                    ricarica(); //aggiorna vista
                    refreshMainFrame(); // aggiorna altri pannelli
                    JOptionPane.showMessageDialog(PanelInterventi.this, "Intervento aggiornato");
                }
            }
        });

        panelAzioni.add(btnNuovo);
        panelAzioni.add(btnModifica);
        panelAzioni.add(btnElimina);
        add(panelAzioni, BorderLayout.SOUTH); //Aggiunge i pulsanti in basso

        /**
         * Listener sul modello per monitorare cambi di stato tramite combobox in cella
         */
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                //Se viene modificata la colonna 3, aggiorna tutta l'interfaccia
                if (e.getColumn() == 3) {
                    refreshMainFrame();
                }
            }
        });
    }

    /**
     * Applica i filtri sulla tabella
     * Crea un RowFilter che controlla in contemporanea le colonne Tecnico e Stato
     */
    private void applicaFiltri() {
        /**Tecnico selezionato */
        final Object tecnicoSelezionato = comboFiltroTecnico.getSelectedItem(); //scelta tecnico
        /**Stato selezionato */
        final Object statoSelezionato = comboFiltroStato.getSelectedItem(); //scelta stato

        /*Definizione dell'oggetto filtro dinamico:
        generics: il filtro lavora su InterventoTableModel, le righe sono identificate da un Integer */
        /**Definzione dell'oggetto filtro dinamico */
        RowFilter<InterventoTableModel, Integer> filtro = new RowFilter<InterventoTableModel, Integer>() {
            @Override

            /* Metodo chiamato automaticamente per ogni riga della tabella, controllando se la riga deve essere mostrata o no:
            Entry<...> serve a descrivere il tipo di dati contenuti nell'oggetto Entry:
            Entry rappresenta una riga della tabella vista dal filtro (RowFilter)
            InterventoTableModel = tipo del modello dei dati
            Integer = tipo identificativo della riga, extends rende il codice più flessibile*/

            /**Metodo cchiamato per ogni riga della tabella che controlla se la riga deve essere mostrata o no */
            public boolean include(Entry<? extends InterventoTableModel, ? extends Integer> entry) {
                /* recupero dei dati della riga:
                Restituisce il contenuto della colonna convertito in una stringa */
                /** recupero dell'id del tecnico */
                String idTecnicoTabella = entry.getStringValue(4);
                /** recupero dello stato dell'intervento */
                String statoTabella = entry.getStringValue(3);

                /* Se non hai selezionato nulla oppure hai scelto "Tutti", non filtrare niente -> passa tutto, se il tecnico è selezionato -> deve coincidere con la riga*/
                /**filtragigio in base al tecnico */
                boolean matchTecnico = (tecnicoSelezionato == null || tecnicoSelezionato.equals("Tutti")) || 
                                       idTecnicoTabella.equals(tecnicoSelezionato.toString());
                /* Se non hai selezionato nulla oppure hai scelto "Tutti", non filtrare niente -> passa tutto, se lo stato è selezionato -> deve coincidere con la riga*/
                /**filtraggio in base allo stato */
                boolean matchStato = (statoSelezionato == null || statoSelezionato.equals("Tutti")) || 
                                     statoTabella.equals(statoSelezionato.toString());
                /* la riga viene mostrata solo se passa il filtro tecnico E filtro stato */
                return matchTecnico && matchStato;
            }
        };
        /* Applica il filtro al sorter della tabella */
        sorter.setRowFilter(filtro);
    }

    /**
     * Aggiorna la lista dei tecnici nel filtro
     */
    public void aggiornaComboTecniciFiltro() {
        comboFiltroTecnico.removeAllItems();
        comboFiltroTecnico.addItem("Tutti");
        for (Tecnico t : controller.getGestioneTecnici().getElementi()) {
            comboFiltroTecnico.addItem(t.getId());
        }
    }

    /**
     * Aggiorna la tabella e i filtri
     */
    public void ricarica() {
        tableModel.aggiornaTabella(); //notifica il modello del cambio dati
        aggiornaComboTecniciFiltro(); //ricarica gli ID tecnici nel filtro
    }

    /**
     * Metodo per aggiornare il MainFrame:
     * Mantiene sincronizzate le viste
     */
    /* se un intervento viene assegnato, il tecnico cambia colore nel PanelTecnici */
    private void refreshMainFrame() {
        // Cerca il genitore MainFrame risalendo l'albero dei componenti
        MainFrame main = (MainFrame) SwingUtilities.getWindowAncestor(this);
        if (main != null) main.refreshTotale(); //invoca il refresh globale
    }
}