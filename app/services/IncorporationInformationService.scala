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

import javax.inject.Inject

import cats.data.OptionT
import com.google.inject.ImplementedBy
import connectors.{IncorporationInformationConnector, VatRegistrationConnector, OptionalResponse}
import models.api.ScrsAddress
import models.external.{CoHoCompanyProfile, Officer, IncorporationInfo}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[IncorporationInformationService])
trait IncorpInfoService {
  def getRegisteredOfficeAddress()(implicit hc: HeaderCarrier): OptionalResponse[ScrsAddress]

  def getOfficerList()(implicit headerCarrier: HeaderCarrier): Future[Seq[Officer]]

  def getIncorporationInfo()(implicit headerCarrier: HeaderCarrier): OptionalResponse[IncorporationInfo]
}

class IncorporationInformationService @Inject()(iiConnector: IncorporationInformationConnector, vatRegConnector: VatRegistrationConnector)
  extends IncorpInfoService with CommonService {

  private def companyProfile()(implicit hc: HeaderCarrier) =
    OptionT(keystoreConnector.fetchAndGet[CoHoCompanyProfile]("CompanyProfile"))

  override def getRegisteredOfficeAddress()(implicit hc: HeaderCarrier): OptionalResponse[ScrsAddress] =
    for {
      profile <- companyProfile()
      address <- iiConnector.getRegisteredOfficeAddress(profile.transactionId)
    } yield address: ScrsAddress // implicit conversion

  override def getOfficerList()(implicit hc: HeaderCarrier): Future[Seq[Officer]] =
    (for {
      profile <- companyProfile()
      officerList <- iiConnector.getOfficerList(profile.transactionId)
    } yield officerList.items).getOrElse(Seq.empty[Officer])

  def getIncorporationInfo()(implicit headerCarrier: HeaderCarrier): OptionalResponse[IncorporationInfo] =
    companyProfile().flatMap(profile => vatRegConnector.getIncorporationInfo(profile.transactionId))

}
