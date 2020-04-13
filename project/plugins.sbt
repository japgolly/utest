val scalaJSVersion = Option(System.getenv("SCALAJS_VERSION")).getOrElse("0.6.32")

addSbtPlugin("com.jsuereth"       % "sbt-pgp"                  % "1.1.2-1")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % scalaJSVersion)
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
