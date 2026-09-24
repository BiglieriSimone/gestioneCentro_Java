package logic;

/**
 * Classe che gestisce il salvataggio automatico periodico di un file temporaneo
 */
public class AutoSaveThread extends Thread {
    /** riferimento al controller centrale di cui salvare lo stato  */
    private GestioneCentro centro;
    
    /** Nome del file temporaneo */
    private String nomeTemp = "backup_temp.dat";

    /** frequenza di salvataggi */
    private int IntervalloSecondi = 10;

    /**
     * Costruttore del thread 
     * @param centro istanza di GestioneCentro contenente le liste correnti da salvare
     */
    public AutoSaveThread(GestioneCentro centro){
        this.centro = centro;
    }

    /**
     * punto di ingresso del thread. 
     * viene avviato invocando esternamente il metodo start()
     */
    @Override
    public void run(){
        /* ciclo infinito di monitoraggio */
        while (true) { 
            try {
                /*
                Entra nello stato "sleeping" per il tempo indicato, non consuma CPU:
                il Thread sospende temporaneamente la sua esecuzione per 10 secondi (sleeping)
                Moltiplica per 1000 poichè il metodo .sleep() accetta durata in millisecondi 
                */
                Thread.sleep(IntervalloSecondi * 1000);

                /*
                Al risveglio dell'attesa, il thread invoca il metodo statico della classe delegata.
                Poichè GestioneCentro e GestioneDati sono sincronizzati, la lettura avviene in 
                totale sicurezza anche se l'utente sta interagendo con le tabelle grafiche
                */
                GestioneFile.salvaStato(centro, nomeTemp);
                System.out.println("Autosave completato su: "+nomeTemp);
            } catch (InterruptedException e) {
                /*
                Se il programma principale si sta chiudendo o richiede lo stop di questo thread, invierà 
                un segnale tramite .interrupt(). QUesto risveglia forzatamente il thread dallo sleep, sollevando una eccezione.
                Intercetta l'eccezione, stampa l'avviso e usa break per fermare definitivamente il ciclo infinito.
                Il thread termina senza memory leak
                */
                System.err.println("Thread di salvataggio interrotto");
                break;
            }
        }
    }

}
