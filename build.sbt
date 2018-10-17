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

import TestPhases.oneForkedJvmPerTest
import play.sbt.PlayImport.PlayKeys
import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings, integrationTestSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.SbtAutoBuildPlugin

val appName = "vat-registration-frontend"

lazy val scoverageSettings = Seq(
  ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;controllers.test.*;models.api.*;views.*;forms.test.*;config.*;poc.view.*;poc.config.*;.*(AuthService|BuildInfo|Routes).*",
  ScoverageKeys.coverageMinimum          := 80,
  ScoverageKeys.coverageFailOnMinimum    := false,
  ScoverageKeys.coverageHighlighting     := true
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory) : _*)
  .configs(IntegrationTest)
  .settings(defaultSettings(), scalaSettings, scoverageSettings, publishingSettings)
  .settings(integrationTestSettings())
  .settings(majorVersion := 0)
  .settings()
  .settings(
    scalaVersion                                  :=  "2.11.11",
    libraryDependencies                           ++= AppDependencies(),
    resolvers                                     ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo),
    PlayKeys.playDefaultPort                      :=  9895,
    retrieveManaged                               :=  true,
    evictionWarningOptions in update              :=  EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    addTestReportOption(IntegrationTest, "int-test-reports")
  )