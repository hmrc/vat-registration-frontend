/*
 * Copyright 2023 HM Revenue & Customs
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

package common.validators

import config.FrontendAppConfig
import models.api.{Address, Country}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Results.BadRequest
import play.api.mvc.{AnyContent, Call, Request, Result}
import uk.gov.hmrc.http.InternalServerException
import views.html.CaptureInternationalAddress

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class AddressFormResultsHandler @Inject()(view: CaptureInternationalAddress)
                                         (implicit appConfig: FrontendAppConfig) {
  private val postcodeField = "postcode"
  private val countryField = "country"
  private val postcodeRequiredErrorKey = "internationalAddress.error.postcode.empty"
  private val postcodeRequiredCountries = List("United Kingdom", "Isle of Man", "Guernsey", "Jersey")

  //scalastyle:off
  def handle(
              countries: Seq[Country],
              headingMessageKey: String,
              addressForm: Form[Address],
              submitAction: Call,
              saveContactDetails: Address => Future[Result],
              name: Option[String] = None,
              isPpob: Boolean = false
            )(implicit request: Request[AnyContent],
              messages: Messages): Future[Result] = {

    addressForm.fold(
      formWithErrors => {
        val maybeCountryName = formWithErrors(countryField).value
        val finalForm = if (missingPostcode(maybeCountryName, formWithErrors(postcodeField).value)) {
          formWithErrors.withError(postcodeField, postcodeRequiredErrorKey, getCountryName(countries, maybeCountryName))
        } else {
          formWithErrors
        }
        Future.successful(BadRequest(view(finalForm, countries.flatMap(_.name), submitAction, headingMessageKey, name, isPpob)))
      },
      internationalAddress => {
        val maybeCountryName = internationalAddress.country.flatMap(_.name)
        if (missingPostcode(maybeCountryName, internationalAddress.postcode)) {
          Future.successful(BadRequest(view(
            internationalAddressForm = addressForm.fill(internationalAddress).withError(
              postcodeField, postcodeRequiredErrorKey, getCountryName(countries, maybeCountryName)
            ),
            countries = countries.flatMap(_.name),
            submitAction = submitAction,
            headingKey = headingMessageKey,
            name = name,
            isPpob = isPpob
          )))
        } else {
          saveContactDetails(internationalAddress)
        }
      }
    )
  }

  private def missingPostcode(maybeCountry: Option[String], maybePostcode: Option[String]) = {
    maybeCountry.nonEmpty && postcodeRequiredCountries.contains(maybeCountry.get) && maybePostcode.isEmpty
  }

  private[validators] def getCountryName(countries: Seq[Country], maybeCountryName: Option[String]): String = {
    val countryName = maybeCountryName.getOrElse(
      throw new InternalServerException("[AddressFormResultsHandler] Missing country name")
    )

    countries.find(_.name.contains(countryName)).flatMap(_.name).getOrElse(
      throw new InternalServerException(s"[AddressFormResultsHandler] Missing country mapping for '$countryName'")
    )
  }
}