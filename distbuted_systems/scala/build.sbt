name := "rmi-distributed-system"

version := "1.0"

scalaVersion := "2.13.14"

lazy val shared = project.in(file("shared"))
    .settings(
        scalaVersion := "2.13.14",
        libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value
    )

lazy val worker = project.in(file("worker"))
    .dependsOn(shared)
    .settings(
        scalaVersion := "2.13.14",
        libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value
    )

lazy val master = project.in(file("master"))
    .dependsOn(shared)
    .settings(
        scalaVersion := "2.13.14",
        libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value
    )

lazy val client = project.in(file("client"))
    .dependsOn(shared)
    .settings(
        scalaVersion := "2.13.14",
        libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value
    )

lazy val root = project.in(file("."))
    .aggregate(shared, worker, master, client)
    .dependsOn(shared, worker, master, client)
    .settings(
        scalaVersion := "2.13.14",
        assembly / mainClass := None,
        assembly / assemblyJarName := "project-assembly-1.0.jar"
    )

libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value