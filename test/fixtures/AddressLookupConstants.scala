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

package fixtures

import models.external.addresslookup._
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl

object AddressLookupConstants {

  val testContinueUrl = "continueUrl"
  val testSignOutHref = "signOutHref"
  val testPhaseFeedbackLink = "phaseFeedbackLink"
  val testAcessibilityUrl = "accessibilityUrl"
  val testDeskproServiceName = "VATREG"
  val testTimeoutUrl = "timeoutUrl"

  val testAppLevelMessages = AppLevelMessagesModel(
    navTitle = "test nav title"
  )

  val testLookupMessages = LookupPageMessagesModel(
    title = Some("test lookup page title"),
    heading = Some("test lookup page heading"),
    filterLabel = None,
    postcodeLabel = None,
    submitLabel = None,
    noResultsFoundMessage = None,
    resultLimitExceededMessage = None,
    manualAddressLinkText = None
  )

  val testEditMessages = EditPageMessagesModel(
    title = Some("test edit page title"),
    heading = Some("test edit page heading"),
    line1Label = None,
    line2Label = None,
    line3Label = None,
    townLabel = None,
    countryLabel = None,
    postcodeLabel = None,
    submitLabel = None
  )

  val testSelectMessages = SelectPageMessagesModel(
    title = Some("test select page title"),
    heading = Some("test select page heading"),
    headingWithPostcode = None,
    proposalListLabel = None,
    submitLabel = None,
    searchAgainLinkText = None,
    editAddressLinkText = None
  )

  val testConfirmMessages = ConfirmPageMessagesModel(
    title = Some("test confirm page title"),
    heading = Some("test confirm page heading"),
    infoMessage = None,
    infoSubheading = None,
    submitLabel = None,
    searchAgainLinkText = None,
    changeLinkText = None,
    confirmChangeText = None
  )

  val testAlfConfig = AddressLookupConfigurationModel(
    version = 2,
    options = AddressLookupOptionsModel(
      continueUrl = SafeRedirectUrl(testContinueUrl),
      signOutHref = SafeRedirectUrl(testSignOutHref),
      phaseFeedbackLink = SafeRedirectUrl(testPhaseFeedbackLink),
      accessibilityFooterUrl = testAcessibilityUrl,
      deskProServiceName = testDeskproServiceName,
      showPhaseBanner = true,
      showBackButtons = true,
      includeHMRCBranding = true,
      ukMode = true,
      selectPageConfig = AddressLookupSelectConfigModel(
        showSearchAgainLink = true
      ),
      confirmPageConfig = AddressLookupConfirmConfigModel(
        showChangeLinkcontinueUrl = true,
        showSubHeadingAndInfo = false,
        showSearchAgainLink = true,
        showConfirmChangeText = false
      )
    ),
    labels = AddressMessageLanguageModel(
      en = AddressMessagesModel(
        appLevelLabels = testAppLevelMessages,
        lookupPageLabels = testLookupMessages,
        selectPageLabels = testSelectMessages,
        editPageLabels = testEditMessages,
        confirmPageLabels = testConfirmMessages
      ),
      cy = AddressMessagesModel(
        appLevelLabels = testAppLevelMessages,
        lookupPageLabels = testLookupMessages,
        selectPageLabels = testSelectMessages,
        editPageLabels = testEditMessages,
        confirmPageLabels = testConfirmMessages
      )
    )
  )

}
