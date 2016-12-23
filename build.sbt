name := "entropy"

version := "0.1"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

scalaVersion := "2.11.8"

val sparkVersion = "2.0.1"

updateOptions := updateOptions.value.withCachedResolution(true)

libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-common" % "2.6.0" % "provided",
  "org.apache.spark" %% "spark-core"    % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql"     % sparkVersion % "provided",
  "org.apache.spark" %% "spark-hive"    % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib"   % sparkVersion % "provided",
  "org.apache.commons" % "commons-math3" % "3.6.1" % "provided")

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
  case "application.conf"                            => MergeStrategy.concat
  case "unwanted.txt"                                => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

