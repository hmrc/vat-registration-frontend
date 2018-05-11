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

import java.time.LocalDate
import javax.inject.Inject

import connectors._
import models.CurrentProfile
import models.api.ScrsAddress
import models.external.{CoHoRegisteredOfficeAddress, IncorporationInfo, Officer}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class IncorporationInformationServiceImpl @Inject()(val iiConnector: IncorporationInformationConnector,
                                                    val vatRegConnector: RegistrationConnector) extends IncorporationInformationService

trait IncorporationInformationService {
  val iiConnector: IncorporationInformationConnector
  val vatRegConnector: RegistrationConnector

  def getRegisteredOfficeAddress(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Option[ScrsAddress]] = {
    iiConnector.getRegisteredOfficeAddress(profile.transactionId) map {
      _.map(CoHoRegisteredOfficeAddress.convertToAddress)
    }
  }

  def getOfficerList(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Seq[Officer]] = {
    iiConnector.getOfficerList(profile.transactionId) map {
      _.fold[Seq[Officer]](Seq())(_.items)
    }
  }

  def getCompanyName(regId: String, txId: String)(implicit hc: HeaderCarrier): Future[String] = {
    iiConnector.getCompanyName(regId, txId) map(_.\("company_name").as[String])
  }

  def getIncorpDate(regId: String, txId: String)(implicit headerCarrier: HeaderCarrier): Future[Option[LocalDate]] = {
    iiConnector.getIncorpUpdate(regId, txId) map {
      case Some(json) => (json \ "incorporationDate").asOpt[LocalDate]
      case _          => None
    }
  }

  def registerInterest(regId: String, txId: String)(implicit headerCarrier: HeaderCarrier): Future[Boolean] = {
    iiConnector.registerInterest(regId, txId) flatMap { _.fold (
        rejected => vatRegConnector.saveTransactionId(regId, txId) flatMap (_ =>
          vatRegConnector.clearVatScheme(txId) map (_ => true)
        ),
        accepted => vatRegConnector.saveTransactionId(regId, txId) map (_ => true)
      )
    }
  }
}
