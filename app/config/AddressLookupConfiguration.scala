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

package config

import common.enums.AddressLookupJourneyIdentifier
import models.external.addresslookup._
import models.external.addresslookup.messages.AddressMessagesModel
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.Call
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl

import javax.inject.Inject

class AddressLookupConfiguration @Inject()(implicit appConfig: FrontendAppConfig, messages: MessagesApi) {

  def apply(journeyId: AddressLookupJourneyIdentifier.Value, continueRoute: Call, useUkMode: Boolean, optName: Option[String] = None): AddressLookupConfigurationModel = {
    val english = Lang("en")
    val welsh = Lang("cy")

    AddressLookupConfigurationModel(
      version = 2,
      options = AddressLookupOptionsModel(
        continueUrl = SafeRedirectUrl(appConfig.hostUrl + continueRoute.url),
        signOutHref = SafeRedirectUrl(appConfig.feedbackUrl),
        phaseFeedbackLink = SafeRedirectUrl(appConfig.betaFeedbackUrl),
        accessibilityFooterUrl = appConfig.accessibilityStatementUrl,
        deskProServiceName = appConfig.contactFormServiceIdentifier,
        showPhaseBanner = true,
        showBackButtons = true,
        includeHMRCBranding = false,
        ukMode = useUkMode,
        selectPageConfig = AddressLookupSelectConfigModel (
          showSearchAgainLink = false
        ),
        confirmPageConfig = AddressLookupConfirmConfigModel(
          showChangeLinkcontinueUrl = true,
          showSubHeadingAndInfo = false,
          showSearchAgainLink = false,
          showConfirmChangeText = false
        )
      ),
      labels = AddressMessageLanguageModel(
        en = AddressMessagesModel.forJourney(journeyId.toString, english, useUkMode, optName),
        cy = AddressMessagesModel.forJourney(journeyId.toString, welsh, useUkMode, optName)
      )
    )
  }

}
