package scala.meta.internal.fastpass.pantsbuild.commands

import metaconfig.cli.Command
import metaconfig.cli.CliApp
import scala.collection.immutable.Nil
import ujson.Bool

object FixDependenciesCommand
    extends Command[FixDependenciesOptions]("fix-dependencies") {

  case class Node(
      name: String,
      id: String,
      isTargetRoot: Boolean,
      deps: List[String],
      exports: List[String]
  )

  override def run(value: Value, app: CliApp): Int = {
    SharedCommand.currentProject(value.common) match {
      case None =>
        app.error(
          "no active fastpass project. " +
            "To fix this problem, run 'fastpass switch' to an existing project. " +
            "Use 'fastpass list' to see available projects. "
        )
        1
      case Some(project) =>
        for {
          _ <- value.common.exec(
            List("./pants", "export-classpath") ++ project.targets,
            app
          )
          exportLines <- value.common.exec(
            List("./pants", "export-fastpass") ++ project.targets,
            // List("./pants", "export-classpath", "export") ++ project.targets,
            app
          )
        } yield {
          val export = ujson.read(exportLines.mkString)
          val dist =
            value.common.workspace.resolve("dist").resolve("export-classpath")
          val nodes = for {
            (name, target) <- export.obj("targets").obj.value
          } yield Node(
            name,
            target.obj("id").str,
            target.obj.get("is_target_root").contains(Bool(true)),
            target.obj("targets").arr.map(_.str).toList,
            Nil
          )
          pprint.log(nodes)
          pprint.log(nodes.filter(_.isTargetRoot))
          1
        }
        1
    }
  }

}
