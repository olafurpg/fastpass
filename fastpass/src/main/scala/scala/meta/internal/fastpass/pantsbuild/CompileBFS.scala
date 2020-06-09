package scala.meta.internal.fastpass.pantsbuild

import scala.collection.mutable
import bloop.config.Config.CompileSetup

class CompileBFS(export: PantsExport) {
  private val exportsCache = mutable.Map.empty[String, Iterable[PantsTarget]]
  private val nonStrictDeps = new RuntimeBFS(export, CompileScope)

  def dependencies(target: PantsTarget): Iterable[PantsTarget] = {
    if (!target.strictDeps) {
      nonStrictDeps.dependencies(target)
    } else {
      val result = new mutable.LinkedHashSet[PantsTarget]()
      result ++= target.dependencies.iterator.map(export.targets)
      target.dependencies.foreach { dependencyName =>
        result ++= dependencyExports(dependencyName)
      }
      result
    }
  }
  private def dependencyExports(name: String): Iterable[PantsTarget] = {
    def uncached(): Iterable[PantsTarget] = {
      val result = new mutable.LinkedHashSet[PantsTarget]()
      val dep = export.targets(name)
      val exports =
        if (dep.pantsTargetType.isTarget) dep.dependencies
        else dep.exports
      exports.foreach { exportName =>
        result += export.targets(exportName)
        result ++= dependencyExports(exportName)
      }
      result
    }
    exportsCache.getOrElse(name, uncached())
  }

}
