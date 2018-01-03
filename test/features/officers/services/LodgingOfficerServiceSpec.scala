/*
 * Copyright 2018 HM Revenue & Customs
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

package features.officers.services

import java.time.LocalDate

import common.enums.VatRegStatus
import common.exceptions.InternalExceptions.NoOfficerFoundException
import connectors.{RegistrationConnector, S4LConnect}
import features.officers.models.view.LodgingOfficer
import helpers.FutureAssertions
import mocks.VatMocks
import models.CurrentProfile
import models.api.Name
import models.external.Officer
import models.view.vatLodgingOfficer.OfficerSecurityQuestionsView
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsValue, Json}
import services.IncorporationInfoSrv
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class LodgingOfficerServiceSpec extends PlaySpec with MockitoSugar with VatMocks with FutureAssertions {
  val testRegId = "testRegId"

  implicit val hc = HeaderCarrier()
  implicit val currentProfile = CurrentProfile("Test Me", testRegId, "000-434-1", VatRegStatus.draft, None)

  val emptyLodgingOfficer = LodgingOfficer(None, None)
  val partialLodgingOfficer = LodgingOfficer(Some("TestName"), None)
  val validLodgingOfficer = LodgingOfficer(
    Some("TestName"),
    Some(OfficerSecurityQuestionsView(LocalDate.of(1998, 7, 12), "Director"))
  )
  val validOfficer = Officer(
    name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
    role = "Director"
  )

  class Setup(s4lData: Option[LodgingOfficer] = None, backendData: Option[JsValue] = None) {
    val service = new LodgingOfficerService {
      override val s4lConnector: S4LConnect = mockS4LConnector
      override val incorpInfoService: IncorporationInfoSrv = mockIncorpInfoService
      override val vatRegistrationConnector: RegistrationConnector = mockRegConnector
    }

    when(mockS4LConnector.fetchAndGet[LodgingOfficer](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(s4lData))

    when(mockRegConnector.getLodgingOfficer(any(),any())).thenReturn(Future.successful(backendData))

    when(mockS4LConnector.save(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))
  }

  class SetupForS4LSave(t: LodgingOfficer = emptyLodgingOfficer) {
    val service = new LodgingOfficerService {
      override val s4lConnector: S4LConnect = mockS4LConnector
      override val incorpInfoService: IncorporationInfoSrv = mockIncorpInfoService
      override val vatRegistrationConnector: RegistrationConnector = mockRegConnector

      override def getLodgingOfficer(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[LodgingOfficer] = {
        Future.successful(t)
      }
    }

    when(mockS4LConnector.save(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))
  }

  class SetupForBackendSave(t: LodgingOfficer = validLodgingOfficer, list: Seq[Officer] = Seq(validOfficer)) {
    val service = new LodgingOfficerService {
      override val s4lConnector: S4LConnect = mockS4LConnector
      override val incorpInfoService: IncorporationInfoSrv = mockIncorpInfoService
      override val vatRegistrationConnector: RegistrationConnector = mockRegConnector

      override def getLodgingOfficer(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[LodgingOfficer] = {
        Future.successful(t)
      }
    }

    when(mockIncorpInfoService.getOfficerList(any(), any())).thenReturn(Future.successful(list))

    when(mockRegConnector.patchLodgingOfficer(any(), any())(any(),any())).thenReturn(Future.successful(Json.toJson("""{}""")))

    when(mockS4LConnector.clear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(HttpResponse(200)))
  }

  "Calling getLodgingOfficer" should {
    val jsonLodgingOfficer = Json.parse(
      s"""
         |{
         |  "name": {
         |    "first": "TestFirst",
         |    "middle": "TestMiddle",
         |    "last": "TestLast"
         |  },
         |  "dob": "1998-07-12",
         |  "nino": "SR123456Z"
         |}
       """.stripMargin)

    "return a default LodgingOfficer view model if nothing is in S4L & backend" in new Setup {
      service.getLodgingOfficer returns emptyLodgingOfficer
    }

    "return a partial LodgingOfficer view model from S4L" in new Setup(Some(partialLodgingOfficer)) {
      service.getLodgingOfficer returns partialLodgingOfficer
    }

    "return a full LodgingOfficer view model from backend" in new Setup(None, Some(jsonLodgingOfficer)) {
      val expected = LodgingOfficer(
        Some("TestFirstTestMiddleTestLast"),
        Some(OfficerSecurityQuestionsView(dob = LocalDate.of(1998, 7, 12), nino = "SR123456Z"))
      )
      service.getLodgingOfficer returns expected
    }
  }

  "Calling updateCompletionCapacity" should {
    "return a LodgingOfficer and save to S4L" in new SetupForS4LSave {
      val expected = emptyLodgingOfficer.copy(completionCapacity = Some("FirstLast"))

      service.updateCompletionCapacity("FirstLast") returns expected
    }

    "return a LodgingOfficer and save to backend" in new SetupForBackendSave {
      val expected = validLodgingOfficer.copy(completionCapacity = Some(validOfficer.name.id))

      service.updateCompletionCapacity(validOfficer.name.id) returns expected
    }

    "throw a NoOfficerFoundException if there's no match in Officer List when trying to save to backend" in new SetupForBackendSave {
      service.updateCompletionCapacity("NotFound") failedWith classOf[NoOfficerFoundException]
    }
  }
}
