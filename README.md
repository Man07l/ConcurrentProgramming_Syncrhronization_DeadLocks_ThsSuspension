
## Escuela Colombiana de Ingeniería
### Arquitecturas de Software – ARSW


#### Ejercicio – programación concurrente, condiciones de carrera y sincronización de hilos. EJERCICIO INDIVIDUAL O EN PAREJAS.

##### Parte I – Antes de terminar la clase.

Control de hilos con wait/notify. Productor/consumidor.

1. Revise el funcionamiento del programa y ejecútelo. Mientras esto ocurren, ejecute jVisualVM y revise el consumo de CPU del proceso correspondiente. A qué se debe este consumo?, cual es la clase responsable?
   ![CPUbadPerformance](/media/img/img1.png)
- El consumo alto de la CPU se debe a la clase Consumer pues ejecuta un bucle infinito sin interrupciones y verifica constantemente si hay elementos en la cola.

2. Haga los ajustes necesarios para que la solución use más eficientemente la CPU, teniendo en cuenta que -por ahora- la producción es lenta y el consumo es rápido. Verifique con JVisualVM que el consumo de CPU se reduzca.  
   ![CPUgoodPerformance](/media/img/img2.png)
- Para reducir el consumo de la CPU se definió la cola como BlockingQueue para sincronizar el uso de la misma. Además se usaron metodos como take() y put() para no tener que realizar la verificación de disponibilidad de elementos en la cola.  

3. Haga que ahora el productor produzca muy rápido, y el consumidor consuma lento. Teniendo en cuenta que el productor conoce un límite de Stock (cuantos elementos debería tener, a lo sumo en la cola), haga que dicho límite se respete. Revise el API de la colección usada como cola para ver cómo garantizar que dicho límite no se supere. Verifique que, al poner un límite pequeño para el 'stock', no haya consumo alto de CPU ni errores.  
   ![stockQueue](/media/img/img3.png)  
   ![queueGoodPerformance](/media/img/img4.png)
- Definimos que la cola tenga un limite de 20 elementos y produzca de manera más rápida para que el consumidor tenga que esperar a que haya elementos disponibles en la cola. Al llegar al tope, se respeta el número establecido sin provocar un aumento en el consumo de CPU pues el tipo de cola definida (BlockingQueue) tiene un mecanismo de espera para cuando la cola está llena.  



##### Parte II. – Antes de terminar la clase.

Teniendo en cuenta los conceptos vistos de condición de carrera y sincronización, haga una nueva versión -más eficiente- del ejercicio anterior (el buscador de listas negras). En la versión actual, cada hilo se encarga de revisar el host en la totalidad del subconjunto de servidores que le corresponde, de manera que en conjunto se están explorando la totalidad de servidores. Teniendo esto en cuenta, haga que:

- La búsqueda distribuida se detenga (deje de buscar en las listas negras restantes) y retorne la respuesta apenas, en su conjunto, los hilos hayan detectado el número de ocurrencias requerido que determina si un host es confiable o no (_BLACK_LIST_ALARM_COUNT_).
- Lo anterior, garantizando que no se den condiciones de carrera.

![CorrectSearch](/media/img/img5.png)
![CorrectSearch2](/media/img/img6.png)

- Para hacer mas eficiente el codigo se implemento la sincronizacion de hilos utilizando las clases AtomicInteger y AtomicBoolean, esto garantiza que no se den condiciones de carrera pues se realiza la modificacion de variables compartidas de manera segura. Estas variables Atomicas funcionan como variables globales safe-thread, lo que permite una verificacion y modificacion de manera segura.

##### Parte III. – Avance para el martes, antes de clase.

Sincronización y Dead-Locks.

