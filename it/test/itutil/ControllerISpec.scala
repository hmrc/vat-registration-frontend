/*
 * Copyright 2024 HM Revenue & Customs
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

package itutil

import featuretoggle.FeatureToggleSupport
import helpers.RequestsFinder
import itFixtures.ITRegistrationFixtures
import org.scalatest.TestSuite
import models.CurrentProfile
import org.mongodb.scala.model.Filters
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import repositories.SessionRepository
import support.AppAndStubs
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Await
import scala.concurrent.duration._

trait ControllerISpec extends IntegrationSpecBase with TestSuite
  with AppAndStubs
  with ScalaFutures
  with RequestsFinder
  with ITRegistrationFixtures
  with FeatureToggleSupport {

  override def beforeEach(): Unit = {
    super.beforeEach()
    val sessionRepository = app.injector.instanceOf[SessionRepository]
    val timeout = 5.seconds
    Await.result(sessionRepository.collection.deleteMany(Filters.empty()).toFuture(), timeout)
    Await.result(sessionRepository.upsert(CacheMap(sessionString, Map("CurrentProfile" -> Json.toJson[CurrentProfile](currentProfile)))), timeout)
  }

}
