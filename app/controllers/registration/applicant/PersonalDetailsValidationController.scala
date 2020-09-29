/*
 * Copyright 2020 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.KeystoreConnector
import controllers.BaseController
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ApplicantDetailsService, PersonalDetailsValidationService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector
import controllers.registration.applicant.{routes => applicantRoutes}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PersonalDetailsValidationController @Inject()(mcc: MessagesControllerComponents,
                                                    val authConnector: AuthConnector,
                                                    val keystoreConnector: KeystoreConnector,
                                                    personalDetailsValidationService: PersonalDetailsValidationService,
                                                    applicantDetailsService: ApplicantDetailsService
                                                   )(implicit val appConfig: FrontendAppConfig, ec: ExecutionContext) extends BaseController(mcc) with SessionProfile {
  def startPersonalDetailsValidationJourney(): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit req =>
      _ =>
        val continueUrl = appConfig.getPersonalDetailsCallbackUrl()
        val personalDetailsValidationJourneyUrl = appConfig.getPersonalDetailsValidationJourneyUrl()

        Future.successful(SeeOther(s"$personalDetailsValidationJourneyUrl?completionUrl=$continueUrl"))
  }

  def personalDetailsValidationCallback(validationId: String): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit req => implicit profile =>
      for {
        details <- personalDetailsValidationService.retrieveValidationResult(validationId)
        _ <- applicantDetailsService.saveApplicantDetails(details)
      } yield Redirect(applicantRoutes.FormerNameController.show())
  }

}
