@RunWith(classOf[JUnitRunner])
class RiegoTest extends AnyFunSuite {

  val r = new Riego()

  test("tIR con tres tablones ejemplo 1") {
    val f = Vector((10,3,1),(8,1,1),(4,2,1))
    val pi = Vector(2,0,1)
    assert(r.tIR(f, pi) == Vector(3,0,1))
  }

  test("tIR con treg variados") {
    val f = Vector((10,4,1),(5,2,1),(3,1,1))
    val pi = Vector(1,2,0)
    assert(r.tIR(f, pi) == Vector(1,5,0))
  }

  test("costoRiegoTablon sin multa (se riega antes del tsup)") {
    val f = Vector((5,3,1))
    val pi = Vector(0)
    assert(r.costoRiegoTablon(0, f, pi) == 2)
  }

  test("costoRiegoTablon sin multa con prioridad alta") {
    val f = Vector((4,3,4))
    val pi = Vector(0)
    assert(r.costoRiegoTablon(0, f, pi) == 1)
  }

  test("costoRiegoTablon con multa (riego tardío)") {
    val f = Vector((3,1,2))
    val pi = Vector(0)
    assert(r.costoRiegoTablon(0,f,pi) == 2)
  }

  test("costoRiegoFinca ejemplo básico") {
    val f = Vector(
      (10,3,1),
      (8,1,1),
      (4,2,1)
    )
    val pi = Vector(1,0,2)

    val expected = 6 + 7 + 2

    assert(r.costoRiegoFinca(f, pi) == expected)
  }

  test("costoMovilidad ejemplo simple") {
    val r = new Riego()

    val f = Vector((10,3,1),(8,1,1),(4,2,1))
    val pi = Vector(2,0,1)

    val d = Vector(
      Vector(0, 5, 3),
      Vector(5, 0, 4),
      Vector(3, 4, 0)
    )

    // perm = orden por turnos
    // pi = Vector(2,0,1)  → perm = Vector(1,2,0)
    // movilidad = d(1)(2) + d(2)(0) = 4 + 3 = 7

    assert(r.costoMovilidad(f, pi, d) == 7)
  }

  test("generarProgramacionesRiego para finca de 3 tablones") {
    val r = new Riego()

    val f = Vector(
      (10,3,1),
      (8,1,2),
      (4,2,3)
    )

    val progs = r.generarProgramacionesRiego(f)

    assert(progs.length == 6)   // 3! = 6 permutaciones

    assert(progs.contains(Vector(0,1,2)))
    assert(progs.contains(Vector(0,2,1)))
    assert(progs.contains(Vector(1,0,2)))
    assert(progs.contains(Vector(1,2,0)))
    assert(progs.contains(Vector(2,0,1)))
    assert(progs.contains(Vector(2,1,0)))
  }

  test("ProgramacionRiegoOptimo encuentra la mejor programación") {
    val r = new Riego()

    val f = Vector(
      (10,3,1),
      (8,1,1),
      (4,2,1)
    )

    val d = Vector(
      Vector(0, 2, 4),
      Vector(2, 0, 6),
      Vector(4, 6, 0)
    )

    val (optPi, costo) = r.ProgramacionRiegoOptimo(f, d)

    assert(optPi.toSet == Set(0,1,2))

    val todas = r.generarProgramacionesRiego(f)
    val minCosto = todas.map(pi => r.costoRiegoFinca(f, pi) + r.costoMovilidad(f, pi, d)).min

    assert(costo == minCosto)
  }

  test("costoMovilidad con matriz de distancias asimétrica") {
    val r = new Riego()

    val f = Vector((10,3,1),(8,1,1),(4,2,1))
    val pi = Vector(1,0,2)

    val d = Vector(
      Vector(0, 3, 10),
      Vector(1, 0, 5),
      Vector(2, 7, 0)
    )

    // perm = Vector(1,0,2)
    // movilidad = d(1)(0) + d(0)(2) = 1 + 10 = 11
    assert(r.costoMovilidad(f, pi, d) == 11)
  }

