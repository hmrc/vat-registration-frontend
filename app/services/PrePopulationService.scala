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
import com.google.inject.ImplementedBy
import connectors.{OptionalResponse, PPConnector}
import models.api.ScrsAddress
import models.external.Officer
import models.view.vatLodgingOfficer.CompletionCapacityView
import models.{ApiModelTransformer, S4LVatLodgingOfficer}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[PrePopulationService])
trait PrePopService {

  def getCTActiveDate()(implicit hc: HeaderCarrier): OptionalResponse[LocalDate]

  def getOfficerAddressList()(implicit hc: HeaderCarrier): Future[Seq[ScrsAddress]]

  def getOfficerList()(implicit hc: HeaderCarrier, transformer: ApiModelTransformer[CompletionCapacityView]): Future[Seq[Officer]]

}

class PrePopulationService @Inject()(ppConnector: PPConnector, iis: IncorporationInformationService, s4l: S4LService)
                                    (implicit vrs: VatRegistrationService)
  extends PrePopService with CommonService {

  import cats.instances.future._

  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def getCTActiveDate()(implicit hc: HeaderCarrier): OptionalResponse[LocalDate] =
    for {
      regId <- OptionT.liftF(fetchRegistrationId)
      ctReg <- ppConnector.getCompanyRegistrationDetails(regId)
      accountingDetails <- fromOption(ctReg.accountingDetails)
      dateString <- fromOption(accountingDetails.activeDate)
    } yield LocalDate.parse(dateString, formatter)

  override def getOfficerAddressList()(implicit hc: HeaderCarrier): Future[Seq[ScrsAddress]] = {
    import cats.instances.list._
    import cats.syntax.traverse._
    val addressFromII = iis.getRegisteredOfficeAddress()
    val addressFromBE = OptionT(vrs.getVatScheme() map ApiModelTransformer[ScrsAddress].toViewModel)
    val addressFromS4L = OptionT(s4l.fetchAndGet[S4LVatLodgingOfficer]()).subflatMap { group =>
      group.officerHomeAddress.flatMap(_.address)
    }

    List(addressFromII, addressFromBE, addressFromS4L).traverse(_.value).map(_.flatten.distinct)

    // TODO merge addresses from PrePop service
    // TODO order the addresses
  }

  override def getOfficerList()(implicit hc: HeaderCarrier, transformer: ApiModelTransformer[CompletionCapacityView]): Future[Seq[Officer]] = {
    import cats.syntax.cartesian._
    val officerFromBE = (vrs.getVatScheme() map transformer.toViewModel).map(_.flatMap(_.officer))
    val officerFromS4L = OptionT(s4l.fetchAndGet[S4LVatLodgingOfficer]()).subflatMap(_.completionCapacity.flatMap(_.officer)).value
    (iis.getOfficerList() |@| officerFromBE |@| officerFromS4L).map(_ ++ _ ++ _).map(_.distinct)
  }

}
