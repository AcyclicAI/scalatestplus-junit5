package org.scalatestplus.junit5.integration

import org.scalatest.funspec.AnyFunSpec

object InnerObjectFixture1 {

  object I1 extends AnyFunSpec {

    it("a") {}
  }
}
