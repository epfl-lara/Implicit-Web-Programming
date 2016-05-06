// repository for Typesafe plugins
resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.8")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.0")

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.6")

addSbtPlugin("com.vmunier" % "sbt-play-scalajs" % "0.2.8")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

// This plugin adds commands to generate IDE project files
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

// This plugin adds forked run capabilities to Play projects which is needed for Activator.
addSbtPlugin("com.typesafe.play" % "sbt-fork-run-plugin" % "2.4.6")

// web plugins

addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.7")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.1.0")

/** Following is copied from https://github.com/vmunier/play-with-scalajs-example/blob/master/project/plugins.sbt **/
// Comment to get more information during initialization
//logLevel := Level.Warn


// Sbt plugins
//addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.3")

addSbtPlugin("com.vmunier" % "sbt-play-scalajs" % "0.2.8")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")

/** End **/

// This plugin represents functionality that is to be added to sbt in the future
addSbtPlugin("org.scala-sbt" % "sbt-core-next" % "0.1.1")

//see http://lihaoyi.github.io/hands-on-scala-js/ (The Project Code/project/build.sbt
////provide the auto-reload-on-change behavior and the forwarding of SBT logspam to the browser console.
addSbtPlugin("com.lihaoyi" % "workbench" % "0.2.3")
