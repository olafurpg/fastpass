package scala.meta.internal.fastpass.pantsbuild

import ujson.Value

case class Graph(
    index: String => Int,
    rindex: Int => String,
    graph: Array[Array[Int]]
)
object Graph {
  def fromTargets(targets: IndexedSeq[PantsTarget]): Graph = {
    val edges = new Array[Array[Int]](targets.length)
    val index = targets.map(_.name).zipWithIndex.toMap
    val rindex = index.map(_.swap).toMap
    targets.zipWithIndex.foreach {
      case (project, i) =>
        edges(i) = Iterator(
          project.dependencies.iterator,
          project.javaSources.iterator
        ).flatten.map(index).toArray
    }
    Graph(index.apply _, rindex.apply _, edges)
  }
}
