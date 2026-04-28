## Getting Started

Welcome to the VS Code Java world. Here is a guideline to help you get started to write Java code in Visual Studio Code.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).

## README “masticado”: Búsquedas Locales y Movimientos

Esta sección explica (muy paso a paso) cómo funciona la búsqueda local en este proyecto.
Si te pierdes, quédate con esta idea:

> Una **Búsqueda Local** (Shift/Swap/…) intenta encontrar **un movimiento** que mejore la solución.
> El movimiento calcula su mejora con **Δ-coste** (delta) y, si es buena, se aplica.

### 0) Vocabulario mínimo

- **Problema** (`model.Problema`): tiene los datos de la instancia (clientes, instalaciones, costes, incompatibilidades…).
- **Solución** (`model.Solucion`): dice cuánto suministro va de cada instalación a cada cliente.
	- Guarda:
		- `suministros[i][j]` = cantidad que la instalación `j` suministra al cliente `i`.
		- `capacidadRestante[j]` = capacidad libre en la instalación `j`.
		- `instalacionesAbiertas[j]` = si `j` está abierta.
		- `costeTotal` actualizado incrementalmente cuando haces `añadirSuministro/quitarSuministro`.

### 1) Qué es un “Move” (movimiento)

Un **Move** es un objeto que representa “una acción candidata” de búsqueda local.

Archivo: `src/localsearch/moves/Move.java`

Un `Move` sabe 2 cosas:

1) **Cuánto cambia el coste si lo aplicas** (el Δ-coste)

```java
double delta(Problema problema, Solucion sol);
```

Regla: si `delta < 0`, mejora. Si `delta > 0`, empeora.

2) **Cómo aplicarse** sobre una solución

```java
void apply(Solucion sol);
```

Esto es importante porque así:

- La búsqueda local solo se dedica a “buscar el mejor”.
- El movimiento se dedica a “calcular delta y aplicarse”.

### 2) Por qué usamos `Optional<Move>`

Cuando buscas un movimiento, puede pasar que:

- Encuentras uno (o varios) factibles → devuelves el mejor.
- No hay ninguno factible → no puedes mejorar.

En vez de devolver `null`, devolvemos `Optional<Move>`:

- `Optional.of(move)` significa “sí hay movimiento”.
- `Optional.empty()` significa “no hay movimiento”.

Archivo: `src/localsearch/BusquedaLocal.java`

```java
Optional<Move> encontrarMejorMovimiento(Solucion sol, Problema problema);
```

### 3) La interfaz `BusquedaLocal` (cómo se aplica 1 movimiento)

Archivo: `src/localsearch/BusquedaLocal.java`

#### 3.1) `EPS`

```java
double EPS = 0.001;
```

Sirve para no aplicar “mejoras” ridículas por errores de coma flotante.
Por ejemplo, si `delta = -0.00000001`, eso no es una mejora real.

#### 3.2) `aplicarMejorMovimiento(...)`

Idea en castellano:

1) Pide a la vecindad el mejor movimiento
2) Si no hay, termina
3) Si hay, calcula su delta
4) Si `delta < -EPS`, aplica el movimiento

Esto garantiza: **como máximo 1 movimiento aplicado por llamada**.

#### 3.3) `mejorar(...)`

```java
Solucion copia = new Solucion(solucionActual);
aplicarMejorMovimiento(copia, problema);
return copia;
```

O sea: nunca tocas la solución original; siempre trabajas con una copia.

### 4) Compatibilidad / Incompatibilidades

Regla: un cliente no puede estar en una instalación junto con clientes incompatibles.

La comprobación está centralizada en:

- `model.Problema.esCompatible(Solucion sol, int clienteId, int instalacionId)`

Por eso todas las búsquedas locales llaman a `problema.esCompatible(...)`.

---

## 5) Movimientos concretos (los que existen en el código)

### 5.1) `ShiftMove` (mover un cliente de A a B)

Archivo: `src/localsearch/moves/ShiftMove.java`

Representa:

> Quitar `cantidad` del cliente `c` en `instOrigen`, y añadir la misma cantidad en `instDestino`.

#### Qué hace `delta(...)`

Calcula:

- **Delta transporte** = `cantidad * (coste(c, destino) - coste(c, origen))`
- **Delta fijo**:
	- si el destino estaba cerrado y al añadirlo se abre → suma coste fijo
	- si el origen se queda vacío y se cierra → resta coste fijo

Ejemplo mini:

- Cliente 0 tiene 10 unidades en instalación 2.
- Si lo mueves a instalación 5:
	- transporte cambia: `10*(c[0][5]-c[0][2])`
	- si la 5 estaba cerrada: `+ fijo(5)`
	- si la 2 se queda vacía: `- fijo(2)`

#### Qué hace `apply(...)`

Llama a:

```java
sol.quitarSuministro(clienteId, instOrigen, cantidad);
sol.añadirSuministro(clienteId, instDestino, cantidad);
```

Y como `Solucion` actualiza `costeTotal` automáticamente, la solución queda consistente.

### 5.2) `SwapClientesMove` (intercambiar “suministro principal”)

Archivo: `src/localsearch/moves/SwapClientesMove.java`

Este movimiento intercambia dos asignaciones:

- Cliente `i1` tenía `q1` en `j1`
- Cliente `i2` tenía `q2` en `j2`

Después del swap:

- `i1` pasa a `j2` con `q1`
- `i2` pasa a `j1` con `q2`

