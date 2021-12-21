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

package models.external.addresslookup

import common.enums.AddressLookupJourneyIdentifier
import config.{AddressLookupConfiguration, FrontendAppConfig}
import models.external.addresslookup.AddressLookupConfigurationModel
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Call
import testHelpers.VatRegSpec

class AddressLookupInitializationModelSpec extends VatRegSpec {

  val messagesApi = app.injector.instanceOf[MessagesApi]
  val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val testJourneyId = AddressLookupJourneyIdentifier.homeAddress
  val testContinueUrl = Call("GET", "/continueUrl")

  def addressInitializationModel: AddressLookupConfigurationModel =
    new AddressLookupConfiguration()(frontendAppConfig, messagesApi).apply(testJourneyId, testContinueUrl)

  object LookupPageOptions {
    val title = "Find your home address"
    val heading = "Find your home address"
    val filterLabel = "Property name or number"
    val submitLabel = "Continue"
    val manualAddressLinkText = "The address does not have a UK postcode"
  }

  object SelectPageOptions {
    val title = "Choose the address"
    val heading = "Choose the address"
    val editAddressLinkText = "Enter address manually"
    val submitLabel = "Continue"
  }

  object EditPageOptions {
    val title = "Enter your home address"
    val heading = "Enter your home address"
    val line1Label = "Address line 1"
    val line2Label = "Address line 2"
    val line3Label = "Address line 3"
    val countryLabel = "Country"
    val postcodeLabel = "Postcode"
    val submitLabel = "Continue"
  }

  object ConfirmPageLabels {
    val title = "Confirm your home address"
    val heading = "Confirm your home address"
    val submitLabel = "Continue"
    val changeLinkText = "Edit this address"
  }

  val addressInitializationJson: JsValue = Json.obj(
    "version" -> 2,
    "options" -> Json.obj(
      "continueUrl" -> "http://localhost:9895/continueUrl",
      "signOutHref" -> "http://localhost:9514/feedback/vat-registration",
      "phaseFeedbackLink" -> "http://localhost:9250/contact/beta-feedback?service=vrs",
      "accessibilityFooterUrl" -> "http://localhost:12346/accessibility-statement/vat-registration",
      "deskProServiceName" -> "vrs",
      "showPhaseBanner" -> true,
      "showBackButtons" -> true,
      "includeHMRCBranding" -> false,
      "ukMode" -> false,
      "selectPageConfig" -> Json.obj(
        "showSearchAgainLink" -> false
      ),
      "confirmPageConfig" -> Json.obj(
        "showChangeLinkcontinueUrl" -> true,
        "showSubHeadingAndInfo" -> false,
        "showSearchAgainLink" -> false,
        "showConfirmChangeText" -> false
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
          "submitLabel" -> SelectPageOptions.submitLabel,
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
          "submitLabel" -> ConfirmPageLabels.submitLabel,
          "changeLinkText" -> ConfirmPageLabels.changeLinkText
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
          "editAddressLinkText" -> SelectPageOptions.editAddressLinkText,
          "submitLabel" -> SelectPageOptions.submitLabel
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
          "submitLabel" -> ConfirmPageLabels.submitLabel,
          "changeLinkText" -> ConfirmPageLabels.changeLinkText
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