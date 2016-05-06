import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt._

/**
 * Application settings. Configure the build for your application here.
 * You normally don't have to touch the actual build definition after this.
 */
object Settings {
  /** The name of your application */
  val name = "implicit-web-programming"

  /** The version of your application */
  val version = "1.1.2"

  /** Options for the scala compiler */
  val scalacOptions = Seq(
    "-Xlint",
    "-unchecked",
    "-deprecation",
    "-feature"
  )

  /** Declare global dependency versions here to avoid mismatches in multi part dependencies */
  object versions {
    val scala = "2.11.8"
    val scalaDom = "0.9.0"
    val scalajsReact = "0.11.1"
    val scalaCSS = "0.4.1"
    val scalajs = "0.6.8"
    val log4js = "1.4.10"
    val autowire = "0.2.5"
    val booPickle = "1.1.3"
    val diode = "0.5.1"
    val uTest = "0.4.3"
    val scalajsjQuery = "0.8.0"

    val react = "15.0.1"
    val jQuery = "2.1.3"
    val bootstrap = "3.3.2"
    val chartjs = "1.0.1"

    val playScripts = "0.4.0"
  }

  /**
   * These dependencies are shared between JS and JVM projects
   * the special %%% function selects the correct version for each project
   */
  val sharedDependencies = Def.setting(Seq(
    "me.chrons" %%% "boopickle" % versions.booPickle,
    "com.lihaoyi" %%% "utest" % versions.uTest
  ))

  /** Dependencies only used by the JVM project */
  val jvmDependencies = Def.setting(Seq(
    "com.vmunier" %% "play-scalajs-scripts" % versions.playScripts,
    "org.webjars" % "font-awesome" % "4.3.0-1" % Provided,
    "org.webjars" % "bootstrap" % versions.bootstrap % Provided,
    "com.lihaoyi" %% "autowire" % versions.autowire,
    "org.webjars" % "jquery" % "2.1.1",
    "org.scala-js" % s"scalajs-compiler_${versions.scala}" % versions.scalajs,
    "org.scala-js" %% "scalajs-tools" % versions.scalajs,
    //"org.scala-lang.modules" %% "scala-async" % "0.9.1" % "provided",
    //"com.lihaoyi" %% "scalatags" % "0.4.5",
    "com.lihaoyi" %% "acyclic" % "0.1.2" % "provided"
  ))

  /** Dependencies only used by the JS project (note the use of %%% instead of %%) */
  val scalajsDependencies = Def.setting(Seq(
    "com.github.japgolly.scalajs-react" %%% "core" % versions.scalajsReact,
    "com.github.japgolly.scalajs-react" %%% "extra" % versions.scalajsReact,
    "com.github.japgolly.scalacss" %%% "ext-react" % versions.scalaCSS,
    "be.doeraene" %%% "scalajs-jquery" % versions.scalajsjQuery,
    "me.chrons" %%% "diode" % versions.diode,
    "me.chrons" %%% "diode-react" % versions.diode,
    "org.scala-js" %%% "scalajs-dom" % versions.scalaDom,
    "com.lihaoyi" %%% "autowire" % versions.autowire
  ))

  /** Dependencies for external JS libs that are bundled into a single .js file according to dependency order */
  val jsDependencies = Def.setting(Seq(
    "org.webjars.bower" % "react" % versions.react / "react-with-addons.js" minified "react-with-addons.min.js" commonJSName "React",
    "org.webjars.bower" % "react" % versions.react / "react-dom.js" minified "react-dom.min.js" dependsOn "react-with-addons.js" commonJSName "ReactDOM",
    "org.webjars.bower" % "react" % versions.react / "react-dom-server.js" minified  "react-dom-server.min.js" dependsOn "react-dom.js" commonJSName "ReactDOMServer",
    "org.webjars" % "jquery" % versions.jQuery / "jquery.js" minified "jquery.min.js",
    "org.webjars" % "bootstrap" % versions.bootstrap / "bootstrap.js" minified "bootstrap.min.js" dependsOn "jquery.js",
    "org.webjars" % "chartjs" % versions.chartjs / "Chart.js" minified "Chart.min.js",
    "org.webjars" % "log4javascript" % versions.log4js / "js/log4javascript_uncompressed.js" minified "js/log4javascript.js",
    "org.webjars" % "ace" % "01.08.2014" / "META-INF/resources/webjars/ace/01.08.2014/src-noconflict/ace.js"
  ))
}
