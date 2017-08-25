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

import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

package services {

  import common.ErrorUtil.fail
  import models.S4LTradingDetails
  import models.api.{VatScheme, VatTradingDetails}

  trait TradingDetailsService extends CommonService {

    self: RegistrationService =>

    import cats.syntax.all._

    def submitTradingDetails()(implicit hc: HeaderCarrier): Future[VatTradingDetails] = {
      def merge(fresh: Option[S4LTradingDetails], vs: VatScheme): VatTradingDetails =
        fresh.fold(
          vs.tradingDetails.getOrElse(throw fail("VatTradingDetails"))
        )(s4l => S4LTradingDetails.apiT.toApi(s4l))

      for {
        (vs, vlo) <- (getVatScheme() |@| s4l[S4LTradingDetails]()).tupled
        response <- vatRegConnector.upsertVatTradingDetails(vs.id, merge(vlo, vs))
      } yield response
    }
  }
}

package connectors {

  import cats.instances.FutureInstances
  import models.api.{VatChoice, VatTradingDetails}
  import uk.gov.hmrc.play.http.HttpReads

  import scala.concurrent.Future

  trait TradingDetailsConnector extends FutureInstances {
    self: RegistrationConnector =>

    def upsertVatTradingDetails(regId: String, vatTradingDetails: VatTradingDetails)
                               (implicit hc: HeaderCarrier, rds: HttpReads[VatTradingDetails]): Future[VatTradingDetails] =
      http.PATCH[VatTradingDetails, VatTradingDetails](s"$vatRegUrl/vatreg/$regId/trading-details", vatTradingDetails).recover{
        case e: Exception => throw logResponse(e, className, "upsertVatTradingDetails")
      }

    // TODO - doesn't appear to be called from anywhere ? Remove?
    def upsertVatChoice(regId: String, vatChoice: VatChoice)(implicit hc: HeaderCarrier, rds: HttpReads[VatChoice]): Future[VatChoice] =
      http.PATCH[VatChoice, VatChoice](s"$vatRegUrl/vatreg/$regId/vat-choice", vatChoice).recover{
        case e: Exception => throw logResponse(e, className, "upsertVatChoice")
      }
  }
}