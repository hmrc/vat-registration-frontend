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

package controllers.grs

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import featureswitch.core.config.TaskList
import models.api.{NETP, NonUkNonEstablished}
import models.external.soletraderid.{JourneyLabels, SoleTraderIdJourneyConfig, TranslationLabels}
import play.api.i18n.Lang
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TransactorIdController @Inject()(val sessionService: SessionService,
                                       val authConnector: AuthConnector,
                                       val transactorDetailsService: TransactorDetailsService,
                                       vatRegistrationService: VatRegistrationService,
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
              continueUrl = appConfig.transactorCallbackUrl,
              deskProServiceId = appConfig.contactFormServiceIdentifier,
              signOutUrl = appConfig.feedbackUrl,
              accessibilityUrl = appConfig.accessibilityStatementUrl,
              regime = appConfig.regime,
              businessVerificationCheck = true,
              labels = Some(JourneyLabels(
                en = TranslationLabels(
                  optServiceName = messagesApi.translate("service.name", Nil)(Lang("en"))
                ),
                cy = TranslationLabels(
                  optServiceName = messagesApi.translate("service.name", Nil)(Lang("cy"))
                )
              ))
            )
          ).map(url => Redirect(url))
    }

  def callback(journeyId: String): Action[AnyContent] =
    isAuthenticatedWithProfile() { implicit request =>
      implicit profile =>
        for {
          personalDetails <- soleTraderIdentificationService.retrieveIndividualDetails(journeyId)
          _ <- transactorDetailsService.saveTransactorDetails(personalDetails)
          partyType <- vatRegistrationService.partyType
        } yield {
          if (isEnabled(TaskList)) {
            Redirect(controllers.routes.TaskListController.show)
          } else {
            partyType match {
              case NETP | NonUkNonEstablished =>
                Redirect(controllers.transactor.routes.TransactorInternationalAddressController.show)
              case _ =>
                Redirect(controllers.transactor.routes.TransactorHomeAddressController.redirectToAlf)
            }
          }
        }
    }
}