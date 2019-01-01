name := """diskreport"""

version := "1.0"

scalaVersion := "2.12.7"

// Change this to another test framework if you prefer
libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "11-R16",
  "org.scalatest" %% "scalatest" % "3.2.0-SNAP10" % Test
)

//enablePlugins(JavaAppPackaging)

mainClass in assembly := Some("com.binbo_kodakusan.Application")
test in assembly := {}

// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.11"

// ScalaFXでデフォルトのCSSが見当たらない - きくらげ観察日記 <http://inkar-us-i.hatenablog.com/entry/2016/02/24/150000>
unmanagedJars in Compile += {
  val ps = new sys.SystemProperties
  val jh = ps("java.home")
  Attributed.blank(file(jh) / "lib/ext/jfxrt.jar")
}
