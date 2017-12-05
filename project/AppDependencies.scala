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

import play.core.PlayVersion
import sbt._
import play.sbt.PlayImport._

object AppDependencies {
  def apply(): Seq[ModuleID] = CompileDependencies() ++ UnitTestDependencies() ++ IntegrationTestDependencies()
}

private object CompileDependencies {
  private val bootstrapVersion       = "8.11.0"
  private val partialsVersion        = "6.1.0"
  private val cachingClientVersion   = "7.0.0"
  private val formMappingVersion     = "0.2.0"
  private val timeVersion            = "3.1.0"
  private val whitelistFilterVersion = "2.0.0"
  private val catsVersion            = "0.9.0"

  private val compileDependencies: Seq[ModuleID] = Seq(
    cache,
    "uk.gov.hmrc"   %% "frontend-bootstrap"            % bootstrapVersion,
    "uk.gov.hmrc"   %% "play-partials"                 % partialsVersion,
    "uk.gov.hmrc"   %% "http-caching-client"           % cachingClientVersion,
    "uk.gov.hmrc"   %% "play-conditional-form-mapping" % formMappingVersion,
    "uk.gov.hmrc"   %% "time"                          % timeVersion,
    "uk.gov.hmrc"   %% "play-whitelist-filter"         % whitelistFilterVersion,
    "org.typelevel" %% "cats"                          % catsVersion
  )

  def apply(): Seq[ModuleID] = compileDependencies
}

private trait TestDependencies {
  val scalaTestPlusPlayVersion = "2.0.1"
  val scoverageVersion         = "1.3.1"
  val jsoupVersion             = "1.11.2"
  val mockitoVersion           = "2.12.0"
  val scalaMockVersion         = "3.6.0"
  val wireMockVersion          = "2.6.0"
  val hmrcTestVersion          = "2.4.0"

  val scope: Configuration
  val testDependencies: Seq[ModuleID]
}

private object UnitTestDependencies extends TestDependencies {
  override val scope: Configuration = Test
  override val testDependencies: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "hmrctest"                      % hmrcTestVersion           % scope,
    "org.scalatestplus.play"  %% "scalatestplus-play"            % scalaTestPlusPlayVersion  % scope,
    "org.scoverage"           %  "scalac-scoverage-runtime_2.11" % scoverageVersion          % scope,
    "org.jsoup"               %  "jsoup"                         % jsoupVersion              % scope,
    "org.mockito"             %  "mockito-core"                  % mockitoVersion            % scope,
    "org.scalamock"           %% "scalamock-scalatest-support"   % scalaMockVersion          % scope
  )

  def apply(): Seq[ModuleID] = testDependencies
}

private object IntegrationTestDependencies extends TestDependencies {
  override val scope: Configuration = IntegrationTest
  override val testDependencies: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "hmrctest"                      % hmrcTestVersion          % scope,
    "org.scalatestplus.play"  %% "scalatestplus-play"            % scalaTestPlusPlayVersion % scope,
    "com.github.tomakehurst"  %  "wiremock"                      % wireMockVersion          % scope,
    "org.jsoup"               %  "jsoup"                         % jsoupVersion             % scope,
    "org.scoverage"           %  "scalac-scoverage-runtime_2.11" % scoverageVersion         % scope
  )

  def apply(): Seq[ModuleID] = testDependencies
}