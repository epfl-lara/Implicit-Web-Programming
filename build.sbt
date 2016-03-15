import sbt.Keys._
import sbt.Project.projectToRef

/**
  * For debugging scalajs:
  * * To check that the paths to the unmanagedSourceDirectories are correct:
  * > show sharedJS/compile:unmanagedSourceDirectories
  * * To check the exact set of .scala files that are used.
  * > show sharedJS/compile:sources
  */

// a special crossProject for configuring a JS/JVM/shared structure
lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(
    scalaVersion := Settings.versions.scala,
    libraryDependencies ++= Settings.sharedDependencies.value,
//	unmanagedSourceDirectories in Compile += baseDirectory.value / "../../leon/library",
//    scalaSource in Compile := file("../../leon/library")
    scalaSource in Compile := baseDirectory.value / "../../leon/library"
  )
  // set up settings specific to the JS project
  .jsConfigure(_ enablePlugins ScalaJSPlay)

/*backup*/ lazy val sharedJVM = shared.jvm.settings(name := "sharedJVM")
//lazy val sharedJVM = shared.jvm.settings(name := "sharedJVM", resources in Compile += (fastOptJS in sharedJS).value.data)

/*backup*/ lazy val sharedJS = shared.js.settings(name := "sharedJS")
//lazy val sharedJS = shared.js.settings(name := "sharedJS", artifactPath in fastOptJS :=
//  (resourceManaged in sharedJVM in Compile).value /
//    ((moduleName in fastOptJS).value + "-fastopt.js"))

lazy val leonSource = RootProject(file("leon"))

//lazy val leonLibraryCollection: Project = (project in file("leon/library/collection"))
lazy val leonLibrary = (project in file("leon/library"))
  .settings(
      name := "leonLibrary",
      version := Settings.version,
      scalaVersion := Settings.versions.scala,
      scalacOptions ++= Settings.scalacOptions,
      scalaSource in Compile := file("leon/library"),
      libraryDependencies += "me.chrons" %%% "boopickle" % "1.1.0"
  )

/*lazy val webDSL: Project = (project in file("leon/library/webDSL"))
  .settings(
      name := "webDSL",
      version := Settings.version,
      scalaVersion := Settings.versions.scala,
      scalacOptions ++= Settings.scalacOptions
  )
  .dependsOn(leonLibrary)*/
//lazy val webDSL = RootProject(file("leon/library/webDSL")).dependsOn(leonLibraryCollection)

// use eliding to drop some debug code in the production build
lazy val elideOptions = settingKey[Seq[String]]("Set limit for elidable functions")

//Resolver for "libraryDependencies += "com.scalawarrior" %%% "scalajs-ace" % "0.0.2","
resolvers += "amateras-repo" at "http://amateras.sourceforge.jp/mvn/"

// instantiate the JS project for SBT with some additional settings
lazy val client: Project = (project in file("client"))
  .settings(
    name := "client",
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Settings.scalajsDependencies.value,
    libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.5.4",
    libraryDependencies += "com.scalawarrior" %%% "scalajs-ace" % "0.0.2",
    libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "0.10.4",
    // by default we do development build, no eliding
    elideOptions := Seq(),
    scalacOptions ++= elideOptions.value,
    jsDependencies ++= Settings.jsDependencies.value,
    // RuntimeDOM is needed for tests
    jsDependencies += RuntimeDOM % "test",
    // yes, we want to package JS dependencies
    skip in packageJSDependencies := false,
    // use Scala.js provided launcher code to start the client app
    persistLauncher := true,
    persistLauncher in Test := false,
    // use uTest framework for tests
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .dependsOn(sharedJS)



// Client projects (just one in this case)
lazy val clients = Seq(client)

// instantiate the JVM project for SBT with some additional settings
lazy val server = (project in file("server"))
  .settings(
    name := "server",
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Settings.jvmDependencies.value,
    commands += ReleaseCmd,
    // connect to the client project
    scalaJSProjects := clients,
    pipelineStages := Seq(scalaJSProd, digest, gzip),
    // compress CSS
    LessKeys.compress in Assets := true
    //attempt at adding the output path of JavaScripts from scala.js to Play (http://stackoverflow.com/questions/25990605/how-to-add-the-output-path-of-javascripts-from-scala-js-to-play)
    //    ,unmanagedResourceDirectories in Assets += (target in client).value / "scala-2.11"
  )
  .enablePlugins(PlayScala)
//  .disablePlugins(PlayLayoutPlugin) // use the standard directory layout instead of Play's custom
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(sharedJVM)
  .dependsOn(leonSource)
  .dependsOn(leonLibrary)

// Command for building a release
lazy val ReleaseCmd = Command.command("release") {
  state => "set elideOptions in client := Seq(\"-Xelide-below\", \"WARNING\")" ::
    "client/clean" ::
    "client/test" ::
    "server/clean" ::
    "server/test" ::
    "server/dist" ::
    "set elideOptions in client := Seq()" ::
    state
}

// lazy val root = (project in file(".")).aggregate(client, server)

// loads the Play server project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


