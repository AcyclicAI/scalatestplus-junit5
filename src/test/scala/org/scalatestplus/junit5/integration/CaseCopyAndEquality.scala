package org.scalatestplus.junit5.integration

import org.scalatest.funspec.AnyFunSpec

import java.util.UUID

object CaseCopyAndEquality {
  trait X1 extends Product1[UUID] {

    def id: UUID

    override def _1: UUID = id

    override def canEqual(that: Any): Boolean = {
      if (that.isInstanceOf[X1]) true
      else false
    }
  }

  case class XX private[CaseCopyAndEquality] (val id: UUID = UUID.randomUUID())(val value: String)

}

class CaseCopyAndEquality extends AnyFunSpec {

  import CaseCopyAndEquality._

  it("copy should not change equality") {

    val r1 = XX()("a")

    val r2 = r1.copy()("b")

    assert(r1 == r2)

    assert(r1.equals(r2))

  }
}
