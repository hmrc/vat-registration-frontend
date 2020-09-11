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

package models.external.addresslookup

import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.{Json, Writes}
import utils.MessageOption

case class AddressMessageLanguageModel(en: AddressMessagesModel, cy: AddressMessagesModel)

object AddressMessageLanguageModel {
  implicit val writes: Writes[AddressMessageLanguageModel] = Json.writes[AddressMessageLanguageModel]
}

case class AddressMessagesModel(appLevelLabels: AppLevelMessagesModel,
                                lookupPageLabels: LookupPageMessagesModel,
                                selectPageLabels: SelectPageMessagesModel,
                                editPageLabels: EditPageMessagesModel,
                                confirmPageLabels: ConfirmPageMessagesModel)

object AddressMessagesModel {
  implicit val writes: Writes[AddressMessagesModel] = Json.writes[AddressMessagesModel]

  def forJourney(journeyId: String, lang: Lang)(implicit messagesApi: MessagesApi): AddressMessagesModel = {
    AddressMessagesModel(
      appLevelLabels = AppLevelMessagesModel.forLang(lang),
      lookupPageLabels = LookupPageMessagesModel.forJourney(journeyId, lang),
      selectPageLabels = SelectPageMessagesModel.forJourney(journeyId, lang),
      editPageLabels = EditPageMessagesModel.forJourney(journeyId, lang),
      confirmPageLabels = ConfirmPageMessagesModel.forJourney(journeyId, lang)
    )
  }
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
                                   manualAddressLinkText: Option[String]
                                )

object LookupPageMessagesModel {
  implicit val writes: Writes[LookupPageMessagesModel] = Json.writes[LookupPageMessagesModel]

  def forJourney(journeyId: String, lang: Lang)(implicit messagesApi: MessagesApi): LookupPageMessagesModel = {
    LookupPageMessagesModel (
      title = MessageOption(s"addressLookup.$journeyId.lookupPage.title", lang),
      heading = MessageOption(s"addressLookup.$journeyId.lookupPage.heading", lang),
      filterLabel = MessageOption(s"addressLookup.$journeyId.lookupPage.filterLabel", lang),
      postcodeLabel = MessageOption(s"addressLookup.$journeyId.LookupPage.postcodeLabel", lang),
      submitLabel = MessageOption(s"addressLookup.$journeyId.lookupPage.submitLabel", lang),
      noResultsFoundMessage = MessageOption(s"addressLookup.$journeyId.lookupPage.noResultsFoundMessage", lang),
      resultLimitExceededMessage = MessageOption(s"addressLookup.$journeyId.lookupPage.resultLimitExceededMessage", lang),
      manualAddressLinkText = MessageOption(s"addressLookup.$journeyId.lookupPage.manualAddressLinkText", lang)
    )
  }
}

case class SelectPageMessagesModel(title: Option[String],
                                   heading: Option[String],
                                   headingWithPostcode: Option[String],
                                   proposalListLabel: Option[String],
                                   submitLabel: Option[String],
                                   searchAgainLinkText: Option[String],
                                   editAddressLinkText: Option[String]
                                  )

object SelectPageMessagesModel {
  implicit val writes: Writes[SelectPageMessagesModel] = Json.writes[SelectPageMessagesModel]

  def forJourney(journeyId: String, lang: Lang)(implicit messagesApi: MessagesApi): SelectPageMessagesModel = {
    SelectPageMessagesModel(
      title = MessageOption(s"addressLookup.$journeyId.selectPage.title", lang),
      heading = MessageOption(s"addressLookup.$journeyId.selectPage.heading", lang),
      headingWithPostcode = MessageOption(s"addressLookup.$journeyId.selectPage.headingWithPostcode", lang),
      proposalListLabel = MessageOption(s"addressLookup.$journeyId.selectPage.proposalListLabel", lang),
      submitLabel = MessageOption(s"addressLookup.$journeyId.selectPage.submitLabel", lang),
      searchAgainLinkText = MessageOption(s"addressLookup.$journeyId.selectPage.searchAgainLinkText", lang),
      editAddressLinkText = MessageOption(s"addressLookup.$journeyId.selectPage.editAddressLinkText", lang)
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
                                 submitLabel: Option[String]
                                )

object EditPageMessagesModel {
  implicit val writes: Writes[EditPageMessagesModel] = Json.writes[EditPageMessagesModel]

  def forJourney(journeyId: String, lang: Lang)(implicit messagesApi: MessagesApi): EditPageMessagesModel = {
    EditPageMessagesModel(
      title = MessageOption(s"addressLookup.$journeyId.editPage.title", lang),
      heading = MessageOption(s"addressLookup.$journeyId.editPage.heading", lang),
      line1Label = MessageOption(s"addressLookup.$journeyId.editPage.line1Label", lang),
      line2Label = MessageOption(s"addressLookup.$journeyId.editPage.line2Label", lang),
      line3Label = MessageOption(s"addressLookup.$journeyId.editPage.line3Label", lang),
      townLabel = MessageOption(s"addressLookup.$journeyId.editPage.townLabel", lang),
      postcodeLabel = MessageOption(s"addressLookup.$journeyId.editPage.postcodeLabel", lang),
      countryLabel = MessageOption(s"addressLookup.$journeyId.editPage.countryLabel", lang),
      submitLabel = MessageOption(s"addressLookup.$journeyId.editPage.submitLabel", lang)
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
                                    confirmChangeText: Option[String]
                                   )

object ConfirmPageMessagesModel {
  implicit val writes: Writes[ConfirmPageMessagesModel] = Json.writes[ConfirmPageMessagesModel]

  def forJourney(journeyId: String, lang: Lang)(implicit messagesApi: MessagesApi): ConfirmPageMessagesModel = {
    ConfirmPageMessagesModel(
      title = MessageOption(s"addressLookup.$journeyId.confirmPage.title", lang),
      heading = MessageOption(s"addressLookup.$journeyId.confirmPage.heading", lang),
      infoMessage = MessageOption(s"addressLookup.$journeyId.confirmPage.infoMessage", lang),
      infoSubheading = MessageOption(s"addressLookup.$journeyId.confirmPage.infoSubheading", lang),
      submitLabel = MessageOption(s"addressLookup.$journeyId.confirmPage.submitLabel", lang),
      searchAgainLinkText = MessageOption(s"addressLookup.$journeyId.confirmPage.searchAgainLinkText", lang),
      changeLinkText = MessageOption(s"addressLookup.$journeyId.confirmPage.changeLinkText", lang),
      confirmChangeText = MessageOption(s"addressLookup.$journeyId.confirmPage.confirmChangeText", lang)
    )
  }
}
