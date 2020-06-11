package scala.meta.internal.fastpass.pantsbuild.commands

import metaconfig.cli.Command
import metaconfig.cli.CliApp

object FixCommand extends Command[FixOptions]("fix") {

  override def run(value: Value, app: CliApp): Int = {
    FixDependenciesCommand.run(
      value.dependencies.copy(common = value.common),
      app
    )
  }

}
