package org.scalatestplus.junit5

import org.junit.jupiter.api.Assertions
import org.junit.platform.engine.TestExecutionResult

import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions

package object listener {

  object Listener {

    abstract class Record[T](
        title: String = "<undefined>", // e.g. Junit/ScalaTest
        val internal: ListBuffer[T] = ListBuffer.empty[T]
    ) extends Product {

      private lazy val subtitle = this.productPrefix // e.g. Started/Finished

      private lazy val fullTitle = s"$title.$subtitle"

      def shouldBeText(
          expected: String = null,
          effectiveTitle: String = this.fullTitle
      ): Unit = {
        val actual: String = internal.mkString("\n")

        if (expected == null)
          throw new IllegalArgumentException(s"expecting ground truth for $effectiveTitle:\n\n$actual")
        Assertions.assertEquals(
          "\n" + expected.trim.split("\n").sorted.mkString("\n") + "\n",
          "\n" + actual.trim.split("\n").sorted.mkString("\n") + "\n",
          s"Runners ($effectiveTitle) yield inconsistent results:\n"
        )
      }

      def shouldBe(expected: Record[T] = null): Unit = {

        val effectiveTitle = s"expected: ${expected.fullTitle} --- actual: ${this.fullTitle}"

        shouldBeText(expected.internal.mkString("\n"), effectiveTitle)
      }
    }

    object Record {

      implicit def toInternal[T](r: Record[T]): ListBuffer[T] = r.internal

//      implicit def fromStr(v: String): Record = {}
    }

  }

  trait Listener {

    import Listener._

    lazy val title: String = this.getClass.getSimpleName.stripSuffix("$").stripSuffix("Listener")

    type QN = String // Qualified Name

    case object started extends Record[QN](title)
    case object finished extends Record[(TestExecutionResult.Status, QN)](title)

    def clear(): Unit = {
      started.clear()
      finished.clear()
    }
  }

}
