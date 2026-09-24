import javax.swing.SwingUtilities;
import logic.*;
import view.MainFrame;

/**
 * Classe principale dell'applicazione
 * Si occupa di coordinare l'avvio della logica, controllo e vista
 */

public class Main {
    /**
     * blocco del main
     * @param args argomenti passati come parametro
     */
    public static void main(String[] args) {
        /** Stringa contenente il nome del file di salvataggio*/
        String nomeFile = "dati_centro";

        /** Riferimento all'oggetto principale della logica caricato da file tramite GestioneFile*/
        GestioneCentro centro = GestioneFile.caricaStato(nomeFile);

        /*Controllo sull'esito del caricamento: se il file non esiste crea un nuovo centro vuoto */
        if(centro == null){
            System.out.println("File di salvataggio non trovato. Inizializzazione centro vuoto");
            centro = new GestioneCentro();
            //popolaDatiEsempio(centro);
        }else{
            System.out.println("Dati caricati correttamente all'avvio");
        }

        /** Creazione dell'istanza del thread per il salvataggio automatico */ 
        //Viene passato l'oggetto "centro" per permettere al thread di monitorarne lo stato
        AutoSaveThread autosave = new AutoSaveThread(centro);
        /* Avvio del thread di autosave (invoca internamente il metodo run() del thread) */
        autosave.start();

        /*
            Copia dell'oggetto centro dichiarata finale per poter essere
            utilizzata all'interno della classe anonima Runnable per
            l'interfaccia grafica 
        */
       /** copia dell'oggetto centro */
        final GestioneCentro centroFinale = centro;

        /* 
            Avvio dell'interfaccia grafica all'interno dell' Event Dispatch Thread
            SwingUtilities.invokeLater garantisce che la creazione della GUI
            avvenga in modo thread-safe

            questo serve perchè se due thread diversi (ad esempio main e autosavethread)
            provassero a modificare lo stesso pulsante contemporaneamente, il programma
            potrebbe crashare o buggarsi. 
            Tutte le istruzioni che creano o modificano componenti grafici devono
            essere eseguite esclusivamente sull'EDT

            Cosa succede in invokeLater?
            - Il thread main crea un "pacchetto" di istruzioni (il Runnable)
            - invece di eseguirlo subito, lo mette in coda (Event Queue)
            - l'EDT non appena finisce di fare quello che sta facendo, prende il pacchetto ed esegue run()

            è fondamentale: il thread main carica i dati e avvia l'autosavethread
            poi usa invokeLater per dire all'EDT di creare la finestra MainFrame usando i dati
        */
        SwingUtilities.invokeLater(new Runnable() {

            /**
             * Metodo eseguito dall'Event Dispatch Thread per inizializzare e 
             * rendere visibile il frame principale
             */
            @Override
            public void run(){
                /* Istanza della finestra principale che riceve il riferimento alla logica */
                MainFrame frame = new MainFrame(centroFinale);
                /* Rendere effettivamente visibile a schermo la finestra */
                frame.setVisible(true);
            }
        });
    }

    /**
     * Metodo per inserire nel sistema, SOLO PER TESTARE
     */
    /*
    private static void popolaDatiEsempio(GestioneCentro centro){

        Tecnico t1 = new TecnicoInterno(centro.generaProssimoIdTecnico(), "Mario", "Rossi");
        Tecnico t2 = new TecnicoEsterno(centro.generaProssimoIdTecnico(), "Luca","Bianchi",40.0);
        Tecnico t3 = new TecnicoInterno(centro.generaProssimoIdTecnico(),"Giuseppe","Verdi");

        centro.getGestioneTecnici().aggiungi(t1);
        centro.getGestioneTecnici().aggiungi(t2);
        centro.getGestioneTecnici().aggiungi(t3);

        Intervento i1 = new Intervento(centro.generaProssimoIdIntervento(),"Riparazione quadro elettrico",6, t1);
        Intervento i2 = new Intervento(centro.generaProssimoIdIntervento(), "Sostituzione lampadine magazzino", 2, t2);
        
        centro.getGestioneInterventi().aggiungi(i1); 
        centro.getGestioneInterventi().aggiungi(i2);
    }
        */
}