![](http://files.explosm.net/comics/Matt/Bummed-forever.png)

1. Revise el programa “highlander-simulator”, dispuesto en el paquete edu.eci.arsw.highlandersim. Este es un juego en el que:

	* Se tienen N jugadores inmortales.
	* Cada jugador conoce a los N-1 jugador restantes.
	* Cada jugador, permanentemente, ataca a algún otro inmortal. El que primero ataca le resta M puntos de vida a su contrincante, y aumenta en esta misma cantidad sus propios puntos de vida.
	* El juego podría nunca tener un único ganador. Lo más probable es que al final sólo queden dos, peleando indefinidamente quitando y sumando puntos de vida.

2. Revise el código e identifique cómo se implemento la funcionalidad antes indicada. Dada la intención del juego, un invariante debería ser que la sumatoria de los puntos de vida de todos los jugadores siempre sea el mismo(claro está, en un instante de tiempo en el que no esté en proceso una operación de incremento/reducción de tiempo). Para este caso, para N jugadores, cual debería ser este valor?.  
- Para N jugadores, el invariante que siempre se de cumplir es: $I = N \times \text{Health}_0$ donde $\text{Health}_0$ es la salud inicial de cada jugador y $N$ es la cantidad de jugadores.  

3. Ejecute la aplicación y verifique cómo funcionan las opción ‘pause and check’. Se cumple el invariante?.
   ![Pa&Ch1](/media/img/img7.png)
   ![Pa&Ch2](/media/img/img8.png)
   ![Pa&Ch3](/media/img/img9.png)  

4. Una primera hipótesis para que se presente la condición de carrera para dicha función (pause and check), es que el programa consulta la lista cuyos valores va a imprimir, a la vez que otros hilos modifican sus valores. Para corregir esto, haga lo que sea necesario para que efectivamente, antes de imprimir los resultados actuales, se pausen todos los demás hilos. Adicionalmente, implemente la opción ‘resume’.
- Implementamos una clase 'Controller' encargada de pausar, reanudar y verificar el estado de los hilos. De esta manera, en 'Pause and check' ponemos los hilos en espera e implementamos la opción 'Resume' que vuelve a correr los hilos. 

5. Verifique nuevamente el funcionamiento (haga clic muchas veces en el botón). Se cumple o no el invariante?.
   ![Pa&ChResume1](/media/img/img10.png)  
   ![Pa&ChResume2](/media/img/img11.png)  
- No se cumple el invariante a pesar de que los hilos se detienen al momento de la verificacion.

6. Identifique posibles regiones críticas en lo que respecta a la pelea de los inmortales. Implemente una estrategia de bloqueo que evite las condiciones de carrera. Recuerde que si usted requiere usar dos o más ‘locks’ simultáneamente, puede usar bloques sincronizados anidados:
- Al analizar las regiones criticas de la pelea de inmortales identificamos que en el método fight hay valores que no deben ser consultados y modificados en el mismo instante. 

7. Tras implementar su estrategia, ponga a correr su programa, y ponga atención a si éste se llega a detener. Si es así, use los programas jps y jstack para identificar por qué el programa se detuvo.
   ![DeadLock](/media/img/img12.png)
- Al analizar el jstack se puede ver que hay un deadlock en el metodo fight.

8. Plantee una estrategia para corregir el problema antes identificado (puede revisar de nuevo las páginas 206 y 207 de _Java Concurrency in Practice_).
- Para corregir el problema quitamos un synchronized del metodo fight, pues dentro del método ya habiamos definido un lock para los dos inmortales. Este triple bloqueo ocasionaba problemas de deadlocks a pesar de que ya se habia implementado una estrategia para evitar estos problemas (un orden especifico de adquisicion de locks).

9. Una vez corregido el problema, rectifique que el programa siga funcionando de manera consistente cuando se ejecutan 100, 1000 o 10000 inmortales. Si en estos casos grandes se empieza a incumplir de nuevo el invariante, debe analizar lo realizado en el paso 4.
- Para solucionar el problema de funcionalidad con valores grandes, tuvimos que verificar que todos los hilos estuvieran pausados antes de verificar la suma de vida e imprimir el resultado.
  ![100,1000,10000](/media/img/img13.png)
  ![100,1000,10000(2)](/media/img/img14.png)
- ![100,1000,10000(3)](/media/img/img15.png)

10. Un elemento molesto para la simulación es que en cierto punto de la misma hay pocos 'inmortales' vivos realizando peleas fallidas con 'inmortales' ya muertos. Es necesario ir suprimiendo los inmortales muertos de la simulación a medida que van muriendo. Para esto:
	* Analizando el esquema de funcionamiento de la simulación, esto podría crear una condición de carrera? Implemente la funcionalidad, ejecute la simulación y observe qué problema se presenta cuando hay muchos 'inmortales' en la misma. Escriba sus conclusiones al respecto en el archivo RESPUESTAS.txt.
	* Corrija el problema anterior __SIN hacer uso de sincronización__, pues volver secuencial el acceso a la lista compartida de inmortales haría extremadamente lenta la simulación.

- Tratar de implementar la funcionalidad requerida genera condición de carrera pues al eliminar al inmortal de la lista, su tamaño varia ocasionando conflictos en el momento de consultar su tamaño.
- No nos fue posible realizar modificaciones sobre la lista pues los hilos continuan en ejecución (continúan luchando) y llega un momento donde el pelea solo por la longitud de la lista generando errores de ejecucion.

11. Para finalizar, implemente la opción STOP.
- La opción STOP finaliza todo el proceso.

<!--
### Criterios de evaluación

1. Parte I.
	* Funcional: La simulación de producción/consumidor se ejecuta eficientemente (sin esperas activas).

2. Parte II. (Retomando el laboratorio 1)
	* Se modificó el ejercicio anterior para que los hilos llevaran conjuntamente (compartido) el número de ocurrencias encontradas, y se finalizaran y retornaran el valor en cuanto dicho número de ocurrencias fuera el esperado.
	* Se garantiza que no se den condiciones de carrera modificando el acceso concurrente al valor compartido (número de ocurrencias).


2. Parte III.
	* Diseño:
		- Coordinación de hilos:
			* Para pausar la pelea, se debe lograr que el hilo principal induzca a los otros a que se suspendan a sí mismos. Se debe también tener en cuenta que sólo se debe mostrar la sumatoria de los puntos de vida cuando se asegure que todos los hilos han sido suspendidos.
			* Si para lo anterior se recorre a todo el conjunto de hilos para ver su estado, se evalúa como R, por ser muy ineficiente.
			* Si para lo anterior los hilos manipulan un contador concurrentemente, pero lo hacen sin tener en cuenta que el incremento de un contador no es una operación atómica -es decir, que puede causar una condición de carrera- , se evalúa como R. En este caso se debería sincronizar el acceso, o usar tipos atómicos como AtomicInteger).

		- Consistencia ante la concurrencia
			* Para garantizar la consistencia en la pelea entre dos inmortales, se debe sincronizar el acceso a cualquier otra pelea que involucre a uno, al otro, o a los dos simultáneamente:
			* En los bloques anidados de sincronización requeridos para lo anterior, se debe garantizar que si los mismos locks son usados en dos peleas simultánemante, éstos será usados en el mismo orden para evitar deadlocks.
			* En caso de sincronizar el acceso a la pelea con un LOCK común, se evaluará como M, pues esto hace secuencial todas las peleas.
			* La lista de inmortales debe reducirse en la medida que éstos mueran, pero esta operación debe realizarse SIN sincronización, sino haciendo uso de una colección concurrente (no bloqueante).

	

	* Funcionalidad:
		* Se cumple con el invariante al usar la aplicación con 10, 100 o 1000 hilos.
		* La aplicación puede reanudar y finalizar(stop) su ejecución.
		
		-->

<a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/4.0/88x31.png" /></a><br />Este contenido hace parte del curso Arquitecturas de Software del programa de Ingeniería de Sistemas de la Escuela Colombiana de Ingeniería, y está licenciado como <a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/">Creative Commons Attribution-NonCommercial 4.0 International License</a>.
