package view;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import logic.*;
import model.*;

/**
 * Finestra di dialogo per l'inserimento di un nuovo tecnico
 * permette di inserire dati anagrafici del tecnico, se è interno o esterno e la sua paga
 * Estende JDialog per bloccare l'interazione con la finestra principale
 * finchè l'inserimento non è completato o annullato
 */

public class DialogoTecnico extends JDialog{
    /* componenti grafici per l'input */
    /** Campo di testo: ID */
    private JTextField txtId;
    /** Campo di testo Nome */
    private JTextField txtNome;
    /** Campo di testo Cognome */
    private JTextField txtCognome;
    /** ComboBox per il tipo del tecnico */
    private JComboBox<String> comboTipo;
    /** Campo di testo per la paga oraria */
    private JTextField txtPaga;

    /** Riferimento al controller per generare ID e validare i dati */
    private GestioneCentro controller;

    /** Oggetto Tecnico (Interno o Esterno) creato dopo la validazione */
    private Tecnico tecnicoInserito;
    
    /** Flag per indicare se l'utente ha premuto "salva" o ha chiuso la finestra */
    private boolean confermato = false;


    /**
     * Costruttore della finestra di dialogo
     * @param parent    Frame genitore (MainFrame) per il posizionamento e la modalità
     * @param controller Riferimento al centro della gestione
     */

    public DialogoTecnico(Frame parent, GestioneCentro controller){
        /* 'true' imposta la modalità modale: l'utente non può cliccare altrove */
        super(parent, "Aggiungi nuovo Tecnico", true);
        this.controller = controller;
        setLayout(new BorderLayout());
        setComponenti();
        pack(); // ridimensiona la finestra in base al contenuto
        setLocationRelativeTo(parent); //Centra il dialogo rispetto al MainFrame
    }

