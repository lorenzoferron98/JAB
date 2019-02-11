# Intoduzione
> Java Appointment Book (JAB) is a free CLI desktop application that allows private users to manage appointments and 
bookings.

<p style='text-align: justify;'>
Il progetto è stato sviluppato seguendo il paradigma di programmazione orientata agli oggetti (OOP). Il programma 
modella la realtà descritta assegnado ad ogni attore, coinvolto in essa, un ruolo ben definito e circoscritto dalle
sue operazioni. <br>
Il termine attore designa un componente, a volte semplificato, della realtà da descrivere, non necessariamente animato. 
I principali attori individuati sono rappresentati dalle classi <i>Appointment</i> e <i>Book</i>. La classe che permette 
l'interfacciamento con le due classi sopracitate è <i>GUI</i>.
</p>

Si allega alla presente documentazione:
* l'eseguibile *JAB.jar*;
* la cartella *lib/*, nella quale sono contenute le librerie usate nel progetto;
* le *resources* nelle directory *src* e *test* per provare il programma con un *dataset* già definito
* [link](https://github.com/lorenzoferron98/JAB) al progetto GitHub

### Usage
```bash
java -jar JAB.jar [filename]
```
`filename` è il percorso del file (CSV) che può essere caricato nel programma. Eventuali parametri aggiuntivi, oltre a 
`filename`, passati su linea di comando saranno ignorati.

# Scelte progettuali
Nel seguito verrano brevemente discusse le scelte fatte per le classi citate nella sezione precedente.

## Appointment
La classe costruisce un nuovo appuntamento chiedendo come input quattro `String` e un `int`:
```java
public final class Appointment {
    public Appointment(String date, String startTime, int duration, String description, String place) {
        /*...*/
    }
}
```
Facendo uso dei medoti `public` *Setter* si validano e si assegnano i parametri per il nuovo oggetto. Si noti l'uso della
*keyword* `final` nella definizone di classe, essa è stata necessaria perché si è fatto riferimento a metodi pubblici
(*Setter*) nel construttore. Tale approccio è stato adottato per evitare codice ridontate, necessario per la correttezza
dei parametri. <br>
Generalmente l'uso di metodi pubblici o privati, i quali a loro volta referenziano i primi, nel costruttore è fortemente
sconsigliato.

Altre interessante implementazione riguarda i metodi:
```java
public final class Appointment {
    public Instant getStartInstant() {
        /*...*/
    }
    
    public Instant getEndInstant() {
        /*...*/
    }
}
```
Essi permettono di conoscere gli istanti in cui comincia e finisce un appuntamento. Insieme possono essere considerati 
come `Interval` e suffrattando i metodi `equals` e `compareTo` si può facilmente verificare se due appuntamenti 
condividono parte del loro tempo(== si sovrappongo).

## Book
Nella seguente classe si vogliono mettere in evidenza due metodi:
```java
public class Book {
    public Map<Appointment, Appointment> loadBookFromFile() {
        /*...*/
    }
}
```
* Esso permette di mantenere una corrispondenza diretta, mediante l'uso di un `HashMap`, tra due appuntamenti che si 
sovrappongono: uno dei due è presente in `book`, l'altro invece non può esserlo perché altrimenti l'agenda non sarebbe
consistente con l'iserimento fatto dai metodi `add(...)`.


Si fa presente, in questo come in altri metodi, anche della classe `GUI`, l'uso di un `LOGGER`. Tale scelta è stata fatta
per avere un maggior grado di dettaglio qualora il programma riscontri un errore. Tra i principali vantaggi dei logger si
ricordano la possibilità di disabilitarli e impostare il livello di errore. In questo progetto è stata presa la seguente
convenzione:
* `LOGGEER.warning(msg)`: usato per errori gravi (esempio errore di *parsing* di un appuntamento);
* `LOGGER.info(msg)`: errori non gravi (esempio scelta non corretta dell'operazione)

Questo metodo deve stampare sul terminale, con `LOGGER`, gli eventuali errori di *parsing* del file perché altrimenti
non vi sarebbe modo, all'eventuale metodo chiamante, di mostrarli **TUTTI**, a meno che non si faccia uso di qualche 
struttura dati che li conservi.

```java
public class Book {
    public List<Appointment> getSortedBook() {
        /*...*/
    }
}
```
* L'interessante implementazione di questo metodo è quella di creare un clone del `book`, riordinarlo e restituirlo al 
chiamante. La scelta implemenetativa verte sul fatto che non si vuole mostrare verso l'esterno il reale contenuto della
struttura (`ArrayList`), che conserva gli appuntamenti. Essa infatti potrebbe essere modificata da operazioni non definite
dalla classe `Book`.


## GUI
La realizzazzione dell'interfaccia testuale è avvenuta facendo uso dell'[ASCII art](https://it.wikipedia.org/wiki/ASCII_art)
e del [Box-drawing character](https://en.wikipedia.org/wiki/Box-drawing_character). Quest'ultimo aspetto è stato facilitato
grazie all'uso di una [libreria](https://github.com/vdmeer/asciitable), i cui file sono contenuti nella directory *lib/*. <br>
Tuttavia quest'ultima non implementa un metodo per re-inizializzare la `AsciiTable`, senza dover istanziare un nuovo 
oggetto. Per tale motivo si cerca di ridurre al minimo lo spreco di memoria istanziando un nuovo oggetto `AsciiTable` 
solo quando il numero di colonne della tabella varia. Per ulteriori dettagli si veda 
[documentazione](http://www.vandermeer.de/projects/skb/java/asciitable/apidocs/index.html) e si cerchi di capire la 
differenza (visto il nome) tra i seguentoi metodi:
```java
public class GUI {
    // ======================================================
    // (START) ASCII TABLE METHODS
    // ======================================================
    
        private void setupAsciiTable() {
            /*...*/
        }
        
        private void initAsciiTable(boolean search) {
            /*...*/
        }
}
```
Qualsiasi modifica a `book` richiede prima una ricerca, per tale motivo il codice è stato strutturato in modo tale che i 
metodi `editAction()`, `deleteAction()` e `searchAction()` invochino come primo metodo `methodsBasedOnSearch(msg)` e 
successivamente richiedano un criterio di ricerca, si veda il metodo `search()`.

Per permettere una maggiore UX (*User Experience*) si è implementato il medoto `clearScreen()`, il quale funziona solo con
terminali o emulatori di terminale che supportano lo standard 
[ANSI escape code](https://en.wikipedia.org/wiki/ANSI_escape_code). Windows 10 lo supporta nativamente solo dal 
[2016](https://en.wikipedia.org/wiki/ANSI_escape_code#Windows).
