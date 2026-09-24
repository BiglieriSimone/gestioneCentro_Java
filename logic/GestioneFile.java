package logic;
import  java.io.*;

/**
 * Classe per il salvataggio e il caricamento dei dati del sistema
 * Utilizza la serializzazione per gestire la persistenza dei dati su file binari
 */
public class GestioneFile {

    /**
     * Salva l'intera istanza di GestioneCentro su un file binario permanente
     * sfrutta il meccanismo di serializzazione per convertire l'oggetto in byte
     * @param centro GestioneCentro contenente tutti i dati da salvare
     * @param nomeFile nome del file di destinazione (dati_centro)
     */

    public static void salvaStato(GestioneCentro centro, String nomeFile){
        
        /*
        Utilizzo try-with-resources con pattern decorator:
        Garantisce la chiusura automatica delle risorse evitando 
        memory leak e garantendo che i dati vengano effettivamente scritti su disco.

        FileOutputStream è uno stream che scrive un byte alla volta
        viene avvolto dentro un ObjectOutputStream che traduce gli oggetti in byte
        La struttura try-catch garantisce che qualsiasi risorsa venga chiusa automaticamente in ogni caso.
        Migliore rispetto al try-catch classico in quanto chiude automaticamente alla fine del blocco 
        */
        try (ObjectOutputStream salva = new ObjectOutputStream(new FileOutputStream(nomeFile))){//apre file e usa wrap, non solo byte

            /* Serializza l'oggetto scrivendolo nel file. Vengono salvati anche tutti gli oggetti
            collegati a GestioneCentro (liste di tecnici e interventi) purchè implementino Serializable */
            salva.writeObject(centro);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio del file "+nomeFile );
        } //chiude automaticamente lo stream
    }
    /**
     * carica l'istanza di GestioneCentro precedentemente salvata su file:
     * effettua la deserializzazione
     * @param nomeFile percorso del file binario da leggere
     * @return GestioneCentro caricato, oppure null in caso di errore.
     */
    public static GestioneCentro caricaStato(String nomeFile){

        /* 
        try-with-resources e pattern decorator:
        FileInputStream viene avvolto da ObjectInputStream
         */
        try(ObjectInputStream carica = new ObjectInputStream(new FileInputStream(nomeFile))){
            /*
            Il metodo readObject() è generico e restituisce sempre un oggetto di tipo base "Object".
            Poiché il compilatore non può sapere cosa c'è scritto nel file binario, è obbligatorio
            inserire un cast esplicito a (GestioneCentro) per informare Java del tipo reale 
            dell'oggetto e poterlo assegnare correttamente alla variabile di destinazione.
             */
            return (GestioneCentro) carica.readObject(); //java legge un oggetto da carica che è un ObjectInputStream, quindi serve un cast per leggere un GestioneCentro
        } catch(IOException e){
            System.err.println("Errore nel caricamento del file "+nomeFile);
        } catch(ClassNotFoundException e){
            System.err.println("Classe non trovata");
        }
        return null; //se si verifica un'eccezione, restituisce null
    }

}
