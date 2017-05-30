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
import connectors.{IncorporationInformationConnector, OptionalResponse}
import models.api.{Officer, ScrsAddress}
import models.external.CoHoCompanyProfile
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

@ImplementedBy(classOf[IncorporationInformationService])
trait IncorpInfoService {
  def getRegisteredOfficeAddress()(implicit hc: HeaderCarrier): OptionalResponse[ScrsAddress]

  def getOfficerList()(implicit headerCarrier: HeaderCarrier): OptionalResponse[Seq[Officer]]
}

class IncorporationInformationService @Inject()(iiConnector: IncorporationInformationConnector)
  extends IncorpInfoService with CommonService {

  import cats.instances.future._

  override def getRegisteredOfficeAddress()(implicit headerCarrier: HeaderCarrier): OptionalResponse[ScrsAddress] = {
    for {
      companyProfile <- OptionT(keystoreConnector.fetchAndGet[CoHoCompanyProfile]("CompanyProfile"))
      address <- iiConnector.getRegisteredOfficeAddress(companyProfile.transactionId)
    } yield address: ScrsAddress // implicit conversion
  }

  override def getOfficerList()(implicit headerCarrier: HeaderCarrier): OptionalResponse[Seq[Officer]] = {
    for {
      companyProfile <- OptionT(keystoreConnector.fetchAndGet[CoHoCompanyProfile]("CompanyProfile"))
      officerList <- iiConnector.getOfficerList(companyProfile.transactionId)
    } yield officerList.items
  }
}
