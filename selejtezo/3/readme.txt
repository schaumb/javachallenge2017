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
C + igen fele hajlok.

konstruktor != metódus.
static metódus -> osztály lock
non-static metódus -> object lock

D ? :D 

E - Senki nem definiálja ezt.
