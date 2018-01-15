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

import auth.VatTaxRegime
import models.CurrentProfile
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.SessionProfile
import uk.gov.hmrc.play.frontend.auth.{Actions, AuthContext}
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

trait VatRegistrationControllerNoAux extends FrontendController with I18nSupport with Actions {
  self: SessionProfile =>

  type AuthorisedRequest = (AuthContext) => (Request[AnyContent]) => Future[Result]
  type AuthorisedRequestWithCurrentProfile = (AuthContext) => (Request[AnyContent]) => (CurrentProfile) => Future[Result]

  def authorised: AuthenticatedBy = AuthorisedFor(taxRegime = VatTaxRegime, pageVisibility = GGConfidence)
  def withCurrentProfile(f: => AuthorisedRequestWithCurrentProfile): AuthorisedRequest = {
    a => r => withCurrentProfile(f(a)(r))(r, hc(r))
  }

  def authorisedWithCurrentProfile(f: => AuthorisedRequestWithCurrentProfile): Action[AnyContent] = {
    authorised async withCurrentProfile(f)
  }
}
