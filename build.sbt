/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import play.sbt.PlayImport.PlayKeys
import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "vat-registration-frontend"

val silencerVersion = "1.7.0"

lazy val scoverageSettings = Seq(
  ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;connectors.test.*;controllers.test.*;featureswitch.*;models.test.*;utils.*;models.api.*;views.test.*;forms.test.*;config.*;poc.view.*;poc.config.*;.*(AuthService|BuildInfo|Routes).*",
  ScoverageKeys.coverageMinimumStmtTotal := 90,
  ScoverageKeys.coverageFailOnMinimum := true,
  ScoverageKeys.coverageHighlighting := true
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(PlayScala, SbtDistributablesPlugin): _*)
  .configs(IntegrationTest)
  .settings(defaultSettings(), scalaSettings, scoverageSettings, publishingSettings)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(majorVersion := 1)
  .settings(
    isPublicArtefact := true
  )
  .settings(
    IntegrationTest / fork                        := true,
    IntegrationTest / testForkedParallel          := false,
    IntegrationTest / parallelExecution           := false,
    IntegrationTest / logBuffered                 := false,
    IntegrationTest / unmanagedSourceDirectories  := (IntegrationTest / baseDirectory) (base => Seq(base / "it")).value,
    Test / fork                                   := true,
    Test / testForkedParallel                     := false,
    Test / parallelExecution                      := true,
    Test / logBuffered                            := false,
    addTestReportOption(IntegrationTest, "int-test-reports")
  )
  .settings(
    scalaVersion := "2.12.12",
    libraryDependencies ++= AppDependencies(),
    PlayKeys.playDefaultPort := 9895,
    retrieveManaged := true,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api._",
      "play.api.i18n._",
      "play.api.mvc._",
      "play.api.data._"
    ),
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.govukfrontend.views.html.components.implicits._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components.implicits._",
      "views.ViewUtils._"
    )
  )
  .settings(
    // silence all warnings on autogenerated files
    scalacOptions += "-P:silencer:pathFilters=target/.*",
    // Make sure you only exclude warnings for the project directories, i.e. make builds reproducible
    scalacOptions += s"-P:silencer:sourceRoots=${baseDirectory.value.getCanonicalPath}",
    // Suppress warnings due to mongo dates using `$date` in their Json representation
    scalacOptions += "-P:silencer:globalFilters=possible missing interpolator: detected interpolated identifier `\\$date`",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
  )
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
