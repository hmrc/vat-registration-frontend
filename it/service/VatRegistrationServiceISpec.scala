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

package service

import common.enums.VatRegStatus
import connectors._
import itutil.{IntegrationSpecBase, WiremockHelper}
import models.CurrentProfile
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.SessionId

import java.util.UUID

class VatRegistrationServiceISpec extends IntegrationSpecBase {
  val mockHost = WiremockHelper.wiremockHost
  val mockPort = WiremockHelper.wiremockPort
  val mockUrl = s"http://$mockHost:$mockPort"

  val additionalConfiguration = Map(
    "microservice.services.vat-registration.host" -> s"$mockHost",
    "microservice.services.vat-registration.port" -> s"$mockPort",
    "microservice.services.cachable.session-cache.host" -> s"$mockHost",
    "microservice.services.cachable.session-cache.port" -> s"$mockPort",
    "application.router" -> "testOnlyDoNotUseInAppConf.Routes",
    "microservice.services.incorporation-information.uri" -> "/test-incorporation-information",
    "microservice.services.incorporation-information.host" -> s"$mockHost",
    "microservice.services.incorporation-information.port" -> s"$mockPort"
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(additionalConfiguration)
    .build

  val sId = UUID.randomUUID().toString
  implicit val hc = HeaderCarrier(sessionId = Some(SessionId(sId)))

  def currentProfile(regId: String): CurrentProfile = CurrentProfile(
    registrationId = regId,
    vatRegistrationStatus = VatRegStatus.draft
  )

  "submitRegistration" should {

    "get a Success response if submitted successfully" in {
      val regId = "12345"

      stubPut(s"/vatreg/$regId/submit-registration", 200, "")

      val vatRegistrationService = app.injector.instanceOf[VatRegistrationService]
      val response = vatRegistrationService.submitRegistration()(hc, currentProfile(regId), FakeRequest())

      await(response) mustBe Success
    }
  }
}