    /**
     * Inizializza e dispone i componenti nel dialogo
     */
    private void setComponenti(){
        // Pannello centrale con GridLayout per allineare etichette e campi (5 righe, 2 colonne, 10 spazio tra comp.)
        JPanel panelCampi = new JPanel(new GridLayout(5,2,10,10));
        // Aggiunge un bordo vuoto attorno al pannello, cioè del padding
        panelCampi.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Campo ID: pre-visualizzato e non modificabile per consistenza
        panelCampi.add(new JLabel("ID:"));
        // visualizza ma non salva il prossimo id tecnico
        txtId = new JTextField(controller.visualizzaProssimoIdTecnico());
        txtId.setEditable(false);
        //grigio per indicare che è in sola lettura
        txtId.setBackground(Color.LIGHT_GRAY);
        panelCampi.add(txtId);
        // Campo nome
        panelCampi.add(new JLabel("Nome:"));
        txtNome = new JTextField();
        panelCampi.add(txtNome);
        // Campo cognome
        panelCampi.add(new JLabel("Cognome:"));
        txtCognome = new JTextField();
        panelCampi.add(txtCognome);
        // Selezione del tipo: COMPORTAMENTO POLIMORFICO - influisce sulla logica di creazione di un tecnico
        panelCampi.add(new JLabel("Tipo Tecnico:"));
        comboTipo = new JComboBox<>(new String[]{"Interno", "Esterno"});
        panelCampi.add(comboTipo);

        // JSpinner per la paga: solo se il tecnico è Esterno
        panelCampi.add(new JLabel("Paga Oraria (€):"));
        txtPaga = new JTextField("25.0");
        panelCampi.add(txtPaga);
        // di default, il tipo è Interno, quindi disabilitiamo lo spinner perchè è fisso se interno
        txtPaga.setEnabled(false);


        /*
            Listener sulla ComboBox: abilita o disabilita lo spinner della paga
            a run time in base alla selezione
         */
        comboTipo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                String scelta = (String) comboTipo.getSelectedItem();
                if(scelta.equals("Esterno")){
                    txtPaga.setEnabled(true);
                }else{
                    /**PREVIENE: seleziono esterno, modifico, riseleziono interno */
                    txtPaga.setEnabled(false);
                    txtPaga.setText("25.0"); // Reset al valore di default per interni
                }
            }
        });

        add(panelCampi, BorderLayout.CENTER);

        //pannello inferiore per i comandi di conferma/annullamento
        JPanel panelBottoni = new JPanel();
        JButton btnSalva = new JButton("Salva");
        JButton btnAnnulla = new JButton("Annulla");

        /*
            Salva la finestra
         */
        btnSalva.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salva();
            }
        });

        /*
            Chiude la finestra
         */
        btnAnnulla.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        panelBottoni.add(btnSalva);
        panelBottoni.add(btnAnnulla);
        add(panelBottoni, BorderLayout.SOUTH);
    }
    /**
     * Valida i dati inseriti e crea l'istanza del tecnico
     */
    private void salva(){
        try {
            //String id = txtId.getText().trim();
            String nome = txtNome.getText().trim();
            String cognome = txtCognome.getText().trim();
            String tipo = (String) comboTipo.getSelectedItem();

            // Validazione base: impedisce nomi vuoti
            if (nome.isEmpty() || cognome.isEmpty()){
                throw new IllegalArgumentException("Tutti i campi sono obbligatori!");
            }

            // Richiesta dell'ID definitivo al controller (sincronizzato)
            String id = controller.generaProssimoIdTecnico();

            /*applicazione del polimorfismo:
                La variabile "tecnicoInserito" è di tipo Tecnico
                ma punta a un'istanza di TecnicoInterno o TecnicoEsterno in base alla scelta
             */
            if(tipo.equals("Interno")){
                tecnicoInserito = new TecnicoInterno(id, nome, cognome);
            }else{
                /** Paga inserita nel dialogo */
                double pagaInserita = 0.0;
                /** salva nella variabile ciò che è stato scritto nel campo di testo */
                String testoPaga = txtPaga.getText().trim();

                // CONTROLLO RIGOROSO DEI CARATTERI (Regex)
                // Questo pattern permette solo numeri interi (es. 42) o decimali con il punto (es. 42.50)
                // Rifiuta categoricamente qualsiasi lettera, spazi, o simboli speciali

                // controllo: ^[0-9]+: La stringa deve iniziare e contenere obbligatoriamente una o più cifre numeriche da 0 a 9.
                // controllo: (\\.[0-9]+)?$: La stringa può terminare opzionalmente con un punto seguito da altre cifre numeriche (per la parte decimale).
                if (!testoPaga.matches("^[0-9]+(\\.[0-9]+)?$")) {
                    throw new IllegalArgumentException("La paga oraria deve contenere solo numeri! Non sono ammessi caratteri, lettere o simboli.");
                }

                try {
                    // La conversione ora è sicura al 100% perché la Regex ha già escluso tutte le lettere
                    pagaInserita = Double.parseDouble(testoPaga);

                    // Controllo di business: la paga deve essere un valore positivo
                    if (pagaInserita <= 0) {
                        throw new IllegalArgumentException("La paga oraria deve essere maggiore di zero!");
                    }

                } catch (NumberFormatException ex) {
                    // Protezione extra nel caso in cui il numero superi i limiti massimi del tipo double
                    throw new IllegalArgumentException("Il numero inserito non è valido.");
                }

                tecnicoInserito = new TecnicoEsterno(id, nome, cognome, pagaInserita);
            }

            confermato = true; // segnala operazione riuscita
            dispose(); //chiude il dialogo
        } catch (IllegalArgumentException e) {
            // Visualizzazione dell'errore all'utente tramite popup
            JOptionPane.showMessageDialog(this, "Errore: "+e, "Errore",JOptionPane.ERROR_MESSAGE);
        }
    }

    /** controlla se l'utente ha completato il salvataggio
     * @return true se l'utente ha completato il salvataggio correttamente
     */
    public boolean isConfermato(){return confermato; }

    /** ricava l'oggetto tecnico
     * @return l'oggetto Tecnico appena creato
     */
    public Tecnico getTecnico(){return tecnicoInserito;}
}
