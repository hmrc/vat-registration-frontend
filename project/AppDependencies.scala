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

import play.sbt.PlayImport._
import sbt._

object AppDependencies {
  def apply(): Seq[ModuleID] = CompileDependencies() ++ UnitTestDependencies() ++ IntegrationTestDependencies()
}

private object CompileDependencies {
  private val simpleReactivemongoVersion = "7.31.0-play-26"
  private val bootstrapVersion = "3.2.0"
  private val timeVersion = "3.19.0"
  private val partialsVersion = "7.1.0-play-26"
  private val cachingClientVersion = "9.2.0-play-26"
  private val formMappingVersion = "1.5.0-play-26"
  private val catsVersion = "1.0.0"
  private val govukTemplateVersion = "5.60.0-play-26"
  private val playUiVersion = "8.19.0-play-26"
  private val playJsonJodaVersion = "2.9.2"

  private val playGovukFrontendVersion = "0.56.0-play-26"
  private val playHmrcFrontendVersion = "0.50.0-play-26"
  private val govukFrontendVersion = "3.10.2"

  private val compileDependencies: Seq[ModuleID] = Seq(
    cache,
    "uk.gov.hmrc" %% "time" % timeVersion,
    "uk.gov.hmrc" %% "simple-reactivemongo" % simpleReactivemongoVersion,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-26" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-partials" % partialsVersion,
    "uk.gov.hmrc" %% "http-caching-client" % cachingClientVersion,
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % formMappingVersion,
    "org.typelevel" %% "cats-core" % catsVersion,
    "uk.gov.hmrc" %% "govuk-template" % govukTemplateVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "com.typesafe.play" %% "play-json-joda" % playJsonJodaVersion,
    "uk.gov.hmrc" %% "play-frontend-govuk" % playGovukFrontendVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc" % playHmrcFrontendVersion,
    "org.webjars.npm" % "govuk-frontend" % govukFrontendVersion
  )

  def apply(): Seq[ModuleID] = compileDependencies
}

private trait TestDependencies {
  val scalaTestPlusPlayVersion = "3.1.3"
  val pegdownVersion = "1.6.0"
  val jsoupVersion = "1.11.2"
  val mockitoVersion = "2.13.0"
  val scalaMockVersion = "3.6.0"
  val wireMockVersion = "2.26.3"
  val reactivemongoTestVersion = "4.22.0-play-26"

  val scope: Configuration
  val testDependencies: Seq[ModuleID]
}

private object UnitTestDependencies extends TestDependencies {
  override val scope: Configuration = Test
  override val testDependencies: Seq[ModuleID] = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
    "org.pegdown" % "pegdown" % pegdownVersion % scope,
    "org.jsoup" % "jsoup" % jsoupVersion % scope,
    "org.mockito" % "mockito-core" % mockitoVersion % scope,
    "org.scalamock" %% "scalamock-scalatest-support" % scalaMockVersion % scope
  )

  def apply(): Seq[ModuleID] = testDependencies
}

private object IntegrationTestDependencies extends TestDependencies {
  override val scope: Configuration = IntegrationTest
  override val testDependencies: Seq[ModuleID] = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
    "com.github.tomakehurst" % "wiremock-jre8" % wireMockVersion % scope,
    "org.jsoup" % "jsoup" % jsoupVersion % scope,
    "uk.gov.hmrc" %% "reactivemongo-test" % reactivemongoTestVersion % scope
  )

  def apply(): Seq[ModuleID] = testDependencies
}
