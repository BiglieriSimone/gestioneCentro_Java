package view;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import logic.*;
import model.*;

/**
 * Finestra di dialogo per la creazione o modifica di un intervento
 * Permette di inserire descrizione, durata e selezionare un tecnico libero
 * Estende JDialog per bloccare l'interazione con la finestra principale
 * finchè l'inserimento non è completato o annullato
 */
public class DialogoIntervento extends JDialog{
    // Componenti di input per gli attributi dell'intervento
    /** Campo di testo per il codice */
    private JTextField txtCodice;
    /** Campo di testo per la descrizione */
    private JTextField txtDescrizione;
    /** JSpinner per la durata */
    private JSpinner spinDurata;
    /** ComboBox per la scelta del tecnico */
    private JComboBox<Tecnico> comboTecnici;

    /** Riferimento al controller centrale */
    private GestioneCentro controller;

    /** Contiene la nuova istanza di Intervento creata */
    private Intervento interventoInserito;

    /** Riferimento all'intervento già esistente se la finestra è aperta in modifica */
    private Intervento interventoInModifica = null;

    /** comunica al pannello se l'utente ha confermato il salvataggio */
    private boolean confermato = false;

    /**
     * Costruttore della finestra di dialogo per un nuovo intervento
     * @param parent Frame genitore (MainFrame) per il posizionamento e la modalità
     * @param controller riferimento al centro della gestione
     */
    public DialogoIntervento(Frame parent, GestioneCentro controller){
        /* 'true' imposta la modalità modale: l'utente non può cliccare altrove */
        super(parent, "Apri nuovo intervento",true);
        this.controller = controller;

        /* BorderLayout per separare la griglia dei dati dai pulsanti di controllo */
        setLayout(new BorderLayout());
        setComponenti();
        pack(); // ridimensiona la finestra in base al contenuto
        setLocationRelativeTo(parent); //Centra il dialogo rispetto al MainFrame
    }

