import sbt.Tests.{Group, SubProcess}

TwirlKeys.templateImports := Seq.empty

val appName = "organisations-details-api"

lazy val ItTest = config("it") extend Test
lazy val playSettings: Seq[Setting[_]] = Seq(
  routesImport ++= Seq("uk.gov.hmrc.organisationsdetailsapi.utils.Binders._")
)

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;" +
      ".*BuildInfo.;uk.gov.hmrc.BuildInfo;.*Routes;.*RoutesPrefix*;" +
      // All after this is due to Early project and getting pipelines up and running. May be removed later.
      "uk.gov.hmrc.organisationsdetailsapi.views;" +
      ".*DocumentationController*;" +
      "uk.gov.hmrc.organisationsdetailsapi.handlers;" +
      ".*definition*;",
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

def intTestFilter(name: String): Boolean = name startsWith "it"
def unitFilter(name: String): Boolean = name startsWith "unit"
def componentFilter(name: String): Boolean = name startsWith "component"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    onLoadMessage := "",
    majorVersion := 0,
    scalaVersion := "2.13.12",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    scalacOptions += "-Wconf:src=routes/.*:s",
    Test / testOptions := Seq(Tests.Filter(unitFilter))
  )
  .settings(PlayKeys.playDefaultPort := 9656)
  .settings(playSettings)

  // Integration tests
  .configs(ItTest)
  .settings(inConfig(ItTest)(Defaults.testSettings): _*)
  .settings(
    ItTest / Keys.fork := true,
    ItTest / unmanagedSourceDirectories := (ItTest / baseDirectory)(base => Seq(base / "test")).value,
    ItTest / testOptions := Seq(Tests.Filter(intTestFilter)),
    ItTest / parallelExecution := false
  )
  .configs(ComponentTest)
  .settings(inConfig(ComponentTest)(Defaults.testSettings): _*)
  .settings(
    ComponentTest / testOptions := Seq(Tests.Filter(componentFilter)),
    ComponentTest / unmanagedSourceDirectories := (ComponentTest / baseDirectory)(base => Seq(base / "test")).value,
    ComponentTest / testGrouping := oneForkedJvmPerTest((ComponentTest / definedTests).value),
    ComponentTest / parallelExecution := false
  )
  .settings(scoverageSettings: _*)
  .settings(
    resolvers ++= Seq(
      Resolver.jcenterRepo
    )
  )
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources")

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
lazy val ComponentTest = config("component") extend Test
