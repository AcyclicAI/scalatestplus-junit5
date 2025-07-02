this is an up-to-date fork of the original [scalatest engine](https://github.com/scalatest/scalatestplus-junit5), there
is only 1 difference:

## The effect of every test must be verified to be irrelevant to its runner

effects of JUnit and ScalaTest runner are rigorously compared for all fixtures under
`org.scalatestplus.junit5.integration` package. If you encounter inconsistency in your project, submit your PR with a
new, miniaturised example, we will fix it ASAP.