    /**
     * Inizializza e dispone i componenti nel dialogo
     */
    private void setComponenti(){
        // Pannello centrale con GridLayout per allineare etichette e campi (4 righe, 2 colonne, 10 spazio tra comp.)
        /**Pannello centrale con GridLayout */
        JPanel panelCampi = new JPanel(new GridLayout(4,2,10,10));
        //aggiunta del padding
        panelCampi.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        //Codice automatico
        panelCampi.add(new JLabel("Codice Intervento:"));
        txtCodice = new JTextField(controller.visualizzaProssimoIdIntervento());
        txtCodice.setEditable(false); //impedisce la scrittura manuale
        txtCodice.setBackground(Color.LIGHT_GRAY); 
        panelCampi.add(txtCodice);

        //Descrizione
        panelCampi.add(new JLabel("Descrizione:"));
        txtDescrizione = new JTextField(20);
        panelCampi.add(txtDescrizione);

        //Durata (usiamo uno spinner per i numeri)
        panelCampi.add(new JLabel("Durata stimata (ore):"));
        spinDurata = new JSpinner(new SpinnerNumberModel(1.0,0.5,336.0, 0.5));
        
        // Recupera il componente di editing dello JSpinner (editor di default),
        // che è un JComponent generico. Poiché l'editor standard è di tipo
        // JSpinner.DefaultEditor, viene effettuato un cast per poter accedere
        // al metodo getTextField(), che restituisce il JFormattedTextField interno.
        // Questo permette di manipolare direttamente il campo di input dello spinner
        // (es. modifica aspetto, comportamento o validazione dell'input).

        /**
         * Recupera il campo di testo interno allo JSpinner per manipolarne direttamente le proprietà (es. disabilitare la digitazione manuale)
        */
        JFormattedTextField tf = ((JSpinner.DefaultEditor) spinDurata.getEditor()).getTextField();
        tf.setEditable(false);
        tf.setBackground(Color.WHITE);
        panelCampi.add(spinDurata);

        //selezione tecnico
        panelCampi.add(new JLabel("Assegna tecnico libero:"));

        /** Recupera dal controller la lista filtrata contenente solo i tecnici con StatoTecnico.LIBERO */
        List<Tecnico> liberi = controller.getTecniciLiberi();

        // Inizializza la ComboBox convertendo la List in un array tipizzato di oggetti Tecnico in quanto la combobox non accetta una list.
        // NOTA: La JComboBox invocherà implicitamente il metodo toString() ridefinito polimorficamente 
        // nelle sottoclassi (TecnicoInterno / TecnicoEsterno) mostrando anagrafica e tariffe orarie nella tendina.
        comboTecnici = new JComboBox<>(liberi.toArray(new Tecnico[0]));

        // controllo se nessun tecnico è attualmente disponibile
        if(liberi.isEmpty()){
            comboTecnici.addItem(null);
            comboTecnici.setEnabled(false);
            /** messaggio di avviso nel caso in cui non ci sono tecnici disponibili */
            JLabel lblAvviso = new JLabel("Nessun tecnico disponibile!");
            lblAvviso.setForeground(Color.RED);
            panelCampi.add(lblAvviso);
        }else{
            panelCampi.add(comboTecnici); // aggiunge regolarmente la tendina se popolata
        }
        // aggiunge il pannello dei moduli al centro
        add(panelCampi, BorderLayout.CENTER);

        /** Pannello bottoni */
        JPanel panelBottoni = new JPanel();
        /**Bottone di registrazione di un nuovo intervento */
        JButton btnSalva = new JButton("Registra Intervento");
        /** annulla la registrazione */
        JButton btnAnnulla = new JButton("Annulla");
        
        /*Associa l'ascoltatore per il pulsante di salvataggio */
        btnSalva.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salva();
            }
        });

        /* Associa l'ascoltatore per il pulsante Annulla */
        btnAnnulla.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // Composizione e posizionamento dei bottoni a sud
        panelBottoni.add(btnSalva);
        panelBottoni.add(btnAnnulla);
        add(panelBottoni, BorderLayout.SOUTH);
    }
    
    /**
     * Configura il dialogo in modalità "Modifica", precaricando i campi con i dati dell'intervento selezionato
     * @param i oggetto già esistente da viualizzare e aggiornare
     */
    public void caricaDati(Intervento i){
        this.interventoInModifica = i; // Memorizza il riferimento per saltare la creazione di un nuovo oggetto nel salva()

        // Modifica dinamicamente le intestazioni
        setTitle("Modifica Intervento: "+ i.getCodice());
        txtCodice.setText(i.getCodice());
        txtDescrizione.setText(i.getDescrizione());
        spinDurata.setValue(i.getStimaDurata());

        // Rigenera la lista includendo i tecnici liberi più il tecnico attualmente assegnato
        popolaComboTecnici(i);

        //Sposta l'indice di selezione della JComboBox sul tecnico originario dell'intervento
        comboTecnici.setSelectedItem(i.getTecnicoAssociato());
        
        //il codice non deve essere toccato
        txtCodice.setEditable(false);
    }

    /**
     * Raccoglie i dati inseriti dall'utente, esegue i controlli di validazione 
     * e decide se istanziare un nuovo intervento o preparare la modifica
     */
    private void salva() {
        try {
            // Estrae e pulisce i testi da spazi bianchi iniziali o finali superflui
            /** recupera il contenuto del campo della descrizione senza spazi */
            String desc = txtDescrizione.getText().trim();
            /** recupera il contenuto dello spinner durata */
            double durata = (double) spinDurata.getValue();
            /** recupera il tecnico selezionato dalla combobox */
            Tecnico selezionato = (Tecnico) comboTecnici.getSelectedItem();

            // Validazione dello stato logico dei dati immessi
            if (desc.isEmpty()) throw new IllegalArgumentException("Descrizione vuota");
            if (selezionato == null) throw new IllegalArgumentException("Seleziona un tecnico");

            // Se è un NUOVO intervento, allora lo crea
            if (interventoInModifica == null) {
                /** recupera il prossimo ID dell'intervento */
                String codice = controller.generaProssimoIdIntervento(); // Prende il codice generato nel setComponenti
                this.interventoInserito = new Intervento(codice, desc, durata, selezionato);
            } else {
                // Se siamo in modifica, lasciamo interventoInserito a null 
                // perché useremo i getter (getNuovaDescrizione, etc.)
                this.interventoInserito = null; 
            }

            confermato = true; //flag di successo prima della chiusura
            dispose(); //chiude la finestra ritornando il controllo al thread chiamante
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Errore: " + e.getMessage());
        }
    }
    
    // Aggiungi dei getter nel Dialogo per far leggere i dati al Panel

    /** aggiunge la nuova descrizione 
     * @return descrizione aggiornata inserita nella casella di testo. */
    public String getNuovaDescrizione() { return txtDescrizione.getText().trim(); }

    /** Aggiunge la nuova durata 
     *  @return valore numerico della durata prelevato dallo JSpinner. */
    public double getNuovaDurata() { return (double) spinDurata.getValue(); }

    /** aggiunge il nuovo tecnico
     *  @return oggetto Tecnico correntemente evidenziato nella ComboBox. */
    public Tecnico getNuovoTecnico() { return (Tecnico) comboTecnici.getSelectedItem(); }

    /**
     * Svuota e ripopola la comboBox dei tecnici in fase di modifica.
     * Garantisce che la lista contenga tutti i candidati liberi più il titolare attuale dell'intervento
     * escludendo gli altri dipendenti già occupati in altri progetti
     * @param i intervento in fase di analisi
     */
    private void popolaComboTecnici(Intervento i) {
        comboTecnici.removeAllItems(); // pulisce il modello dai vecchi record

        /**Recupera la lista completa di tutti i tecnici */
        List<Tecnico> tecnici = controller.getGestioneTecnici().getElementi();
        for (Tecnico t : tecnici) {
            // Un tecnico può essere inserito nella tendina se si verifica almeno una di queste condizioni:
            // 1. È libero (t.isDisponibile() == true)
            // 2. È il tecnico originariamente assegnato a questo intervento (evita che sparisca dalla lista durante la modifica)
            if (t.isDisponibile() || (i != null && t.equals(i.getTecnicoAssociato()))) {
                comboTecnici.addItem(t); // aggiunge l'oggetto alla combobox
            }
        }
    }
    
    /** controlla se l'operazione è confermata 
     * @return true se l'utente ha confermato l'operazione premendo su 'Registra Intervento'. */
    public boolean isConfermato(){return confermato;}

    /** ricava l'intervento inserito 
     * @return nuova istanza dell'Intervento creato (solo per i nuovi inserimenti) */
    public Intervento getIntervento(){return interventoInserito;}
}