  test("generarProgramacionesRiego con 4 tablones produce 24 permutaciones") {
    val r = new Riego()

    val f = Vector(
      (10,3,1),(8,1,2),(4,2,3),(6,1,4)
    )

    val progs = r.generarProgramacionesRiego(f)

    assert(progs.length == 24)
    assert(progs.contains(Vector(0,1,2,3)))
    assert(progs.contains(Vector(3,2,1,0)))
  }

  test("costoRiegoFinca con prioridad alta (multas grandes)") {
    val r = new Riego()

    val f = Vector(
      (5,3,10),  // prioridad muy alta
      (7,2,1)
    )

    val pi = Vector(1,0)

    // Calculamos manualmente:
    // perm = Vector(1,0)
    // tIR = Vector(2,0)
    // Tablón 0: tsup=5, treg=3 → fin=2+3=5 → (llega justo) costo = 0
    // Tablón 1: tsup=7, treg=2 → fin=0+2=2 → riega antes → costo = 7 - 2 = 5

    val expected = 5

    assert(r.costoRiegoFinca(f, pi) == expected)
  }
  //Test costoRiegoFincaPar
  test("costoRiegoFincaPar — finca simple sin multa") {
    val f = Vector((10,3,1),(8,1,1),(4,2,1))
    val pi = Vector(1,0,2)
    assert(r.costoRiegoFincaPar(f,pi) == r.costoRiegoFinca(f,pi))
  }

  test("costoRiegoFincaPar — con atraso y multa calculada") {
    val f = Vector((3,1,2),(7,2,1),(6,3,2))
    val pi = Vector(2,0,1)
    assert(r.costoRiegoFincaPar(f,pi) == r.costoRiegoFinca(f,pi))
  }

  test("costoRiegoFincaPar — prioridad alta penaliza fuerte si se atrasa") {
    val f = Vector((5,3,8),(4,1,2),(6,3,10))
    val pi = Vector(2,1,0)
    assert(r.costoRiegoFincaPar(f,pi) == r.costoRiegoFinca(f,pi))
  }

  test("costoRiegoFincaPar — riego justo en t = tsup → costo 0 esperado") {
    val f = Vector((5,3,3),(8,2,1),(6,2,1))
    val pi = Vector(1,0,2)
    assert(r.costoRiegoFincaPar(f,pi) == r.costoRiegoFinca(f,pi))
  }

  test("costoRiegoFincaPar — caso grande con 6 tablones (no trivial)") {
    val f = Vector((10,3,3),(7,2,1),(6,3,2),(9,1,3),(4,2,1),(8,2,3))
    val pi = Vector(3,1,5,0,2,4)
    assert(r.costoRiegoFincaPar(f,pi) == r.costoRiegoFinca(f,pi))
  }
  //Test casoMovilidadPar

  test("costoMovilidadPar — 3 tablones simple") {
    val d = Vector(Vector(0,5,3),Vector(5,0,4),Vector(3,4,0))
    val f = Vector((1,1,1),(1,1,1),(1,1,1))
    val pi = Vector(2,0,1)
    assert(r.costoMovilidadPar(f,pi,d) == r.costoMovilidad(f,pi,d))
  }

  test("costoMovilidadPar — distancias asimétricas") {
    val d = Vector(Vector(0,3,10),Vector(1,0,5),Vector(2,7,0))
    val f = Vector((1,1,1),(1,1,1),(1,1,1))
    val pi = Vector(1,0,2)
    assert(r.costoMovilidadPar(f,pi,d) == r.costoMovilidad(f,pi,d))
  }

  test("costoMovilidadPar — 5 tablones con movilidad compleja") {
    val d = Vector(
      Vector(0,2,2,4,4), Vector(2,0,4,2,6), Vector(2,4,0,2,2),
      Vector(4,2,2,0,4), Vector(4,6,2,4,0)
    )
    val f = Vector.fill(5)((1,1,1))
    val pi = Vector(0,1,3,4,2)
    assert(r.costoMovilidadPar(f,pi,d) == r.costoMovilidad(f,pi,d))
  }

