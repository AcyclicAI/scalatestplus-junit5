package demo

import java.nio.file.Paths

object RunNestedFixtureDemo {
  def main(args: Array[String]): Unit = {
    println("Running all tests in org.scalatestplus.junit5.integration package...\n")

//    val classPath: String = System.getProperty("java.class.path").split(':').head
    val classPath = this.getClass.getProtectionDomain.getCodeSource.getLocation.toURI.getPath

    val result = org.scalatest.tools.Runner.run(
      Array(
        "-o",
        "-R",
        classPath,
        "-w",
        "org.scalatestplus.junit5.integration"
      )
    )

    println(s"\nTests completed with result: $result")
    System.exit(if (result) 0 else 1)
  }
}
