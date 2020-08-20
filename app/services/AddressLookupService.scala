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

package services

import com.typesafe.config.Config
import common.enums.AddressLookupJourneyIdentifier
import connectors.AddressLookupConnector
import javax.inject.{Inject, Singleton}
import models.api.ScrsAddress
import models.external.addresslookup._
import play.api.Configuration
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

@Singleton
class AddressLookupService @Inject()(val addressLookupConnector: AddressLookupConnector, config: Configuration) {
  lazy val addressConfig: Config = config.underlying.getConfig("address-journeys")
  lazy val addressLookupContinueUrl: String = config.underlying.getString("microservice.services.address-lookup-frontend.new-address-callback.url")

  def getAddressById(id: String)(implicit hc: HeaderCarrier): Future[ScrsAddress] = addressLookupConnector.getAddress(id)

  def getJourneyUrl(journeyId: AddressLookupJourneyIdentifier.Value, continueUrl: Call)(implicit hc: HeaderCarrier, messages: Messages): Future[Call] = {
    addressLookupConnector.getOnRampUrl(buildJourneyJson(continueUrl, journeyId))
  }

  def buildJourneyJson(continueUrl: Call, journeyId: AddressLookupJourneyIdentifier.Value)(implicit messages: Messages): AddressJourneyBuilder = AddressJourneyBuilder(
    continueUrl = s"$addressLookupContinueUrl${continueUrl.url}",
    homeNavHref = messages("addressLookup.common.homeNavHref"),
    navTitle = messages("addressLookup.common.navTitle"),
    showPhaseBanner = addressConfig.getBoolean("common.showPhaseBanner"),
    alphaPhase = addressConfig.getBoolean("common.alphaPhase"),
    phaseBannerHtml = messages("addressLookup.common.phaseBannerHtml"),
    includeHMRCBranding = addressConfig.getBoolean("common.includeHMRCBranding"),
    showBackButtons = addressConfig.getBoolean("common.showBackButtons"),
    deskProServiceName = messages("addressLookup.common.deskProServiceName"),
    ukMode = addressConfig.getBoolean(s"$journeyId.ukMode"),
    lookupPage = buildLookupPageSegment(journeyId),
    selectPage = buildSelectPageSegment(journeyId),
    editPage = buildEditPageSegment(journeyId),
    confirmPage = buildConfirmPageSegment(journeyId)
  )

  private def buildLookupPageSegment(journeyId: AddressLookupJourneyIdentifier.Value)(implicit messages: Messages): LookupPage = LookupPage(
    title = messages(s"addressLookup.$journeyId.lookupPage.title"),
    heading = messages(s"addressLookup.$journeyId.lookupPage.heading"),
    filterLabel = messages(s"addressLookup.$journeyId.lookupPage.filterLabel"),
    submitLabel = messages(s"addressLookup.$journeyId.lookupPage.submitLabel"),
    manualAddressLinkText = messages(s"addressLookup.$journeyId.lookupPage.manualAddressLinkText")
  )

  private def buildSelectPageSegment(journeyId: AddressLookupJourneyIdentifier.Value)(implicit messages: Messages): SelectPage = {
    val selectPageShowSearchAgain = addressConfig.getBoolean(s"$journeyId.selectPage.showSearchAgainLink")
    SelectPage(
      title = messages(s"addressLookup.$journeyId.selectPage.title"),
      heading = messages(s"addressLookup.$journeyId.selectPage.heading"),
      proposalListLimit = addressConfig.getInt(s"$journeyId.selectPage.proposalListLimit"),
      showSearchAgainLink = addressConfig.getBoolean(s"$journeyId.selectPage.showSearchAgainLink"),
      searchAgainLinkText = if (selectPageShowSearchAgain) Some(messages(s"addressLookup.$journeyId.selectPage.searchAgainLinkText")) else None,
      editAddressLinkText = messages(s"addressLookup.$journeyId.selectPage.editAddressLinkText")
    )
  }

  private def buildEditPageSegment(journeyId: AddressLookupJourneyIdentifier.Value)(implicit messages: Messages): EditPage = {
    val editPageShowSearchAgain = addressConfig.getBoolean(s"$journeyId.editPage.showSearchAgainLink")
    EditPage(
      title = messages(s"addressLookup.$journeyId.editPage.title"),
      heading = messages(s"addressLookup.$journeyId.editPage.heading"),
      line1Label = messages(s"addressLookup.$journeyId.editPage.line1Label"),
      line2Label = messages(s"addressLookup.$journeyId.editPage.line2Label"),
      line3Label = messages(s"addressLookup.$journeyId.editPage.line3Label"),
      postcodeLabel = messages(s"addressLookup.$journeyId.editPage.postcodeLabel"),
      countryLabel = messages(s"addressLookup.$journeyId.editPage.countryLabel"),
      submitLabel = messages(s"addressLookup.$journeyId.editPage.submitLabel"),
      showSearchAgainLink = editPageShowSearchAgain,
      searchAgainLinkText = if (editPageShowSearchAgain) Some(messages(s"addressLookup.$journeyId.editPage.searchAgainLinkText")) else None
    )
  }

  private def buildConfirmPageSegment(journeyId: AddressLookupJourneyIdentifier.Value)(implicit messages: Messages): ConfirmPage = {
    val confirmPageShowChangeLink = addressConfig.getBoolean(s"$journeyId.confirmPage.showSearchAgainLink")
    ConfirmPage(
      title = messages(s"addressLookup.$journeyId.confirmPage.title"),
      heading = messages(s"addressLookup.$journeyId.confirmPage.heading"),
      showSubHeadingAndInfo = addressConfig.getBoolean(s"$journeyId.confirmPage.showSubHeadingAndInfo"),
      submitLabel = messages(s"addressLookup.$journeyId.confirmPage.submitLabel"),
      showSearchAgainLink = addressConfig.getBoolean(s"$journeyId.confirmPage.showSearchAgainLink"),
      showChangeLink = confirmPageShowChangeLink,
      changeLinkText = if (confirmPageShowChangeLink) Some(messages(s"addressLookup.$journeyId.confirmPage.changeLinkText")) else None
    )
  }
}
