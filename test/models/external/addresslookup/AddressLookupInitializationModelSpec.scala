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
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import testHelpers.VatRegSpec

class AddressLookupInitializationModelSpec extends VatRegSpec {

  val messagesApi = app.injector.instanceOf[MessagesApi]
  val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val testJourneyId = AddressLookupJourneyIdentifier.homeAddress
  val testContinueUrl = Call("GET", "/continueUrl")

  def addressInitializationModel(ukMode: Boolean = false): AddressLookupConfigurationModel =
    new AddressLookupConfiguration()(frontendAppConfig, messagesApi).apply(testJourneyId, testContinueUrl, ukMode)

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
    val internationalLine3Label = "Address line 3 (optional)"
    val countryLabel = "Country"
    val postcodeLabel = "Postcode"
    val internationalPostcodeLabel = "Postcode (optional)"
    val submitLabel = "Continue"
    val townLabel = "Town/City"
  }

  object ConfirmPageLabels {
    val title = "Confirm your home address"
    val heading = "Confirm your home address"
    val submitLabel = "Continue"
    val changeLinkText = "Edit this address"
  }

  object CountryPickerLabels {
    val title = "Find your home address"
    val heading = "Find your home address"
  }

  object LookupPageOptionsCy {
    val title = "Dewch o hyd i’ch cyfeiriad cartref"
    val heading = "Dewch o hyd i’ch cyfeiriad cartref"
    val filterLabel = "Enw neu rif yr eiddo"
    val submitLabel = "Yn eich blaen"
    val manualAddressLinkText = "Nid oes gan y cyfeiriad god post yn y DU"
  }

  object SelectPageOptionsCy {
    val title = "Dewiswch y cyfeiriad"
    val heading = "Dewiswch y cyfeiriad"
    val editAddressLinkText = "Nodwch y cyfeiriad â llaw"
    val submitLabel = "Yn eich blaen"
  }

  object EditPageOptionsCy {
    val title = "Nodwch eich cyfeiriad cartref"
    val heading = "Nodwch eich cyfeiriad cartref"
    val line1Label = "Cyfeiriad – llinell 1"
    val line2Label = "Cyfeiriad – llinell 2"
    val line3Label = "Cyfeiriad – llinell 3"
    val internationalLine3Label = "Cyfeiriad – llinell 3 (dewisol)"
    val countryLabel = "Gwlad"
    val postcodeLabel = "Cod post"
    val internationalPostcodeLabel = "Cod post (dewisol)"
    val submitLabel = "Yn eich blaen"
    val townLabel = "Tref/Dinas"
  }

  object ConfirmPageLabelsCy {
    val title = "Cadarnhewch eich cyfeiriad cartref"
    val heading = "Cadarnhewch eich cyfeiriad cartref"
    val submitLabel = "Yn eich blaen"
    val changeLinkText = "Golygu’r cyfeiriad hwn"
  }

  object CountryPickerLabelsCy {
    val title = "Dewch o hyd i’ch cyfeiriad cartref"
    val heading = "Dewch o hyd i’ch cyfeiriad cartref"
  }

  private val internationalAddressMessages: JsObject = Json.obj(
    "appLevelLabels" -> Json.obj(
      "navTitle" -> "Register for VAT",
      "phaseBannerHtml" -> "This is a new service. Help us improve it - send your <a href=\"https://www.tax.service.gov.uk/register-for-vat/feedback\">feedback</a>."
    ),
    "lookupPageLabels" -> Json.obj(
    ),
    "selectPageLabels" -> Json.obj(
    ),
    "editPageLabels" -> Json.obj(
      "title" -> EditPageOptions.title,
      "heading" -> EditPageOptions.heading,
      "line1Label" -> EditPageOptions.line1Label,
      "line2Label" -> EditPageOptions.line2Label,
      "line3Label" -> EditPageOptions.internationalLine3Label,
      "townLabel" -> EditPageOptions.townLabel,
      "countryLabel" -> EditPageOptions.countryLabel,
      "postcodeLabel" -> EditPageOptions.internationalPostcodeLabel,
      "submitLabel" -> EditPageOptions.submitLabel
    ),
    "confirmPageLabels" -> Json.obj(
      "title" -> ConfirmPageLabels.title,
      "heading" -> ConfirmPageLabels.heading,
      "submitLabel" -> ConfirmPageLabels.submitLabel,
      "changeLinkText" -> ConfirmPageLabels.changeLinkText
    )
  )

  private val internationalAddressMessagesCy: JsObject = Json.obj(
    "appLevelLabels" -> Json.obj(
      "navTitle" -> "Cofrestru ar gyfer TAW",
      "phaseBannerHtml" -> "Gwasanaeth newydd yw hwn. Helpwch ni i’w wella – anfonwch eich <a href=\"https://www.tax.service.gov.uk/register-for-vat/feedback\">adborth</a>."
    ),
    "lookupPageLabels" -> Json.obj(
    ),
    "selectPageLabels" -> Json.obj(
    ),
    "editPageLabels" -> Json.obj(
      "title" -> EditPageOptionsCy.title,
      "heading" -> EditPageOptionsCy.heading,
      "line1Label" -> EditPageOptionsCy.line1Label,
      "line2Label" -> EditPageOptionsCy.line2Label,
      "line3Label" -> EditPageOptionsCy.internationalLine3Label,
      "townLabel" -> EditPageOptionsCy.townLabel,
      "countryLabel" -> EditPageOptionsCy.countryLabel,
      "postcodeLabel" -> EditPageOptionsCy.internationalPostcodeLabel,
      "submitLabel" -> EditPageOptionsCy.submitLabel
    ),
    "confirmPageLabels" -> Json.obj(
      "title" -> ConfirmPageLabelsCy.title,
      "heading" -> ConfirmPageLabelsCy.heading,
      "submitLabel" -> ConfirmPageLabelsCy.submitLabel,
      "changeLinkText" -> ConfirmPageLabelsCy.changeLinkText
    )
  )

  val addressInitializationJson: JsObject = Json.obj(
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
          "countryLabel" -> EditPageOptions.countryLabel,
          "postcodeLabel" -> EditPageOptions.postcodeLabel,
          "submitLabel" -> EditPageOptions.submitLabel
        ),
        "confirmPageLabels" -> Json.obj(
          "title" -> ConfirmPageLabels.title,
          "heading" -> ConfirmPageLabels.heading,
          "submitLabel" -> ConfirmPageLabels.submitLabel,
          "changeLinkText" -> ConfirmPageLabels.changeLinkText
        ),
        "international" -> internationalAddressMessages
      ),
      "cy" -> Json.obj(
        "appLevelLabels" -> Json.obj(
          "navTitle" -> "Cofrestru ar gyfer TAW",
          "phaseBannerHtml" -> "Gwasanaeth newydd yw hwn. Helpwch ni i’w wella – anfonwch eich <a href=\"https://www.tax.service.gov.uk/register-for-vat/feedback\">adborth</a>."
        ),
        "lookupPageLabels" -> Json.obj(
          "title" -> LookupPageOptionsCy.title,
          "heading" -> LookupPageOptionsCy.heading,
          "filterLabel" -> LookupPageOptionsCy.filterLabel,
          "submitLabel" -> LookupPageOptionsCy.submitLabel,
          "manualAddressLinkText" -> LookupPageOptionsCy.manualAddressLinkText
        ),
        "selectPageLabels" -> Json.obj(
          "title" -> SelectPageOptionsCy.title,
          "heading" -> SelectPageOptionsCy.heading,
          "editAddressLinkText" -> SelectPageOptionsCy.editAddressLinkText,
          "submitLabel" -> SelectPageOptionsCy.submitLabel
        ),
        "editPageLabels" -> Json.obj(
          "title" -> EditPageOptionsCy.title,
          "heading" -> EditPageOptionsCy.heading,
          "line1Label" -> EditPageOptionsCy.line1Label,
          "line2Label" -> EditPageOptionsCy.line2Label,
          "line3Label" -> EditPageOptionsCy.line3Label,
          "countryLabel" -> EditPageOptionsCy.countryLabel,
          "postcodeLabel" -> EditPageOptionsCy.postcodeLabel,
          "submitLabel" -> EditPageOptionsCy.submitLabel
        ),
        "confirmPageLabels" -> Json.obj(
          "title" -> ConfirmPageLabelsCy.title,
          "heading" -> ConfirmPageLabelsCy.heading,
          "submitLabel" -> ConfirmPageLabelsCy.submitLabel,
          "changeLinkText" -> ConfirmPageLabelsCy.changeLinkText
        ),
        "international" -> internationalAddressMessagesCy
      )
    )
  )

  "AddressLookupInitializationModel" must {
    "write to Json correctly when UK mode is false" in {
      val actualResult = Json.toJson(addressInitializationModel())

      actualResult mustBe addressInitializationJson.deepMerge(
        Json.obj(
          "labels" -> Json.obj(
            "en" -> Json.obj(
              "countryPickerLabels" -> Json.obj(
                "title" -> CountryPickerLabels.title,
                "heading" -> CountryPickerLabels.heading
              )
            ),
            "cy" -> Json.obj(
              "countryPickerLabels" -> Json.obj(
                "title" -> CountryPickerLabelsCy.title,
                "heading" -> CountryPickerLabelsCy.heading
              )
            )
          )
        )
      )
    }

    "Send the UK mode flag as true when required" in {
      val actualResult = Json.toJson(addressInitializationModel(ukMode = true))

      actualResult mustBe addressInitializationJson.deepMerge(
        Json.obj("options" ->
          Json.obj("ukMode" -> true)
        )
      )
    }
  }

}