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

package features.frs.controllers

import java.util.MissingResourceException

import connectors.{ConfigConnector, KeystoreConnector}
import features.frs.services.FlatRateService
import features.sicAndCompliance.models.{MainBusinessActivityView, SicAndCompliance}
import features.sicAndCompliance.services.SicAndComplianceService
import features.turnoverEstimates.{TurnoverEstimates, TurnoverEstimatesService}
import fixtures.VatRegistrationFixture
import frs.{FRSDateChoice, FlatRateScheme}
import helpers.{ControllerSpec, MockMessages}
import models._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import services.TimeService
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.Future

class FlatRateControllerSpec extends ControllerSpec with VatRegistrationFixture with MockMessages {
  val jsonBusinessTypes = Json.parse(
    s"""
       |[
       |  {
       |    "groupLabel": "Test 1",
       |    "categories": [
       |      {"id": "020", "businessType": "Hotel or accommodation", "currentFRSPercent": 10.5},
       |      {"id": "019", "businessType": "Test BusinessType", "currentFRSPercent": 3},
       |      {"id": "038", "businessType": "Pubs", "currentFRSPercent": "5"}
       |    ]
       |  },
       |  {
       |    "groupLabel": "Test 2",
       |    "categories": [
       |      {"id": "039", "businessType": "Cafes", "currentFRSPercent": "5"}
       |    ]
       |  }
       |]
        """.stripMargin).as[Seq[JsObject]]

  trait Setup {
    val controller: FlatRateController = new FlatRateController {
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
      override val flatRateService: FlatRateService = mockFlatRateService
      override val turnoverEstimatesService: TurnoverEstimatesService = mockTurnoverEstimatesService
      override val configConnector: ConfigConnector = mockConfigConnector
      override val sicAndComplianceService: SicAndComplianceService = mockSicAndComplianceService
      val authConnector: AuthConnector = mockAuthClientConnector
      val messagesApi: MessagesApi = mockMessagesAPI
      val timeService: TimeService = mockTimeService
    }

    mockAllMessages
    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  s"GET ${routes.FlatRateController.annualCostsInclusivePage()}" should {

    "return a 200 when a previously completed S4LFlatRateScheme is returned" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      callAuthorised(controller.annualCostsInclusivePage()) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }

    "return a 200 when an empty S4LFlatRateScheme is returned from the service" in new Setup {

      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(FlatRateScheme()))

