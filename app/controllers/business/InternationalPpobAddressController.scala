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

package controllers.business

import common.validators.AddressFormResultsHandler
import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.ConfigConnector
import controllers.BaseController
import forms.InternationalAddressForm
import models.api.Country
import play.api.mvc.{Action, AnyContent}
import services.{BusinessContactService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.CaptureInternationalAddress

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class InternationalPpobAddressController  @Inject()(val authConnector: AuthConnector,
                                                    val sessionService: SessionService,
                                                    businessContactService: BusinessContactService,
                                                    configConnector: ConfigConnector,
                                                    view: CaptureInternationalAddress,
                                                    formProvider: InternationalAddressForm,
                                                    addressFormResultsHandler: AddressFormResultsHandler)
                                                   (implicit appConfig: FrontendAppConfig,
                                                    val executionContext: ExecutionContext,
                                                    baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile{

  private val headingMessageKey = "internationalAddress.ppob.heading"
  private lazy val submitAction = routes.InternationalPpobAddressController.submit
  private val invalidCountries = Seq("United Kingdom")

  private val countries: Seq[Country] = configConnector.countries.filter(country => {
    country.name.isDefined && !invalidCountries.contains(country.name.get)
  })

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request => implicit profile =>
      for {
        contactDetails <- businessContactService.getBusinessContact
        filledForm = contactDetails.ppobAddress.fold(formProvider.form(invalidCountries))(formProvider.form(invalidCountries).fill)
      } yield Ok(view(filledForm, countries.flatMap(_.name), submitAction, headingMessageKey))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request => implicit profile =>
      addressFormResultsHandler.handle(
        countries,
        headingMessageKey,
        formProvider.form(invalidCountries).bindFromRequest,
        submitAction,
        internationalAddress => {
          businessContactService.updateBusinessContact(internationalAddress) map { _ =>
            Redirect(routes.BusinessEmailController.show)
          }
        }
      )
  }
}
