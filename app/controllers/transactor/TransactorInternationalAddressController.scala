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

package controllers.transactor

import common.validators.AddressFormResultsHandler
import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.ConfigConnector
import controllers.BaseController
import forms.InternationalAddressForm
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService, TransactorDetailsService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.CaptureInternationalAddress

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TransactorInternationalAddressController @Inject()(val authConnector: AuthConnector,
                                                         val sessionService: SessionService,
                                                         transactorDetailsService: TransactorDetailsService,
                                                         configConnector: ConfigConnector,
                                                         view: CaptureInternationalAddress,
                                                         formProvider: InternationalAddressForm,
                                                         addressFormResultsHandler: AddressFormResultsHandler)
                                                        (implicit appConfig: FrontendAppConfig,
                                                         val executionContext: ExecutionContext,
                                                         baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  private val headingMessageKey = "internationalAddress.home.heading"
  private lazy val submitAction = routes.TransactorInternationalAddressController.submit

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          transactorDetails <- transactorDetailsService.getTransactorDetails
          countries = configConnector.countries.flatMap(_.name)
          filledForm = transactorDetails.address.fold(formProvider.form())(formProvider.form().fill)
        } yield Ok(view(filledForm, countries, submitAction, headingMessageKey))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        val countries = configConnector.countries

        addressFormResultsHandler.handle(
          countries,
          headingMessageKey,
          formProvider.form().bindFromRequest,
          submitAction,
          internationalAddress =>
            transactorDetailsService.saveTransactorDetails(internationalAddress) map { _ =>
              Redirect(controllers.routes.TaskListController.show.url)
            }
        )
  }

}
