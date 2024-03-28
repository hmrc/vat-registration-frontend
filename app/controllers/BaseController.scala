/*
 * Copyright 2024 HM Revenue & Customs
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

import config.{BaseControllerComponents, FrontendAppConfig}
import models.CurrentProfile
import models.error.MissingAnswerException
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import services.SessionProfile
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.CompositePredicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoggingUtil

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

abstract class BaseController @Inject()(implicit ec: ExecutionContext,
                                        bcc: BaseControllerComponents,
                                        appConfig: FrontendAppConfig)
  extends FrontendController(bcc.messagesControllerComponents)
    with I18nSupport
    with LoggingUtil
    with AuthorisedFunctions
    with SessionProfile {

  import utils.EnrolmentUtil._

  override implicit def request2Messages(implicit request: RequestHeader): Messages = {
    messagesApi.preferred(request)
  }

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
        case e: MissingAnswerException =>
          logger.warn(s"Answer missing: ${e.sectionName} for user with session ID: ${hc.sessionId}")
          sessionService.cache("missingAnswer", e.sectionName).map { _ =>
            Redirect(controllers.errors.routes.ErrorController.missingAnswer)
          }
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
          Future.successful(Redirect(controllers.errors.routes.IndividualAffinityKickOutController.show))
        case Some(AffinityGroup.Organisation | AffinityGroup.Agent) =>
          f(request)
        case _ => throw new InternalServerException("User has no affinity group on their credential")
      }.handleErrorResult
  }

  def isAuthenticatedWithProfile(f: Request[AnyContent] => CurrentProfile => Future[Result]): Action[AnyContent] = Action.async {
    implicit request =>
      authorised(authPredicate).retrieve(allEnrolments) { enrolments =>
        withCurrentProfile() { profile =>
          f(request)(profile.copy(agentReferenceNumber = enrolments.agentReferenceNumber))
        }
      }.handleErrorResult
  }

  def isAuthenticatedWithProfileNoStatusCheck(f: Request[AnyContent] => CurrentProfile => Future[Result]): Action[AnyContent] = Action.async {
    implicit request =>
      authorised(authPredicate).retrieve(allEnrolments) { enrolments =>
        withCurrentProfile(checkStatus = false) { profile =>
          f(request)(profile.copy(agentReferenceNumber = enrolments.agentReferenceNumber))
        }
      }.handleErrorResult
  }

  def isAuthenticatedAndSubmitted(f: Request[AnyContent] => CurrentProfile => Future[Result]): Action[AnyContent] = Action.async {
    implicit request =>
      authorised(authPredicate) {
        withSubmittedCurrentProfile { profile =>
          f(request)(profile)
        }
      }.handleErrorResult
  }
}
