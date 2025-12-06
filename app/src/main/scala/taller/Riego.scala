package taller

class Riego {

  type Tablon = (Int, Int, Int)
  type Finca = Vector[Tablon]
  type ProgRiego = Vector[Int]
  type TiempoInicioRiego = Vector[Int]
  type Distancia = Vector[Vector[Int]]

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

  def costoMovilidad(f: Finca, pi: ProgRiego, d: Distancia): Int = {
    val perm = progToPermPure(pi)
    val n = perm.length

    if (n <= 1) 0
    else {
      (0 until (n - 1)).toVector
        .map(j => d(perm(j))(perm(j + 1)))
        .sum
    }
  }

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

  def permToProg(perm: Vector[Int]): ProgRiego =
    Vector.tabulate(perm.length)(i => perm.indexOf(i))

  def generarProgramacionesRiego(f: Finca): Vector[ProgRiego] = {
    val base = (0 until f.length).toVector
    val perms = permutations(base)
    perms.map(permToProg)
  }

  def ProgramacionRiegoOptimo(f: Finca, d: Distancia): (ProgRiego, Int) = {
    val todas = generarProgramacionesRiego(f)

    val evaluaciones = todas.map { pi =>
      val total = costoRiegoFinca(f, pi) + costoMovilidad(f, pi, d)
      (pi, total)
    }

    evaluaciones.minBy(_._2)
  }
}
