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

  private val hmrcMongoVersion = "1.8.0"
  private val bootstrapVersion = "8.4.0"
  private val partialsVersion = "9.1.0"
  private val cachingClientVersion = "11.1.0"
  private val formMappingVersion = "2.0.0"
  private val catsVersion = "2.10.0"
  private val playJsonJodaVersion = "3.0.2"
  private val playHmrcFrontendVersion = "8.5.0"


  val compileDependencies: Seq[ModuleID] = Seq(
    caffeine,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % hmrcMongoVersion,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-partials-play-30" % partialsVersion,
    "uk.gov.hmrc" %% "http-caching-client-play-30" % cachingClientVersion,
    "uk.gov.hmrc" %% "play-conditional-form-mapping-play-30" % formMappingVersion,
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.playframework" %% "play-json-joda" % playJsonJodaVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playHmrcFrontendVersion
  )

  val testDependencies: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalamock" %% "scalamock" % "6.0.0",
    "com.github.tomakehurst" % "wiremock-jre8" % "2.35.1",
    "com.vladsch.flexmark" % "flexmark-all" % "0.64.8"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compileDependencies ++ testDependencies
}










