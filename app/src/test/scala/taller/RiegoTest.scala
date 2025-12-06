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

}
