package scala.meta.internal.fastpass.pantsbuild.commands

import metaconfig.generic
import java.nio.file.Path
import scala.meta.internal.fastpass.pantsbuild.Codecs._
import metaconfig.annotation._
import metaconfig.generic.Settings
import java.nio.file.Paths
import scala.meta.io.AbsolutePath
import metaconfig.{ConfDecoder, ConfEncoder}
import scala.meta.internal.io.PathIO
import scala.sys.process.ProcessLogger
import metaconfig.cli.CliApp
import scala.collection.mutable

case class SharedOptions(
    @Description("The root directory of the Pants build.")
    workspace: Path = PathIO.workingDirectory.toNIO
) {
  val pants: AbsolutePath = AbsolutePath(workspace.resolve("pants"))
  def bloopDirectory: Path = workspace.resolve(".bloop")
  val home: AbsolutePath = AbsolutePath {
    Option(System.getenv("FASTPASS_HOME")) match {
      case Some(value) => Paths.get(value)
      case None => workspace.resolveSibling("bsp-projects")
    }
  }
  def exec(command: List[String], app: CliApp): Either[Int, List[String]] = {
    val lines = mutable.ListBuffer.empty[String]
    app.info(command.mkString(" "))
    val exit = scala.sys.process
      .Process(command, cwd = Some(workspace.toFile()))
      .!(ProcessLogger(out => lines += out, err => app.err.println(err)))
    if (exit == 0) {
      Right(lines.toList)
    } else {
      Left(exit)
    }
  }
}

object SharedOptions {
  val default: SharedOptions = SharedOptions()
  implicit lazy val surface: generic.Surface[SharedOptions] =
    generic.deriveSurface[SharedOptions]
  implicit lazy val encoder: ConfEncoder[SharedOptions] =
    generic.deriveEncoder[SharedOptions]
  implicit lazy val decoder: ConfDecoder[SharedOptions] =
    generic.deriveDecoder[SharedOptions](default)
  implicit lazy val settings: Settings[SharedOptions] = Settings[SharedOptions]
}
