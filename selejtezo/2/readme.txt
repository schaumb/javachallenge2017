2. Mely állítás igaz az alábbi kódrészletre?

public class ConcurrentContainer2<K,V> {

	private Map<K, V> container = Collections.synchronizedMap(new HashMap<>());

	public boolean addValue(K key, V value){
		if(!container.containsKey(key)){
			container.put(key, value);
			return true;
		}
		return false;
	}
	
	public V getValue(K key){
		return container.get(key);
	}
	
}

A
addValue(key, value) igaz visszatérése után közvetlenül következő getValue(key) value értéket adja vissza
B
addValue(key, value) hamis visszatérése után közvetlenül következő getValue(key) nem value értéket adja vissza
C
szálbiztos
D
A hash alapú tárolás miatt K osztálynak implementálnia kell a hashCode és equals metódusokat
E
A hash alapú tárolás miatt V osztálynak implementálnia kell a hashCode és equals metódusokat


--------------------

Hát. Szívem szerint egyik sem igaz. 

A - van az a HashCode, amivel meg lehet hackelni a rendszert - ha kell, kreálok
B - triviálisan nope - ha kétszer ugyanazt tesszük be
C - hmm, https://stackoverflow.com/questions/21792030/threadsafe-vs-synchronized

        The definition of thread safety given in Java Concurrency in Practice is:

            A class is thread-safe if it behaves correctly when accessed from multiple threads, 
            regardless of the scheduling or interleaving of the execution of those threads 
            by the runtime environment, and with no additional synchronization or other coordination 
            on the part of the calling code.

        Egyszerre többen hívhatják meg az addValue -t, és akár több igaz érték is visszatérhetődik 
        anélkül hogy beletevődne ténylegesen.

D - nem "kell".
E - nem "kell", amúgy is marhaság, a kulcsra számol hashCode-ot.

Nem jelölök meg semmit.
