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

import com.google.inject.ImplementedBy
import connectors.{IncorporationInformationConnector, OptionalResponse, VatRegistrationConnector}
import models.CurrentProfile
import models.api.ScrsAddress
import models.external.{IncorporationInfo, Officer}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[IncorporationInformationService])
trait IncorpInfoService {
  def getRegisteredOfficeAddress()(implicit hc: HeaderCarrier, profile: CurrentProfile): OptionalResponse[ScrsAddress]

  def getOfficerList()(implicit headerCarrier: HeaderCarrier, profile: CurrentProfile): Future[Seq[Officer]]

  def getIncorporationInfo(txId: String)(implicit headerCarrier: HeaderCarrier): OptionalResponse[IncorporationInfo]

  def getCompanyName(regId: String, txId: String)(implicit hc: HeaderCarrier): Future[String]
}

class IncorporationInformationService @Inject()(iiConnector: IncorporationInformationConnector, vatRegConnector: VatRegistrationConnector)
  extends IncorpInfoService with CommonService {

  override def getRegisteredOfficeAddress()(implicit hc: HeaderCarrier, profile: CurrentProfile): OptionalResponse[ScrsAddress] =
    for {
      address <- iiConnector.getRegisteredOfficeAddress(profile.transactionId)
    } yield address: ScrsAddress // implicit conversion

  override def getOfficerList()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Seq[Officer]] =
    (for {
      officerList <- iiConnector.getOfficerList(profile.transactionId)
    } yield officerList.items).getOrElse(Seq.empty[Officer])

  def getCompanyName(regId: String, txId: String)(implicit hc: HeaderCarrier): Future[String] = {
    iiConnector.getCompanyName(regId, txId) map(_.\("company_name").as[String])
  }

  def getIncorporationInfo(txId: String)(implicit headerCarrier: HeaderCarrier): OptionalResponse[IncorporationInfo] =
    vatRegConnector.getIncorporationInfo(txId)
}
