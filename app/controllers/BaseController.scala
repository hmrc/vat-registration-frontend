/*
 * Copyright 2018 HM Revenue & Customs
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

import auth.VatExternalUrls
import config.Logging
import models.CurrentProfile
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.SessionProfile
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.authorise.CompositePredicate
import uk.gov.hmrc.auth.core.{AuthProviders, AuthorisationException, AuthorisedFunctions, ConfidenceLevel, NoActiveSession}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

trait BaseController extends FrontendController with I18nSupport with Logging with AuthorisedFunctions {
  self: SessionProfile =>

  implicit val messagesApi: MessagesApi

  implicit class HandleResult(res: Future[Result])(implicit hc: HeaderCarrier) {
    def handleErrorResult: Future[Result] = {
      res recoverWith {
        case _: NoActiveSession =>
          Future.successful(Redirect(VatExternalUrls.loginUrl, Map(
            "continue" -> Seq(VatExternalUrls.continueUrl),
            "origin" -> Seq(VatExternalUrls.defaultOrigin)
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
      authorised(authPredicate)(f(request)) handleErrorResult
  }

  def isAuthenticatedWithProfile(f: Request[AnyContent] => CurrentProfile => Future[Result]): Action[AnyContent] = Action.async {
    implicit request =>
      authorised(authPredicate) {
        withCurrentProfile { profile =>
          f(request)(profile)
        }
      } handleErrorResult
  }
}
