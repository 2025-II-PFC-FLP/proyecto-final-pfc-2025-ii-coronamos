package taller.bench

import org.scalameter.api._
import org.scalameter.picklers.Implicits._
import taller.Riego

object RiegoScalameterBench extends Bench.LocalTime {


  override def reporter: Reporter[Double] = new LoggingReporter

  val sizes: Gen[Int] = Gen.enumeration("n")(6, 7, 8)

  def genFinca(n: Int): Riego#Finca =
    Vector.tabulate(n)(i => (10 + (i % 7), 1 + (i % 3), 1 + (i % 5)))

  def genDist(n: Int): Riego#Distancia =
    Vector.tabulate(n, n) { (i, j) => if (i == j) 0 else math.abs(i - j) }

  performance of "ProgramacionRiegoOptimo" in {
    measure method "seq" in {
      using(sizes) in { n =>
        val r = new Riego()
        val f = genFinca(n)
        val d = genDist(n)
        r.ProgramacionRiegoOptimo(f, d)
      }
    }

    measure method "par" in {
      using(sizes) in { n =>
        val r = new Riego()
        val f = genFinca(n)
        val d = genDist(n)
        r.ProgramacionRiegoOptimoPar(f, d)
      }
    }
  }

  performance of "generarProgramacionesRiego" in {
    measure method "seq" in {
      using(sizes) in { n =>
        val r = new Riego()
        val f = genFinca(n)
        r.generarProgramacionesRiego(f)
      }
    }

    measure method "par" in {
      using(sizes) in { n =>
        val r = new Riego()
        val f = genFinca(n)
        r.generarProgramacionesRiegoPar(f)
      }
    }
  }

  performance of "costoRiegoFinca" in {
    measure method "seq" in {
      using(sizes) in { n =>
        val r = new Riego()
        val f = genFinca(n)
        val pi = Vector.tabulate(n)(i => i)
        r.costoRiegoFinca(f, pi)
      }
    }

    measure method "par" in {
      using(sizes) in { n =>
        val r = new Riego()
        val f = genFinca(n)
        val pi = Vector.tabulate(n)(i => i)
        r.costoRiegoFincaPar(f, pi)
      }
    }
  }
}
