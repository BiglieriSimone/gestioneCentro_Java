package view;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.io.*;
import java.text.MessageFormat;
import javax.swing.*;
import logic.*;

/**
 * Classe che rappresenta la finestra principale della GUI.
 * Gestisce l'organizzazione dei pannelli e la barra degli strumenti principale.
 * Implementa funzioni di persistenza e stampa
 */
public class MainFrame extends JFrame {
    /** Riferimento al controller per la gestioen e la sincronizzazione dei dati */
    private GestioneCentro controller;

    /** Componente per la navigazione a schede tra Tecnici e Interventi */
    private JTabbedPane tabbedPane;
    /** Pannello dedicato alla gestione dei dati del tecnico */
    private PanelTecnici panelTecnici;  
    /** Pannello dedicato alla gestione dei dati degli interventi */
    private PanelInterventi panelInterventi; 

    /**
     * Costruttore della finestra principale
     * @param controller parametro per interagire con i dati del centro
     */
    public MainFrame(GestioneCentro controller){
        this.controller = controller;

        /* Impostazione della finestra */
        setTitle("Gestione Centro Manutenzione");
        setSize(800,600); //Definizione risoluzione iniziale
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Chiude il processo all'uscita
        setLocationRelativeTo(null); //Centra la finestra
        setComponenti(); //Inizializza dei componenti grafici
    }

    /**
     * Configura il layout della finestra e inizializza i pannelli principali.
     * Utilizza un borderlayout per organizzare gli spazi
     */
    private void setComponenti(){
        // Utilizza BorderLayout per posizionare Toolbar (North) e Contenuto (Center)
        setLayout(new BorderLayout());
        
        //Creazione delle schede
        tabbedPane = new JTabbedPane(); 
        panelTecnici = new PanelTecnici(controller); 
        panelInterventi = new PanelInterventi(controller); 
        
        /* Aggiunta dei pannelli al contenitore a schede con etichetta */
        tabbedPane.addTab("Tecnici", panelTecnici); 
        tabbedPane.addTab("Interventi", panelInterventi); 
        
        /* Posizionamento del TabbedPane nella zona centrale */
        add(tabbedPane, BorderLayout.CENTER);
        /* Creazione e posizionamento della barra degli strumenti */
        creaToolbar();
    }

    /**
     * Metodo che esegue una ricarica forzata di tutte le tabelle presenti nei vari pannelli
     */
    public void refreshTotale() {
        if (panelTecnici != null) {
            panelTecnici.ricarica();
        }
        if (panelInterventi != null) {
            panelInterventi.ricarica();
        }
    }   

    /**
     * Crea e aggiunge la barra degli strumenti alla finestra
     */
    private void creaToolbar(){
        /**Gestione toolbar*/
        JToolBar toolbar = new JToolBar();
        /** bottone di salvataggio dei dati */
        JButton btnSalva = new JButton("Salva Dati");
        /** bottone per caricamento dei dati */
        JButton btnCarica = new JButton("Carica Dati");
        /**bottone per stampare le viste */
        JButton btnStampa = new JButton("Stampa Interventi"); // Bottone Stampa


        btnSalva.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                String nomeFile = "dati_centro";
                File file = new File(nomeFile);
                /** flag per confermare il salvataggio */
                int risposta = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "Vuoi salvare le modifiche apportate?",
                    "Conferma Salvataggio",
                    JOptionPane.YES_NO_OPTION
                );
                
                if(risposta == JOptionPane.YES_OPTION){
                    /* Controllo se il file esiste già */
                    if(file.exists()){
                        int conferma = JOptionPane.showConfirmDialog(
                        MainFrame.this, "Il file di salvataggio esiste già. Sovrascriverlo?",
                        "Attenzione: Sovrascrittura",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    if(conferma == JOptionPane.NO_OPTION) return;
                }
                    GestioneFile.salvaStato(controller, nomeFile);
                    JOptionPane.showMessageDialog(MainFrame.this, "Dati salvati correttamente");
                }
            }
        });

        /*
            Logica del pulsante di caricamento:
            Gestisce il caricamento da file e l'aggiornamento di tutte le viste
         */
        btnCarica.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                /** conferma di caricamento*/
                int risposta = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "Caricando i dati perderai le modifiche non salvate. Continuare?",
                    "Conferma caricamento",
                    JOptionPane.YES_NO_OPTION
                );

                if(risposta == JOptionPane.YES_OPTION){
                    /* Deserializzazione dell'oggetto GestioneCentro
                    viene riconvertito il file binario in file eseguibile in modo da
                    caricare correttamente i dati in memoria. */
                    /**Deserializzazione dell'oggetto GestioneCentro */
                    GestioneCentro datiCaricati = GestioneFile.caricaStato("dati_centro");
                    if(datiCaricati != null){
                        /* importa i dati nel controller (metodo sincronizzato) */
                        controller.importaDati(datiCaricati);
                        /* forza l'aggiornamento grafico di tutti i pannelli */
                        refreshTotale();
                        JOptionPane.showMessageDialog(MainFrame.this, "Dati ricaricati correttamente");
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.this, "File di salvataggio non trovato");
                    }
                }
            }
        });

        /*
            Logica del pulsante di stampa:
            Avvia la procedura di stampa cartacea o PDF della tabella interventi
         */
        btnStampa.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                stampaTabellaInterventi();
            }
        });

        // Aggiunta ordinata alla toolbar
        toolbar.add(btnSalva);
        toolbar.add(btnCarica);
        toolbar.add(btnStampa);
        
        add(toolbar, BorderLayout.NORTH);
    }

    /**
     * Metodo che gestisce la logica di stampa del registro interventi
     */
    public void stampaTabellaInterventi(){
        JTable tabella = panelInterventi.getTabella();
        if(tabella == null){
            JOptionPane.showMessageDialog(this, "Errore: Tabella non disponibile.");
            return;
        }
        
        try {
            /** Definizione dell'intestazione (header) del documento */
            MessageFormat header = new MessageFormat("Registro Interventi - Centro Manutenzione");

            /**  {0} permette a Java di inserire il numero di pagina automaticamente */
            MessageFormat footer = new MessageFormat("Pagina {0}"); 

            /** Avvio del processo di stampa  */
            //(apre la finestra di dialogo del sistema operativo)
            boolean completato = tabella.print(
                JTable.PrintMode.FIT_WIDTH, //ridimensiona le colonne adattandoole alla larghezza del foglio 
                header,
                footer,
                true, // mostra la finestra di dialogo di stampa
                null,
                true // Stampa interattiva (mostra progresso)
            );

            if(completato){
                JOptionPane.showMessageDialog(this, "Stampa inviata con successo");
            }
        } catch (PrinterException e) {
            /* Gestione errori legati alla stampante o al driver */
            JOptionPane.showMessageDialog(this, "Errore di stampa: " + e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }
}