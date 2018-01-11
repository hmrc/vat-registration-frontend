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

import models._
import models.api.VatScheme
import models.view.sicAndCompliance._
import models.view.sicAndCompliance.labour._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

trait SicAndComplianceService {
  self: RegistrationService =>

  private val sicAndComplianceS4LKey: S4LKey[S4LVatSicAndCompliance] = S4LKey.sicAndCompliance

  def fetchSicAndCompliance(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[S4LVatSicAndCompliance] = {
    fetchSicFromS4L flatMap {
      case Some(sic) => Future.successful(sic)
      case None => getVatScheme map { vatScheme =>
        vatScheme.vatSicAndCompliance match {
          case Some(_) => sicApiToS4L(vatScheme)
          case None => S4LVatSicAndCompliance()
        }
      }
    }
  }

  private[services] def sicApiToS4L(vs: VatScheme): S4LVatSicAndCompliance = S4LVatSicAndCompliance(
    description                               = ApiModelTransformer[BusinessActivityDescription].toViewModel(vs),
    mainBusinessActivity                      = ApiModelTransformer[MainBusinessActivityView].toViewModel(vs),
    companyProvideWorkers                     = ApiModelTransformer[CompanyProvideWorkers].toViewModel(vs),
    workers                                   = ApiModelTransformer[Workers].toViewModel(vs),
    temporaryContracts                        = ApiModelTransformer[TemporaryContracts].toViewModel(vs),
    skilledWorkers                            = ApiModelTransformer[SkilledWorkers].toViewModel(vs))

  private[services] def fetchSicFromS4L(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Option[S4LVatSicAndCompliance]] = {
    s4LService.fetchAndGetNoAux(sicAndComplianceS4LKey)
  }
}
