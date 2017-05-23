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
import models.ApiModelTransformer
import models.api.ScrsAddress
import models.external.Officer
import models.view.vatLodgingOfficer.{CompletionCapacityView, OfficerHomeAddressView}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[PrePopulationService])
trait PrePopService {

  def getCTActiveDate()(implicit headerCarrier: HeaderCarrier): OptionalResponse[LocalDate]

  def getOfficerAddressList()(implicit headerCarrier: HeaderCarrier): Future[Seq[ScrsAddress]]

  def getOfficerList()(implicit headerCarrier : HeaderCarrier): Future[Seq[Officer]]

}

class PrePopulationService @Inject()(ppConnector: PPConnector, iis: IncorporationInformationService, s4l: S4LService)
                                    (implicit vrs: VatRegistrationService)
  extends PrePopService with CommonService {

  import cats.instances.future._

  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def getCTActiveDate()(implicit headerCarrier: HeaderCarrier): OptionalResponse[LocalDate] =
    for {
      regId <- OptionT.liftF(fetchRegistrationId)
      ctReg <- ppConnector.getCompanyRegistrationDetails(regId)
      accountingDetails <- fromOption(ctReg.accountingDetails)
      dateString <- fromOption(accountingDetails.activeDate)
    } yield LocalDate.parse(dateString, formatter)

  override def getOfficerAddressList()(implicit headerCarrier: HeaderCarrier): Future[Seq[ScrsAddress]] = {
    import cats.instances.list._
    import cats.syntax.traverse._
    val addressFromII = iis.getRegisteredOfficeAddress()
    val addressFromBE = OptionT(vrs.getVatScheme() map ApiModelTransformer[ScrsAddress].toViewModel)
    val addressFromS4L = OptionT(s4l.fetchAndGet[OfficerHomeAddressView]()).subflatMap(_.address)

    List(addressFromII, addressFromBE, addressFromS4L).traverse(_.value).map(_.flatten.distinct)

    // TODO merge addresses from PrePop service
    // TODO order the addresses
  }

  override def getOfficerList()(implicit headerCarrier: HeaderCarrier): Future[Seq[Officer]] = {

    val officerListFromII = iis.getOfficerList().getOrElse(Seq.empty[Officer])

    val officerListFromBE = OptionT(vrs.getVatScheme() map ApiModelTransformer[CompletionCapacityView].toViewModel).subflatMap(_.officer)
    val backEndFutureList = officerListFromBE.fold(Seq.empty[Officer])(Seq(_))

    val officerFromS4L = OptionT(s4l.fetchAndGet[CompletionCapacityView]()).subflatMap(_.officer)
    val s4lFutureList = officerFromS4L.fold(Seq.empty[Officer])(Seq(_))

    for {
      listFromII <- officerListFromII
      backEndList <- backEndFutureList
      officerS4l <- s4lFutureList
    } yield (listFromII ++ officerS4l ++ backEndList).distinct

  }

}
