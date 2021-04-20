import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.DefaultBuildSettings

val appName = "organisations-details-api"

val silencerVersion = "1.7.1"

def intTestFilter(name: String): Boolean = name startsWith "it"
def unitFilter(name: String): Boolean = name startsWith "unit"
def componentFilter(name: String): Boolean = name startsWith "component"

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
    )
    // ***************
  )
  .settings(publishingSettings: _*)
  .settings(scoverageSettings: _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "resources")
  .settings(unitSettings)
  .configs(IntegrationTest)
  .settings(DefaultBuildSettings.integrationTestSettings())
  .settings(inConfig(IntegrationTest)(itSettings): _*)

lazy val unitSettings = Seq(
  testOptions in Test := Seq(Tests.Filter(_.startsWith("unit")))
)

lazy val itSettings = Defaults.itSettings ++ Seq(
  testOptions in IntegrationTest := Seq(Tests.Filter(_.startsWith("it"))),
)