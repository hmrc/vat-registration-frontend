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

import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}

import scala.concurrent.Future

package services {

  import java.time.LocalDate

  import common.ErrorUtil.fail
  import models.api.{VatScheme, VatTradingDetails}
  import models.view.vatTradingDetails.TradingNameView
  import models.view.vatTradingDetails.vatChoice.StartDateView
  import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}
  import models.{ApiModelTransformer, CurrentProfile, S4LKey, S4LTradingDetails}
  import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

  trait TradingDetailsService {
    self: RegistrationService =>

    private val tradingDetailsS4LKey: S4LKey[S4LTradingDetails] = S4LKey[S4LTradingDetails]("VatTradingDetails")

    def fetchTradingDetails(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[S4LTradingDetails] = {
      fetchTradingDetailsFromS4L flatMap {
        case Some(tradingDetails) => Future.successful(tradingDetails)
        case None                 => getVatScheme map { vatScheme =>
          vatScheme.tradingDetails match {
            case Some(_) => tradingDetailsApiToView(vatScheme)
            case None    => S4LTradingDetails()
          }
        }
      }
    }

    def fetchVatStartDate(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Option[LocalDate]] = {
      fetchTradingDetails map (_.startDate flatMap (_.date))
    }

    def submitTradingDetails()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[VatTradingDetails] = {
      def merge(fresh: Option[S4LTradingDetails], vs: VatScheme): VatTradingDetails = fresh.fold(
        vs.tradingDetails.getOrElse(throw fail("VatTradingDetails"))
      )(s4l => S4LTradingDetails.apiT.toApi(s4l))

      for {
        vs       <- getVatScheme
        vlo      <- s4l[S4LTradingDetails]
        response <- vatRegConnector.upsertVatTradingDetails(profile.registrationId, merge(vlo, vs))
      } yield response
    }

    private[services] def tradingDetailsApiToView(vs: VatScheme): S4LTradingDetails = S4LTradingDetails(
      tradingName = ApiModelTransformer[TradingNameView].toViewModel(vs),
      startDate   = ApiModelTransformer[StartDateView].toViewModel(vs),
      euGoods     = ApiModelTransformer[EuGoods].toViewModel(vs),
      applyEori   = ApiModelTransformer[ApplyEori].toViewModel(vs)
    )

    private[services] def fetchTradingDetailsFromS4L(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Option[S4LTradingDetails]] = {
      s4LService.fetchAndGetNoAux(tradingDetailsS4LKey)
    }
  }
}

package connectors {

  import cats.instances.FutureInstances
  import features.tradingDetails.models.TradingDetails
  import models.api.{VatChoice, VatTradingDetails}
  import uk.gov.hmrc.http.NotFoundException
  import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

  import scala.concurrent.Future

  trait TradingDetailsConnector extends FutureInstances {
    self: RegistrationConnector =>

    def upsertVatTradingDetails(regId: String, vatTradingDetails: VatTradingDetails)
                               (implicit hc: HeaderCarrier, rds: HttpReads[VatTradingDetails]): Future[VatTradingDetails] = {
      http.PATCH[VatTradingDetails, VatTradingDetails](s"$vatRegUrl/vatreg/$regId/trading-details", vatTradingDetails).recover{
        case e: Exception => throw logResponse(e, "upsertVatTradingDetails")
      }
    }

    def getTradingDetails(regId: String)
                         (implicit hc: HeaderCarrier, rds: HttpReads[TradingDetails]): Future[Option[TradingDetails]] = {
      http.GET[TradingDetails](s"$vatRegUrl/vatreg/$regId/trading-details").map {resp =>
        Some(resp)
      } recover {
        case _: NotFoundException => None
        case e: Exception => throw logResponse(e, "getTradingDetails")
      }
    }

    def upsertTradingDetails(regId: String, tradingDetails: TradingDetails)
                            (implicit hc: HeaderCarrier, rds: HttpReads[TradingDetails]): Future[TradingDetails] = {
      http.PATCH[TradingDetails, TradingDetails](s"$vatRegUrl/vatreg/$regId/trading-details", tradingDetails).recover {
        case e: Exception => throw logResponse(e, "upsertTradingDetails")
      }
    }
  }
}
