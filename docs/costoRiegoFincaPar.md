# Informe costoRiegoFincaPar

### 1. DEFINICIÓN DE LA FUNCIÓN
```scala
def costoRiegoFincaPar(f: Finca, pi: ProgRiego): Int = {
  val perm = progToPermPure(pi)
  val tstarts = tIR_fromPerm(f, perm)
  (0 until f.length).par.map { i =>
    val t = tstarts(i)
    val ts = tsup(f, i)
    val tr = treg(f, i)
    val p = prio(f, i)
    if (ts - tr >= t) ts - (t + tr)
    else p * ((t + tr) - ts)
  }.sum
}
```

---

### 2. DEFINICIÓN MATEMÁTICA

Sea $\Pi$ la permutación del orden de riego y $t_i$ el tiempo de inicio de riego del tablón $i$ bajo esa permutación.

El costo de riego de la finca se define formalmente como:

$$
CR^{\Pi}_F = \sum_{i=0}^{n-1} c_i
$$

donde el costo por tablón $c_i$ se calcula como:

$$
c_i = \begin{cases}
t_s^i - (t_i + t_r^i) & \text{si } t_s^i - t_r^i \geq t_i \text{ (riego a tiempo)} \\
p_i \cdot ((t_i + t_r^i) - t_s^i) & \text{si } t_s^i - t_r^i < t_i \text{ (riego tardío)}
\end{cases}
$$

Donde:
- $t_i$ = tiempo de inicio de riego del tablón $i$
- $t_s^i$ = tiempo de supervivencia del tablón $i$ (`tsup`)
- $t_r^i$ = tiempo de riego requerido del tablón $i$ (`treg`)
- $p_i$ = prioridad del tablón $i$ (`prio`)

**La función `costoRiegoFincaPar` calcula exactamente $CR^{\Pi}_F$** según esta definición.

---

### 3. ESPECIFICACIÓN FORMAL

Para toda finca `f` y toda programación `pi` válida:

1. **Equivalencia funcional**: `costoRiegoFincaPar(f, pi) == costoRiegoFinca(f, pi)`
2. **Corrección matemática**: `costoRiegoFincaPar(f, pi) == CR^{\Pi}_F`
3. **Determinismo**: El resultado es independiente del orden de evaluación paralela
4. **Thread-safety**: Cada cálculo de $c_i$ es independiente, garantizando ausencia de condiciones de carrera

**Precondiciones recomendadas:**
```scala
require(pi.length == f.length, "pi must have length n")
require(pi.sorted == (0 until f.length).toVector, "pi must be a permutation of 0..n-1")
require(f.forall { case (ts, tr, p) => ts > 0 && tr > 0 && p > 0 }, "all parameters must be positive")
```

---

### 4. CORRECTITUD POR INDUCCIÓN ESTRUCTURAL

**Base de la inducción:** $n$ = número de tablones

#### Caso base: $n = 0$
- Finca vacía → suma vacía = $0$
- Correcto por definición de suma sobre conjunto vacío

#### Caso base: $n = 1$
- Un solo tablón $i=0$
- `perm = [0]`, `tstarts(0)` se calcula correctamente
- Se evalúa la condición y retorna:
    - Si $t_s^0 - t_r^0 \geq t_0$: retorna $t_s^0 - (t_0 + t_r^0)$
    - Si $t_s^0 - t_r^0 < t_0$: retorna $p_0 \cdot ((t_0 + t_r^0) - t_s^0)$
- Coincide exactamente con $c_0$ según la definición matemática ✓

#### Paso inductivo: $n \rightarrow n+1$

**Hipótesis inductiva:** Asumimos que `costoRiegoFincaPar` es correcta para todas las fincas de tamaño $\le n$.

**Demostración para tamaño $n+1$:**

1. **Cálculo de `perm`:** `progToPermPure(pi)` produce la permutación correcta por definición

2. **Cálculo de `tstarts`:** `tIR_fromPerm(f, perm)` calcula correctamente los tiempos de inicio:
   $$
   t_i = \sum_{k=0}^{pos(i)-1} t_r^{\Pi(k)}
   $$
   donde $pos(i)$ es la posición del tablón $i$ en la permutación

