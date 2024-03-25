import play.core.PlayVersion
import sbt._

object AppDependencies {

  val hmrc = "uk.gov.hmrc"
  val playVersion = "play-30"
  val hmrcMongoVersion = "1.7.0"
  var bootstrapVersion = "8.4.0"

  val compile = Seq(
    s"$hmrc.mongo"        %% s"hmrc-mongo-$playVersion"         % hmrcMongoVersion,
    "uk.gov.hmrc"         %% s"play-hmrc-api-$playVersion"      % "8.0.0",
    hmrc                  %% s"play-hal-$playVersion"           % "4.0.0",
    hmrc                  %% s"crypto-json-$playVersion"        % "7.6.0",
  )

  def test(scope: String = "test, it") = Seq(
    hmrc                           %% s"bootstrap-test-$playVersion"    % bootstrapVersion        % scope,
    s"$hmrc.mongo"                 %% s"hmrc-mongo-test-$playVersion"   % hmrcMongoVersion        % scope,
    "org.scalatestplus"            %% "mockito-3-4"                     % "3.2.7.0"               % scope,
    "org.scalatestplus"            %% "scalacheck-1-17"                 % "3.2.16.0"              % scope,
    "org.scalatest"                %% "scalatest"                       % "3.2.15"                % scope,
    "org.playframework"            %% "play-test"                       % PlayVersion.current     % scope,
    "org.scalatestplus.play"       %% "scalatestplus-play"              % "7.0.0"                 % scope,
    "org.pegdown"                  %  "pegdown"                         % "1.6.0"                 % scope,
    "com.github.tomakehurst"       %  "wiremock-jre8"                   % "2.27.2"                % scope,
    "org.mockito"                  %  "mockito-core"                    % "3.8.0"                 % scope,
    "org.scalaj"                   %% "scalaj-http"                     % "2.4.2"                 % scope
  )
}
