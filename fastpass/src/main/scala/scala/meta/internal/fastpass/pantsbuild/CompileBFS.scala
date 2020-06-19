package scala.meta.internal.fastpass.pantsbuild

import scala.collection.mutable
import java.{util => ju}
import scala.meta.internal.fastpass.pantsbuild.commands.StrictDepsMode

/**
 * Implementation of the Pants build graph traversal to compute the JVM compile-time classpath.
 */
class CompileBFS(export: PantsExport, mode: StrictDepsMode) {
  private val exportsCache = mutable.Map.empty[String, Iterable[PantsTarget]]
  private val runtime = new RuntimeBFS(export, CompileScope)
  private val isInProgress = new ju.HashSet[String]

  def dependencies(target: PantsTarget): Iterable[PantsTarget] = {
    if (mode.isTransitive || !target.strictDeps) {
      // Use the same classpath as at runtime
      runtime.dependencies(target)
    } else {
      val result = new mutable.LinkedHashSet[PantsTarget]()
      def loop(dep: PantsTarget, depth: Int): Unit = {
        if (depth < 0) return
        result ++= dep.dependencies.map(export.targets)
        for {
          transitive <- dep.dependencies
        } {
          result ++= dependencyExports(transitive)
        }
        val newDepth =
          if (dep.pantsTargetType.isTarget) depth
          else depth - 1
        dep.dependencies.foreach(d => loop(export.targets(d), newDepth))
      }
      loop(target, mode.plusDepth)
      result
    }
  }

  private def dependencyExports(name: String): Iterable[PantsTarget] = {
    if (isInProgress.contains(name)) {
      throw new IllegalArgumentException(s"illegal cycle at target '$name'")
    }

    def uncached(): Iterable[PantsTarget] = {
      val dep = export.targets(name)
      if (!dep.scope.isCompile) Nil
      else {
        isInProgress.add(name)
        val result = new mutable.LinkedHashSet[PantsTarget]()
        val exportNames: Seq[String] =
          if (dep.pantsTargetType.isTarget) {
            // NOTE(olafur): it seems like `target` types export their
            // dependencies even if they have an empty `exports` field. This
            // should probably be reflected in the output of `export-fastpass`
            // but for now it's OK to do this logic on the fastpass-side
            // instead.
            dep.dependencies
          } else {
            for {
              dependencyName <- dep.dependencies
              if dep.exports.contains(dependencyName) ||
                // NOTE(olafur): all synthetic dependencies are automatically
                // exported if they are derived from this target. We don't check
                // that this synthetic target is derived from this target, which
                // might cause subtle bugs that we may need to fix in the
                // future.
                export.targets(dependencyName).isSynthetic
            } yield dependencyName
          }
        result ++= exportNames.iterator.map(export.targets)
        exportNames.foreach { exportName =>
          result ++= dependencyExports(exportName)
        }
        isInProgress.remove(name)
        result
      }
    }

    exportsCache.getOrElseUpdate(name, uncached())
  }

}
