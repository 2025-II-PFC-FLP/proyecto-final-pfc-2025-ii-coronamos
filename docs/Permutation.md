# Costo Movilidad 

## Definicion matematica 
$CMFΠ=j=0∑n−2DF[πj,πj+1]$

Especificación Formal
Para toda finca f, programación pi válida y matriz d de dimensión n×nn \times n
n×n:


costoMovilidad(f, pi, d) = CMFΠCM_F^\Pi
CMFΠ
El resultado es determinista

Correctitud por Inducción
Caso base (n≤1)(n \leq 1)
(n≤1):
No hay movimientos, retorna 0. ✓
Paso inductivo: Para n>1n > 1
n>1:


progToPermPure(pi) genera la permutación π\pi
π correcta

Itera sobre $j∈[0,n−2]j \in [0, n-2] j∈[0,n−2]$
Calcula $D[πj,πj+1]D[\pi_j, \pi_{j+1}] D[πj,πj+1] $ para cada par consecutivo

.sum acumula exactamente la definición formal

Conclusión: Correcta para todo $n≥0n \geq 0 n≥0$.

---

# Permutación 

### Especificación
Dado vector vv
v de longitud nn
n, genera todas las n!n!
n! permutaciones.

Correctitud por Inducción Estructural
Caso base: ``v=∅⇒v = \emptyset \Rightarrow``
``v=∅⇒ ``retorna ``{∅}\{\emptyset\}``
``{∅} `` (1 permutación vacía).

Paso inductivo: Para $∣v∣=n>0|v| = n > 0$
$∣v∣=n>0:$


Selecciona cada $viv_i$
vi como cabeza (itera sobre
i)
Calcula recursivamente permutaciones de $v∖{vi}v \setminus \{v_i\} v∖{vi}$ $(longitud n−1n-1  n−1)$

Por H.I., el resto tiene $(n−1)!(n-1)! (n−1)!$ permutaciones

Prefijar viv_i
vi a cada una genera $n×(n−1)!=n!n \times (n-1)! = n! n×(n−1)!=n!$ permutaciones distintas


Conclusión: Genera exactamente n!n!
n! permutaciones correctas.

---

# PermToProg

### Especificación
Convierte permutación ``[π0,π1,...,πn−1][\pi_0, \pi_1, ..., \pi_{n-1}] [π0,π1,...,πn−1] `` 
a programación donde ``prog(i)\text{prog}(i)``
``prog(i)`` es el turno del tablón ii
i.

Correctitud

Para cada tablón ii
i,
``indexOf(i)`` encuentra posición jj
j donde ``perm(j)=i\text{perm}(j) = i``
``perm(j)=i``
Significa: tablón ii
i se riega en turno jj
j
Por tanto, ``prog(i)=j\text{prog}(i) = j``
``prog(i)=j`` es correcto


Ejemplo: perm=[2,0,1] ⇒ prog=[1,2,0]

- Tablón 0 está en posición 1 → turno 1 
- Tablón 1 está en posición 2 → turno 2 
- Tablón 2 está en posición 0 → turno 0 

---
 # GenerarProgramacionesRiego

### Especificación

Genera todas las programaciones posibles para finca de nn
n tablones.

Correctitud (por composición)

Crea vector base ``[0,1,...,n−1][0, 1, ..., n-1]
[0,1,...,n−1]``
- permutations genera $n!n!n!$

- ``permutaciones`` (correcto por §2)
- ``permToProg`` convierte cada una a programación (correcto por §3)
- Resultado: $n!n!n!$ programaciones válidas distintas
---
# ProgramaciónRiegoOptimo

### Especificación

$Salida:Π tal que CRFΠ + CMFΠ es minimo$

Correctitud (búsqueda exhaustiva)

- Genera todas las programaciones ${Π1,...,Πn!}\{\Pi_1, ..., \Pi_{n!}\} {Π1,...,Πn!} (correcto por §4)$
- Para cada $Πi\Pi_i Πi$, calcula $total=CRFΠi+CMFΠi\text{total}_i = CR_F^{\Pi_i} + CM_F^{\Pi_i} totalidad=CRFΠi+CMFΠi$
- ``minBy(_._2)`` selecciona tupla con costo mínimo
- Garantía: Como evalúa todas las soluciones, encuentra el óptimo global

Complejidad: $O(n!⋅n)O(n! \cdot n)O(n!⋅n)$ - exploración exhaustiva.

---

# Diagrama de Mermaid

````mermaid
flowchart TD
    A["ProgramacionRiegoOptimo(f, d)"] --> B["generarProgramacionesRiego(f)"]
    B --> C["permutations([0..n-1])"]
    C --> D["Genera n! permutaciones"]
    D --> E["permToProg() para cada perm"]
    E --> F["Vector[ProgRiego] (n! elementos)"]
    F --> G["map: evaluar cada π"]
    G --> H["costoRiegoFinca(f, π)"]
    G --> I["costoMovilidad(f, π, d)"]
    H --> J["total = CR + CM"]
    I --> J
    J --> K["minBy(_._2)"]
    K --> L["(π_óptimo, costo_mínimo)"]
    
    style C fill:#ffcccc
    style E fill:#ccffcc
    style K fill:#ccccff
````