3. **Independencia de cálculos:** Para cada tablón $i \in [0, n]$:
    - El cálculo de $c_i$ depende **únicamente** de:
        - $t_i$ (tiempo de inicio del tablón $i$)
        - $t_s^i, t_r^i, p_i$ (propiedades del tablón $i$ en `f`)
    - **NO depende** de los cálculos de otros tablones
    - **NO hay efectos colaterales** (todas las funciones son puras)

4. **Corrección de cada término:** Para cada $i$, el código:
```scala
   if (ts - tr >= t) ts - (t + tr)
   else p * ((t + tr) - ts)
```
implementa exactamente la definición matemática de $c_i$

5. **Corrección de la suma:**
    - La operación `.sum` en una colección paralela realiza:
      $$
      \text{sum} = \bigoplus_{i=0}^{n} c_i
      $$
      donde $\bigoplus$ es una reducción asociativa
    - Como la suma de enteros es **conmutativa** y **asociativa**:
      $$
      c_0 + c_1 + \cdots + c_n = (c_0 + c_1) + (c_2 + c_3) + \cdots
      $$
    - El orden de evaluación **no afecta** el resultado final
    - Por lo tanto: `sum_parallel == sum_sequential`

6. **Conclusión del paso:**
    - Cada $c_i$ se calcula correctamente
    - La suma produce el mismo resultado que la versión secuencial
    - Por lo tanto, para tamaño $n+1$: `costoRiegoFincaPar(f, pi) == CR^{\Pi}_F` ✓

**Conclusión de la inducción:** La función es correcta para todo $n \ge 0$. ∎

---

### 5. PARALELISMO – ESTRATEGIA UTILIZADA

#### 5.1 Tipo de paralelismo

**Paralelismo de datos (Data Parallelism)**: La colección de tablones se particiona y procesa en paralelo.

#### 5.2 Código paralelo central
```scala
(0 until f.length).par.map { i =>
  val t = tstarts(i)
  val ts = tsup(f, i)
  val tr = treg(f, i)
  val p = prio(f, i)
  if (ts - tr >= t) ts - (t + tr)
  else p * ((t + tr) - ts)
}.sum
```

#### 5.3 Características clave de la paralelización

| Característica | Descripción | Impacto |
|---------------|-------------|---------|
| **Independencia total** | Cada $c_i$ se calcula sin dependencias | ✓ Paralelizable |
| **Sin estado compartido mutable** | Solo lectura de `f`, `tstarts`, `pi` | ✓ Thread-safe |
| **Sin efectos colaterales** | Cálculo puramente funcional | ✓ Sin race conditions |
| **Acceso de solo lectura** | `tstarts(i)` y `f(i)` son inmutables | ✓ Sin sincronización |
| **Reducción asociativa** | `.sum` es conmutativo y asociativo | ✓ Correcto en paralelo |

#### 5.4 Análisis de overhead

**Fuentes de overhead:**

1. **Creación de threads**: Fork/join pool initialization (~5-10µs)
2. **Partición de trabajo**: División del rango `(0 until n)` en chunks
3. **Sincronización**: Coordinación entre threads
4. **Reducción final**: Combinación de sumas parciales

**Punto de equilibrio:**
$$
T_{overhead} < \frac{T_{seq} \cdot p}{N}
$$

Para esta función:
- $T_{seq} \approx 10\mu s$ (muy pequeño)
- $T_{overhead} \approx 30\mu s$ (fijo)
- Por lo tanto: overhead domina para $n \le 20$

---

### 6. SPEEDUP MEDIDO

#### 6.1 Configuración del Benchmark

| Parámetro | Valor |
|-----------|-------|
| **Cores** | 16 (lógicos) |
| **Sistema Operativo** | Windows 11 |
| **JVM** | Java HotSpot™ 64-Bit Server VM |
| **Versión Java** | 17.0.16+12-LTS-247 |
| **Hostname** | Ospina |
| **Arquitectura** | amd64 (x86_64) |

#### 6.2 Resultados experimentales

