package utest.framework

final case class DurationAndName(durMs: Long, nameParts: Array[String]) {
  def name = nameParts.mkString(".")
}

object DurationAndName {

  val slowestFirst: Ordering[DurationAndName] =
    Ordering.by((_: DurationAndName).durMs).reverse
}