      callAuthorised(controller.annualCostsInclusivePage) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }
  }

  s"POST ${routes.FlatRateController.submitAnnualInclusiveCosts()}" should {

    val fakeRequest = FakeRequest(routes.FlatRateController.submitAnnualInclusiveCosts())

    "return 400 with Empty data" in new Setup {

      val emptyRequest: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submitAnnualInclusiveCosts(), emptyRequest){ result =>
        status(result) mustBe 400
      }
    }

    "return 303 with Annual Costs Inclusive selected Yes" in new Setup {


      when(mockFlatRateService.saveOverBusinessGoods(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "annualCostsInclusiveRadio" -> "true"
      )

      submitAuthorised(controller.submitAnnualInclusiveCosts(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(features.frs.controllers.routes.FlatRateController.estimateTotalSales().url)
      }
    }

    "redirect to 16.5% rate page if user selects No" in new Setup {

      when(mockFlatRateService.saveOverBusinessGoods(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "annualCostsInclusiveRadio" -> "false"
      )

      submitAuthorised(controller.submitAnnualInclusiveCosts(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(features.frs.controllers.routes.FlatRateController.registerForFrsPage().url)
      }
    }
  }

  val estimateVatTurnover = TurnoverEstimates(1000000L)

  s"GET ${routes.FlatRateController.annualCostsLimitedPage()}" should {

    "return a 200 and render Annual Costs Limited page when a S4LFlatRateScheme is not found on the vat scheme" in new Setup {


      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate.copy(overBusinessGoodsPercent = None, estimateTotalSales = Some(1234L))))

      callAuthorised(controller.annualCostsLimitedPage()) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }

    "return a 200 and render Annual Costs Limited page when a S4LFlatRateScheme is found on the vat scheme" in new Setup {

      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate.copy(estimateTotalSales = Some(1234L))))

      callAuthorised(controller.annualCostsLimitedPage()) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }
  }

  s"POST ${routes.FlatRateController.submitAnnualCostsLimited()}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitAnnualCostsLimited())

    "return a 400 when the request is empty" in new Setup {

      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate.copy(estimateTotalSales = Some(1234L))))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submitAnnualCostsLimited(), request){ result =>
        status(result) mustBe 400
      }
    }

    "redirect to confirm business sector when user selects Yes" in new Setup{

      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate.copy(estimateTotalSales = Some(1234L))))

      when(mockFlatRateService.saveOverBusinessGoodsPercent(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "annualCostsLimitedRadio" -> "true"
      )

      submitAuthorised(controller.submitAnnualCostsLimited(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(features.frs.controllers.routes.FlatRateController.confirmSectorFrsPage().url)
      }
    }

    "redirect to 16.5% rate page if user selects No" in new Setup {

      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate.copy(estimateTotalSales = Some(1234L))))

      when(mockFlatRateService.saveOverBusinessGoodsPercent(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      private val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "annualCostsLimitedRadio" -> "false"
      )

      submitAuthorised(controller.submitAnnualCostsLimited(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(features.frs.controllers.routes.FlatRateController.registerForFrsPage().url)
      }
    }
  }

  s"GET ${routes.FlatRateController.confirmSectorFrsPage()}" should {

    "return a 200 and render the page" in new Setup {
      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      callAuthorised(controller.confirmSectorFrsPage()){ result =>
        status(result) mustBe 200
      }
    }

    "redirect to choose business type page if there's no match of the business type against main business activity" in new Setup {
      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.failed(new MissingResourceException(s"Missing Business Type for id: testId", "ConfigConnector", "id")))

      callAuthorised(controller.confirmSectorFrsPage()){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(features.frs.controllers.routes.FlatRateController.businessType(true).url)
      }
    }
  }

  s"POST ${routes.FlatRateController.submitConfirmSectorFrs()}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitConfirmSectorFrs())

    "works with Empty data" in new Setup {

      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      when(mockFlatRateService.saveConfirmSector(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submitConfirmSectorFrs, request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/confirm-flat-rate")
      }
    }
  }

  s"GET ${routes.FlatRateController.frsStartDatePage()}" should {

    "return HTML when there's a frs start date in S4L" in new Setup {

      when(mockFlatRateService.getPrepopulatedStartDate(any(), any()))
        .thenReturn(Future.successful( (Some(FRSDateChoice.VATDate), None) ))

      callAuthorised(controller.frsStartDatePage) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in new Setup {

      when(mockFlatRateService.getPrepopulatedStartDate(any(), any()))
        .thenReturn(Future.successful( (None, None) ))

      callAuthorised(controller.frsStartDatePage) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }
  }

  val testsector = ("id", "test", BigDecimal(10))

  s"POST ${routes.FlatRateController.submitFrsStartDate()}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitFrsStartDate())

    "return 400 when no data posted" in new Setup {

      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submitFrsStartDate(), request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return 400 when partial data is posted" in new Setup {

      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "frsStartDateRadio" -> FRSDateChoice.DifferentDate,
        "frsStartDate.day" -> "1",
        "frsStartDate.month" -> "",
        "frsStartDate.year" -> "2017"
      )

      submitAuthorised(controller.submitFrsStartDate(), request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return 400 with Different Date selected and date that is less than 2 working days in the future" in new Setup {

      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "frsStartDateRadio" -> FRSDateChoice.DifferentDate,
        "frsStartDate.day" -> "20",
        "frsStartDate.month" -> "3",
        "frsStartDateDate.year" -> "2017"
      )

      submitAuthorised(controller.submitFrsStartDate(), request) { result =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 with VAT Registration Date selected" in new Setup {

      when(mockFlatRateService.saveStartDate(any(), any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "frsStartDateRadio" -> FRSDateChoice.VATDate
      )

      submitAuthorised(controller.submitFrsStartDate(), request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/check-your-answers")
      }
    }
  }

  s"GET ${routes.FlatRateController.joinFrsPage()}" should {

    "render the page" when {

      "visited for the first time" in new Setup {
        when(mockTurnoverEstimatesService.fetchTurnoverEstimates(any(), any(), any()))
          .thenReturn(Future.successful(Some(TurnoverEstimates(150000L))))

        when(mockFlatRateService.getFlatRate(any(), any(), any()))
          .thenReturn(Future.successful(validFlatRate))

        when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(currentProfile)))

        when(mockFlatRateService.saveJoiningFRS(any())(any(), any()))
          .thenReturn(Future.successful(FlatRateScheme()))

        callAuthorised(controller.joinFrsPage()) { result =>
          status(result) mustBe 200
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("mocked message")
        }
      }

      "user has already answered this question" in new Setup {

        when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(currentProfile)))

        when(mockTurnoverEstimatesService.fetchTurnoverEstimates(any(), any(), any()))
          .thenReturn(Future.successful(Some(TurnoverEstimates(150000L))))

        when(mockFlatRateService.getFlatRate(any(), any(), any()))
          .thenReturn(Future.successful(validFlatRate))

        callAuthorised(controller.joinFrsPage) { result =>
          status(result) mustBe 200
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("mocked message")
        }
      }
    }

    "redirect user to Summary if Turnover Estimates is more than £150K" in new Setup {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      when(mockTurnoverEstimatesService.fetchTurnoverEstimates(any(), any(), any()))
        .thenReturn(Future.successful(Some(TurnoverEstimates(150001L))))

      callAuthorised(controller.joinFrsPage) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.routes.SummaryController.show().url)
      }
    }

    "return an error if Turnover Estimates is empty" in new Setup {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      when(mockTurnoverEstimatesService.fetchTurnoverEstimates(any(), any(), any()))
        .thenReturn(Future.successful(None))

      callAuthorised(controller.joinFrsPage) { result =>
        status(result) mustBe 500
      }
    }
  }

  s"POST ${routes.FlatRateController.submitJoinFRS()}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitJoinFRS())

    "return 400 with Empty data" in new Setup {

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      submitAuthorised(controller.submitJoinFRS(), fakeRequest.withFormUrlEncodedBody(("","")))(result =>
        status(result) mustBe 400
      )
    }

    "return 303 with Join Flat Rate Scheme selected Yes" in new Setup {

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      when(mockFlatRateService.saveJoiningFRS(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "joinFrsRadio" -> "true"
      )
      submitAuthorised(controller.submitJoinFRS(), request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(features.frs.controllers.routes.FlatRateController.annualCostsInclusivePage().url)
      }
    }

    "return 303 with Join Flat Rate Scheme selected No" in new Setup {

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      when(mockFlatRateService.saveJoiningFRS(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "joinFrsRadio" -> "false"
      )

      submitAuthorised(controller.submitJoinFRS(), request){ result =>
        redirectLocation(result) mustBe Some(s"$contextRoot/check-your-answers")
      }
    }
  }

  s"GET ${routes.FlatRateController.registerForFrsPage()}" should {

    "return a 200 and render the page" in new Setup {

      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      callAuthorised(controller.registerForFrsPage()) { result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }
  }

  s"POST ${routes.FlatRateController.submitRegisterForFrs()}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitRegisterForFrs())

    "return 400 with Empty data" in new Setup {

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submitRegisterForFrs(), request) { result =>
        status(result) mustBe 400
      }
    }

    "return 303 with RegisterFor Flat Rate Scheme selected Yes" in new Setup {

      when(mockFlatRateService.saveRegister(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "registerForFrsRadio" -> "true"
      )

      submitAuthorised(controller.submitRegisterForFrs(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(features.frs.controllers.routes.FlatRateController.frsStartDatePage().url)
      }
    }

    "return 303 with RegisterFor Flat Rate Scheme selected No" in new Setup {

      when(mockFlatRateService.saveRegister(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "registerForFrsRadio" -> "false"
      )

      submitAuthorised(controller.submitRegisterForFrs(), request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/check-your-answers")
      }
    }
  }

  s"GET ${routes.FlatRateController.yourFlatRatePage()}" should {

    "return a 200 and render the page" in new Setup {

      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate))
      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      callAuthorised(controller.yourFlatRatePage()){ result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
      }
    }
  }

  s"POST ${routes.FlatRateController.submitYourFlatRate()}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitYourFlatRate())

    "return 400 with Empty data" in new Setup {
      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submitYourFlatRate(), request){ result =>
        status(result) mustBe 400
      }
    }

    "return 303 with RegisterFor Flat Rate Scheme selected Yes" in new Setup {

      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      when(mockFlatRateService.saveUseFlatRate(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "registerForFrsWithSectorRadio" -> "true"
      )

      submitAuthorised(controller.submitYourFlatRate(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(features.frs.controllers.routes.FlatRateController.frsStartDatePage().url)
      }
    }

    "return 303 with RegisterFor Flat Rate Scheme selected No" in new Setup {

      when(mockFlatRateService.retrieveSectorPercent(any(), any()))
        .thenReturn(Future.successful(testsector))

      when(mockFlatRateService.saveUseFlatRate(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "registerForFrsWithSectorRadio" -> "false"
      )

      submitAuthorised(controller.submitYourFlatRate(), request){ result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/check-your-answers")
      }
    }
  }

  s"GET ${routes.FlatRateController.estimateTotalSales()}" should {
    val validFlatRate = FlatRateScheme(
      Some(true),
      Some(true),
      None,
      None,
      None,
      None,
      None,
      None
    )

    "return a 200 and render the page without pre population" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      callAuthorised(controller.estimateTotalSales()){ result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
        val document = Jsoup.parse(contentAsString(result))
        document.getElementById("totalSalesEstimate").attr("value") mustBe ""
      }
    }

    "return a 200 and render the page with pre populated data" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate.copy(estimateTotalSales = Some(30000L))))

      callAuthorised(controller.estimateTotalSales()){ result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
        val document = Jsoup.parse(contentAsString(result))
        document.getElementById("totalSalesEstimate").attr("value") mustBe "30000"
      }
    }
  }

  s"POST ${routes.FlatRateController.submitEstimateTotalSales()}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitEstimateTotalSales())

    "return 400 with Empty data" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      submitAuthorised(controller.submitEstimateTotalSales(), request){ result =>
        status(result) mustBe 400
      }
    }

    "return 400 with value set to 0" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "totalSalesEstimate" -> "0"
      )

      submitAuthorised(controller.submitEstimateTotalSales(), request){ result =>
        status(result) mustBe 400
      }
    }

    "return 400 with value set to 100000000000" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "totalSalesEstimate" -> "100000000000"
      )

      submitAuthorised(controller.submitEstimateTotalSales(), request){ result =>
        status(result) mustBe 400
      }
    }

    "return 400 with decimal numbers" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "totalSalesEstimate" -> "30000.36"
      )

      submitAuthorised(controller.submitEstimateTotalSales(), request){ result =>
        status(result) mustBe 400
      }
    }

    "return 303" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "totalSalesEstimate" -> "30000"
      )

      val validFlatRate = FlatRateScheme(
        Some(true),
        Some(true),
        Some(30000L),
        None,
        None,
        None,
        None,
        None
      )

      when(mockFlatRateService.saveEstimateTotalSales(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      submitAuthorised(controller.submitEstimateTotalSales(), request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some("/register-for-vat/company-spend-business-goods")
      }
    }
  }

  s"GET ${routes.FlatRateController.businessType()}" should {
    val validFlatRate = FlatRateScheme(
      Some(true),
      Some(true),
      Some(30000L),
      None,
      None,
      None,
      Some("019"),
      None
    )

    "return a 200 and render the page" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate.copy(categoryOfBusiness = None)))

      when(mockConfigConnector.businessTypes).thenReturn(jsonBusinessTypes)
      when(mockSicAndComplianceService.getSicAndCompliance(any(),any()))
        .thenReturn(Future.successful(SicAndCompliance(
            mainBusinessActivity = Some(MainBusinessActivityView("12345678"))))
        )

      callAuthorised(controller.businessType()){ result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
        val document = Jsoup.parse(contentAsString(result))
        document.getElementsByAttributeValue("checked", "checked").size mustBe 0
      }
    }

    "return a 200 and render the page with radio pre selected" in new Setup {
      when(mockFlatRateService.getFlatRate(any(), any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      when(mockSicAndComplianceService.getSicAndCompliance(any(),any()))
        .thenReturn(Future.successful(SicAndCompliance(
          mainBusinessActivity = Some(MainBusinessActivityView("12345678"))))
        )
      when(mockConfigConnector.businessTypes).thenReturn(jsonBusinessTypes)

      callAuthorised(controller.businessType()){ result =>
        status(result) mustBe 200
        contentAsString(result) must include(MOCKED_MESSAGE)
        val document = Jsoup.parse(contentAsString(result))
        val id = s"businessType-${validFlatRate.categoryOfBusiness.get}"
        val elements = document.getElementsByAttributeValue("checked", "checked")
        elements.size mustBe 1
        elements.first.attr("id") mustBe id
        document.getElementsByAttributeValue("for", id).first.text mustBe "Test BusinessType"
      }
    }
  }

  s"POST ${routes.FlatRateController.submitBusinessType()}" should {
    val fakeRequest = FakeRequest(routes.FlatRateController.submitBusinessType())

    "return 400 with Empty data" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody()

      when(mockConfigConnector.businessTypes).thenReturn(jsonBusinessTypes)

      submitAuthorised(controller.submitBusinessType(), request){ result =>
        status(result) mustBe 400
      }
    }

    "return 400 with incorrect data" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "businessType" -> "000"
      )

      when(mockConfigConnector.businessTypes).thenReturn(jsonBusinessTypes)

      submitAuthorised(controller.submitBusinessType(), request){ result =>
        status(result) mustBe 400
      }
    }

    "return 303" in new Setup {
      val request: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest.withFormUrlEncodedBody(
        "businessType" -> "019"
      )

      when(mockConfigConnector.businessTypes).thenReturn(jsonBusinessTypes)

      val validFlatRate = FlatRateScheme(
        Some(true),
        Some(true),
        Some(30000L),
        None,
        None,
        None,
        Some("019"),
        None
      )

      when(mockFlatRateService.saveBusinessType(any())(any(), any()))
        .thenReturn(Future.successful(validFlatRate))

      submitAuthorised(controller.submitBusinessType(), request) { result =>
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(features.frs.controllers.routes.FlatRateController.yourFlatRatePage().url)
      }
    }
  }
}