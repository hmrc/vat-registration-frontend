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

import auth.VatTaxRegime
import cats.data.OptionT
import config.FrontendAuthConnector
import models.{ApiModelTransformer, CacheKey}
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Format
import services.{S4LService, VatRegistrationService}
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

abstract class VatRegistrationController(ds: CommonPlayDependencies) extends FrontendController with I18nSupport with Actions {

  //$COVERAGE-OFF$
  lazy val conf: Configuration = ds.conf
  implicit lazy val messagesApi: MessagesApi = ds.messagesApi
  override val authConnector: AuthConnector = FrontendAuthConnector

  //$COVERAGE-ON$

  /**
    * Use this to obtain an [[uk.gov.hmrc.play.frontend.auth.UserActions.AuthenticatedBy]] action builder.
    * Usage of an `AuthenticatedBy` is similar to standard [[play.api.mvc.Action]]. Just like you would do this:
    * {{{Action ( implicit request => Ok(...))}}}
    * or
    * {{{Action.async( implicit request => ??? // generates a Future Result )}}}
    * With `AuthenticatedBy` you would do the same but you get a handle on the current user's [[uk.gov.hmrc.play.frontend.auth.AuthContext]] too:
    * {{{authorised( implicit user => implicit request => Ok(...))}}}
    * or
    * {{{authorised.async( implicit user => imlicit request => ??? // generates a Future Result )}}}
    *
    * @return an AuthenticatedBy action builder that is specific to VatTaxRegime and GGConfidence confidence level
    */
  protected def authorised: AuthenticatedBy = AuthorisedFor(taxRegime = VatTaxRegime, pageVisibility = GGConfidence)

  import cats.instances.future._
  protected def viewModel[T: ApiModelTransformer : CacheKey : Format]()
  (implicit s4LService: S4LService, vatRegistrationService: VatRegistrationService, headerCarrier: HeaderCarrier): Future[T] =
    OptionT(s4LService.fetchAndGet[T]()).getOrElseF(vatRegistrationService.getVatScheme() map ApiModelTransformer[T].toViewModel)

}

@Singleton
final class CommonPlayDependencies @Inject()(val conf: Configuration, val messagesApi: MessagesApi)
