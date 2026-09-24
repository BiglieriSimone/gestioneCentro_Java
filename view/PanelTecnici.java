package view;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import logic.*;
import model.StatoTecnico;
import model.Tecnico;

/**
 * Pannello dedicato alla visualizzazione e gestione dei tecnicii
 * Fornisce un'interfaccia con tabella per il monitoraggio e pulsanti per
 * le diverse operazioni
 */
public class PanelTecnici extends JPanel{
    /** Riferimento al controller per l'accesso ai dati del centro */
    private GestioneCentro controller;

    /** Componente tabella per la visualizzazione dei tecnici */
    private JTable tabellaTecnici;

    /**Modello della tabella per la gestione della lista dei tecnici */
    private TecnicoTableModel tableModel; 
    

    /**
     * Costruttore del pannello:
     * Inizializza il layout, il modello della tabella e i componenti grafici
     * @param controller Riferimento alla gestione del centro
     */
    public PanelTecnici(GestioneCentro controller){
        this.controller = controller;

        /* Viene inizializzato il modello con la lista dei tecnici fornita dal controller*/
        this.tableModel = new TecnicoTableModel(controller.getGestioneTecnici());

        /* Layout principale impostato come BorderLayout per separare la tabella (Center) e comandi (South) */
        setLayout(new BorderLayout());
        setComponenti();

    }

