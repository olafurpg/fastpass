package scala.meta.internal.fastpass.pantsbuild

import ujson.Obj
import scala.collection.mutable

class PantsDependencies(allTargets: Obj) {
  val strictDependencyCache = mutable.Map.empty[String, List[String]]
}
