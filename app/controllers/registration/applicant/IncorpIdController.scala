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
import featureswitch.core.config.UseSoleTraderIdentification

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, IncorpIdService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.ExecutionContext

@Singleton
class IncorpIdController @Inject()(val authConnector: AuthConnector,
                                   val keystoreConnector: KeystoreConnector,
                                   incorpIdService: IncorpIdService,
                                   applicantDetailsService: ApplicantDetailsService
                                  )(implicit appConfig: FrontendAppConfig,
                                    val executionContext: ExecutionContext,
                                    baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def startIncorpIdJourney(): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit req =>
      _ =>
        incorpIdService.createJourney(appConfig.incorpIdCallbackUrl, request2Messages(req)("service.name"), appConfig.contactFormServiceIdentifier, appConfig.feedbackUrl).map(
          journeyStartUrl => SeeOther(journeyStartUrl)
        )
  }

  def incorpIdCallback(journeyId: String): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          incorpDetails <- incorpIdService.getDetails(journeyId)
          _ <- applicantDetailsService.saveApplicantDetails(incorpDetails)
        } yield {
          if (isEnabled(UseSoleTraderIdentification)) {
            Redirect(applicantRoutes.SoleTraderIdentificationController.startJourney())
          }
          else {
            Redirect(applicantRoutes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney())
          }
        }
  }

}
