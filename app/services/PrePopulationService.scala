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

import cats.Traverse
import cats.data.OptionT
import com.google.inject.ImplementedBy
import connectors.{OptionalResponse, PPConnector}
import models.ApiModelTransformer
import models.api.ScrsAddress
import models.view.vatLodgingOfficer.OfficerHomeAddressView
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[PrePopulationService])
trait PrePopService {

  def getCTActiveDate()(implicit headerCarrier: HeaderCarrier): OptionalResponse[LocalDate]

  def getOfficerAddressList()(implicit headerCarrier: HeaderCarrier): Future[Seq[ScrsAddress]]

}

class PrePopulationService @Inject()(ppConnector: PPConnector,
                                     iis: IncorporationInformationService)(implicit vrs: VatRegistrationService)
  extends PrePopService with CommonService {

  import cats.instances.future._

  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def getCTActiveDate()(implicit headerCarrier: HeaderCarrier): OptionalResponse[LocalDate] =
    for {
      regId <- OptionT.liftF(fetchRegistrationId)
      ctReg <- ppConnector.getCompanyRegistrationDetails(regId)
      accountingDetails <- OptionT.fromOption(ctReg.accountingDetails)
      dateString <- OptionT.fromOption(accountingDetails.activeDate)
    } yield LocalDate.parse(dateString, formatter)

  override def getOfficerAddressList()(implicit headerCarrier: HeaderCarrier): Future[Seq[ScrsAddress]] = {
    import cats.instances.list._

    val addressFromBE: OptionalResponse[ScrsAddress] =
      OptionT(vrs.getVatScheme() map ApiModelTransformer[OfficerHomeAddressView].toViewModel).subflatMap(_.address)
    val addressFromII: OptionalResponse[ScrsAddress] = iis.getOfficerAddressList()

    // S4L


    Traverse[List].sequence(List(addressFromBE, addressFromII).map(_.value)).map(_.flatten.distinct)

    // TODO merge addresses from PrePop service

    // TODO order the addresses
  }
}