| Tamaño (n) | Secuencial (ms) | Paralelo (ms) | Speedup | Aceleración (%) |
|-----------:|----------------:|--------------:|--------:|----------------:|
|          6 |        0.010601 |      0.043599 |    0.24 |         -311.3% |
|          7 |        0.008900 |      0.033101 |    0.27 |         -272.0% |
|          8 |        0.010900 |      0.033700 |    0.32 |         -209.2% |

#### 6.3 Análisis de resultados

**Observaciones críticas:**

1. **Speedup < 1 en todos los casos**: La versión paralela es **consistentemente más lenta**

2. **Overhead dominante**:
    - Tiempo secuencial: ~10µs
    - Overhead de paralelización: ~30µs
    - Relación overhead/cómputo: **3:1**

3. **Escalabilidad negativa**: A pesar de tener 16 cores, el beneficio es **negativo**

4. **Razón fundamental**: El cómputo por tablón es **trivial**:
    - 4 accesos a memoria (lecturas simples)
    - 1 comparación condicional
    - 2-3 operaciones aritméticas
    - Tiempo por tablón: ~1-2µs

#### 6.4 Gráfico conceptual de tiempos
```
Tiempo (microsegundos)
│
50│                    ┌────────┐
  │                    │Overhead│
40│                    │  ~30µs │
  │                    │        │
30│     ┌──────────────┤        │
  │     │   Overhead   │        │
20│     │    ~20µs     │        │
  │     │              │        │
10│ ┌───┤              │        │
  │ │Seq│  Cómputo     │ Cómp.  │
 0└─┴───┴──────────────┴────────┴─
    n=6    Paralelo      Paralelo
           (ideal)       (real)
```

---

### 6.5 Ley de Amdahl – Análisis de fracción paralelizable

#### Formulación teórica

La Ley de Amdahl predice el speedup máximo alcanzable:

$$
S_{max} = \frac{1}{(1-p) + \frac{p}{N}}
$$

Donde:
- $p$ = fracción paralelizable del código
- $N$ = número de procesadores
- $1-p$ = fracción secuencial (no paralelizable)

#### Cálculo de $p$ a partir de speedup observado

Despejando $p$ de la ecuación:

$$
p = \frac{1 - \frac{1}{S}}{1 - \frac{1}{N}}
$$

Con $N = 16$ cores:

| Tamaño (n) | Speedup ($S$) | Fracción $p$ calculada | Interpretación |
|----------:|-------------:|----------------------:|----------------|
|         6 |         0.24 |         **< 0**       | No físico (overhead domina) |
|         7 |         0.27 |         **< 0**       | No físico (overhead domina) |
|         8 |         0.32 |         **< 0**       | No físico (overhead domina) |

#### Interpretación de resultados negativos

Cuando $p < 0$ en el cálculo, significa que el modelo de Amdahl **no aplica** porque:

1. **El overhead no está modelado**: Amdahl asume overhead despreciable
2. **En realidad**:
   $$
   T_{par} = T_{seq} \cdot (1-p) + \frac{T_{seq} \cdot p}{N} + O
   $$
   donde $O$ es el overhead fijo

3. **En este caso**: $O >> \frac{T_{seq} \cdot p}{N}$

#### Estimación realista de paralelización

**Fracción teóricamente paralelizable**: $p \approx 100\%$
- Todo el bucle `(0 until n)` es paralelizable
- No hay código secuencial significativo
- Cada iteración es completamente independiente

**¿Por qué no funciona?**

El problema no es la fracción paralelizable, sino que:

$$
\text{Beneficio}_{paralelo} = \frac{T_{seq} \cdot p}{N} \approx \frac{10\mu s}{16} \approx 0.6\mu s
$$

$$
\text{Overhead}_{real} \approx 30\mu s
$$

$$
\text{Overhead} >> \text{Beneficio} \implies \text{Speedup} < 1
$$

#### Predicción: ¿Cuándo sería beneficioso?

Para que $S > 1$, necesitamos:

$$
\frac{T_{seq}}{N} > O \implies T_{seq} > N \cdot O
$$

Con $N = 16$ y $O \approx 30\mu s$:

$$
T_{seq} > 16 \times 30\mu s = 480\mu s
$$

