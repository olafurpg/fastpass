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
      val dependencies = target.dependencies.map(export.targets)
      result ++= dependencies
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
        val exportNames: Seq[String] =
          if (dep.pantsTargetType.isTarget) {
            dep.dependencies
          } else {
            for {
              dependencyName <- dep.dependencies
              if dep.exports.contains(dependencyName) ||
                export.targets(dependencyName).isSynthetic
            } yield dependencyName
          }
        exportNames.foreach { exportName =>
          result += export.targets(exportName)
          result ++= dependencyExports(exportName)
        }
        result
      }
    }

    exportsCache.getOrElseUpdate(name, uncached())
  }

}
