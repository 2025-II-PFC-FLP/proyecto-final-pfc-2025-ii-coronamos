# Informe de Corrección: Programación de Riego

---

## Argumentando sobre corrección de programas recursivos

Sea `` f:V → P(V) f:V→P(V)`` la función matemática que, dado un vector V V
de elementos distintos, devuelve el conjunto de todas las permutaciones posibles de dichos elementos. Sabemos por combinatoria que el tamaño del resultado debe ser
$∣V∣!$

Sea $P perm$ el programa recursivo desarrollado en Scala para calcular $f:$

````scala
def permutations[T](v: Vector[T]): Vector[Vector[T]] = {
  if (v.isEmpty) Vector(Vector())
  else {
    v.indices.toVector.flatMap { i =>
      val elem = v(i)
      val resto = v.patch(i, Nil, 1)
      permutations(resto).map(elem +: _)
    }
  }
}
````
---

## Demostración: 

- Caso base $n = 0$:
 si $v$ es vacio :
$P perm(v) → Vector(Vector())$
Matemáticamente, la única permutación de un conjunto vacío es el conjunto vacío mismo. El programa devuelve una colección conteniendo al conjunto vacío.
 ``` Pperm (∅)==f(∅)```
- Caso Inductivo $n = k+ 1 n=k+1 , con k ≥ 0  k≥0$
  Asumimos como Hipótesis de Inducción (HI) que para cualquier vector de tamaño
  k , la función permutations retorna correctamente todas las permutaciones de ese vector.
  $$
  P_{\text{perm}}(v)
  =
  \bigcup_{i=0}^{k}
  \left\{
  v[i] + : p \mid p \in P_{\text{perm}}(v \setminus \{v[i]\})
  \right\}
  $$
  Como el flatMap une los resultados de fijar cada elemento posible al inicio, se cubren todas las combinaciones posibles.
  Conclusión:
  $$
  \forall v:\; P_{\text{perm}}(v) = f(v)
  $$

---

 ## Argumento resumido de corrección de la optimización

Para justificar la corrección de ``ProgramacionRiegoOptimo``, se validan dos aspectos:

- la generación completa y sin omisiones del espacio de soluciones, y
- la selección correcta del mínimo global mediante la función ``minBy``.

### 1. Corrección de la generación del espacio de búsqueda

````scala
def generarProgramacionesRiego(f: Finca): Vector[ProgRiego] = {
  val base = (0 until f.length).toVector
  val perms = permutations(base)
  perms.map(permToProg)
}
````
$$
\text{permToProg}(p)
= \mathrm{Vector.tabulate}(n)\,\bigl(i \Rightarrow p.\mathrm{indexOf}(i)\bigr)
$$

Esto preserva la cardinalidad: a cada permutación corresponde exactamente una programación de riego y viceversa.

### Conclusión:
``generarProgramacionesRiego`` produce todas las programaciones válidas sin duplicados ni ausencias.

## 2. Corrección de la selección del óptimo

````scala
evaluaciones.minBy(_._2)
````

$$
\mathrm{Inv}(best,\,resto)
\;\equiv\;
\mathrm{Costo}(best)
\;\le\;
\mathrm{Costo}(x)
\quad \text{para todo } x \text{ ya recorrido}
$$

 - En cada paso:

Si el elemento actual tiene costo mayor o igual que best, el mínimo no cambia.

Si tiene menor costo, se actualiza best.

Mediante inducción sobre la lista:

Caso base: una lista de un solo elemento → ese es el mínimo.

Paso inductivo: si m_k es el mínimo de los primeros k elementos, entonces el mínimo de los primeros k+1 es
$$
\min(m_k,\; x_{k+1})
$$

--- 

## Conclusión final

### Dado que:

El espacio de búsqueda contiene todas las programaciones posibles.

El proceso iterativo de minBy selecciona el menor costo global.

Se cumple
$$
\text{ProgramacionRiegoOptimo}(f, d)
=\;
\underset{p \in \text{TodasLasPermutaciones}}{\arg\min}\;
\text{CostoTotal}(p)
$$

### Por tanto, el algoritmo es correcto respecto a su especificación de optimización por fuerza bruta.