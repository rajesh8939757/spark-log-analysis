
name := "spark-log-Analysis"

version := "0.1"

scalaVersion := "2.12.1"

val sparkVersion = "2.4.4"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "com.typesafe" % "config" % "1.3.2",
  "org.apache.spark" %% "spark-hive" % sparkVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0" excludeAll ExclusionRule (organization = "com.fasterxml.jackson.core")
)

resolvers += Resolver.mavenLocal
resolvers += "Cascading repo" at "http://conjars.org/repo"

assemblyMergeStrategy in assembly :=  {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}


assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
assemblyJarName in assembly := "spark-log-analysis_2_12.jar"

fullClasspath in Runtime := (fullClasspath in (Compile, run)).value