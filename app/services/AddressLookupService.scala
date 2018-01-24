/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.Inject

import com.typesafe.config.Config
import common.enums.AddressLookupJourneyIdentifier
import connectors.AddressLookupConnector
import models.api.ScrsAddress
import models.external.addresslookup._
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class AddressLookupServiceImpl @Inject()(val addressLookupConnector: AddressLookupConnector, config: Configuration) extends AddressLookupService {
  lazy val addressConfig            = config.underlying.getConfig("address-journeys")
  lazy val addressLookupContinueUrl = config.underlying.getString("microservice.services.address-lookup-frontend.new-address-callback.url")
}

trait AddressLookupService {
  val addressLookupConnector: AddressLookupConnector

  val addressLookupContinueUrl: String

  val addressConfig: Config

  def getAddressById(id: String)(implicit hc: HeaderCarrier): Future[ScrsAddress] = addressLookupConnector.getAddress(id)

  def getJourneyUrl(journeyId: AddressLookupJourneyIdentifier.Value, continueUrl: Call)(implicit hc: HeaderCarrier, messagesApi: MessagesApi): Future[Call] = {
    val qwe = buildJourneyJson(continueUrl, journeyId)
    addressLookupConnector.getOnRampUrl(qwe)
  }

  def buildJourneyJson(continueUrl: Call, journeyId: AddressLookupJourneyIdentifier.Value)(implicit messagesApi: MessagesApi) = AddressJourneyBuilder(
    continueUrl = s"$addressLookupContinueUrl${continueUrl.url}",
    homeNavHref = messagesApi("addressLookup.common.homeNavHref"),
    navTitle = messagesApi("addressLookup.common.navTitle"),
    showPhaseBanner = addressConfig.getBoolean("common.showPhaseBanner"),
    alphaPhase = addressConfig.getBoolean("common.alphaPhase"),
    phaseBannerHtml = messagesApi("addressLookup.common.phaseBannerHtml"),
    includeHMRCBranding = addressConfig.getBoolean("common.includeHMRCBranding"),
    showBackButtons = addressConfig.getBoolean("common.showBackButtons"),
    deskProServiceName = messagesApi("addressLookup.common.deskProServiceName"),
    ukMode = addressConfig.getBoolean(s"$journeyId.ukMode"),
    lookupPage = buildLookupPageSegment(journeyId),
    selectPage = buildSelectPageSegment(journeyId),
    editPage = buildEditPageSegment(journeyId),
    confirmPage = buildConfirmPageSegment(journeyId)
  )

  private def buildLookupPageSegment(journeyId: AddressLookupJourneyIdentifier.Value)(implicit messagesApi: MessagesApi): LookupPage = LookupPage(
    title = messagesApi(s"addressLookup.$journeyId.lookupPage.title"),
    heading = messagesApi(s"addressLookup.$journeyId.lookupPage.heading"),
    filterLabel = messagesApi(s"addressLookup.$journeyId.lookupPage.filterLabel"),
    submitLabel = messagesApi(s"addressLookup.$journeyId.lookupPage.submitLabel"),
    manualAddressLinkText = messagesApi(s"addressLookup.$journeyId.lookupPage.manualAddressLinkText")
  )

  private def buildSelectPageSegment(journeyId: AddressLookupJourneyIdentifier.Value)(implicit messagesApi: MessagesApi): SelectPage = {
    val selectPageShowSearchAgain    = addressConfig.getBoolean(s"$journeyId.selectPage.showSearchAgainLink")
    SelectPage(
      title = messagesApi(s"addressLookup.$journeyId.selectPage.title"),
      heading = messagesApi(s"addressLookup.$journeyId.selectPage.heading"),
      proposalListLimit = addressConfig.getInt(s"$journeyId.selectPage.proposalListLimit"),
      showSearchAgainLink = addressConfig.getBoolean(s"$journeyId.selectPage.showSearchAgainLink"),
      searchAgainLinkText = if(selectPageShowSearchAgain) Some(messagesApi(s"addressLookup.$journeyId.selectPage.searchAgainLinkText")) else None,
      editAddressLinkText = messagesApi(s"addressLookup.$journeyId.selectPage.editAddressLinkText")
    )
  }

  private def buildEditPageSegment(journeyId: AddressLookupJourneyIdentifier.Value)(implicit messagesApi: MessagesApi): EditPage = {
    val editPageShowSearchAgain      = addressConfig.getBoolean(s"$journeyId.editPage.showSearchAgainLink")
    EditPage(
      title = messagesApi(s"addressLookup.$journeyId.editPage.title"),
      heading = messagesApi(s"addressLookup.$journeyId.editPage.heading"),
      line1Label = messagesApi(s"addressLookup.$journeyId.editPage.line1Label"),
      line2Label = messagesApi(s"addressLookup.$journeyId.editPage.line2Label"),
      line3Label = messagesApi(s"addressLookup.$journeyId.editPage.line3Label"),
      postcodeLabel = messagesApi(s"addressLookup.$journeyId.editPage.postcodeLabel"),
      countryLabel = messagesApi(s"addressLookup.$journeyId.editPage.countryLabel"),
      submitLabel = messagesApi(s"addressLookup.$journeyId.editPage.submitLabel"),
      showSearchAgainLink = editPageShowSearchAgain,
      searchAgainLinkText = if(editPageShowSearchAgain) Some(messagesApi(s"addressLookup.$journeyId.editPage.searchAgainLinkText")) else None
    )
  }

  private def buildConfirmPageSegment(journeyId: AddressLookupJourneyIdentifier.Value)(implicit messagesApi: MessagesApi): ConfirmPage = {
    val confirmPageShowChangeLink    = addressConfig.getBoolean(s"$journeyId.confirmPage.showSearchAgainLink")
    ConfirmPage(
      title = messagesApi(s"addressLookup.$journeyId.confirmPage.title"),
      heading = messagesApi(s"addressLookup.$journeyId.confirmPage.heading"),
      showSubHeadingAndInfo = addressConfig.getBoolean(s"$journeyId.confirmPage.showSubHeadingAndInfo"),
      submitLabel = messagesApi(s"addressLookup.$journeyId.confirmPage.submitLabel"),
      showSearchAgainLink = addressConfig.getBoolean(s"$journeyId.confirmPage.showSearchAgainLink"),
      showChangeLink = confirmPageShowChangeLink,
      changeLinkText = if(confirmPageShowChangeLink) Some(messagesApi(s"addressLookup.$journeyId.confirmPage.changeLinkText")) else None
    )
  }
}