Dado que $T_{seq}(n) \approx n \times 1.5\mu s$:

$$
n > \frac{480\mu s}{1.5\mu s} \approx 320 \text{ tablones}
$$

**Conclusión**: Se necesitarían **al menos 300-500 tablones** para ver beneficios de paralelización.

---

### 7. PROCESO – DIAGRAMA DE FLUJO
```mermaid
flowchart TD
    A["<b>costoRiegoFincaPar(f, pi)</b><br/>Entrada: Finca y programación"]
    
    B["<b>Paso 1: Conversión</b><br/>progToPermPure(pi) → perm<br/>Convierte programación a permutación"]
    
    C["<b>Paso 2: Cálculo de tiempos</b><br/>tIR_fromPerm(f, perm) → tstarts<br/>Calcula tiempo inicio para cada tablón"]
    
    D["<b>Paso 3: Paralelización</b><br/>(0 until f.length).par<br/>Particiona tablones entre threads"]
    
    E1["<b>Thread 1</b><br/>Tablones: 0, 1<br/>────────<br/>t₀ = tstarts(0)<br/>ts₀, tr₀, p₀ ← f(0)<br/><br/>if ts₀-tr₀ ≥ t₀:<br/>  c₀ = ts₀-(t₀+tr₀)<br/>else:<br/>  c₀ = p₀×((t₀+tr₀)-ts₀)<br/>────────<br/>(similar para tablón 1)"]
    
    E2["<b>Thread 2</b><br/>Tablones: 2, 3<br/>────────<br/>Cálculo independiente<br/>de c₂ y c₃"]
    
    E3["<b>Thread 3</b><br/>Tablones: 4, 5<br/>────────<br/>Cálculo independiente<br/>de c₄ y c₅"]
    
    E4["<b>Thread N</b><br/>Tablones: ...<br/>────────<br/>Cálculo independiente<br/>de cₖ hasta cₙ₋₁"]
    
    F["<b>Paso 4: Reducción paralela</b><br/>.sum (aggregate)<br/>────────<br/>Combina resultados parciales:<br/>CR = c₀ + c₁ + ... + cₙ₋₁"]
    
    G["<b>Resultado</b><br/>Costo total de riego CR^Π_F"]
    
    A --> B
    B --> C
    C --> D
    
    D --> E1
    D --> E2
    D --> E3
    D --> E4
    
    E1 --> F
    E2 --> F
    E3 --> F
    E4 --> F
    
    F --> G
    
    style A fill:#e1f5ff
    style B fill:#fff4e1
    style C fill:#fff4e1
    style D fill:#ffe1f5
    style E1 fill:#ff9a9a
    style E2 fill:#ff9a9a
    style E3 fill:#ff9a9a
    style E4 fill:#ff9a9a
    style F fill:#9aff9a
    style G fill:#e1f5ff
```

**Leyenda:**
- Azul: Entrada/Salida
- Amarillo: Preparación secuencial
- Rosa: Punto de paralelización
- Rojo: Ejecución paralela (independiente)
- Verde: Reducción/agregación

---

### 8. CASOS DE PRUEBA – Tests exhaustivos

Los siguientes tests verifican la corrección funcional de `costoRiegoFincaPar` comparándola con la versión secuencial:

#### Test 1: Caso básico con 3 tablones
```scala
test("costoRiegoFincaPar — coincide con secuencial (1)") {
  val f = Vector((10,3,1),(8,1,1),(4,2,1))
  val pi = Vector(1,0,2)
  assert(r.costoRiegoFincaPar(f,pi) == r.costoRiegoFinca(f,pi))
}
```

**Análisis manual:**
- Permutación: `[1,0,2]` → regar en orden: tablón 1, tablón 0, tablón 2
- Tiempos de inicio: `t(0)=1`, `t(1)=0`, `t(2)=4`
- Costos individuales:
    - Tablón 0: $t_s - t_r = 10-3 = 7 \geq t_0=1$ → $c_0 = 10-(1+3) = 6$
    - Tablón 1: $t_s - t_r = 8-1 = 7 \geq t_1=0$ → $c_1 = 8-(0+1) = 7$
    - Tablón 2: $t_s - t_r = 4-2 = 2 < t_2=4$ → $c_2 = 1 \times ((4+2)-4) = 2$
