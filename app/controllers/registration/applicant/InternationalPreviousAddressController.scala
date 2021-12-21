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

package controllers.registration.applicant

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.ConfigConnector
import controllers.BaseController
import forms.InternationalAddressForm
import models.view.{HomeAddressView, PreviousAddressView}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.CaptureInternationalAddress

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class InternationalPreviousAddressController  @Inject()(val authConnector: AuthConnector,
                                                        val sessionService: SessionService,
                                                        applicantDetailsService: ApplicantDetailsService,
                                                        configConnector: ConfigConnector,
                                                        view: CaptureInternationalAddress,
                                                        formProvider: InternationalAddressForm)
                                                       (implicit appConfig: FrontendAppConfig,
                                                        val executionContext: ExecutionContext,
                                                        baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile{

  private val headingMessageKey = "internationalAddress.prev.heading"
  private lazy val submitAction = routes.InternationalPreviousAddressController.submit

  private val postcodeRequiredCountry = "United Kingdom"
  private val postcodeField = "postcode"
  private val countryField = "country"
  private val postcodeRequiredErrorKey = "internationalAddress.error.postcode.empty"

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request => implicit profile =>
      for {
        applicantDetails <- applicantDetailsService.getApplicantDetails
        countries = configConnector.countries.flatMap(_.name)
        filledForm = applicantDetails.previousAddress.flatMap(_.address).fold(formProvider.form())(formProvider.form().fill)
      } yield Ok(view(filledForm, countries, submitAction, headingMessageKey))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request => implicit profile =>
      val countries = configConnector.countries.flatMap(_.name)

      formProvider.form().bindFromRequest.fold(
        formWithErrors => {
          val finalForm = if (formWithErrors(countryField).value.contains(postcodeRequiredCountry) && formWithErrors(postcodeField).value.contains("")) {
            formWithErrors.withError(postcodeField, postcodeRequiredErrorKey)
          } else {
            formWithErrors
          }
          Future.successful(BadRequest(view(finalForm, countries, submitAction, headingMessageKey)))
        },
        internationalAddress => {
          if (internationalAddress.country.flatMap(_.name).contains(postcodeRequiredCountry) && internationalAddress.postcode.isEmpty) {
            Future.successful(BadRequest(view(
              internationalAddressForm = formProvider.form().fill(internationalAddress).withError(postcodeField, postcodeRequiredErrorKey),
              countries = countries,
              submitAction = submitAction,
              headingKey = headingMessageKey
            )))
          } else {
            applicantDetailsService.saveApplicantDetails(PreviousAddressView(yesNo = true, Some(internationalAddress))) map { _ =>
              Redirect(routes.CaptureEmailAddressController.show)
            }
          }
        }
      )
  }

}
