1. Mely állítás(ok) igaz(ak) az alábbi kódrészletre?

public class ConcurrentContainer<T> {

	private static final String PRIVATE_LOCK_OBJECT = "Lock";
	private static final String PRIVATE_DOUBLELOCK_OBJECT = "DoubleLock";
	
	private Set<T> container = new HashSet<>(); 
	
	public void addObject(T object){
		synchronized (PRIVATE_LOCK_OBJECT) {
				synchronized (PRIVATE_DOUBLELOCK_OBJECT) {
				if (!container.contains(object)){
					container.add(object);
				}
			}
		}
	}
	
	public boolean removeObject(T object){
		synchronized (PRIVATE_LOCK_OBJECT) {
			synchronized (PRIVATE_DOUBLELOCK_OBJECT) {
				return container.remove(object);
			}
		}
	}
	
	@Override
	public String toString(){
		return "Content of the container: " + container;
	}
	
}

A
deadlock előfordulhat benne
B
ConcurrentModificationException-t dobhat
C
szálbiztos
D
az osztály Singleton


------------------------

A ? Úgy tudom hogy minden ConcurrentModificationException esetén lehetséges a deadlock, de nem biztos (azért jelöltem be, mert többet is be lehet jelölni)
B + Igen, mivel a toString() metódusban meghívódik az AbstractCollection :: toString(), ahol az iteráció közben dobódhat.
C - nooope -> AbstractCollection :: toString() metóddus hívás miatt nem.
D - Marhaság :D
