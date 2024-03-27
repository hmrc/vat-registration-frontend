/*
 * Copyright 2024 HM Revenue & Customs
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

import models.external.addresslookup._
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.{Json, Writes}

case class InternationalAddressMessagesModel(appLevelLabels: AppLevelMessagesModel,
                                             lookupPageLabels: LookupPageMessagesModel,
                                             selectPageLabels: SelectPageMessagesModel,
                                             editPageLabels: EditPageMessagesModel,
                                             confirmPageLabels: ConfirmPageMessagesModel)

object InternationalAddressMessagesModel {
  implicit val writes: Writes[InternationalAddressMessagesModel] = Json.writes[InternationalAddressMessagesModel]

  def forJourney(journeyId: String, lang: Lang, optName: Option[String] = None)
                (implicit messagesApi: MessagesApi): InternationalAddressMessagesModel = {

    InternationalAddressMessagesModel(
      appLevelLabels = AppLevelMessagesModel.forLang(lang),
      lookupPageLabels = LookupPageMessagesModel.forJourney(journeyId, lang, msgPrefix = Some("international"), optName),
      selectPageLabels = SelectPageMessagesModel.forJourney(journeyId, lang, msgPrefix = Some("international"), optName),
      editPageLabels = EditPageMessagesModel.forJourney(journeyId, lang, msgPrefix = Some("international"), optName),
      confirmPageLabels = ConfirmPageMessagesModel.forJourney(journeyId, lang, msgPrefix = Some("international"), optName)
    )
  }
}
