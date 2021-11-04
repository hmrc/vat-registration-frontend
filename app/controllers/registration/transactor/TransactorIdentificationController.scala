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

package controllers.registration.transactor

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import controllers.registration.applicant.{routes => applicantRoutes}
import models.external.soletraderid.SoleTraderIdJourneyConfig
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TransactorIdentificationController @Inject()(val keystoreConnector: KeystoreConnector,
                                                   val authConnector: AuthConnector,
                                                   val transactorDetailsService: TransactorDetailsService,
                                                   soleTraderIdentificationService: SoleTraderIdentificationService
                                                  )(implicit val appConfig: FrontendAppConfig,
                                                    val executionContext: ExecutionContext,
                                                    baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def startJourney(): Action[AnyContent] =
    isAuthenticatedWithProfile() {
      implicit request =>
        implicit profile =>
          soleTraderIdentificationService.startIndividualJourney(
            SoleTraderIdJourneyConfig(
              continueUrl = appConfig.individualCallbackUrl,
              optServiceName = Some(request2Messages(request)("service.name")),
              deskProServiceId = appConfig.contactFormServiceIdentifier,
              signOutUrl = appConfig.feedbackUrl,
              accessibilityUrl = appConfig.accessibilityStatementUrl
            )
          ).map(url => Redirect(url))
    }

  def callback(journeyId: String): Action[AnyContent] =
    isAuthenticatedWithProfile() { implicit request =>
      implicit profile =>
        for {
          personalDetails <- soleTraderIdentificationService.retrieveIndividualDetails(journeyId)
          _ <- transactorDetailsService.saveTransactorDetails(personalDetails)
        } yield {
          NotImplemented
        }
    }
}
