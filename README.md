this is an up-to-date fork of the original [scalatest engine](https://github.com/scalatest/scalatestplus-junit5), with
only 1 difference:

## Engine Irrelevance: All test results must be verifiably equal on both engines (JUnit / ScalaTest)

this principle is rigorously enforced by comparing execution logs of engines running examples under
`org.scalatestplus.junit5.integration` package. If you found a counter-example, please submit your PR with a
new, miniaturised example, we will fix it ASAP.
