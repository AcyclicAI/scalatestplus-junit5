package org.scalatestplus.junit5.integration

import org.scalatest.funspec.AnyFunSpec

object InnerObjectFixture2 {

  class Inner extends AnyFunSpec {
    it("a") {}
  }

  object I1 extends Inner
}
