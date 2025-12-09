package taller.bench

import java.io.{BufferedWriter, File, FileWriter}
import scala.util.Try
import scala.math.sqrt
import scala.collection.parallel.CollectionConverters._
import taller.Riego

object BenchManual {

  case class Stats(mean: Double, stddev: Double)
  case class Row(n: Int,
                 genSeq: Stats, genPar: Stats,
                 evalSeq: Stats, evalPar: Stats,
                 totalSeq: Stats, totalPar: Stats) {
    def speedupMean: Double = if (totalPar.mean == 0.0) Double.PositiveInfinity else totalSeq.mean / totalPar.mean
    def speedupPercentMean: Double = if (totalPar.mean == 0.0) 0.0 else (speedupMean - 1.0) * 100.0
  }

  // Generadores
  def genFinca(n: Int): Riego#Finca =
    Vector.tabulate(n)(i => (10 + (i % 7), 1 + (i % 3), 1 + (i % 5)))

  def genDist(n: Int): Riego#Distancia =
    Vector.tabulate(n, n) { (i, j) => if (i == j) 0 else math.abs(i - j) }

  private def timeTrials(reps: Int)(block: => Any): Stats = {
    Try(block)
    val times = (1 to reps).map { _ =>
      val t0 = System.nanoTime()
      Try(block)
      val t1 = System.nanoTime()
      (t1 - t0) / 1_000_000.0
    }.filter(!_.isNaN).map(_.toDouble)
    if (times.isEmpty) Stats(Double.PositiveInfinity, Double.PositiveInfinity)
    else {
      val mean = times.sum / times.size
      val variance = times.map(x => math.pow(x - mean, 2)).sum / times.size
      Stats(mean, sqrt(variance))
    }
  }

  def estimateP(speedup: Double, cores: Int): Double = {
    if (speedup <= 1.0 || cores <= 1) 0.0
    else {
      val invS = 1.0 / speedup
      val p = (1.0 - invS) / (1.0 - 1.0 / cores)
      math.max(0.0, math.min(1.0, p))
    }
  }

  def main(args: Array[String]): Unit = {
    val sizes: Seq[Int] = if (args.nonEmpty) args.map(_.toInt).toSeq else Seq(6, 7, 8)
    val reps = if (args.length >= 2) args(1).toInt else 5 // default reps = 5
    println(s"BenchManual: tamaños = ${sizes.mkString(", ")}, reps = $reps")
    val r = new Riego()
    val cores = Runtime.getRuntime.availableProcessors()
    println(s"Nº de cores lógicos detectados: $cores")

    val rows = sizes.map { n =>
      println(s"\n--- Ejecutando n = $n ---")
      val f = genFinca(n)
      val d = genDist(n)

      // 1) medición de generación de permutaciones (seq / par)
      val genSeqStats = timeTrials(reps) { r.generarProgramacionesRiego(f) }
      val genParStats = timeTrials(reps) { r.generarProgramacionesRiegoPar(f) }

      // Para evaluación, generamos permutaciones secuencialmente y usamos esa lista
      val permsSeq: Vector[r.ProgRiego] = r.generarProgramacionesRiego(f)

      val evalSeqStats = timeTrials(reps) {
        // evaluamos secuencialmente sobre la lista de permutaciones
        permsSeq.map(pi => r.costoRiegoFinca(f, pi) + r.costoMovilidad(f, pi, d)).sum
      }
      val evalParStats = timeTrials(reps) {
        // usar .par aquí; CollectionConverters import habilita esto
        permsSeq.par.map(pi => r.costoRiegoFincaPar(f, pi) + r.costoMovilidadPar(f, pi, d)).sum
      }

      // Total: medir ProgramacionRiegoOptimo completo
      val totalSeqStats = timeTrials(reps) { r.ProgramacionRiegoOptimo(f, d) }
      val totalParStats = timeTrials(reps) { r.ProgramacionRiegoOptimoPar(f, d) }

      val row = Row(n, genSeqStats, genParStats, evalSeqStats, evalParStats, totalSeqStats, totalParStats)

      println(f"gen seq: ${row.genSeq.mean}%.3f±${row.genSeq.stddev}%.3f ms, gen par: ${row.genPar.mean}%.3f±${row.genPar.stddev}%.3f ms")
      println(f"eval seq: ${row.evalSeq.mean}%.3f±${row.evalSeq.stddev}%.3f ms, eval par: ${row.evalPar.mean}%.3f±${row.evalPar.stddev}%.3f ms")
      println(f"total seq: ${row.totalSeq.mean}%.3f±${row.totalSeq.stddev}%.3f ms, total par: ${row.totalPar.mean}%.3f±${row.totalPar.stddev}%.3f ms")
      println(f"speedup mean = ${row.speedupMean}%.3f, accel = ${row.speedupPercentMean}%.2f%%")

      if (row.speedupMean <= 1.0) println("[NOTICE] La versión paralela no mejora para este tamaño (overhead mayor).")

      row
    }

    val docsDir = new File("docs")
    if (!docsDir.exists()) docsDir.mkdirs()
    val csvFile = new File(docsDir, "benchmarks.csv")
    val bw = new BufferedWriter(new FileWriter(csvFile))
    try {
      bw.write("n,genSeqMean,genSeqStd,genParMean,genParStd,evalSeqMean,evalSeqStd,evalParMean,evalParStd,totalSeqMean,totalSeqStd,totalParMean,totalParStd,speedupPercent\n")
      rows.foreach { r =>
        bw.write(f"${r.n},${r.genSeq.mean}%.3f,${r.genSeq.stddev}%.3f,${r.genPar.mean}%.3f,${r.genPar.stddev}%.3f,${r.evalSeq.mean}%.3f,${r.evalSeq.stddev}%.3f,${r.evalPar.mean}%.3f,${r.evalPar.stddev}%.3f,${r.totalSeq.mean}%.3f,${r.totalSeq.stddev}%.3f,${r.totalPar.mean}%.3f,${r.totalPar.stddev}%.3f,${r.speedupPercentMean}%.3f\n")
      }
      println(s"\n[OK] CSV escrito en: ${csvFile.getAbsolutePath}")
    } catch {
      case e: Exception => println(s"[ERROR] No se pudo escribir el archivo: ${e.getMessage}")
    } finally bw.close()

    // Amdahl estimate
    println("\nEstimación de p (Ley de Amdahl) usando número de cores detectado:")
    rows.foreach { r =>
      val s = if (r.totalPar.mean == 0.0) 1.0 else r.totalSeq.mean / r.totalPar.mean
      val p = estimateP(s, cores)
      println(f"n=${r.n}%2d: speedup=${s}%.3f, p ≈ ${p*100.0}%.2f%%")
    }

    println("\nBenchManual finalizado.")
  }
}