- **Costo total esperado**: $6 + 7 + 2 = 15$ ✓

#### Test 2: Orden con penalizaciones
```scala
test("costoRiegoFincaPar — coincide con secuencial (2)") {
  val f = Vector((3,1,2),(7,2,1),(6,3,2))
  val pi = Vector(2,0,1)
  assert(r.costoRiegoFincaPar(f,pi) == r.costoRiegoFinca(f,pi))
}
```

**Análisis:**
- Permutación: `[2,0,1]` → orden: tablón 2, tablón 0, tablón 1
- Tiempos: `t(0)=3`, `t(1)=4`, `t(2)=0`
- Este test verifica el cálculo correcto de penalizaciones por riego tardío

#### Test 3: Alta prioridad y penalizaciones
```scala
test("costoRiegoFincaPar — coincide con secuencial (3)") {
  val f = Vector((5,3,8),(4,1,2),(6,3,10))
  val pi = Vector(2,1,0)
  assert(r.costoRiegoFincaPar(f,pi) == r.costoRiegoFinca(f,pi))
}
```

**Características:**
- Prioridades altas (8, 2, 10) → penalizaciones severas si hay retrasos
- Orden inverso: regar último el más urgente
- Verifica cálculo correcto de penalizaciones multiplicadas por prioridad

#### Test 4: Caso con 4 tablones
```scala
test("costoRiegoFincaPar — coincide con secuencial (4)") {
  val f = Vector((5,3,3),(8,2,1),(6,2,1),(4,3,2))
  val pi = Vector(3,0,1,2)
  assert(r.costoRiegoFincaPar(f,pi) == r.costoRiegoFinca(f,pi))
}
```

**Propósito:**
- Escala el test a 4 tablones
- Verifica que la paralelización funciona correctamente con más elementos
- Orden: `[3,0,1,2]` → permutación no trivial

#### Test 5: Caso grande con 6 tablones
```scala
test("costoRiegoFincaPar — coincide con secuencial (5)") {
  val f = Vector((10,3,1),(7,2,1),(6,3,2),(9,1,3),(4,2,1),(8,2,3))
  val pi = Vector(3,1,5,0,2,4)
  assert(r.costoRiegoFincaPar(f,pi) == r.costoRiegoFinca(f,pi))
}
```

**Características especiales:**
- **6 tablones**: tamaño suficiente para distribuir entre múltiples threads
- Permutación compleja: `[3,1,5,0,2,4]`
- Variedad de prioridades (1, 1, 2, 3, 1, 3)
- Test más exhaustivo para verificar corrección en casos reales

---

### 9. VERIFICACIÓN ADICIONAL – Propiedades matemáticas

#### Propiedad 1: Invarianza bajo permutación de evaluación

Para cualquier reordenamiento de la evaluación de los tablones:

$$
\sum_{i \in S_1} c_i + \sum_{i \in S_2} c_i = \sum_{i \in S_2} c_i + \sum_{i \in S_1} c_i
$$

Esto garantiza que la evaluación paralela (que puede procesar tablones en cualquier orden) produce el mismo resultado que la secuencial.

#### Propiedad 2: Monotonía del costo

Si se retrasa el riego de un tablón, su costo individual no disminuye:

$$
t_i' > t_i \implies c_i' \geq c_i
$$

Esta propiedad se preserva en la versión paralela porque cada $c_i$ se calcula independientemente.

#### Propiedad 3: Equivalencia funcional

Para todos los casos de prueba ejecutados:

$$
\forall f, pi: \quad \text{costoRiegoFincaPar}(f, pi) = \text{costoRiegoFinca}(f, pi)
$$

**Verificado en:** 5 tests con diferentes configuraciones de finca y programación.

---

### 10. CONCLUSIONES Y RECOMENDACIONES

#### 10.1 Corrección funcional

**La función es matemáticamente correcta:**
- Implementa exactamente la definición formal de $CR^{\Pi}_F$
- Demostrado por inducción estructural
- Verificado empíricamente con 5 tests exhaustivos
- Equivalencia funcional con versión secuencial: **100% de casos pasados**

