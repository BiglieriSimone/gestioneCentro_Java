package logic;
import java.io.Serializable;
import java.util.*;

/**
 * Classe generica per la gestione di collezioni di oggetti
 * Implementa Serializable per la persistenza dello stato su file.
 * @param <T> tipo di elementi gestiti all'interno della collezione
 */
public class GestioneDati<T> implements Serializable{ //non lavora con un tipo specifico, ma con un tipo generico T

    // ospita gli elementi in una struttura dati tipizzata con parametro generico T
    /**ospita gli elementi in una struttura dati tipizzata con parametro generico T */
    private List<T> elementi;

    /**
     * Costruttore della classe:
     */
    public GestioneDati(){
        /* Utilizza una lista thread-safe già pronta in java */
        /* Se più thread modificano la lista nello stesso momento senza sincronizzazione:
        si incorre a dati corrotti, errori, comportamenti imprevedibili. */
        this.elementi = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Inserisce un nuovo elemento all'interno della collezione
     * @param elemento oggetto di tipo T da aggiungere alla lista.
     */
    public void aggiungi(T elemento){
        if(elemento == null){
            throw new IllegalArgumentException("Elemento nullo non consentito");
        }
        elementi.add(elemento);
    }

    /**
     * Rimuove un elemento specifico della collezione
     * @param elemento oggetto di tipo T da eliminare
     */
    public void rimuovi(T elemento){
        if(elemento == null){
            throw new IllegalArgumentException("Elemento nullo non valido");
        }
        elementi.remove(elemento);
    }

    /**
     * Ritorna una copia della lista per preservare l'incapsulamento
     * @return ritorna tutti gli elementi presenti di quella lista
     */
    public List<T> getElementi(){
        /*
        SPIEGAZIONE DEL BLOCCO SYNCHRONIZED E DELLA COPIA DIFENSIVA:
        Anche se la lista è sincronizzata, l'operazione di scorrimento (iterazione) o di copia 
        NON è protetta automaticamente dal wrapper. Se un thread prova a creare una copia 
        mentre un altro aggiunge un elemento, viene sollevata una 'ConcurrentModificationException'.
        Usa un blocco 'synchronized(elementi)' per ottenere il lock esclusivo sulla risorsa. 
        Restituisce una COPIA DIFENSIVA (new ArrayList): in questo modo la View lavora su una lista 
        indipendente e non può modificare direttamente la struttura dati interna privata.
         */
        synchronized(elementi){
            return new ArrayList<>(elementi); //Servirà per popolare le JTable
        }
    }

    /**
     * Restituisce l'elemento all'indice specificato.
     * Serve per collegare l'indice della riga della tabella all'oggetto reale
     * @param index Posizione dell'elemento nella lista
     * @return oggetto di tipo T trovato
     */
    public T get(int index){
        // validazione dei limiti dell'array
        if(index < 0 || index >=elementi.size()){
            throw new IndexOutOfBoundsException("Indice non valido: "+index);
        }
        return elementi.get(index);
    }

    /**
     * Verifica se la collezione è priva di elementi
     * @return true se la lista è vuota, false altrimenti
     */
    public boolean isEmpty(){
        return elementi.isEmpty();
    }

    /**
     * Restituisce il numero totale di elementi attualmente presenti nella collezione
     * @return numero di record inseriti (dimensione della lista)
     */
    public int size(){
        return elementi.size();
    }

    /**
     * Verifica la presenza di un determinato elemento all'interno della collezione
     * @param elemento oggetto da ricercare
     * @return true se l'elemento è presente, false in caso contrario
     */
    public boolean contiene(T elemento){
        return elementi.contains(elemento);
    }

    /**
     * Svuota la lista attuale
     */
    public void pulisci(){
        elementi.clear();
    }


}
