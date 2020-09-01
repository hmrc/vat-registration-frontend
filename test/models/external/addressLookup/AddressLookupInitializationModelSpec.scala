/*
 * Copyright 2020 HM Revenue & Customs
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

package models.external.addressLookup

import common.enums.AddressLookupJourneyIdentifier
import config.{AddressLookupConfiguration, FrontendAppConfig}
import models.external.addresslookup.AddressLookupConfigurationModel
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsValue, Json}
import testHelpers.VatRegSpec

class AddressLookupInitializationModelSpec extends VatRegSpec {

  val messagesApi = app.injector.instanceOf[MessagesApi]
  val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val testJourneyId = AddressLookupJourneyIdentifier.homeAddress
  val testContinueUrl = "http://continueUrl"

  def addressInitializationModel: AddressLookupConfigurationModel =
    new AddressLookupConfiguration()(frontendAppConfig, messagesApi).apply(testJourneyId, testContinueUrl)

  object LookupPageOptions {
    val title = "Search for your address"
    val heading = "Search address"
    val filterLabel = "House name or number (optional)"
    val submitLabel = "Search address"
    val manualAddressLinkText = "I don't have these details"
  }

  object SelectPageOptions {
    val title = "Choose an address"
    val heading = "Choose an address"
    val searchAgainLinkText = "Search again"
    val editAddressLinkText = "Enter address manually"
  }

  object EditPageOptions {
    val title = "Enter your address"
    val heading = "Enter address"
    val line1Label = "Address line 1"
    val line2Label = "Address line 2"
    val line3Label = "Address line 3"
    val countryLabel = "Country"
    val postcodeLabel = "Postcode"
    val submitLabel = "Next"
  }

  object ConfirmPageLabels {
    val title = "Confirm address"
    val heading = "Review and confirm"
    val submitLabel = "Save and continue"
  }

  val addressInitializationJson: JsValue = Json.obj(
    "version" -> 2,
    "options" -> Json.obj(
      "continueUrl" -> "http://localhost:9895http://vatRegEFEUrl/question?pageId=http://continueUrl",
      "phaseFeedbackLink" -> "http://localhost:9250/contact/beta-feedback?service=VATREG",
      "accessibilityFooterUrl" -> controllers.routes.WelcomeController.start().url,
      "deskProServiceName" -> "VATREG",
      "showPhaseBanner" -> true,
      "showBackButtons" -> true,
      "includeHMRCBranding" -> false,
      "ukMode" -> false,
      "selectPageConfig" -> Json.obj(
        "showSearchAgainLink" -> true
      ),
      "confirmPageConfig" -> Json.obj(
        "showChangeLinkcontinueUrl" -> true,
        "showSubHeadingAndInfo" -> false,
        "showSearchAgainLink" -> true,
        "showConfirmChangeText" -> false
      ),
      "timeoutConfig" -> Json.obj(
        "timeoutAmount" -> 900,
        "timeoutUrl" -> "http://localhost:9895/register-for-vat/sign-out"
      )
    ),
    "labels" -> Json.obj(
      "en" -> Json.obj(
        "appLevelLabels" -> Json.obj(
          "navTitle" -> "Register for VAT",
          "phaseBannerHtml" -> "This is a new service. Help us improve it - send your <a href=\"https://www.tax.service.gov.uk/register-for-vat/feedback\">feedback</a>."
        ),
        "lookupPageLabels" -> Json.obj(
          "title" -> LookupPageOptions.title,
          "heading" -> LookupPageOptions.heading,
          "filterLabel" -> LookupPageOptions.filterLabel,
          "submitLabel" -> LookupPageOptions.submitLabel,
          "manualAddressLinkText" -> LookupPageOptions.manualAddressLinkText
        ),
        "selectPageLabels" -> Json.obj(
          "title" -> SelectPageOptions.title,
          "heading" -> SelectPageOptions.heading,
          "searchAgainLinkText" -> SelectPageOptions.searchAgainLinkText,
          "editAddressLinkText" -> SelectPageOptions.editAddressLinkText
        ),
        "editPageLabels" -> Json.obj(
          "title" -> EditPageOptions.title,
          "heading" -> EditPageOptions.heading,
          "line1Label" -> EditPageOptions.line1Label,
          "line2Label" -> EditPageOptions.line2Label,
          "line3Label" -> EditPageOptions.line3Label,
          "postcodeLabel" -> EditPageOptions.postcodeLabel,
          "countryLabel" -> EditPageOptions.countryLabel,
          "submitLabel" -> EditPageOptions.submitLabel
        ),
        "confirmPageLabels" -> Json.obj(
          "title" -> ConfirmPageLabels.title,
          "heading" -> ConfirmPageLabels.heading,
          "submitLabel" -> ConfirmPageLabels.submitLabel
        )
      ),
      "cy" -> Json.obj(
        "appLevelLabels" -> Json.obj(
          "navTitle" -> "Register for VAT",
          "phaseBannerHtml" -> "This is a new service. Help us improve it - send your <a href=\"https://www.tax.service.gov.uk/register-for-vat/feedback\">feedback</a>."
        ),
        "lookupPageLabels" -> Json.obj(
          "title" -> LookupPageOptions.title,
          "heading" -> LookupPageOptions.heading,
          "filterLabel" -> LookupPageOptions.filterLabel,
          "submitLabel" -> LookupPageOptions.submitLabel,
          "manualAddressLinkText" -> LookupPageOptions.manualAddressLinkText
        ),
        "selectPageLabels" -> Json.obj(
          "title" -> SelectPageOptions.title,
          "heading" -> SelectPageOptions.heading,
          "searchAgainLinkText" -> SelectPageOptions.searchAgainLinkText,
          "editAddressLinkText" -> SelectPageOptions.editAddressLinkText
        ),
        "editPageLabels" -> Json.obj(
          "title" -> EditPageOptions.title,
          "heading" -> EditPageOptions.heading,
          "line1Label" -> EditPageOptions.line1Label,
          "line2Label" -> EditPageOptions.line2Label,
          "line3Label" -> EditPageOptions.line3Label,
          "postcodeLabel" -> EditPageOptions.postcodeLabel,
          "countryLabel" -> EditPageOptions.countryLabel,
          "submitLabel" -> EditPageOptions.submitLabel
        ),
        "confirmPageLabels" -> Json.obj(
          "title" -> ConfirmPageLabels.title,
          "heading" -> ConfirmPageLabels.heading,
          "submitLabel" -> ConfirmPageLabels.submitLabel
        )
      )
    )
  )

  "AddressLookupInitializationModel" must {
    "write to Json correctly" in {
      val actualResult = Json.toJson(addressInitializationModel)

      actualResult mustBe addressInitializationJson
    }
  }

}