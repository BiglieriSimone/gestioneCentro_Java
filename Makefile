# Variabili per evitare ripetizioni
JAR_NAME=GestioneCentro.jar
BIN_DIR=bin
DOC_DIR=doc
ENTRY_POINT=Main

.PHONY: all compile jar doc clean run reset

# Il comando di default: pulisce, compila e fa il jar
all: clean compile jar doc

# 1. Reset e pulizia delle build precedenti
clean:
	rm -rf $(BIN_DIR)
	rm -rf $(DOC_DIR)
	rm -f $(JAR_NAME)

# 2. Compilazione dei file sorgente
compile:
	mkdir -p $(BIN_DIR)
	javac -d $(BIN_DIR) Main.java model/*.java view/*.java logic/*.java

# 3. Creazione del file .jar eseguibile
jar:
	jar cfe $(JAR_NAME) $(ENTRY_POINT) -C $(BIN_DIR) .

# 4. Generazione della documentazione Javadoc
doc:
	mkdir -p $(DOC_DIR)
	javadoc -d $(DOC_DIR) $$(find . -name "*.java" -not -path "*/bin/*")

# 5. Esecuzione al volo del JAR appena creato
run:
	java -jar $(JAR_NAME)

# Comando combinato: compila tutto e avvia l'app immediatamente
reset: all run