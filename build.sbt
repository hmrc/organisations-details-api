import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

TwirlKeys.templateImports := Seq.empty

val appName = "organisations-details-api"

val silencerVersion = "1.7.1"

lazy val playSettings: Seq[Setting[_]] = Seq(
  routesImport ++= Seq(
    "uk.gov.hmrc.organisationsdetailsapi.utils.Binders._"))

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;" +
      ".*BuildInfo.;uk.gov.hmrc.BuildInfo;.*Routes;.*RoutesPrefix*;" +
      //All after this is due to Early project and getting pipelines up and running. May be removed later.
      "uk.gov.hmrc.organisationsdetailsapi.views;" +
      ".*DocumentationController*;" +
      "uk.gov.hmrc.organisationsdetailsapi.handlers;" +
      ".*definition*;",
    ScoverageKeys.coverageMinimum := 80,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

def intTestFilter(name: String): Boolean = name startsWith "it"
def unitFilter(name: String): Boolean = name startsWith "unit"
def componentFilter(name: String): Boolean = name startsWith "component"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.12.12",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test(),
    // ***************
    // Use the silencer plugin to suppress warnings
    scalacOptions += "-P:silencer:pathFilters=routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    ),
    // ***************
    testOptions in Test := Seq(Tests.Filter(unitFilter))
  )
  .settings(PlayKeys.playDefaultPort := 9656)
  .settings(playSettings)

  // Integration tests

  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := true,
    unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest)(
      base => Seq(base / "test")).value,
    testOptions in IntegrationTest := Seq(Tests.Filter(intTestFilter)),
    testGrouping in IntegrationTest := oneForkedJvmPerTest(
      (definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest := false
  )
  .configs(ComponentTest)
  .settings(inConfig(ComponentTest)(Defaults.testSettings): _*)
  .settings(
    testOptions in ComponentTest := Seq(Tests.Filter(componentFilter)),
    unmanagedSourceDirectories in ComponentTest := (baseDirectory in ComponentTest)(base => Seq(base / "test")).value,
    testGrouping in ComponentTest := oneForkedJvmPerTest((definedTests in ComponentTest).value),
    parallelExecution in ComponentTest := false
  )
  .settings(publishingSettings: _*)
  .settings(scoverageSettings: _*)
  .settings(resolvers ++= Seq(
    Resolver.jcenterRepo
  ))
  .settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "resources")

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
lazy val ComponentTest = config("component") extend Test