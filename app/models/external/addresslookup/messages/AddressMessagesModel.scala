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

package models.external.addresslookup.messages

import models.external.addresslookup.{AppLevelMessagesModel, ConfirmPageMessagesModel, CountryPickerMessagesModel, EditPageMessagesModel, LookupPageMessagesModel, SelectPageMessagesModel}
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.{Json, Writes}

case class AddressMessagesModel(appLevelLabels: AppLevelMessagesModel,
                                lookupPageLabels: LookupPageMessagesModel,
                                selectPageLabels: SelectPageMessagesModel,
                                editPageLabels: EditPageMessagesModel,
                                confirmPageLabels: ConfirmPageMessagesModel,
                                countryPickerLabels: Option[CountryPickerMessagesModel] = None,
                                international: InternationalAddressMessagesModel
                               )

object AddressMessagesModel {
  implicit val writes: Writes[AddressMessagesModel] = Json.writes[AddressMessagesModel]

  def forJourney(journeyId: String, lang: Lang, useUkMode: Boolean = false)
                (implicit messagesApi: MessagesApi): AddressMessagesModel = {

    AddressMessagesModel(
      appLevelLabels = AppLevelMessagesModel.forLang(lang),
      lookupPageLabels = LookupPageMessagesModel.forJourney(journeyId, lang),
      selectPageLabels = SelectPageMessagesModel.forJourney(journeyId, lang),
      editPageLabels = EditPageMessagesModel.forJourney(journeyId, lang),
      confirmPageLabels = ConfirmPageMessagesModel.forJourney(journeyId, lang),
      countryPickerLabels = if (useUkMode) None else Some(CountryPickerMessagesModel.forJourney(journeyId, lang)),
      international = InternationalAddressMessagesModel.forJourney(journeyId, lang)
    )
  }
}