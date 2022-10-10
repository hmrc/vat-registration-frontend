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
import controllers.partners.PartnerIndexValidation
import featureswitch.core.config.TaskList
import models.Entity
import models.Entity.leadEntityIndex
import models.external.BusinessEntity
import models.external.incorporatedentityid.{IncorpIdJourneyConfig, JourneyLabels}
import play.api.i18n.Lang
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class PartnerIncorpIdController @Inject()(val authConnector: AuthConnector,
                                          val sessionService: SessionService,
                                          incorpIdService: IncorpIdService,
                                          val entityService: EntityService
                                         )(implicit appConfig: FrontendAppConfig,
                                           val executionContext: ExecutionContext,
                                           baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with PartnerIndexValidation {

  def startJourney(index: Int): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        validateIndex(index, routes.PartnerIncorpIdController.startJourney, minIndex = 1) {
          case Some(Entity(_, partyType, _, _)) =>
            val journeyConfig = IncorpIdJourneyConfig(
              continueUrl = appConfig.incorpIdPartnerCallbackUrl(index),
              optServiceName = messagesApi.translate("service.name", Nil)(Lang("cy")),
              deskProServiceId = appConfig.contactFormServiceIdentifier,
              signOutUrl = appConfig.feedbackUrl,
              accessibilityUrl = appConfig.accessibilityStatementUrl,
              regime = appConfig.regime,
              businessVerificationCheck = false,
              labels = Some(JourneyLabels(messagesApi.translate("service.name", Nil)(Lang("cy"))))
            )

            incorpIdService.createJourney(journeyConfig, partyType).map { journeyStartUrl =>
              SeeOther(journeyStartUrl)
            }
          case _ =>
            throw new InternalServerException(s"[PartnerIncorpIdController] Missing entity with partyType for entity: $index")
        }
  }

  def callback(index: Int, journeyId: String): Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        validateIndexSubmit(index, routes.PartnerIncorpIdController.startJourney, minIndex = 1) {
          for {
            incorpDetails <- incorpIdService.getDetails(journeyId)
            _ <- entityService.upsertEntity[BusinessEntity](profile.registrationId, index, incorpDetails)
          } yield {
            if (index == leadEntityIndex) {
              if (isEnabled(TaskList)) {
                Redirect(controllers.routes.TaskListController.show)
              } else {
                Redirect(routes.IndividualIdController.startJourney)
              }
            } else {
              NotImplemented
            }
          }
        }
  }
}
