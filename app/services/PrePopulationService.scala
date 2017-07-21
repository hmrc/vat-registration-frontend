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
import javax.inject.Inject

import cats.data.OptionT
import cats.data.OptionT.fromOption
import cats.instances.ListInstances
import cats.syntax.TraverseSyntax
import com.google.inject.ImplementedBy
import connectors.{OptionalResponse, PPConnector}
import models.api._
import models.external.Officer
import models.view.vatLodgingOfficer.OfficerView
import models.{ApiModelTransformer, S4LKey, S4LPpob, S4LVatLodgingOfficer}
import play.api.libs.json.Format
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[PrePopulationService])
trait PrePopService {

  def getCTActiveDate()(implicit hc: HeaderCarrier): OptionalResponse[LocalDate]

  def getOfficerAddressList()(implicit hc: HeaderCarrier): Future[Seq[ScrsAddress]]

  def getPpobAddressList()(implicit hc: HeaderCarrier): Future[Seq[ScrsAddress]]

  def getOfficerList()(implicit hc: HeaderCarrier): Future[Seq[Officer]]

}

class PrePopulationService @Inject()(ppConnector: PPConnector, iis: IncorporationInformationService, s4l: S4LService)
                                    (implicit vrs: VatRegistrationService)
  extends PrePopService with CommonService with TraverseSyntax with ListInstances {

  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def getCTActiveDate()(implicit hc: HeaderCarrier): OptionalResponse[LocalDate] =
    for {
      regId <- OptionT.liftF(fetchRegistrationId)
      ctReg <- ppConnector.getCompanyRegistrationDetails(regId)
      accountingDetails <- fromOption[Future](ctReg.accountingDetails)
      dateString <- fromOption[Future](accountingDetails.activeDate)
    } yield LocalDate.parse(dateString, formatter)

  private def getAddresses[T: S4LKey : Format]
  (s4lExtractor: T => Option[ScrsAddress])
  (implicit mt: ApiModelTransformer[ScrsAddress], hc: HeaderCarrier): Future[Seq[ScrsAddress]] =
    List(
      iis.getRegisteredOfficeAddress(),
      OptionT(vrs.getVatScheme() map mt.toViewModel),
      OptionT(s4l.fetchAndGet[T]()).subflatMap(s4lExtractor)
    ).traverse(_.value).map(_.flatten.distinct)

  override def getOfficerAddressList()(implicit hc: HeaderCarrier): Future[Seq[ScrsAddress]] = {
    import ScrsAddress.modelTransformerOfficerHomeAddressView
    getAddresses((_: S4LVatLodgingOfficer).officerHomeAddress.flatMap(_.address))
  }

  override def getPpobAddressList()(implicit hc: HeaderCarrier): Future[Seq[ScrsAddress]] = {
    import ScrsAddress.modelTransformerPpobView
    getAddresses((_: S4LPpob).address.flatMap(_.address))
  }

  override def getOfficerList()(implicit hc: HeaderCarrier): Future[Seq[Officer]] = {
    val officerListFromII = iis.getOfficerList()
    val officerFromS4L = OptionT(s4l.fetchAndGet[S4LVatLodgingOfficer]()).subflatMap(s4l =>
      for {
        completionCapacityView <- s4l.completionCapacity
        cc <- completionCapacityView.completionCapacity
      } yield Officer(cc.name, cc.role, s4l.officerSecurityQuestions.map(_.dob).map(DateOfBirth.apply)))
    val officerFromBE = OptionT(vrs.getVatScheme() map ApiModelTransformer[OfficerView].toViewModel).map(_.officer)

    for {
      ii <- officerListFromII
      s4l <- officerFromS4L.value
      be <- officerFromBE.value
    } yield (ii.map(Option.apply) :+ s4l :+ be).flatten.distinct
  }

}
