/*
 * Copyright 2023 HM Revenue & Customs
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

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.FormerNameDateForm
import models.error.MissingAnswerException
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.applicant.FormerNameDate

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FormerNameDateController @Inject()(val authConnector: AuthConnector,
                                         val sessionService: SessionService,
                                         val applicantDetailsService: ApplicantDetailsService,
                                         formerNameDatePage: FormerNameDate
                                        )(implicit appConfig: FrontendAppConfig,
                                          val executionContext: ExecutionContext,
                                          baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          applicant <- applicantDetailsService.getApplicantDetails
          optName <- applicantDetailsService.getApplicantNameForTransactorFlow
          isTransactor = optName.isDefined
          dob = applicant.personalDetails.flatMap(_.dateOfBirth).getOrElse(
            throw MissingAnswerException(missingDataSection(isTransactor)
          ))
          formerName = applicant.changeOfName.name.getOrElse(
            throw MissingAnswerException(missingDataSection(isTransactor))
          )
          filledForm = applicant.changeOfName.change.fold(FormerNameDateForm.form(dob))(FormerNameDateForm.form(dob).fill)
        } yield Ok(formerNameDatePage(filledForm, formerName.asLabel, optName))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        applicantDetailsService.getApplicantDetails flatMap {
          applicantDetails =>
            applicantDetailsService.getApplicantNameForTransactorFlow.flatMap { optName =>
              val isTransactor = optName.isDefined

              val dob = applicantDetails.personalDetails.flatMap(_.dateOfBirth).getOrElse(
                throw MissingAnswerException(missingDataSection(isTransactor))
              )

              FormerNameDateForm.form(dob).bindFromRequest().fold(
                badForm => {
                  val formerName = applicantDetails.changeOfName.name.getOrElse(
                    throw MissingAnswerException(missingDataSection(isTransactor))
                  )
                  Future.successful(BadRequest(formerNameDatePage(badForm, formerName.asLabel, optName)))
                },
                data => {
                  applicantDetailsService.saveApplicantDetails(data) map { _ =>
                    Redirect(controllers.routes.TaskListController.show)
                  }
                }
              )
            }
        }
  }

  private def missingDataSection(isTransactor: Boolean): String =
    if (isTransactor) {
      "tasklist.aboutBusinessContact.personalDetails"
    } else {
      "tasklist.aboutYou.personalDetails"
    }

}
