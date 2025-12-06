package taller

class Riego {

  type Tablon = (Int, Int, Int)
  type Finca = Vector[Tablon]
  type ProgRiego = Vector[Int]
  type TiempoInicioRiego = Vector[Int]

  def tsup(f: Finca, i: Int): Int = f(i)._1
  def treg(f: Finca, i: Int): Int = f(i)._2
  def prio(f: Finca, i: Int): Int = f(i)._3


  def progToPermPure(pi: ProgRiego): Vector[Int] = {
    val pairs = pi.zipWithIndex
    pairs.sortBy(_._1).map(_._2)
  }

  def progIndexOfPerm(perm: Vector[Int]): Vector[Int] = {
    val arr = Array.fill(perm.length)(0)
    for (turno <- perm.indices) {
      val idx = perm(turno)
      arr(idx) = turno
    }
    arr.toVector
  }

  def tIR_fromPerm(f: Finca, perm: Vector[Int]): TiempoInicioRiego = {
    val n = f.length

    val timesByTurn: Vector[Int] = {
      def go(j: Int, acc: Int, accVec: Vector[Int]): Vector[Int] =
        if (j >= n) accVec
        else {
          val nuevoAccVec = accVec :+ acc
          val siguienteAcc = acc + treg(f, perm(j))
          go(j + 1, siguienteAcc, nuevoAccVec)
        }
      go(0, 0, Vector.empty)
    }

    val indexToTurn = progIndexOfPerm(perm)
    Vector.tabulate(n)(i => timesByTurn(indexToTurn(i)))
  }

  def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = {
    val perm = progToPermPure(pi)
    tIR_fromPerm(f, perm)
  }

  def costoRiegoTablon(i: Int, f: Finca, pi: ProgRiego): Int = {
    val t = tIR(f, pi)(i)
    val tsi = tsup(f, i)
    val tri = treg(f, i)

    if (tsi - tri >= t)
      tsi - (t + tri)
    else
      prio(f, i) * ((t + tri) - tsi)
  }

  def costoRiegoFinca(f: Finca, pi: ProgRiego): Int = {
    (0 until f.length).toVector.map(i => costoRiegoTablon(i, f, pi)).sum
  }

}
