package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RiegoTest extends AnyFunSuite {

  val r = new Riego()

  test("tIR — ejemplo básico") {
    val f = Vector((10,3,1),(8,1,1),(4,2,1))
    val pi = Vector(2,0,1)
    assert(r.tIR(f,pi) == Vector(3,0,1))
  }

  test("tIR — todas iguales") {
    val f = Vector((5,1,1),(5,1,1),(5,1,1))
    val pi = Vector(0,1,2)
    assert(r.tIR(f,pi) == Vector(0,1,2))
  }

  test("tIR — turnos invertidos") {
    val f = Vector((4,2,1),(4,2,1),(4,2,1))
    val pi = Vector(2,1,0)
    assert(r.tIR(f,pi) == Vector(4,2,0))
  }

  test("tIR — diferentes treg") {
    val f = Vector((10,4,1),(5,2,1),(3,1,1))
    val pi = Vector(1,2,0)
    assert(r.tIR(f,pi) == Vector(1,5,0))
  }

  test("tIR — finca de 1 tablon") {
    val f = Vector((10,3,1))
    val pi = Vector(0)
    assert(r.tIR(f,pi) == Vector(0))
  }

  test("costoRiegoTablon — sin multa") {
    val f = Vector((5,3,1))
    assert(r.costoRiegoTablon(0,f,Vector(0)) == 2)
  }

  test("costoRiegoTablon — con prioridad alta") {
    val f = Vector((4,3,4))
    assert(r.costoRiegoTablon(0,f,Vector(0)) == 1)
  }

  test("costoRiegoTablon — con multa simple") {
    val f = Vector((3,1,2))
    assert(r.costoRiegoTablon(0,f,Vector(0)) == 2)
  }

  test("costoRiegoTablon — riego justo a tiempo") {
    val f = Vector((5,3,3))
    assert(r.costoRiegoTablon(0,f,Vector(0)) == 2)
  }

  test("costoRiegoTablon — multas grandes") {
    val f = Vector((10,1,10))
    val pi = Vector(0)
    assert(r.costoRiegoTablon(0,f,pi) >= 0)
  }

  test("costoRiegoFinca — ejemplo básico") {
    val f = Vector((10,3,1),(8,1,1),(4,2,1))
    val pi = Vector(1,0,2)
    assert(r.costoRiegoFinca(f,pi) == 15)
  }

  test("costoRiegoFinca — atraso grande con multas") {
    val f = Vector((5,3,10),(7,2,1))
    val pi = Vector(1,0)
    assert(r.costoRiegoFinca(f,pi) == 5)
  }

  test("costoRiegoFinca — sin multas") {
    val f = Vector((5,3,1),(4,2,1),(3,1,1))
    val pi = Vector(0,1,2)
    assert(r.costoRiegoFinca(f,pi) >= 0)
  }

  test("costoRiegoFinca — orden invertido") {
    val f = Vector((10,3,1),(8,2,1),(6,2,1))
    val pi = Vector(2,1,0)
    assert(r.costoRiegoFinca(f,pi) >= 0)
  }

  test("costoRiegoFinca — finca de 1 tablon") {
    val f = Vector((10,3,1))
    assert(r.costoRiegoFinca(f,Vector(0)) >= 0)
  }


  test("costoMovilidad — ejemplo simple") {
    val f = Vector((1,1,1),(1,1,1),(1,1,1))
    val pi = Vector(2,0,1)
    val d = Vector(
      Vector(0,5,3),
      Vector(5,0,4),
      Vector(3,4,0)
    )
    assert(r.costoMovilidad(f,pi,d) == 7)
  }

  test("costoMovilidad — matriz asimétrica") {
    val d = Vector(Vector(0,3,10),Vector(1,0,5),Vector(2,7,0))
    val f = Vector.fill(3)((1,1,1))
    assert(r.costoMovilidad(f,Vector(1,0,2),d) == 11)
  }

  test("costoMovilidad — 1 tablon") {
    val f = Vector((10,3,1))
    val d = Vector(Vector(0))
    assert(r.costoMovilidad(f,Vector(0),d) == 0)
  }

  test("costoMovilidad — distancias lineales") {
    val f = Vector.fill(4)((1,1,1))
    val d = Vector.tabulate(4,4)((i,j)=>math.abs(i-j))
    val pi = Vector(3,2,1,0)
    assert(r.costoMovilidad(f,pi,d) == 3)
  }

  test("costoMovilidad — caso grande") {
    val f = Vector.fill(5)((1,1,1))
    val d = Vector.tabulate(5,5)((i,j)=>math.abs(i-j))
    val pi = Vector(0,2,4,3,1)
    assert(r.costoMovilidad(f,pi,d) >= 0)
  }

  test("generarProgramacionesRiego — 3 tablones (6 permutaciones)") {
    val f = Vector((1,1,1),(1,1,1),(1,1,1))
    assert(r.generarProgramacionesRiego(f).length == 6)
  }

  test("generarProgramacionesRiego — contiene permutación ordenada") {
    val f = Vector((1,1,1),(1,1,1),(1,1,1))
    assert(r.generarProgramacionesRiego(f).contains(Vector(0,1,2)))
  }

  test("generarProgramacionesRiego — contiene permutación invertida") {
    val f = Vector((1,1,1),(1,1,1),(1,1,1))
    assert(r.generarProgramacionesRiego(f).contains(Vector(2,1,0)))
  }

  test("generarProgramacionesRiego — 4 tablones (24 permutaciones)") {
    val f = Vector((1,1,1),(1,1,1),(1,1,1),(1,1,1))
    assert(r.generarProgramacionesRiego(f).length == 24)
  }

  test("generarProgramacionesRiego — todas distintas") {
    val f = Vector((1,1,1),(1,1,1),(1,1,1))
    val progs = r.generarProgramacionesRiego(f)
    assert(progs.distinct.length == progs.length)
  }

  test("ProgramacionRiegoOptimo — encuentra óptimo básico") {
    val f = Vector((10,3,1),(8,1,1),(4,2,1))
    val d = Vector(Vector(0,2,4),Vector(2,0,6),Vector(4,6,0))
    val (_,c) = r.ProgramacionRiegoOptimo(f,d)
    val min = r.generarProgramacionesRiego(f).map(pi => r.costoRiegoFinca(f,pi)+r.costoMovilidad(f,pi,d)).min
    assert(c == min)
  }

  test("ProgramacionRiegoOptimo — distancias asimétricas") {
    val f = Vector((10,3,1),(8,1,1),(4,2,1))
    val d = Vector(Vector(0,3,10),Vector(1,0,5),Vector(2,7,0))
    val (_,c) = r.ProgramacionRiegoOptimo(f,d)
    val min = r.generarProgramacionesRiego(f).map(pi => r.costoRiegoFinca(f,pi)+r.costoMovilidad(f,pi,d)).min
    assert(c == min)
  }

  test("ProgramacionRiegoOptimo — finca 1 tablon") {
    val f = Vector((10,3,1))
    val d = Vector(Vector(0))
    val (pi,c) = r.ProgramacionRiegoOptimo(f,d)
    assert(pi == Vector(0) && c >= 0)
  }

  test("ProgramacionRiegoOptimo — caso grande 4 tablones") {
    val f = Vector((5,3,1),(8,2,1),(6,2,1),(4,3,2))
    val d = Vector(Vector(0,1,4,3),Vector(1,0,2,5),Vector(4,2,0,6),Vector(3,5,6,0))
    val (_,c) = r.ProgramacionRiegoOptimo(f,d)
    val min = r.generarProgramacionesRiego(f).map(pi => r.costoRiegoFinca(f,pi)+r.costoMovilidad(f,pi,d)).min
    assert(c == min)
  }

  test("ProgramacionRiegoOptimo — óptimo produce permutación válida") {
    val f = Vector((5,2,1),(7,1,1),(4,2,1))
    val d = Vector.fill(3,3)(1)
    val (pi,_) = r.ProgramacionRiegoOptimo(f,d)
    assert(pi.toSet == Set(0,1,2))
  }

  test("costoRiegoFincaPar — coincide con secuencial (1)") {
    val f = Vector((10,3,1),(8,1,1),(4,2,1))
    val pi = Vector(1,0,2)
    assert(r.costoRiegoFincaPar(f,pi) == r.costoRiegoFinca(f,pi))
  }

  test("costoRiegoFincaPar — coincide con secuencial (2)") {
    val f = Vector((3,1,2),(7,2,1),(6,3,2))
    val pi = Vector(2,0,1)
    assert(r.costoRiegoFincaPar(f,pi) == r.costoRiegoFinca(f,pi))
  }

  test("costoRiegoFincaPar — coincide con secuencial (3)") {
    val f = Vector((5,3,8),(4,1,2),(6,3,10))
    val pi = Vector(2,1,0)
    assert(r.costoRiegoFincaPar(f,pi) == r.costoRiegoFinca(f,pi))
  }

  test("costoRiegoFincaPar — coincide con secuencial (4)") {
    val f = Vector((5,3,3),(8,2,1),(6,2,1),(4,3,2))
    val pi = Vector(3,0,1,2)
    assert(r.costoRiegoFincaPar(f,pi) == r.costoRiegoFinca(f,pi))
  }

  test("costoRiegoFincaPar — coincide con secuencial (5)") {
    val f = Vector((10,3,1),(7,2,1),(6,3,2),(9,1,3),(4,2,1),(8,2,3))
    val pi = Vector(3,1,5,0,2,4)
    assert(r.costoRiegoFincaPar(f,pi) == r.costoRiegoFinca(f,pi))
  }


  test("costoMovilidadPar — coincide (1)") {
    val d = Vector(Vector(0,5,3),Vector(5,0,4),Vector(3,4,0))
    val f = Vector.fill(3)((1,1,1))
    val pi = Vector(2,0,1)
    assert(r.costoMovilidadPar(f,pi,d) == r.costoMovilidad(f,pi,d))
  }

  test("costoMovilidadPar — coincide (2)") {
    val d = Vector(Vector(0,3,10),Vector(1,0,5),Vector(2,7,0))
    val f = Vector.fill(3)((1,1,1))
    val pi = Vector(1,0,2)
    assert(r.costoMovilidadPar(f,pi,d) == r.costoMovilidad(f,pi,d))
  }

  test("costoMovilidadPar — coincide (3)") {
    val d = Vector.tabulate(6,6)((i,j)=>math.abs(i-j))
    val f = Vector.fill(6)((1,1,1))
    val pi = Vector(5,4,3,2,1,0)
    assert(r.costoMovilidadPar(f,pi,d) == r.costoMovilidad(f,pi,d))
  }

  test("costoMovilidadPar — coincide (4)") {
    val d = Vector(
      Vector(0,2,2,4,4), Vector(2,0,4,2,6),
      Vector(2,4,0,2,2), Vector(4,2,2,0,4),
      Vector(4,6,2,4,0)
    )
    val f = Vector.fill(5)((1,1,1))
    val pi = Vector(0,1,3,4,2)
    assert(r.costoMovilidadPar(f,pi,d) == r.costoMovilidad(f,pi,d))
  }

  test("costoMovilidadPar — coincide (5)") {
    val f = Vector((10,3,1))
    val pi = Vector(0)
    val d = Vector(Vector(0))
    assert(r.costoMovilidadPar(f,pi,d) == 0)
  }


  test("generarProgramacionesRiegoPar — coincide con secuencial (1)") {
    val f = Vector((10,3,1),(8,1,2),(4,2,3))
    assert(r.generarProgramacionesRiegoPar(f).toSet == r.generarProgramacionesRiego(f).toSet)
  }

  test("generarProgramacionesRiegoPar — coincide con secuencial (2)") {
    val f = Vector((5,2,1),(7,1,2),(3,1,3))
    val par = r.generarProgramacionesRiegoPar(f)
    assert(par.contains(Vector(0,1,2)))
    assert(par.contains(Vector(2,1,0)))
  }

  test("generarProgramacionesRiegoPar — coincide en tamaño (3)") {
    val f = Vector((1,1,1),(1,1,1),(1,1,1),(1,1,1))
    assert(r.generarProgramacionesRiegoPar(f).length == 24)
  }

  test("generarProgramacionesRiegoPar — coincide con secuencial (4)") {
    val f = Vector((10,3,1),(8,1,2),(4,2,3),(6,1,4))
    assert(r.generarProgramacionesRiegoPar(f).toSet == r.generarProgramacionesRiego(f).toSet)
  }

  test("generarProgramacionesRiegoPar — todas distintas (5)") {
    val f = Vector((1,1,1),(1,1,1),(1,1,1))
    val par = r.generarProgramacionesRiegoPar(f)
    assert(par.distinct.length == par.length)
  }


  test("ProgramacionRiegoOptimoPar — coincide con secuencial (1)") {
    val f = Vector((10,3,1),(8,1,1),(4,2,1))
    val d = Vector(Vector(0,2,4),Vector(2,0,6),Vector(4,6,0))
    val (_,c1) = r.ProgramacionRiegoOptimo(f,d)
    val (_,c2) = r.ProgramacionRiegoOptimoPar(f,d)
    assert(c1 == c2)
  }

  test("ProgramacionRiegoOptimoPar — coincide con secuencial (2)") {
    val f = Vector((5,3,3),(8,2,1),(6,2,1),(4,3,2))
    val d = Vector(
      Vector(0,1,4,3),Vector(1,0,2,5),
      Vector(4,2,0,6),Vector(3,5,6,0)
    )
    val (_,c1) = r.ProgramacionRiegoOptimo(f,d)
    val (_,c2) = r.ProgramacionRiegoOptimoPar(f,d)
    assert(c1 == c2)
  }

  test("ProgramacionRiegoOptimoPar — asimétrico (3)") {
    val f = Vector((10,3,1),(8,1,1),(4,2,1))
    val d = Vector(Vector(0,3,10),Vector(1,0,5),Vector(2,7,0))
    val (_,c1) = r.ProgramacionRiegoOptimo(f,d)
    val (_,c2) = r.ProgramacionRiegoOptimoPar(f,d)
    assert(c1 == c2)
  }

  test("ProgramacionRiegoOptimoPar — permutación válida (4)") {
    val f = Vector((5,2,1),(7,1,1),(4,2,1))
    val d = Vector.fill(3,3)(1)
    val (pi,_) = r.ProgramacionRiegoOptimoPar(f,d)
    assert(pi.toSet == Set(0,1,2))
  }

  test("ProgramacionRiegoOptimoPar — caso grande (5)") {
    val f = Vector((5,3,3),(8,2,1),(6,2,1),(4,3,2))
    val d = Vector(
      Vector(0,1,4,3),Vector(1,0,2,5),
      Vector(4,2,0,6),Vector(3,5,6,0)
    )
    val todas = r.generarProgramacionesRiego(f)
    val min = todas.map(pi => r.costoRiegoFinca(f,pi)+r.costoMovilidad(f,pi,d)).min
    val (_,cPar) = r.ProgramacionRiegoOptimoPar(f,d)
    assert(cPar == min)
  }

}
