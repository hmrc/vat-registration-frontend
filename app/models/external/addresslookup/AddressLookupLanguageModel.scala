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

import models.external.addresslookup.messages.AddressMessagesModel
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.{Json, Writes}
import utils.MessageOption

case class AddressMessageLanguageModel(en: AddressMessagesModel, cy: AddressMessagesModel)

object AddressMessageLanguageModel {
  implicit val writes: Writes[AddressMessageLanguageModel] = Json.writes[AddressMessageLanguageModel]
}

case class AppLevelMessagesModel(navTitle: String, phaseBannerHtml: Option[String] = None)

object AppLevelMessagesModel {
  implicit val writes: Writes[AppLevelMessagesModel] = Json.writes[AppLevelMessagesModel]

  def forLang(lang: Lang)(implicit messagesApi: MessagesApi): AppLevelMessagesModel = {
    val messages = messagesApi.preferred(Seq(lang))

    AppLevelMessagesModel(
      navTitle = messages("app.title"),
      phaseBannerHtml = MessageOption("addressLookup.common.phaseBannerHtml", lang)
    )
  }
}

case class LookupPageMessagesModel(title: Option[String],
                                   heading: Option[String],
                                   filterLabel: Option[String],
                                   postcodeLabel: Option[String],
                                   submitLabel: Option[String],
                                   noResultsFoundMessage: Option[String],
                                   resultLimitExceededMessage: Option[String],
                                   manualAddressLinkText: Option[String])

object LookupPageMessagesModel {
  implicit val writes: Writes[LookupPageMessagesModel] = Json.writes[LookupPageMessagesModel]

  def forJourney(journeyId: String, lang: Lang, msgPrefix: Option[String] = None, optName: Option[String] = None)(implicit messagesApi: MessagesApi): LookupPageMessagesModel = {
    val section = s"addressLookup.$journeyId.lookupPage"
    val sectionPrefix = msgPrefix.fold(section)(prefix => s"$prefix.$section")

    LookupPageMessagesModel(
      title = MessageOption(s"$sectionPrefix.title", lang, optName.toSeq: _*),
      heading = MessageOption(s"$sectionPrefix.heading", lang, optName.toSeq: _*),
      filterLabel = MessageOption(s"$sectionPrefix.filterLabel", lang),
      postcodeLabel = MessageOption(s"$sectionPrefix.postcodeLabel", lang),
      submitLabel = MessageOption(s"$sectionPrefix.submitLabel", lang),
      noResultsFoundMessage = MessageOption(s"$sectionPrefix.noResultsFoundMessage", lang),
      resultLimitExceededMessage = MessageOption(s"$sectionPrefix.resultLimitExceededMessage", lang),
      manualAddressLinkText = MessageOption(s"$sectionPrefix.manualAddressLinkText", lang)
    )
  }
}

case class CountryPickerMessagesModel(title: Option[String],
                                      heading: Option[String])

object CountryPickerMessagesModel {
  implicit val writes: Writes[CountryPickerMessagesModel] = Json.writes[CountryPickerMessagesModel]

  def forJourney(journeyId: String, lang: Lang, optName: Option[String] = None)(implicit messagesApi: MessagesApi): CountryPickerMessagesModel = {
    CountryPickerMessagesModel(
      title = MessageOption(s"addressLookup.$journeyId.countryPicker.title", lang, optName.toSeq: _*),
      heading = MessageOption(s"addressLookup.$journeyId.countryPicker.heading", lang, optName.toSeq: _*)
    )
  }
}

case class SelectPageMessagesModel(title: Option[String],
                                   heading: Option[String],
                                   headingWithPostcode: Option[String],
                                   proposalListLabel: Option[String],
                                   submitLabel: Option[String],
                                   searchAgainLinkText: Option[String],
                                   editAddressLinkText: Option[String])

object SelectPageMessagesModel {
  implicit val writes: Writes[SelectPageMessagesModel] = Json.writes[SelectPageMessagesModel]

