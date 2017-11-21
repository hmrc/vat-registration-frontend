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
  import models.{ApiModelTransformer, CurrentProfile, S4LKey, S4LTradingDetails}
  import models.api.{VatScheme, VatTradingDetails}
  import models.view.frs.FrsStartDateView
  import models.view.vatTradingDetails.TradingNameView
  import models.view.vatTradingDetails.vatChoice.StartDateView
  import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}

  trait TradingDetailsService extends CommonService {
    self: RegistrationService =>

    private val tradingDetailsS4LKey: S4LKey[S4LTradingDetails] = S4LKey[S4LTradingDetails]("VatTradingDetails")

    def fetchTradingDetails(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[S4LTradingDetails] = {
      fetchTradingDetailsFromS4L flatMap {
        case Some(tradingDetails) => Future.successful(tradingDetails)
        case None => getVatScheme map { vatScheme =>
          vatScheme.tradingDetails match {
            case Some(_) => tradingDetailsApiToView(vatScheme)
            case None => S4LTradingDetails()
          }
        }
      }
    }

    def saveFRSStartDateAsVatRegistrationDate(frsStartDate: FrsStartDateView)
                                            (implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {
      fetchTradingDetails flatMap {
        _.startDate match {
          case Some(vatStartDate) => saveFRSStartDate(Some(frsStartDate.copy(date = vatStartDate.date)))
          case None => throw new IllegalStateException("VAT start date should exist here") // TODO should be here
        }
      }
    }

    def submitTradingDetails()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[VatTradingDetails] = {
      def merge(fresh: Option[S4LTradingDetails], vs: VatScheme): VatTradingDetails =
        fresh.fold(
          vs.tradingDetails.getOrElse(throw fail("VatTradingDetails"))
        )(s4l => S4LTradingDetails.apiT.toApi(s4l))

      for {
        vs       <- getVatScheme()
        vlo      <- s4l[S4LTradingDetails]()
        response <- vatRegConnector.upsertVatTradingDetails(profile.registrationId, merge(vlo, vs))
      } yield response
    }

    private[services] def tradingDetailsApiToView(vs: VatScheme): S4LTradingDetails = {
      S4LTradingDetails(
        tradingName = ApiModelTransformer[TradingNameView].toViewModel(vs),
        startDate = ApiModelTransformer[StartDateView].toViewModel(vs),
        euGoods = ApiModelTransformer[EuGoods].toViewModel(vs),
        applyEori = ApiModelTransformer[ApplyEori].toViewModel(vs)
      )
    }

    private[services] def fetchTradingDetailsFromS4L(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Option[S4LTradingDetails]] = {
      s4LService.fetchAndGetNoAux(tradingDetailsS4LKey)
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