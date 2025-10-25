package org.scalatestplus.junit5

import org.junit.platform.engine.discovery.DiscoverySelectors.{selectClass, selectPackage}
import org.junit.platform.launcher.core.{LauncherDiscoveryRequestBuilder, LauncherFactory}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite, funspec}
import org.scalatestplus.junit5.integration.NestedFixture
import listener.{JUnitListener, ScalaTestListener}

import java.lang.reflect.Modifier
import java.nio.file.{Files, Path, Paths}
import java.util.jar.JarFile

class IntegrationTests extends funspec.AnyFunSpec with BeforeAndAfterAll with BeforeAndAfterEach {

  import scala.collection.JavaConverters._

  var scalaTestEngineProperty: Option[String] = None

  override def beforeAll(): Unit = {
    scalaTestEngineProperty = Option(System.clearProperty("org.scalatestplus.junit5.ScalaTestEngine.disabled"))
  }

  override def afterAll(): Unit = {
    scalaTestEngineProperty.foreach(System.setProperty("org.scalatestplus.junit5.ScalaTestEngine.disabled", _))
  }

  val classPathRoot: Path = Paths.get(this.getClass.getProtectionDomain.getCodeSource.getLocation.toURI)

  lazy val integrationDir: Path = {

    def resolveIntegrationDir(root: Path) = {

      val result = root.resolve("org/scalatestplus/junit5/integration")

      println(s"integration path set to $result")
      result
    }

    resolveIntegrationDir(classPathRoot)
  }

  lazy val integrationClasspath: Path = {
    val classPathRoot = Paths.get(this.getClass.getProtectionDomain.getCodeSource.getLocation.toURI)
    // Returns the classpath root: either a directory containing .class files or a .jar file
    classPathRoot
  }

  private def findTopLevelTestClasses(path: Path): Set[Class[_]] = {
    val suiteClass = classOf[Suite]

    def isQualified(cls: Class[_]): Boolean = {

      val isSuite = suiteClass.isAssignableFrom(cls)
      val isConcrete = !Modifier.isAbstract(cls.getModifiers) && !cls.isInterface
      val notInner = cls.getEnclosingClass == null

      isSuite && isConcrete && notInner
    }

    val classPathRoot = Paths.get(getClass.getProtectionDomain.getCodeSource.getLocation.toURI)

    val classNames = if (Files.isDirectory(path)) {
      Files
        .list(path)
        .iterator()
        .asScala
        .filter(p => p.toString.endsWith(".class"))
        .map(p => classPathRoot.relativize(p).toString.replace('/', '.').dropRight(".class".length))
        .toSet
    } else if (path.toString.toLowerCase.endsWith(".jar")) {
      val jarFile = new JarFile(path.toFile)
      try {
        jarFile
          .entries()
          .asScala
          .filter(e => e.getName.endsWith(".class"))
          .map(_.getName.replace('/', '.').dropRight(".class".length))
          .toSet
      } finally {
        jarFile.close()
      }
    } else {
      Set.empty[String]
    }

    classNames
      .flatMap { className =>
        try {
          val classLoader = getClass.getClassLoader
          val cls = Class.forName(className, false, classLoader)
          val result: Option[Class[_]] = Some(cls).filter(isQualified)
          result
        } catch {
          case _: Throwable => None
        }
      }
  }

  lazy val integrationTestClasses: Set[(Class[_], Int)] = findTopLevelTestClasses(integrationDir).zipWithIndex

  override def beforeEach(): Unit = {
    ScalaTestListener.clear()
    JUnitListener.clear()
  }

  describe("ScalaTest & JUnit runners should discovery & run identical tests") {

    describe("in class") {

      integrationTestClasses.foreach { case (clz, i) =>
        val clzName = clz.getName
        it(s"[$i] ${clzName}") {

          // ScalaTest runner
          org.scalatest.tools.Runner.run(
            Array(
              "-R",
              classPathRoot.toString,
              "-s",
              clzName,
              "-C",
              classOf[ScalaTestListener].getName
              //            "-oN"
            )
          )

          // JUnit 5 runner
          {
            val launcher = LauncherFactory.create()

            val discoveryRequest = LauncherDiscoveryRequestBuilder.request
              .selectors(
                selectClass(clzName)
              )
              .build()

            launcher.execute(discoveryRequest, new JUnitListener())
          }

          JUnitListener.started.shouldBe(ScalaTestListener.started)
          JUnitListener.finished.shouldBe(ScalaTestListener.finished)
        }
      }
    }

    describe("in class (-R integrationDir)") {

      integrationTestClasses.foreach { case (clz, i) =>
        val clzName = clz.getName
        it(s"[$i] ${clz.getName}") {

          // ScalaTest runner
          org.scalatest.tools.Runner.run(
            Array(
              "-R",
              integrationDir.toString,
              "-s",
              clzName,
              "-C",
              classOf[ScalaTestListener].getName
              //            "-oN"
            )
          )

          // JUnit 5 runner
          {
            val launcher = LauncherFactory.create()

            val discoveryRequest = LauncherDiscoveryRequestBuilder.request
              .selectors(
                selectClass(clzName)
              )
              .build()

            launcher.execute(discoveryRequest, new JUnitListener())
          }

          JUnitListener.started.shouldBe(ScalaTestListener.started)
          JUnitListener.finished.shouldBe(ScalaTestListener.finished)
        }
      }
    }

    ignore("in package") {
      // TODO: wait for https://github.com/scalatest/scalatest/issues/2405

      val pkgNames = Seq(classOf[NestedFixture].getPackage.getName)

      pkgNames.foreach { pkg =>
        it(pkg) {

          // ScalaTest runner
          org.scalatest.tools.Runner.run(
            Array(
              "-R",
              classPathRoot.toString,
              "-w",
              pkg,
              "-C",
              classOf[ScalaTestListener].getName
              //        "-oN"
            )
          )

          // JUnit 5 runner
          {
            val launcher = LauncherFactory.create()

            val discoveryRequest = LauncherDiscoveryRequestBuilder.request
              .selectors(
                selectPackage(pkg)
              )
              .build()

            launcher.execute(discoveryRequest, new JUnitListener())
          }

          JUnitListener.started.shouldBe(ScalaTestListener.started)
          JUnitListener.finished.shouldBe(ScalaTestListener.finished)
        }
      }
    }

    ignore("in package (-R integrationDir)") {
      // TODO: not working, should be a bug of scalatest runner
      //  wait for https://github.com/scalatest/scalatest/issues/2406

      val pkgNames = Seq(classOf[NestedFixture].getPackage.getName)

      pkgNames.foreach { pkg =>
        it(pkg) {

          // ScalaTest runner
          org.scalatest.tools.Runner.run(
            Array(
              "-R",
              integrationDir.toString,
              "-w",
              pkg,
              "-C",
              classOf[ScalaTestListener].getName
              //        "-oN"
            )
          )

          // JUnit 5 runner
          {
            val launcher = LauncherFactory.create()

            val discoveryRequest = LauncherDiscoveryRequestBuilder.request
              .selectors(
                selectPackage(pkg)
              )
              .build()

            launcher.execute(discoveryRequest, new JUnitListener())
          }

          JUnitListener.started.shouldBe(ScalaTestListener.started)
          JUnitListener.finished.shouldBe(ScalaTestListener.finished)
        }
      }
    }
  }
}
