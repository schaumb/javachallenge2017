4. Az alábbi kódra mely állítás(ok) igaz(ak)?

public interface StreamUtil {
    public static<T> java.util.stream.Stream<T> stream( final java.util.Collection<T> collection ) {
        if( collection instanceof java.util.LinkedList<?> ||
            collection instanceof java.util.concurrent.LinkedBlockingQueue<?>) {
            return collection.parallelStream();
        }
        return collection.stream();
    }
}
A
null pointer hibával elszállhat
B
a programozó hibásan írta meg a kódot, a két return utasítást fel kell cserélni
C
a LinkedBlockingQueue helyett BlockingQueue-t kellene használni
D
teljesen felesleges a használata, a Java 8-as verziója teljeskörűen kezeli a problémát
E
kis CPU igényű műveleteknél van értelme a függvény használatának

----------------------

A : igaz, lsd kódrészlet
B : igen, lsd https://blog.oio.de/2016/01/22/parallel-stream-processing-in-java-8-performance-of-sequential-vs-parallel-stream-processing/
C : igen, lsd fentebbi link
D : nem sajna, ez is a link alatt
E : vicceske. Kis CPU igénynél lenne jó a szekvenciális.