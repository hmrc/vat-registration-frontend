/*
 * Copyright 2017 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}

import auth.VatAuthenticationProvider
import config.FrontendAuthConnector
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.auth.connectors.domain.Accounts
import uk.gov.hmrc.play.frontend.auth.{Actions, AuthenticationProvider, TaxRegime}
import uk.gov.hmrc.play.frontend.controller.FrontendController

abstract class VatRegistrationController(ds: CommonPlayDependencies) extends FrontendController with I18nSupport with Actions {
  //$COVERAGE-OFF$

  lazy val conf: Configuration = ds.conf
  implicit lazy val messagesApi: MessagesApi = ds.messagesApi
  override val authConnector: AuthConnector = FrontendAuthConnector
  //$COVERAGE-ON$

  protected def authorised: AuthenticatedBy = AuthorisedFor(taxRegime = VatRegime, pageVisibility = GGConfidence)

}

@Singleton
final class CommonPlayDependencies @Inject()(val conf: Configuration, val messagesApi: MessagesApi)

object VatRegime extends TaxRegime {

  override def isAuthorised(accounts: Accounts): Boolean = true

  override def authenticationType: AuthenticationProvider = VatAuthenticationProvider

}