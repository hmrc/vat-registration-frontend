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
import models.ApplicantDetails
import models.api.Individual
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, SessionProfile, SoleTraderIdentificationService, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SoleTraderIdentificationController @Inject()(val keystoreConnector: KeystoreConnector,
                                                   val authConnector: AuthConnector,
                                                   val applicantDetailsService: ApplicantDetailsService,
                                                   soleTraderIdentificationService: SoleTraderIdentificationService,
                                                   vatRegistrationService: VatRegistrationService)
                                                  (implicit val appConfig: FrontendAppConfig,
                                                   val executionContext: ExecutionContext,
                                                   baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  def startJourney(): Action[AnyContent] =
    isAuthenticatedWithProfile() {
      implicit request =>
        implicit profile =>
          vatRegistrationService.getVatScheme.flatMap { vatScheme =>
            soleTraderIdentificationService.startJourney(
              continueUrl = appConfig.getSoleTraderIdentificationCallbackUrl,
              serviceName = request2Messages(request)("service.name"),
              deskproId = appConfig.contactFormServiceIdentifier,
              signOutUrl = appConfig.feedbackUrl,
              enableSautrCheck = vatScheme.eligibilitySubmissionData.exists(_.partyType.equals(Individual))
            ) map (url => Redirect(url))
          }
    }

  def callback(journeyId: String): Action[AnyContent] =
    isAuthenticatedWithProfile() { implicit request =>
      implicit profile =>
        for {
          (transactorDetails, optSoleTrader) <- soleTraderIdentificationService.retrieveSoleTraderDetails(journeyId)
          _ <- applicantDetailsService.saveApplicantDetails(transactorDetails)
          _ <- optSoleTrader.fold(Future.successful(ApplicantDetails()))(applicantDetailsService.saveApplicantDetails(_))
        } yield Redirect(applicantRoutes.CaptureRoleInTheBusinessController.show())
    }

}
