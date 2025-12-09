# Informe del Proyecto: Optimización de Programación de Riego en Finca

**Integrantes del Grupo:**
- Nombre: Juan Diego Ospina, Código: 2359486
- Nombre: Juan Felipe Ruiz, Código: 2359397
- Nombre: Mauricio Alejandro Cardenas, Código: 2359701
- Nombre: Jhorman Ricardo Loaiza, Código: 2359710

**Fecha:** Diciembre 08, 2025

## Informe de Procesos

En esta sección, se analizan los procesos generados por las funciones recursivas del programa, enfocándonos en las funciones de generación de permutaciones (`permutations` en la versión secuencial y `perms` en la paralela). Estas funciones son clave para generar todas las posibles programaciones de riego, ya que exploran exhaustivamente las permutaciones de los tablones.

Se utilizan ejemplos con valores pequeños (n=2 y n=3) para ilustrar la pila de llamadas recursivas. La pila se representa como una secuencia de llamadas, mostrando cómo se acumulan y resuelven.

### Ejemplo para `permutations` (Versión Secuencial)
Esta función genera permutaciones de un vector `v` de manera recursiva. Para cada índice `i`, selecciona un elemento y recursa sobre el resto del vector.

**Ejemplo con n=2:** `v = Vector(0, 1)`
- Llamada inicial: `permutations(Vector(0,1))`
    - i=0: elem=0, resto=Vector(1) → Recursa: `permutations(Vector(1))`
        - i=0: elem=1, resto=Vector() → Retorna: `Vector(Vector())` → Agrega: `Vector(Vector(0,1))`
    - i=1: elem=1, resto=Vector(0) → Recursa: `permutations(Vector(0))`
        - i=0: elem=0, resto=Vector() → Retorna: `Vector(Vector())` → Agrega: `Vector(Vector(1,0))`
- Pila de llamadas (máxima profundidad: 2):
    1. `permutations(Vector(0,1))`
    2. `permutations(Vector(1))` (para i=0) → Resuelve y retrocede.
- Resultado final: `Vector(Vector(0,1), Vector(1,0))`

**Ejemplo con n=3:** `v = Vector(0,1,2)`
- Llamada inicial: `permutations(Vector(0,1,2))`
    - i=0: elem=0, resto=Vector(1,2) → Recursa: `permutations(Vector(1,2))`
        - Subpila: Genera `Vector(1,2)` y `Vector(2,1)` → Agrega 0 al frente: `Vector(0,1,2)`, `Vector(0,2,1)`
    - i=1: elem=1, resto=Vector(0,2) → Recursa: `permutations(Vector(0,2))`
        - Subpila: Genera `Vector(0,2)`, `Vector(2,0)` → Agrega 1 al frente: `Vector(1,0,2)`, `Vector(1,2,0)`
    - i=2: elem=2, resto=Vector(0,1) → Recursa: `permutations(Vector(0,1))`
        - Subpila: Genera `Vector(0,1)`, `Vector(1,0)` → Agrega 2 al frente: `Vector(2,0,1)`, `Vector(2,1,0)`
- Pila de llamadas (máxima profundidad: 3):
    1. `permutations(Vector(0,1,2))`
    2. `permutations(Vector(1,2))` (para i=0)
    3. `permutations(Vector(2))` (subllamada) → Resuelve y retrocede secuencialmente.
- El proceso es lineal y secuencial, expandiéndose en anchura por el `flatMap`.

### Ejemplo para `perms` (Versión Paralela en `generarProgramacionesRiegoPar`)
Esta función es similar pero paraleliza el nivel superior usando `base.par.flatMap`. Para cada elemento inicial, genera permutaciones del resto de manera secuencial, pero las ejecuciones para diferentes inicios son paralelas.

**Ejemplo con n=2:** `base = Vector(0,1)`
- Nivel superior paralelo: `base.par.flatMap { i => ... }`
    - Hilo 1 (i=0): resto=Vector(1) → `perms(Vector(1))` → `Vector(Vector(1))` → Agrega: `Vector(Vector(0,1))`
    - Hilo 2 (i=1): resto=Vector(0) → `perms(Vector(0))` → `Vector(Vector(0))` → Agrega: `Vector(Vector(1,0))`
- Pila por hilo (profundidad: 2): Independientes, resuelven en paralelo.
- Resultado: Igual al secuencial, pero potencialmente más rápido para n mayor.

**Ejemplo con n=3:** Similar al secuencial, pero el `flatMap` sobre índices es paralelo, distribuyendo subárboles recursivos en hilos.

El proceso recursivo en ambas versiones es arborescente, con expansión factorial, pero la paralela reduce el tiempo al distribuir ramas.

## Informe de Paralelización

La estrategia de paralelización se centra en las partes computacionalmente intensivas: generación de permutaciones y evaluación de costos.

- **Generación de programaciones (`generarProgramacionesRiegoPar`):** Se paraleliza el nivel superior de permutaciones usando `base.par.flatMap`, donde cada hilo genera permutaciones empezando por un elemento diferente. Esto distribuye el trabajo factorial entre núcleos.
- **Evaluación de costos (`costoRiegoFincaPar` y `costoMovilidadPar`):** Se usa `par.map` para calcular costos por tablon o movimiento en paralelo.
- **Optimización global (`ProgramacionRiegoOptimoPar`):** Combina lo anterior, paralelizando la evaluación de todas las programaciones con `progs.par.map`.

