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
  private val simpleReactivemongoVersion = "8.0.0-play-28"
  private val bootstrapVersion = "5.16.0"
  private val timeVersion = "3.25.0"
  private val partialsVersion = "8.2.0-play-28"
  private val cachingClientVersion = "9.5.0-play-28"
  private val formMappingVersion = "1.10.0-play-28"
  private val catsVersion = "1.0.0"
  private val playUiVersion = "9.7.0-play-28"
  private val playJsonJodaVersion = "2.9.2"

  private val playHmrcFrontendVersion = "3.3.0-play-28"

  private val compileDependencies: Seq[ModuleID] = Seq(
    caffeine,
    "uk.gov.hmrc" %% "time" % timeVersion,
    "uk.gov.hmrc" %% "simple-reactivemongo" % simpleReactivemongoVersion,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-partials" % partialsVersion,
    "uk.gov.hmrc" %% "http-caching-client" % cachingClientVersion,
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % formMappingVersion,
    "org.typelevel" %% "cats-core" % catsVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "com.typesafe.play" %% "play-json-joda" % playJsonJodaVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc" % playHmrcFrontendVersion
  )

  def apply(): Seq[ModuleID] = compileDependencies
}

private trait TestDependencies {
  val scalaTestPlusPlayVersion = "5.1.0"
  val pegdownVersion = "1.6.0"
  val jsoupVersion = "1.13.1"
  val mockitoVersion = "3.3.0"
  val scalaMockVersion = "3.6.0"
  val wireMockVersion = "2.27.2"
  val reactivemongoTestVersion = "5.0.0-play-28"

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
    "org.scalamock" %% "scalamock-scalatest-support" % scalaMockVersion % scope,
    "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % scope,
    "org.scalatestplus" %% "mockito-3-4" % "3.2.9.0" % "test"
  )

  def apply(): Seq[ModuleID] = testDependencies
}

private object IntegrationTestDependencies extends TestDependencies {
  override val scope: Configuration = IntegrationTest
  override val testDependencies: Seq[ModuleID] = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
    "com.github.tomakehurst" % "wiremock-jre8" % wireMockVersion % scope,
    "org.jsoup" % "jsoup" % jsoupVersion % scope,
    "uk.gov.hmrc" %% "reactivemongo-test" % reactivemongoTestVersion % scope,
    "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % scope
  )

  def apply(): Seq[ModuleID] = testDependencies
}
