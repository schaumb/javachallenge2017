3. Mely állítás igaz?
A
ha egy osztálynak nincsenek mezői, akkor szálbiztos
B
ha egy osztály csak szálbiztos mezőket tartalmaz, akkor szálbiztos
C
tetszőleges metódus kaphat synchronized modifyert
D
a java.lang csomag csak szálbiztos osztályokat tartalmaz
E
ha több szál módosít egy osztályt, akkor ConcurrentModificationException dobódik


------------------------------

A - false, nem muszáj a saját mezőiben tárolni az állapotát
B - false, lsd előző
C - false

https://stackoverflow.com/questions/23453568/what-is-the-reason-why-synchronized-is-not-allowed-in-java-8-interface-methods


D - false,

https://docs.oracle.com/javase/7/docs/api/java/lang/StringBuilder.html
java.lang.StringBuilder

E - Senki nem definiálja ezt.
