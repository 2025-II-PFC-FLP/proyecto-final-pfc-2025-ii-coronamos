package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

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
}