Según la Ley de Amdahl, la aceleración máxima es \( S = \frac{1}{(1-p) + \frac{p}{N}} \), donde \( p \) es la fracción paralelizable y \( N \) es el número de núcleos (16 detectados).

Resultados de benchmarks (BenchManual, reps=5, tamaños=6,7,8):

| Tamaño de la finca (tablones) | Versión secuencial (ms) | Versión paralela (ms) | Aceleración (%) |
|-------------------------------|--------------------------|------------------------|-----------------|
| 6                             | 6.129 ± 0.366           | 12.329 ± 0.978        | -50.29          |
| 7                             | 40.303 ± 4.040          | 34.021 ± 8.560        | 18.46           |
| 8                             | 291.741 ± 9.000         | 132.351 ± 26.915      | 120.43          |

- Para n=6: Overhead domina (p ≈ 0%), no hay ganancia.
- Para n=7: Mejora moderada (p ≈ 16.62%), parte secuencial limita.
- Para n=8: Buena aceleración (p ≈ 58.28%), paralelismo efectivo en generación/evaluación factorial.

Scalameter confirma tiempos secuenciales: n=6: 5.142 ms, n=7: 45.800 ms, n=8: 295.469 ms.

En resumen, para n pequeños, el overhead de paralelismo (creación de hilos) penaliza; para n>7, las ganancias crecen con la complejidad factorial, alineándose con Amdahl: más p implica mejor escalabilidad.

## Informe de Corrección

### Argumentación sobre la Corrección

Usamos notación matemática para argumentar la corrección. Asumimos que las funciones son correctas si satisfacen sus especificaciones para todo input válido.

- **tIR y tIR_fromPerm:** Calcula tiempos de inicio basados en permutación. Correcto porque acumula \( t_{j+1} = t_j + t_{reg}(perm(j)) \), con \( t_0 = 0 \). Matemáticamente: \( TIR_i = \sum_{k=0}^{turno(i)-1} t_{reg}(perm(k)) \), donde turno(i) es el índice en la permutación. Pruebas exhaustivas confirman coincidencia con cálculo manual.

- **costoRiegoTablon:** Para tablon i, si \( t \leq t_{sup} - t_{reg} \), costo = \( t_{sup} - (t + t_{reg}) \geq 0 \); sino, multa = \( p_i \times ((t + t_{reg}) - t_{sup}) \). Correcto por definición condicional, cubre casos tempranos/tardíos.

- **costoRiegoFinca:** Suma de costos individuales: \( \sum_{i=0}^{n-1} costo(i) \). Correcto si cada costo individual lo es (por inducción sobre n).

- **costoMovilidad:** Suma de distancias en permutación: \( \sum_{j=0}^{n-2} d(perm(j), perm(j+1)) \). Correcto por iteración lineal sobre permutación.

- **generarProgramacionesRiego y Par:** Genera todas las permutaciones de {0..n-1} convertidas a ProgRiego. Correcto porque permutations/perms generan exactamente n! permutaciones únicas (por teoría de permutaciones), y permToProg mapea biyectivamente.

- **ProgramacionRiegoOptimo y Par:** Encuentra min por evaluación exhaustiva. Correcto si generación cubre todo el espacio y costos son precisos (minBy sobre conjunto completo).

Las versiones paralelas son equivalentes a secuenciales por preservación de semántica en colecciones paralelas.

### Casos de Prueba

Se incluyen al menos 5 pruebas por función principal en `RiegoTest.scala` (ya implementadas). Ejemplos:

- **tIR:** 5 tests (básico, iguales, invertidos, diferentes treg, 1 tablon). Todos pasan.
- **costoRiegoTablon:** 5 tests (sin multa, prioridad alta, con multa, justo a tiempo, multas grandes). Pasan.
- **costoRiegoFinca:** 5 tests (básico, atraso con multas, sin multas, orden invertido, 1 tablon). Pasan.
- **costoMovilidad:** 5 tests (simple, asimétrica, 1 tablon, lineales, grande). Pasan.
- **generarProgramacionesRiego:** 5 tests (3 tablones=6, contiene ordenada, invertida, 4=24, distintas). Pasan.
- **ProgramacionRiegoOptimo:** 5 tests (óptimo básico, asimétricas, 1 tablon, 4 tablones, perm válida). Pasan.
- **Paralelas (costoRiegoFincaPar, etc.):** 5 tests cada una, coinciden con secuenciales. Pasan.

Todas las 55+ pruebas en la suite pasan, evidenciando corrección.

## Conclusiones

El proyecto implementa una solución exhaustiva para optimizar riego, con paralelismo efectivo para n≥7, logrando aceleraciones >100% en n=8. La corrección se valida por argumentos matemáticos y pruebas exhaustivas. Limitaciones: escalabilidad factorial (O(n!)), sugerimos heuristics para n>10. Futuro: integrar más paralelismo o algoritmos aproximados. El paralelismo demuestra ganancias reales alineadas con Amdahl, destacando la importancia de fracciones paralelizables altas.