`delta(...)` calcula el cambio de transporte de ambas partes.

`apply(...)` hace 4 operaciones: quitar, quitar, añadir, añadir.

### 5.3) `CompositeMove` (un movimiento con muchas operaciones)

Archivo: `src/localsearch/moves/CompositeMove.java`

Sirve cuando un “movimiento” en realidad son muchas reasignaciones.
Por ejemplo: cerrar una instalación y recolocar muchos clientes.

#### ¿Qué es `record Operation(...)`?

Dentro de `CompositeMove` hay esto:

```java
public record Operation(int clienteId, int instalacionId, double cantidad, boolean esAdd) {}
```

Un `record` es una forma corta de crear una clase de “datos puros” e inmutable.

Esta `Operation` significa:

- `clienteId`: a qué cliente afecta
- `instalacionId`: a qué instalación
- `cantidad`: cuánto suministro
- `esAdd`:
	- `true` → hay que **añadir** suministro
	- `false` → hay que **quitar** suministro

Ejemplo:

- `Operation(3, 7, 5.0, false)` = “quita 5 unidades al cliente 3 desde la instalación 7”.
- `Operation(3, 2, 5.0, true)` = “añade 5 unidades al cliente 3 en la instalación 2”.

#### Por qué `CompositeMove.delta(...)` devuelve un número ya precomputado

`CompositeMove` guarda `precomputedDelta`. Eso significa:

- La vecindad calcula el delta una vez y lo almacena.
- `delta(...)` solo devuelve ese número.

Ventaja: el movimiento no depende de recomputar nada raro ni de mirar `getCosteTotal()`.

---

## 6) Búsquedas locales (vecindades) concretas

Todas viven en `src/localsearch/` y todas implementan `BusquedaLocal`.

### 6.1) `Shift` (vecindad de ShiftMove)

Archivo: `src/localsearch/Shift.java`

Qué hace:

1) Recorre todos los clientes `i`
2) Recorre todas las instalaciones origen `j1`
	 - si `suministros[i][j1] <= 0`, no hay nada que mover
3) Recorre todas las instalaciones destino `j2`
	 - `j2 != j1`
	 - `capacidadRestante[j2] >= cantidad`
	 - `problema.esCompatible(sol, i, j2)`
4) Para cada candidato, crea `ShiftMove` y calcula su `delta`
5) Se queda con el menor delta (el más negativo = la mejor mejora)
6) Devuelve `Optional.of(mejorMove)` o `Optional.empty()`

### 6.2) `SwapClientes` (vecindad de SwapClientesMove)

Archivo: `src/localsearch/SwapClientes.java`

Qué entiende por “instalación principal” de un cliente:

> La instalación `j` donde `suministros[cliente][j]` es máximo.

Entonces prueba parejas de clientes `(i1, i2)`, obtiene sus principales `(j1, j2)` y cantidades `(q1, q2)`,
comprueba si el swap es factible (capacidad + compatibilidad) y si lo es calcula el delta.

### 6.3) `SwapInstalaciones` (cerrar una abierta y abrir una cerrada)

Archivo: `src/localsearch/SwapInstalaciones.java`

Objetivo:

> Elegir una instalación abierta `jOpen` que quiero cerrar, y una cerrada `jClosed` que podría abrir,
> y mover todos los suministros que estaban en `jOpen` a `jClosed` o a otras abiertas.

Por qué es “más complicado”:

- Para saber si es factible hay que ir cliente a cliente y ver si cabe/si es compatible.
- A veces hay que repartir un cliente entre varias instalaciones.
- Por eso construye una lista de operaciones (`List<Operation>`).

Punto clave: **simular y deshacer (rollback)**

- Mientras construye el plan, va aplicando `añadir/quitar` sobre `sol` para poder mirar capacidades reales.
- Pero al final, antes de devolver el `Move`, hace `rollback(...)` para dejar la solución como estaba.
- El `CompositeMove` guardará las operaciones, y si se aplica de verdad se ejecutarán otra vez.

### 6.4) `EliminarIncompatibilidad`

Archivo: `src/localsearch/EliminarIncompatibilidad.java`

Idea (simplificada):

1) Busca la instalación abierta “más cara” (coste fijo + transporte actual de lo que sirve).
2) Dentro de esa instalación elige un cliente “problemático” (según su contador de incompatibilidades).
3) Intenta mover TODO su suministro a otra instalación abierta compatible con capacidad.
4) Usa `ShiftMove` como movimiento.

---

## 7) Ejemplo completo (mini) de cómo se ejecuta todo

Imagina que GVNS/GRASP tiene una lista de búsquedas locales:

```java
List<BusquedaLocal> bls = List.of(new Shift(), new SwapClientes(), new SwapInstalaciones(), new EliminarIncompatibilidad());
```

Para cada búsqueda local (una por una):

1) Llama a `mejorar(solActual, problema)`.
2) Esa búsqueda crea una copia y llama a `aplicarMejorMovimiento(copia, problema)`.
3) Dentro:
	 - `encontrarMejorMovimiento(...)` propone el mejor `Move` posible.
	 - Si existe y su `delta < -EPS`, se aplica.
4) Devuelve la copia mejorada (o igual si no había mejora).

Resultado:

- Código más limpio (sin repetir “Movimiento/Plan” en cada vecindad).
- Una única forma estándar de aplicar movimientos.
- Siempre como máximo 1 movimiento por llamada (como pediste).
