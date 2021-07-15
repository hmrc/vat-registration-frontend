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
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, PartnershipIdService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class PartnershipIdController @Inject()(val authConnector: AuthConnector,
                                        val keystoreConnector: KeystoreConnector,
                                        partnershipIdService: PartnershipIdService,
                                        applicantDetailsService: ApplicantDetailsService
                                       )(implicit appConfig: FrontendAppConfig,
                                         val executionContext: ExecutionContext,
                                         baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def startPartnershipIdJourney(): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit req =>
      _ =>
        partnershipIdService.createJourney(appConfig.partnershipIdCallbackUrl, request2Messages(req)("service.name"), appConfig.contactFormServiceIdentifier, appConfig.feedbackUrl).map(
          journeyStartUrl => SeeOther(journeyStartUrl)
        )
  }

  def partnershipIdCallback(journeyId: String): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          partnershipDetails <- partnershipIdService.getDetails(journeyId)
          _ <- applicantDetailsService.saveApplicantDetails(partnershipDetails)
        } yield {
          Redirect(applicantRoutes.LeadPartnerEntityController.showLeadPartnerEntityType())
        }
  }

}
