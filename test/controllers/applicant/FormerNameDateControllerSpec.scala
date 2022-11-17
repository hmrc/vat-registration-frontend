/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.applicant

import controllers.applicant.{routes => applicantRoutes}
import featureswitch.core.config.FeatureSwitching
import fixtures.ApplicantDetailsFixtures
import models.api.UkCompany
import models.external.Name
import models.view.FormerNameDateView
import play.api.test.{DefaultAwaitTimeout, FakeRequest}
import services.mocks.{MockApplicantDetailsService, MockVatRegistrationService}
import testHelpers.ControllerSpec
import views.html.applicant.former_name_date

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FormerNameDateControllerSpec extends ControllerSpec
  with DefaultAwaitTimeout
  with MockApplicantDetailsService
  with ApplicantDetailsFixtures
  with MockVatRegistrationService
  with FeatureSwitching {

  trait Setup {
    val controller: FormerNameDateController = new FormerNameDateController(
      mockAuthClientConnector,
      mockSessionService,
      mockApplicantDetailsService,
      app.injector.instanceOf[former_name_date]
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  val fakeRequest = FakeRequest(applicantRoutes.FormerNameDateController.show)

  val incompleteApplicantDetails = emptyApplicantDetails
    .copy(hasFormerName = Some(false), formerName = Some(Name(Some("Old"), last = "Name")),personalDetails = Some(testPersonalDetails))

  val incompleteApplicantDetailsDate = incompleteApplicantDetails
    .copy(formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 6, 23))),
      personalDetails = Some(testPersonalDetails))

  val onlyTranscatorDetails = emptyApplicantDetails.copy(personalDetails = Some(testPersonalDetails))

  "show" should {
    "return OK when there's data" in new Setup {
      mockGetApplicantDetails(currentProfile)(incompleteApplicantDetailsDate)
      mockGetTransactorApplicantName(currentProfile)(Some(testFirstName))

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }
    "return OK when there's no data" in new Setup {
      mockGetApplicantDetails(currentProfile)(incompleteApplicantDetails)
      mockGetTransactorApplicantName(currentProfile)(Some(testFirstName))

      val res = controller.show(fakeRequest)

      status(res) mustBe OK
    }

    "return OK when there's data and the user is a transactor" in new Setup {
      mockGetApplicantDetails(currentProfile)(incompleteApplicantDetailsDate)
      mockIsTransactor(Future.successful(true))
      mockGetTransactorApplicantName(currentProfile)(Some(testFirstName))

      callAuthorised(controller.show) {
        status(_) mustBe OK
      }
    }

    "return OK when there's no data and the user is a transactor" in new Setup {
      mockGetApplicantDetails(currentProfile)(incompleteApplicantDetails)
      mockIsTransactor(Future.successful(true))
      mockGetTransactorApplicantName(currentProfile)(Some(testFirstName))

      val res = controller.show(fakeRequest)

      status(res) mustBe OK
    }

    "throw an IllegalStateException when dob is missing" in new Setup {
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails)

      val ex: IllegalStateException = intercept[IllegalStateException] {
        await(controller.show(fakeRequest))
      }
      ex.getMessage mustBe "Missing date of birth"
    }

    "throw an IllegalStateException when the former name is missing" in new Setup {
      mockGetApplicantDetails(currentProfile)(emptyApplicantDetails.copy(personalDetails = Some(testPersonalDetails)))

      val ex: IllegalStateException = intercept[IllegalStateException] {
        await(controller.show(fakeRequest))
      }
      ex.getMessage mustBe "Missing applicant former name"
    }
  }

  "submit" when {
    "redirect to the task list" in new Setup {
      mockGetApplicantDetails(currentProfile)(onlyTranscatorDetails)
      mockPartyType(Future.successful(UkCompany))
      mockSaveApplicantDetails(FormerNameDateView(LocalDate.parse("2020-02-01")))(onlyTranscatorDetails)

      submitAuthorised(controller.submit, fakeRequest.withMethod("POST").withFormUrlEncodedBody(
        "formerNameDate.day" -> "1",
        "formerNameDate.month" -> "2",
        "formerNameDate.year" -> "2020"
      )) { res =>
        status(res) mustBe SEE_OTHER
        redirectLocation(res) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }

  }
}
