package scala.meta.internal.fastpass.pantsbuild

import scala.collection.mutable
import bloop.config.Config.CompileSetup
import java.{util => ju}

class CompileBFS(export: PantsExport) {
  private val exportsCache = mutable.Map.empty[String, Iterable[PantsTarget]]
  private val nonStrictDeps = new RuntimeBFS(export, CompileScope)
  private val isInProgress = new ju.ArrayDeque[String]

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
      val dep = export.targets(name)
      if (!dep.scope.isCompile) Nil
      else {
        val result = new mutable.LinkedHashSet[PantsTarget]()
        val exports: Seq[PantsTarget] =
          if (dep.pantsTargetType.isTarget) {
            dep.dependencies.map(export.targets)
          } else {
            for {
              dependencyName <- dep.dependencies
              if dep.exports.contains(dependencyName) ||
                // TODO(olafur): verify that this synthetic target is derived from exported target.
                export.targets(dependencyName).isSynthetic
            } yield export.targets(dependencyName)
          }
        exports.foreach { export =>
          result += export
          result ++= dependencyExports(export.name)
        }
        result
      }
    }
    exportsCache.getOrElseUpdate(name, uncached())
  }

}