  test("costoMovilidadPar — permutación invertida con 6 tablones") {
    val f = Vector.fill(6)((1,1,1))
    val d = Vector.tabulate(6,6)((i,j)=>if(i==j)0 else math.abs(i-j))
    val pi = Vector(5,4,3,2,1,0)
    assert(r.costoMovilidadPar(f,pi,d) == r.costoMovilidad(f,pi,d))
  }

  test("costoMovilidadPar — caso borde: solo un tablón (movilidad = 0)") {
    val f = Vector((10,3,1))
    val pi = Vector(0)
    val d = Vector(Vector(0))
    assert(r.costoMovilidadPar(f,pi,d) == 0)
  }
  test("generarProgramacionesRiegoPar — igualdad con versión secuencial (3 tablones)") {
    val r = new Riego()

    val f = Vector(
      (10,3,1),
      (8,1,2),
      (4,2,3)
    )

    val seq = r.generarProgramacionesRiego(f)
    val par = r.generarProgramacionesRiegoPar(f)

    assert(par.toSet == seq.toSet)
    assert(par.length == seq.length)
    assert(par.length == 6) // 3! = 6
  }
  test("generarProgramacionesRiegoPar — igualdad con versión secuencial (4 tablones)") {
    val r = new Riego()

    val f = Vector(
      (10,3,1), (8,1,2), (4,2,3), (6,1,4)
    )

    val seq = r.generarProgramacionesRiego(f)
    val par = r.generarProgramacionesRiegoPar(f)

    assert(par.toSet == seq.toSet)
    assert(par.length == seq.length)
    assert(par.length == 24) // 4! = 24
  }
  test("generarProgramacionesRiegoPar — incluye permutaciones específicas") {
    val r = new Riego()

    val f = Vector(
      (5,2,1), (7,1,2), (3,1,3)
    )

    val par = r.generarProgramacionesRiegoPar(f)

    assert(par.contains(Vector(0,1,2)))
    assert(par.contains(Vector(2,1,0)))
  }
  test("ProgramacionRiegoOptimoPar — mismo costo que la versión secuencial") {
    val r = new Riego()

    val f = Vector(
      (10,3,1),
      (8,1,1),
      (4,2,1)
    )

    val d = Vector(
      Vector(0, 2, 4),
      Vector(2, 0, 6),
      Vector(4, 6, 0)
    )

    val (piSeq, costoSeq) = r.ProgramacionRiegoOptimo(f, d)
    val (piPar, costoPar) = r.ProgramacionRiegoOptimoPar(f, d)

    assert(costoSeq == costoPar)
    assert(piPar.toSet == piSeq.toSet)
  }
  test("ProgramacionRiegoOptimoPar — encuentra un óptimo válido") {
    val r = new Riego()

    val f = Vector(
      (5,3,3),
      (8,2,1),
      (6,2,1),
      (4,3,2)
    )

    val d = Vector(
      Vector(0,1,4,3),
      Vector(1,0,2,5),
      Vector(4,2,0,6),
      Vector(3,5,6,0)
    )

    val todas = r.generarProgramacionesRiego(f)

    val minCosto = todas.map(pi =>
      r.costoRiegoFinca(f, pi) + r.costoMovilidad(f, pi, d)
    ).min

    val (_, costoPar) = r.ProgramacionRiegoOptimoPar(f, d)

    assert(costoPar == minCosto)
  }
  test("ProgramacionRiegoOptimoPar — funciona con distancias asimétricas") {
    val r = new Riego()

    val f = Vector(
      (10,3,1),
      (8,1,1),
      (4,2,1)
    )

    val d = Vector(
      Vector(0, 3, 10),
      Vector(1, 0, 5),
      Vector(2, 7, 0)
    )

    val (piSeq, costoSeq) = r.ProgramacionRiegoOptimo(f, d)
    val (piPar, costoPar) = r.ProgramacionRiegoOptimoPar(f, d)

    assert(costoSeq == costoPar)
    assert(piPar.toSet == Set(0,1,2))
  }

}
