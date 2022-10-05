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
import models.external.BusinessEntity
import models.external.incorporatedentityid.{IncorpIdJourneyConfig, JourneyLabels}
import play.api.i18n.Lang
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class PartnerIncorpIdController @Inject()(val authConnector: AuthConnector,
                                          val sessionService: SessionService,
                                          incorpIdService: IncorpIdService,
                                          entityService: EntityService
                                         )(implicit appConfig: FrontendAppConfig,
                                           val executionContext: ExecutionContext,
                                           baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def startPartnerJourney: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        val journeyConfig = IncorpIdJourneyConfig(
          continueUrl = appConfig.incorpIdPartnerCallbackUrl,
          optServiceName = messagesApi.translate("service.name", Nil)(Lang("cy")),
          deskProServiceId = appConfig.contactFormServiceIdentifier,
          signOutUrl = appConfig.feedbackUrl,
          accessibilityUrl = appConfig.accessibilityStatementUrl,
          regime = appConfig.regime,
          businessVerificationCheck = false,
          labels = Some(JourneyLabels(messagesApi.translate("service.name", Nil)(Lang("cy"))))
        )

        for {
          entity <- entityService.getEntity(profile.registrationId, 1)
          journeyStartUrl <- incorpIdService.createJourney(journeyConfig, entity.partyType)
        } yield {
          SeeOther(journeyStartUrl)
        }
  }

  def partnerCallback(journeyId: String): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          incorpDetails <- incorpIdService.getDetails(journeyId)
          _ <- entityService.upsertEntity[BusinessEntity](profile.registrationId, 1, incorpDetails)
        } yield {
          if (isEnabled(TaskList)) {
            Redirect(controllers.routes.TaskListController.show)
          } else {
            Redirect(routes.IndividualIdController.startJourney)
          }
        }
  }
}