    /**
     * Configura la tabella e i pulsanti di controllo
     * Gestisce la disposizione e i colori dei componenti nel pannello
    */
   //è privato perchè deve creare la tabella solo all'inizio
    private void setComponenti(){
        tabellaTecnici = new JTable(tableModel) {
            @Override
            /** Aggiunge colore in base allo stato di un tecnico */
            // pubblico perchè appartiene a pacchetti esterni di java
            //personalizza l'aspetto grafico delle celle delle tabelle
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {

                /** renderer = oggetto che la tabella ha già selezionato per quella cella */
                Component c = super.prepareRenderer(renderer, row, column);
                
                /* Colorazione applicata solo se la riga non è selezionata dall'utente tramite click */
                if (!isRowSelected(row)) {

                    /** Recupera lo stato del tecnico */
                    StatoTecnico stato = (StatoTecnico) getValueAt(row, 4); // Colonna 4 = Stato
                    
                    if (stato != null) {
                        switch (stato) {
                            case LIBERO -> c.setBackground(new Color(144,238,144));    // Verde chiaro
                            case OCCUPATO -> c.setBackground(new Color(240,128,128));  // Rosso chiaro
                            case ASSENTE -> c.setBackground(new Color(255, 153, 0));   // Grigio chiaro
                            default -> c.setBackground(Color.WHITE);
                        }
                    }
                }
                return c;
            }
        };
        /** Inserimento della tabella in uno JScrollPane per gestire liste lunghe */
        JScrollPane scrollPane = new JScrollPane(tabellaTecnici);
        add(scrollPane, BorderLayout.CENTER);

        /** Pannello inferiore per i pulsanti di azione, allineati a sinistra
            (Dispone i componenti in riga, uno dopo l'altro, partendo dal margine sinistro) */
        JPanel panelAzioni = new JPanel(new FlowLayout(FlowLayout.LEFT)); 

        /**Bottone per aggiungere un nuovo tecnico*/
        JButton btnAggiungi = new JButton("Aggiungi Tecnico");
        /**Bottone per rimuovere un tecnico*/
        JButton btnRimuovi = new JButton("Rimuovi selezionato");
        /**Bottone per segnare assente (o presente) un tecnico*/
        JButton btnAssenza = new JButton("Segna Assente/Presente");

        //aggiunta dei pulsanti al pannello azioni
        panelAzioni.add(btnAggiungi);
        panelAzioni.add(btnRimuovi);
        panelAzioni.add(btnAssenza);

        /**
         * Azione per gestire la creazione e apertura del dialogo 
         * per l'inserimento di un nuovo tecnico.
         * Il dialog raccoglie i dati inseriti dall'utente
         */
        btnAggiungi.addActionListener(new ActionListener() {
            @Override
            /*Crea il dialogo per l'input */
            public void actionPerformed(ActionEvent e){
                //Il cast (Frame) è necessario perchè getWindowAncestor restituisce un oggetto generico di tipo Window
                //viene passato il controller come riferimento all'oggetto permettendo al dialogo di comunicare le azioni dell'utente direttamente al controller
                //getWindowAncestor serve a risalire la gerarchia dei componenti grafici per trovare la finestra
                //getWindowAncestor posiziona un nuovo popup al centro dello schermo o della finestra principale
                /* JFrame > JPanel > JPanel > JButton, se chiami getWindowAncestor sul JButton il metodo ignorerà
                    i pannelli intermedi e restituirà direttamente il JFrame
                */
                DialogoTecnico dialog = new DialogoTecnico((Frame) SwingUtilities.getWindowAncestor(PanelTecnici.this),controller);
                
                //il dialogo si centrerà rispetto alla superficie occupata dal pannello dei tecnici
                dialog.setLocationRelativeTo(PanelTecnici.this);
                dialog.setVisible(true);
                
                //Controlla se l'utente ha confermato l'inserimento
               if(dialog.isConfermato()){
                   try {
                    /*Registrazione del nuovo tecnico nel livello logico*/
                    controller.registraNuovoTecnico(dialog.getTecnico());
                    
                    /*Aggiornamento della vista:*/
                    ricarica();
                    //Messaggio di conferma per utente:
                    JOptionPane.showMessageDialog(PanelTecnici.this, "Tecnico aggiunto con successo!");
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(PanelTecnici.this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                }
                }
            }
        });

        btnRimuovi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                int rigaSelezionata = tabellaTecnici.getSelectedRow();
                if(rigaSelezionata == -1){
                    JOptionPane.showMessageDialog(PanelTecnici.this, "Seleziona un tecnico dalla tabella per procedere con la rimozione","Nessuna selezione",JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Recuperiamo il tecnico selezionato
                Tecnico t = controller.getGestioneTecnici().get(rigaSelezionata);

                // CONTROLLO STATO: Blocca SOLO se è OCCUPATO.
                // Se è LIBERO o ASSENTE, lo lascia passare.
                if(t.getStato() == StatoTecnico.OCCUPATO){
                    JOptionPane.showMessageDialog(PanelTecnici.this, 
                        "Impossibile rimuovere il tecnico " + t.getCognome() + " perché è impegnato in un incarico attivo.",
                        "Errore rimozione", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                /**  Chiediamo conferma solo se il tecnico può effettivamente essere rimosso*/
                int conferma = JOptionPane.showConfirmDialog(PanelTecnici.this, 
                    "Sei sicuro di voler rimuovere il tecnico " + t.getCognome() + "?",
                    "Conferma Rimozione", 
                    JOptionPane.YES_NO_OPTION);

                if(conferma == JOptionPane.YES_OPTION){
                    controller.getGestioneTecnici().rimuovi(t);
                    ricarica();
                    
                    /** Aggiorna il pannello interventi nel caso ci siano stati cambiamenti visivi */
                    MainFrame main = (MainFrame) SwingUtilities.getWindowAncestor(PanelTecnici.this);
                    if(main != null) main.refreshTotale();
                    
                    JOptionPane.showMessageDialog(PanelTecnici.this,"Tecnico rimosso correttamente");
                }
            }
        });

        btnAssenza.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                int riga = tabellaTecnici.getSelectedRow();
                if(riga == -1){
                    JOptionPane.showMessageDialog(PanelTecnici.this, "Seleziona un tecnico");
                    return;
                }
                //Recuperiamo il tecnico dalla lista
                Tecnico t = controller.getGestioneTecnici().get(riga);

                try {
                    if(t.getStato() == StatoTecnico.OCCUPATO){
                        /** controlla la risposta per quando il tecnico è occupato */
                        int risposta = JOptionPane.showConfirmDialog(PanelTecnici.this,
                            "Il tecnico "+t.getCognome() + " è occupato in un intervento\n"+
                            "Segnandolo come assente, l'intervento tornerà \"IN_ATTESA\" senza tecnico."+
                            "Continuare?","Conferma sostituzione", JOptionPane.YES_NO_OPTION);
                            
                        if(risposta == JOptionPane.NO_OPTION) return;
                    }

                    //Se siamo qui, o era libero o l'utente ha confermato il distacco
                    controller.commutaAssenzaTecnico(t);
                    ricarica();
                    //PROVA
                    MainFrame main = (MainFrame) SwingUtilities.getWindowAncestor(PanelTecnici.this);
                    if(main != null) main.refreshTotale();
                    JOptionPane.showMessageDialog(PanelTecnici.this, "Stato aggiornato correttamente.");
                } catch (IllegalStateException ex) {
                    JOptionPane.showMessageDialog(PanelTecnici.this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });



        //posizionamento del pannello pulsanti nella prte inferiore
        add(panelAzioni, BorderLayout.SOUTH);
    }

    /**Metodo per ricaricare la tabella quando aggiungiamo un tecnico,
     * sfrutta un fireTableDataChanged(): notifica a tutti i listener registrati che
     * il contenuto dei dati nel modello è cambiato
     */
    public void ricarica(){
        tableModel.aggiornaTabella();
    }
}
