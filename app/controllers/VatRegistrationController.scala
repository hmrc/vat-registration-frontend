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

import javax.inject.{Inject, Singleton}

import auth.VatTaxRegime
import cats.data.OptionT
import cats.instances.FutureInstances
import cats.syntax.ApplicativeSyntax
import models._
import play.api.Configuration
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Format
import services.{RegistrationService, S4LService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

abstract class VatRegistrationController(ds: CommonPlayDependencies) extends FrontendController
  with I18nSupport with Actions with ApplicativeSyntax with FutureInstances {

  implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global

  lazy val conf: Configuration = ds.conf
  implicit lazy val messagesApi: MessagesApi = ds.messagesApi

  protected[controllers] def authorised: AuthenticatedBy = AuthorisedFor(taxRegime = VatTaxRegime, pageVisibility = GGConfidence)

  protected[controllers] def viewModel[T] = new ViewModelLookupHelper[T]

  protected final class ViewModelLookupHelper[T] {
    def apply[G]()
                (implicit s4l: S4LService,
                 vrs: RegistrationService,
                 currentProfile: CurrentProfile,
                 r: ViewModelFormat.Aux[T, G],
                 f: Format[G],
                 k: S4LKey[G],
                 hc: HeaderCarrier,
                 s4lTransformer: S4LModelTransformer[G]
                ): OptionT[Future, T] =
      s4l.getViewModel[T, G](s4lContainer[G]())
  }

  /****
    * Get an up-to-date S4LContainer[G] - either from s4l or db (if not present in s4l)
    */
  protected[controllers] def s4lContainer[G]()
                                            (implicit s4l: S4LService,
                                             currentProfile: CurrentProfile,
                                             vrs: RegistrationService,
                                             f: Format[G],
                                             k: S4LKey[G],
                                             hc: HeaderCarrier,
                                             s4lTransformer: S4LModelTransformer[G]
                                            ): Future[G] =
    OptionT(s4l.fetchAndGet[G]).getOrElseF(vrs.getVatScheme map s4lTransformer.toS4LModel)

  protected[controllers] def save[T] = new ViewModelUpdateHelper[T]

  protected final class ViewModelUpdateHelper[T] {
    def apply[G](data: T)
                (implicit s4l: S4LService,
                 vrs: RegistrationService,
                 currentProfile: CurrentProfile,
                 r: ViewModelFormat.Aux[T, G],
                 f: Format[G],
                 k: S4LKey[G],
                 s4lTransformer: S4LModelTransformer[G],
                 hc: HeaderCarrier): Future[CacheMap] =
      s4l.updateViewModel(data, s4lContainer[G]())
  }

  protected[controllers] def copyGlobalErrorsToFields[T](globalErrors: String*): Form[T] => Form[T] =
    fwe => fwe.copy(errors = fwe.errors ++ fwe.globalErrors.collect {
      case fe if fe.args.headOption.exists(globalErrors.contains) =>
        FormError(fe.args.head.asInstanceOf[String], fe.message, fe.args)
    })
}

@Singleton
final class CommonPlayDependencies @Inject()(val conf: Configuration, val messagesApi: MessagesApi)
