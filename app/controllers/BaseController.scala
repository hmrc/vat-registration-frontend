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

package controllers

import config.{BaseControllerComponents, FrontendAppConfig, Logging}
import featureswitch.core.config.{FeatureSwitching, TrafficManagementPredicate}
import models.CurrentProfile
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SessionProfile
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.CompositePredicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

abstract class BaseController @Inject()(implicit ec: ExecutionContext,
                                        bcc: BaseControllerComponents,
                                        appConfig: FrontendAppConfig)
  extends FrontendController(bcc.messagesControllerComponents)
    with I18nSupport
    with Logging
    with AuthorisedFunctions
    with SessionProfile
    with FeatureSwitching {

  implicit class HandleResult(res: Future[Result])(implicit hc: HeaderCarrier) {
    def handleErrorResult: Future[Result] = {
      res recoverWith {
        case _: NoActiveSession =>
          Future.successful(Redirect(appConfig.loginUrl, Map(
            "continue" -> Seq(appConfig.continueUrl),
            "origin" -> Seq(appConfig.defaultOrigin)
          )))
        case ae: AuthorisationException =>
          logger.info(s"User is not authorised - reason: ${ae.reason}")
          Future.successful(InternalServerError)
        case e =>
          logger.warn(s"An exception occurred - err: ${e.getMessage}")
          throw e
      }
    }
  }

  private val authPredicate = CompositePredicate(AuthProviders(GovernmentGateway), ConfidenceLevel.L50)

  def isAuthenticated(f: Request[AnyContent] => Future[Result]): Action[AnyContent] = Action.async {
    implicit request =>
      authorised(authPredicate).retrieve(affinityGroup) {
        case Some(AffinityGroup.Individual) =>
          Future.successful(Redirect(routes.IndividualAffinityKickOutController.show))
        case Some(AffinityGroup.Organisation | AffinityGroup.Agent) =>
          f(request)
        case _ => throw new InternalServerException("User has no affinity group on their credential")
      } handleErrorResult
  }

  def isAuthenticatedWithProfile(checkTrafficManagement: Boolean = true)
                                (f: Request[AnyContent] => CurrentProfile => Future[Result]): Action[AnyContent] = Action.async {
    implicit request =>
      authorised(authPredicate) {
        withCurrentProfile() { profile =>
          if (isEnabled(TrafficManagementPredicate) && checkTrafficManagement) {
            bcc.trafficManagementService.passedTrafficManagement(profile.registrationId).flatMap {
              case true => f(request)(profile)
              case false =>
                logger.warn("[BaseController][isAuthenticatedWithProfile] User attempted to enter flow without passing TM")
                Future.successful(Redirect(routes.WelcomeController.show))
            }
          }
          else {
            f(request)(profile)
          }
        }
      } handleErrorResult
  }

  def isAuthenticatedWithProfileNoStatusCheck(f: Request[AnyContent] => CurrentProfile => Future[Result]): Action[AnyContent] = Action.async {
    implicit request =>
      authorised(authPredicate) {
        withCurrentProfile(checkStatus = false) { profile =>
          f(request)(profile)
        }
      } handleErrorResult
  }
}