  def forJourney(journeyId: String, lang: Lang, msgPrefix: Option[String] = None, optName: Option[String] = None)(implicit messagesApi: MessagesApi): SelectPageMessagesModel = {
    val section = s"addressLookup.$journeyId.selectPage"
    val sectionPrefix = msgPrefix.fold(section)(prefix => s"$prefix.$section")

    SelectPageMessagesModel(
      title = MessageOption(s"$sectionPrefix.title", lang, optName.toSeq: _*),
      heading = MessageOption(s"$sectionPrefix.heading", lang, optName.toSeq: _*),
      headingWithPostcode = MessageOption(s"$sectionPrefix.headingWithPostcode", lang),
      proposalListLabel = MessageOption(s"$sectionPrefix.proposalListLabel", lang),
      submitLabel = MessageOption(s"$sectionPrefix.submitLabel", lang),
      searchAgainLinkText = MessageOption(s"$sectionPrefix.searchAgainLinkText", lang),
      editAddressLinkText = MessageOption(s"$sectionPrefix.editAddressLinkText", lang)
    )
  }
}

case class EditPageMessagesModel(title: Option[String],
                                 heading: Option[String],
                                 line1Label: Option[String],
                                 line2Label: Option[String],
                                 townLabel: Option[String],
                                 line3Label: Option[String],
                                 postcodeLabel: Option[String],
                                 countryLabel: Option[String],
                                 submitLabel: Option[String])

object EditPageMessagesModel {
  implicit val writes: Writes[EditPageMessagesModel] = Json.writes[EditPageMessagesModel]

  def forJourney(journeyId: String, lang: Lang, msgPrefix: Option[String] = None, optName: Option[String] = None)(implicit messagesApi: MessagesApi): EditPageMessagesModel = {
    val section = s"addressLookup.$journeyId.editPage"
    val sectionPrefix = msgPrefix.fold(section)(prefix => s"$prefix.$section")

    EditPageMessagesModel(
      title = MessageOption(s"$sectionPrefix.title", lang, optName.toSeq: _*),
      heading = MessageOption(s"$sectionPrefix.heading", lang, optName.toSeq: _*),
      line1Label = MessageOption(s"$sectionPrefix.line1Label", lang),
      line2Label = MessageOption(s"$sectionPrefix.line2Label", lang),
      line3Label = MessageOption(s"$sectionPrefix.line3Label", lang),
      townLabel = MessageOption(s"$sectionPrefix.townLabel", lang),
      postcodeLabel = MessageOption(s"$sectionPrefix.postcodeLabel", lang),
      countryLabel = MessageOption(s"$sectionPrefix.countryLabel", lang),
      submitLabel = MessageOption(s"$sectionPrefix.submitLabel", lang)
    )
  }
}

case class ConfirmPageMessagesModel(title: Option[String],
                                    heading: Option[String],
                                    infoSubheading: Option[String],
                                    infoMessage: Option[String],
                                    submitLabel: Option[String],
                                    searchAgainLinkText: Option[String],
                                    changeLinkText: Option[String],
                                    confirmChangeText: Option[String])

object ConfirmPageMessagesModel {
  implicit val writes: Writes[ConfirmPageMessagesModel] = Json.writes[ConfirmPageMessagesModel]

  def forJourney(journeyId: String, lang: Lang, msgPrefix: Option[String] = None, optName: Option[String] = None)(implicit messagesApi: MessagesApi): ConfirmPageMessagesModel = {
    val section = s"addressLookup.$journeyId.confirmPage"
    val sectionPrefix = msgPrefix.fold(section)(prefix => s"$prefix.$section")

    ConfirmPageMessagesModel(
      title = MessageOption(s"$sectionPrefix.title", lang, optName.toSeq: _*),
      heading = MessageOption(s"$sectionPrefix.heading", lang, optName.toSeq: _*),
      infoMessage = MessageOption(s"$sectionPrefix.infoMessage", lang),
      infoSubheading = MessageOption(s"$sectionPrefix.infoSubheading", lang),
      submitLabel = MessageOption(s"$sectionPrefix.submitLabel", lang),
      searchAgainLinkText = MessageOption(s"$sectionPrefix.searchAgainLinkText", lang),
      changeLinkText = MessageOption(s"$sectionPrefix.changeLinkText", lang),
      confirmChangeText = MessageOption(s"$sectionPrefix.confirmChangeText", lang)
    )
  }
}