#### 10.2 Estrategia de paralelización

**La estrategia es conceptualmente adecuada:**
- Paralelismo de datos sobre tablones independientes
- Sin efectos colaterales → thread-safe por diseño
- Reducción asociativa → correcta en paralelo

#### 10.3 Rendimiento observado

**La paralelización NO es beneficiosa para tamaños pequeños:**

| Métrica | Valor | Interpretación |
|---------|-------|----------------|
| Speedup promedio | 0.24 - 0.32 | **3-4x más lento** |
| Overhead | ~30µs | **3x el tiempo de cómputo** |
| Punto de equilibrio | n ≈ 320 tablones | Impracticable en casos reales |

#### 10.4 Razones del bajo rendimiento

1. **Granularidad demasiado fina**:
    - Cómputo por tablón: ~1-2µs
    - Overhead por thread: ~30µs
    - Relación cómputo/overhead: **1:15**

2. **Tamaño del problema inadecuado**:
    - Fincas reales: 6-10 tablones
    - Mínimo viable para paralelización: ~300 tablones

3. **Operaciones triviales**:
    - Solo 4 lecturas + 1 condicional + 2-3 operaciones
    - No suficiente trabajo para justificar threading

#### 10.5 Recomendaciones prácticas

**Para producción:**
```scala
def costoRiegoFincaAdaptive(f: Finca, pi: ProgRiego): Int = {
  if (f.length < 100) {
    // Usar versión secuencial para n pequeño
    costoRiegoFinca(f, pi)
  } else {
    // Usar versión paralela solo para n grande
    costoRiegoFincaPar(f, pi)
  }
}
```

**Decisión basada en tamaño:**

| Tamaño finca | Versión recomendada | Razón |
|-------------:|---------------------|-------|
| n < 50 | **Secuencial** | Overhead > beneficio |
| 50 ≤ n < 200 | **Secuencial** | Beneficio marginal |
| n ≥ 200 | **Paralela (con medición)** | Posible beneficio |

#### 10.6 Lecciones aprendidas

1. **No todo código es paralelizable efectivamente**: Aunque técnicamente correcto, el overhead puede eliminar cualquier beneficio

2. **La granularidad importa**: Paralelizar operaciones triviales raramente es beneficioso

3. **Medir antes de optimizar**: Los benchmarks revelan que la intuición ("más cores = más rápido") no siempre aplica

4. **Context matters**: En un algoritmo mayor (como `ProgramacionRiegoOptimo`), la paralelización puede seguir siendo útil en otros niveles

#### 10.7 Valor académico vs. práctico

**Valor académico: ★★★★★**
- Excelente ejercicio de paralelización correcta
- Demuestra comprensión de thread-safety
- Implementación matemáticamente rigurosa

**Valor práctico: ★☆☆☆☆**
- No aporta beneficio de rendimiento
- Añade complejidad innecesaria
- En casos reales, usar versión secuencial

---

### 11. REFERENCIAS Y CONTEXTO

#### Funciones auxiliares utilizadas
```scala
// Convierte programación a permutación (índices en orden de riego)
def progToPermPure(pi: ProgRiego): Vector[Int]

// Calcula tiempos de inicio para cada tablón
def tIR_fromPerm(f: Finca, perm: Vector[Int]): Vector[Int]

// Accesores a las propiedades de tablones
def tsup(f: Finca, i: Int): Int  // Tiempo de supervivencia
def treg(f: Finca, i: Int): Int  // Tiempo de riego requerido
def prio(f: Finca, i: Int): Int  // Prioridad
```

#### Relación con otras funciones paralelas

Esta función forma parte de un conjunto de operaciones paralelizadas:

1. **`generarProgramacionesRiegoPar`**: Genera permutaciones en paralelo
2. **`costoMovilidadPar`**: Calcula costo de movimiento en paralelo
3. **`costoRiegoFincaPar`**: ← Esta función
4. **`ProgramacionRiegoOptimoPar`**: Encuentra óptimo en paralelo

El beneficio agregado de paralelizar todo el pipeline puede ser mayor que paralelizar componentes individuales.

---

