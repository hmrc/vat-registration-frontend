/*
 * Copyright 2017 HM Revenue & Customs
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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

import cats.data.OptionT
import cats.data.OptionT.fromOption
import cats.instances.ListInstances
import cats.syntax.TraverseSyntax
import com.google.inject.ImplementedBy
import connectors.{OptionalResponse, PPConnector}
import models._
import models.api._
import models.external.Officer
import play.api.libs.json.Format
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[PrePopulationService])
trait PrePopService {
  def getCTActiveDate()(implicit hc: HeaderCarrier, profile: CurrentProfile): OptionalResponse[LocalDate]
  def getOfficerAddressList()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Seq[ScrsAddress]]
  def getPpobAddressList()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Seq[ScrsAddress]]
  def getOfficerList()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Seq[Officer]]
}

@Singleton
class PrePopulationService @Inject()(ppc: PPConnector, iis: IncorporationInformationService, s4l: S4LService)
                                    (implicit vrs: VatRegistrationService)
  extends PrePopService with CommonService with TraverseSyntax with ListInstances {

  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val ppConnector = ppc
  val incorpInfoService = iis
  val save4later = s4l
  val vatRegService = vrs

  def getCTActiveDate()(implicit hc: HeaderCarrier, profile: CurrentProfile): OptionalResponse[LocalDate] =
    for {
      ctReg <- ppConnector.getCompanyRegistrationDetails
      accountingDetails <- fromOption[Future](ctReg.accountingDetails)
      dateString <- fromOption[Future](accountingDetails.activeDate)
    } yield LocalDate.parse(dateString, formatter)

  private def getAddresses[T: S4LKey : Format]
  (save4laterExtractor: T => Option[ScrsAddress])
  (implicit mt: ApiModelTransformer[ScrsAddress], hc: HeaderCarrier, profile: CurrentProfile): Future[Seq[ScrsAddress]] =
    List(
      incorpInfoService.getRegisteredOfficeAddress(),
      OptionT(vatRegService.getVatScheme() map mt.toViewModel),
      OptionT(save4later.fetchAndGet[T]()).subflatMap(save4laterExtractor)
    ).traverse(_.value).map(_.flatten.distinct)

  def getOfficerAddressList()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Seq[ScrsAddress]] = {
    import ScrsAddress.modelTransformerOfficerHomeAddressView
    getAddresses((_: S4LVatLodgingOfficer).officerHomeAddress.flatMap(_.address))
  }

  def getPpobAddressList()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Seq[ScrsAddress]] = {
    import ScrsAddress.modelTransformerPpobView
    getAddresses((_: S4LVatContact).ppob.flatMap(_.address))
  }

  def getOfficerList()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Seq[Officer]] = {
    incorpInfoService.getOfficerList() map (_.distinct)
  }
}
