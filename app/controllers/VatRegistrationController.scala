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
import cats.instances.FutureInstances
import cats.syntax.ApplicativeSyntax
import config.FrontendAuthConnector
import models.{ApiModelTransformer, S4LKey, S4LModelTransformer, ViewModelFormat}
import play.api.Configuration
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Format
import services.{RegistrationService, S4LService}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

abstract class VatRegistrationController(ds: CommonPlayDependencies) extends FrontendController
  with I18nSupport with Actions with ApplicativeSyntax with FutureInstances {

  implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global

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
  protected[controllers] def authorised: AuthenticatedBy = AuthorisedFor(taxRegime = VatTaxRegime, pageVisibility = GGConfidence)

  protected[controllers] def viewModel[T] = new ViewModelLookupHelper[T]

  protected final class ViewModelLookupHelper[T] {
    def apply[G]()
                (implicit s4l: S4LService,
                 vrs: RegistrationService,
                 r: ViewModelFormat.Aux[T, G],
                 f: Format[G],
                 k: S4LKey[G],
                 hc: HeaderCarrier,
                 transformer: ApiModelTransformer[T]): OptionT[Future, T] =
      s4l.getViewModel[T, G]().orElseF(vrs.getVatScheme() map transformer.toViewModel)
  }

  protected[controllers] def save[T] = new ViewModelUpdateHelper[T]

  protected final class ViewModelUpdateHelper[T] {
    def apply[G](data: T)
                (implicit s4l: S4LService,
                 vrs: RegistrationService,
                 r: ViewModelFormat.Aux[T, G],
                 f: Format[G],
                 k: S4LKey[G],
                 transformer: S4LModelTransformer[G],
                 hc: HeaderCarrier): Future[CacheMap] = {

      val container = OptionT(s4l.fetchAndGet[G]()).getOrElseF(vrs.getVatScheme() map transformer.toS4LModel)

      s4l.updateViewModel(data, container)
    }
  }

  protected[controllers] def copyGlobalErrorsToFields[T](globalErrors: String*): Form[T] => Form[T] =
    fwe => fwe.copy(errors = fwe.errors ++ fwe.globalErrors.collect {
      case fe if fe.args.headOption.exists(globalErrors.contains) =>
        FormError(fe.args.head.asInstanceOf[String], fe.message, fe.args)
    })

}

@Singleton
final class CommonPlayDependencies @Inject()(val conf: Configuration, val messagesApi: MessagesApi)
