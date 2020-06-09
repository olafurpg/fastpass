package scala.meta.internal.fastpass.pantsbuild

sealed abstract class SearchScope
case object CompileScope extends SearchScope
case object RuntimeScope extends SearchScope
