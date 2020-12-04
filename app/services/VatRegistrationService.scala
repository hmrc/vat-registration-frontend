/*
 * Copyright 2020 HM Revenue & Customs
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

import common.enums.VatRegStatus
import connectors._
import javax.inject.{Inject, Singleton}
import models.api._
import models.{TurnoverEstimates, _}
import play.api.Logger
import play.api.libs.json.{Format, JsObject}
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatRegistrationService @Inject()(val s4LService: S4LService,
                                       val vatRegConnector: VatRegistrationConnector,
                                       val keystoreConnector: KeystoreConnector
                                      )(implicit ec: ExecutionContext) {

  type RegistrationFootprint = (String, String)

  private[services] def s4l[T: Format : S4LKey](implicit hc: HeaderCarrier, profile: CurrentProfile) =
    s4LService.fetchAndGet[T]

  def getVatScheme(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[VatScheme] =
    vatRegConnector.getRegistration(profile.registrationId)

  def getAckRef(regId: String)(implicit hc: HeaderCarrier): Future[String] = vatRegConnector.getAckRef(regId)

  def getTaxableThreshold(date: LocalDate = LocalDate.now())(implicit hc: HeaderCarrier): Future[String] = {
    vatRegConnector.getTaxableThreshold(date) map { taxableThreshold =>
      "%,d".format(taxableThreshold.threshold.toInt)
    }
  }

  def deleteVatScheme(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Boolean] =
    vatRegConnector.deleteVatScheme(profile.registrationId)

  def createRegistrationFootprint(implicit hc: HeaderCarrier): Future[String] = {
    Logger.info("[createRegistrationFootprint] Creating registration footprint")
    for {
      vatScheme <- vatRegConnector.createNewRegistration
    } yield {
      vatScheme.id
    }
  }

  def getStatus(regId: String)(implicit hc: HeaderCarrier): Future[VatRegStatus.Value] = vatRegConnector.getStatus(regId)

  def getEligibilityData(implicit hc: HeaderCarrier, cp: CurrentProfile): Future[JsObject] = vatRegConnector.getEligibilityData

  def submitRegistration()(implicit hc: HeaderCarrier, profile: CurrentProfile, request: Request[_]): Future[DESResponse] = {
    for {
      submit <- vatRegConnector.submitRegistration(profile.registrationId, request.headers.toSimpleMap)
    } yield submit

  } recover {
    case e =>
      SubmissionFailedRetryable

  }

  def getThreshold(regId: String)(implicit hc: HeaderCarrier): Future[Threshold] =
    vatRegConnector.getThreshold(regId) map (_.getOrElse(throw new IllegalStateException(s"No threshold block found in the back end for regId: $regId")))

  def fetchTurnoverEstimates(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Option[TurnoverEstimates]] = {
    vatRegConnector.getTurnoverEstimates
  }
}