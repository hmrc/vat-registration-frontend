/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.registration.flatratescheme

import fixtures.FlatRateFixtures
import models.FRSDateChoice
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import testHelpers.ControllerSpec
import views.html.frs_start_date

import java.time.LocalDate
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class StartDateControllerSpec extends ControllerSpec with FlatRateFixtures {

  trait Setup {
    val view = app.injector.instanceOf[frs_start_date]
    val controller: StartDateController = new StartDateController(
      mockAuthClientConnector,
      mockKeystoreConnector,
      mockFlatRateService,
      mockReturnsService,
      mockTimeService,
      view,
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }
  val controller = app.injector.instanceOf[StartDateController]

  s"show" should {
    "return OK" when {
      "there's a frs start date in S4L" in new Setup {
        when(mockTimeService.today).thenReturn(LocalDate.of(2017, 3, 21))
        when(mockFlatRateService.getPrepopulatedStartDate(any())(any(), any()))
          .thenReturn(Future.successful((Some(FRSDateChoice.VATDate), None)))

        when(mockReturnsService.retrieveCalculatedStartDate(any(), any()))
          .thenReturn(Future.successful(LocalDate.of(2017, 3, 18).plusMonths(2)))

        when(mockFlatRateService.fetchVatStartDate(any(), any()))
          .thenReturn(Future.successful(None))

        callAuthorised(controller.show) { result =>
          status(result) mustBe OK
        }
      }
      "there's nothing in S4L and vatScheme contains data" in new Setup {
        when(mockTimeService.today).thenReturn(LocalDate.of(2017, 3, 21))
        when(mockFlatRateService.getPrepopulatedStartDate(any())(any(), any()))
          .thenReturn(Future.successful((None, None)))

        when(mockReturnsService.retrieveCalculatedStartDate(any(), any()))
          .thenReturn(Future.successful(LocalDate.of(2017,3,18).plusMonths(2)))

        when(mockFlatRateService.fetchVatStartDate(any(), any()))
          .thenReturn(Future.successful(None))

        callAuthorised(controller.show) { result =>
          status(result) mustBe OK
        }
      }
    }
  }

  s"submit" should {
    "return BAD_REQUEST" when {
      "no data posted" in new Setup {
        when(mockFlatRateService.fetchVatStartDate(any(), any())).thenReturn(Future.successful(None))
        when(mockTimeService.today).thenReturn(LocalDate.of(2017, 3, 21))
        when(mockReturnsService.retrieveCalculatedStartDate(any(), any())).thenReturn(Future.successful(LocalDate.now))
        when(mockFlatRateService.retrieveSectorPercent(any(), any())).thenReturn(Future.successful(testsector))

        val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody()

        submitAuthorised(controller.submit(), request) { result =>
          status(result) mustBe BAD_REQUEST
        }
      }
      "when partial data is posted" in new Setup {
        when(mockFlatRateService.fetchVatStartDate(any(), any())).thenReturn(Future.successful(Some(LocalDate.of(2017, 3, 1))))
        when(mockTimeService.today).thenReturn(LocalDate.of(2017, 3, 21))
        when(mockReturnsService.retrieveCalculatedStartDate(any(), any())).thenReturn(Future.successful(LocalDate.now))
        when(mockFlatRateService.retrieveSectorPercent(any(), any())).thenReturn(Future.successful(testsector))

        val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(
          "frsStartDate" -> FRSDateChoice.DifferentDate,
          "frsStartDate.day" -> "1",
          "frsStartDate.month" -> "",
          "frsStartDate.year" -> "2017"
        )

        submitAuthorised(controller.submit(), request) { result =>
          status(result) mustBe BAD_REQUEST
        }
      }
      "'different date' is selected and the date is before the earliest allowed date" in new Setup {
        when(mockFlatRateService.fetchVatStartDate(any(), any())).thenReturn(Future.successful(None))
        when(mockTimeService.today).thenReturn(LocalDate.of(2017, 3, 21))
        when(mockReturnsService.retrieveCalculatedStartDate(any(), any())).thenReturn(Future.successful(LocalDate.of(2017,3,18)))
        when(mockFlatRateService.retrieveSectorPercent(any(), any())).thenReturn(Future.successful(testsector))

        val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(
          "frsStartDate" -> FRSDateChoice.DifferentDate,
          "frsStartDate.day" -> "20",
          "frsStartDate.month" -> "3",
          "frsStartDateDate.year" -> "2017"
        )

        submitAuthorised(controller.submit(), request) { result =>
          status(result) mustBe BAD_REQUEST
        }
      }
      "'different date' is selected and the date is after the latest allowed date" in new Setup {
        when(mockFlatRateService.fetchVatStartDate(any(), any())).thenReturn(Future.successful(None))
        when(mockTimeService.today).thenReturn(LocalDate.of(2017, 3, 21))
        when(mockReturnsService.retrieveCalculatedStartDate(any(), any())).thenReturn(Future.successful(LocalDate.of(2017,3,18)))
        when(mockFlatRateService.retrieveSectorPercent(any(), any())).thenReturn(Future.successful(testsector))

        val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(
          "frsStartDate" -> FRSDateChoice.DifferentDate,
          "frsStartDate.day" -> "22",
          "frsStartDate.month" -> "6",
          "frsStartDateDate.year" -> "2017"
        )

        submitAuthorised(controller.submit(), request) { result =>
          status(result) mustBe BAD_REQUEST
        }
      }
    }
    "Redirect to the next page" when {
      "VAT Registration Date selected" in new Setup {
        when(mockTimeService.today).thenReturn(LocalDate.of(2017, 3, 21))
        when(mockFlatRateService.fetchVatStartDate(any(), any()))
          .thenReturn(Future.successful(None))

        when(mockReturnsService.retrieveCalculatedStartDate(any(), any()))
          .thenReturn(Future.successful(LocalDate.of(2017, 3, 18).plusMonths(2)))

        when(mockFlatRateService.saveStartDate(any(), any())(any(), any()))
          .thenReturn(Future.successful(validFlatRate))

        val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(
          "frsStartDateRadio" -> FRSDateChoice.VATDate
        )

        submitAuthorised(controller.submit(), request) { result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some("/register-for-vat/attachments-resolve")
        }
      }
      "a valid different date is posted" in new Setup {
        val minDate = LocalDate.of(2017, 3, 1)
        val maxDate = minDate.plusMonths(2)
        when(mockTimeService.today).thenReturn(LocalDate.of(2017, 3, 21))

        when(mockFlatRateService.fetchVatStartDate(any(), any()))
          .thenReturn(Future.successful(Some(minDate)))

        when(mockReturnsService.retrieveCalculatedStartDate(any(), any()))
          .thenReturn(Future.successful(maxDate))

        when(mockFlatRateService.retrieveSectorPercent(any(), any()))
          .thenReturn(Future.successful(testsector))

        val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(
          "frsStartDate" -> FRSDateChoice.DifferentDate,
          "frsStartDate.day" -> "1",
          "frsStartDate.month" -> "3",
          "frsStartDate.year" -> "2017"
        )

        submitAuthorised(controller.submit(), request) { result =>
          status(result) mustBe BAD_REQUEST
        }
      }
    }
  }

}
