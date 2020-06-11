package scala.meta.internal.fastpass.pantsbuild

import scala.collection.mutable
import bloop.config.Config.CompileSetup
import java.{util => ju}

class CompileBFS(export: PantsExport) {
  private case class ExportKey(name: String, isDepthOne: Boolean)
  private val exportsCache = mutable.Map.empty[ExportKey, Iterable[PantsTarget]]
  private val nonStrictDeps = new RuntimeBFS(export, CompileScope)
  private val isInProgress = new ju.ArrayDeque[String]

  def dependencies(target: PantsTarget): Iterable[PantsTarget] = {
    if (!target.strictDeps) {
      nonStrictDeps.dependencies(target)
    } else {
      val result = new mutable.LinkedHashSet[PantsTarget]()
      result ++= target.dependencies.iterator.map(export.targets)
      target.dependencies.foreach { dependencyName =>
        result ++= dependencyExports(dependencyName, 1)
      }
      result
    }
  }

  private def dependencyExports(
      name: String,
      depth: Int
  ): Iterable[PantsTarget] = {

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
                // TODO(olafur): verify that this synthetic target is derived from exported target.
                export.targets(dependencyName).isSynthetic
            } yield dependencyName
          }
        exportNames.foreach { exportName =>
          result += export.targets(exportName)
          result ++= dependencyExports(exportName, depth + 1)
        }
        result
      }
    }
    val key = ExportKey(name, depth == 1)
    exportsCache.getOrElseUpdate(key, uncached())
  }

}
