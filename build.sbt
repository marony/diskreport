name := """diskreport"""

version := "1.0"

scalaVersion := "2.11.8"

// Change this to another test framework if you prefer
libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "8.0.92-R10",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

enablePlugins(JavaAppPackaging)

//mainClass in Compile := Some("com.binbo_kodakusan.Application")

// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.11"

// ScalaFXでデフォルトのCSSが見当たらない - きくらげ観察日記 <http://inkar-us-i.hatenablog.com/entry/2016/02/24/150000>
unmanagedJars in Compile += {
  val ps = new sys.SystemProperties
  val jh = ps("java.home")
  Attributed.blank(file(jh) / "lib/ext/jfxrt.jar")
}