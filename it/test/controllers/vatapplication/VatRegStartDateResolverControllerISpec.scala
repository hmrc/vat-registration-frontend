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

package controllers.vatapplication

import itutil.ControllerISpec
import models.api.{Partnership, Trust}
import models.{GroupRegistration, Voluntary}
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class VatRegStartDateResolverControllerISpec extends ControllerISpec {

  val url = "/resolve-vat-start-date"

  "GET /resolve-vat-start-date" when {
    "the user is voluntary" when {
      "GRS returns a date of incorporation for the business" must {
        "redirect to the voluntary start date (with date choice) page" in new Setup {
          given
            .user.isAuthorised()
            .registrationApi.getRegistration(emptyUkCompanyVatScheme.copy(
            eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(registrationReason = Voluntary)),
            applicantDetails = Some(validFullApplicantDetails)
          ))

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res = await(buildClient(url).get)

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.VoluntaryStartDateController.show.url)
        }
      }
      "GRS doesn't return a date of incorporation for a UK company" must {
        "redirect to the voluntary start date (no date choice) page" in new Setup {
          given
            .user.isAuthorised()
            .registrationApi.getRegistration(emptyUkCompanyVatScheme.copy(
            eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(registrationReason = Voluntary)),
            applicantDetails = Some(validFullApplicantDetails.copy(entity = Some(testApplicantIncorpDetails.copy(dateOfIncorporation = None))))
          ))

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res = await(buildClient(url).get)

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.VoluntaryStartDateNoChoiceController.show.url)
        }
      }
      "GRS doesn't return a date of incorporation for a Partnership" must {
        "redirect to the voluntary start date (no date choice) page" in new Setup {
          given
            .user.isAuthorised()
            .registrationApi.getRegistration(emptyUkCompanyVatScheme.copy(
            eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Partnership, registrationReason = Voluntary)),
            applicantDetails = Some(validFullApplicantDetails.copy(entity = Some(testPartnership.copy(dateOfIncorporation = None))))
          ))

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res = await(buildClient(url).get)

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.VoluntaryStartDateNoChoiceController.show.url)
        }
      }
      "date of incorporation is not relevant for the user's party type" must {
        "redirect to the voluntary start date (no date choice) page" in new Setup {
          given
            .user.isAuthorised()
            .registrationApi.getRegistration(emptyUkCompanyVatScheme.copy(
            eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Trust, registrationReason = Voluntary)),
            applicantDetails = Some(validFullApplicantDetails.copy(entity = Some(testMinorEntity)))
          ))

          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res = await(buildClient(url).get)

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.VoluntaryStartDateNoChoiceController.show.url)
        }
      }
    }
    "the user is mandatory" must {
      "redirect to the mandatory start date page" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getRegistration(emptyUkCompanyVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData),
          applicantDetails = Some(validFullApplicantDetails)
        ))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.MandatoryStartDateController.show.url)
      }
    }
    "the user is a VAT group" must {
      "redirect to the voluntary start date page (no date choice)" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getRegistration(emptyUkCompanyVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(registrationReason = GroupRegistration)),
          applicantDetails = Some(validFullApplicantDetails)
        ))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.VoluntaryStartDateNoChoiceController.show.url)
      }
    }
    "the registration reason is missing" must {
      "redirect to the missing answer page exception" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getRegistration(emptyUkCompanyVatScheme.copy(
            eligibilitySubmissionData = None
          ))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
      }
    }
  }

}
