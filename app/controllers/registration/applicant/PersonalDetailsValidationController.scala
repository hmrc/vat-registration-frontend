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

package controllers.registration.applicant

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import controllers.registration.applicant.{routes => applicantRoutes}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, PersonalDetailsValidationService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PersonalDetailsValidationController @Inject()(val authConnector: AuthConnector,
                                                    val keystoreConnector: KeystoreConnector,
                                                    personalDetailsValidationService: PersonalDetailsValidationService,
                                                    applicantDetailsService: ApplicantDetailsService
                                                   )(implicit appConfig: FrontendAppConfig,
                                                     val executionContext: ExecutionContext,
                                                     baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def startPersonalDetailsValidationJourney(): Action[AnyContent] = isAuthenticatedWithProfile() {
    _ => _ =>
        val continueUrl = appConfig.getPersonalDetailsCallbackUrl()
        val personalDetailsValidationJourneyUrl = appConfig.getPersonalDetailsValidationJourneyUrl()

        Future.successful(SeeOther(s"$personalDetailsValidationJourneyUrl?completionUrl=$continueUrl"))
  }

  def personalDetailsValidationCallback(validationId: String): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit req =>
      implicit profile =>
        for {
          details <- personalDetailsValidationService.retrieveValidationResult(validationId)
          _ <- applicantDetailsService.saveApplicantDetails(details)
        } yield Redirect(applicantRoutes.CaptureRoleInTheBusinessController.show()).removingFromSession("ValidationId")
  }

}
