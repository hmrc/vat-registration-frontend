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

package controllers.registration.applicant

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import controllers.registration.applicant.{routes => applicantRoutes}
import forms.FormerNameDateForm
import models.api.{NETP, NonUkNonEstablished}
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, SessionProfile, SessionService, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.former_name_date

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FormerNameDateController @Inject()(val authConnector: AuthConnector,
                                         val sessionService: SessionService,
                                         val applicantDetailsService: ApplicantDetailsService,
                                         vatRegistrationService: VatRegistrationService,
                                         formerNameDatePage: former_name_date
                                        )(implicit appConfig: FrontendAppConfig,
                                          val executionContext: ExecutionContext,
                                          baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          applicant <- applicantDetailsService.getApplicantDetails
          dob = applicant.personalDetails.map(_.dateOfBirth).getOrElse(throw new IllegalStateException("Missing date of birth"))
          formerName = applicant.formerName.flatMap(_.formerName).getOrElse(throw new IllegalStateException("Missing applicant former name"))
          filledForm = applicant.formerNameDate.fold(FormerNameDateForm.form(dob))(FormerNameDateForm.form(dob).fill)
          name <- applicantDetailsService.getTransactorApplicantName
        } yield Ok(formerNameDatePage(filledForm, formerName, name))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        applicantDetailsService.getApplicantDetails flatMap {
          applicantDetails =>
            val dob = applicantDetails.personalDetails.map(_.dateOfBirth).getOrElse(throw new IllegalStateException("Missing date of birth"))
            FormerNameDateForm.form(dob).bindFromRequest().fold(
              badForm => for {
                applicant <- applicantDetailsService.getApplicantDetails
                formerName = applicant.formerName.flatMap(_.formerName).getOrElse(throw new IllegalStateException("Missing applicant former name"))
                name <- applicantDetailsService.getTransactorApplicantName
              } yield BadRequest(formerNameDatePage(badForm, formerName, name)),
              data => {
                applicantDetailsService.saveApplicantDetails(data) flatMap { _ =>
                  vatRegistrationService.partyType map {
                    case NETP | NonUkNonEstablished =>
                      Redirect(applicantRoutes.InternationalHomeAddressController.show)
                    case _ =>
                      Redirect(applicantRoutes.HomeAddressController.redirectToAlf)
                  }
                }
              }
            )
        }
  }

}
