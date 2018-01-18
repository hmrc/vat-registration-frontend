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

import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

package services.frs {

  import com.google.inject.Inject
  import common.ErrorUtil.fail
  import connectors.RegistrationConnector
  import features.turnoverEstimates.TurnoverEstimatesService
  import models._
  import models.api.{VatFlatRateScheme, VatScheme}
  import models.view.frs.AnnualCostsLimitedView.{NO, YES, YES_WITHIN_12_MONTHS}
  import models.view.frs._
  import services.{RegistrationService, S4LService}
  import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

  class FlatRateServiceImpl @Inject()(val turnoverEstimateService: TurnoverEstimatesService,
                                      val s4LService: S4LService,
                                      val vatService: RegistrationService,
                                      val vatRegConnector: RegistrationConnector) extends FlatRateService

  trait FlatRateService  {
    protected val turnoverEstimateService : TurnoverEstimatesService
    val s4LService: S4LService
    val vatService: RegistrationService
    val vatRegConnector: RegistrationConnector

    private val flatRateSchemeS4LKey: S4LKey[S4LFlatRateScheme] = S4LFlatRateScheme.vatFlatRateScheme
    private val LIMITED_COST_TRADER_THRESHOLD                   = 1000L

    type SavedFlatRateScheme = Either[S4LFlatRateScheme, VatFlatRateScheme]

    def getFlatRateSchemeThreshold()(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Long] = {
      turnoverEstimateService.fetchTurnoverEstimates map {
          case Some(estimate) => Math.round(estimate.vatTaxable * 0.02)
          case None           => 0L
        }
    }

    def fetchFlatRateScheme(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[S4LFlatRateScheme] = {
      fetchFRSFromS4L flatMap {
        case Some(frs) => Future.successful(frs)
        case None      => vatService.getVatScheme map { vatScheme =>
          vatScheme.vatFlatRateScheme match {
            case Some(_) => frsApiToView(vatScheme)
            case None    => S4LFlatRateScheme()
          }
        }
      }
    }

    def saveJoinFRS(joinFRSView: JoinFrsView)(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {
      if (joinFRSView.selection) {
        fetchFlatRateScheme flatMap { frs =>
          saveFRS(frs.copy(joinFrs = Some(joinFRSView)))
        }
      } else {
        saveFRStoAPI(frsViewToApi(S4LFlatRateScheme(joinFrs = Some(joinFRSView)))) map Right.apply
      }
    }

    def saveStartDate(startDateView: FrsStartDateView)(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {

      def resolveStartDate(startDateView: FrsStartDateView) = {
        startDateView.dateType match {
          case FrsStartDateView.VAT_REGISTRATION_DATE =>
            // TODO - AUX start date logic used viewModel[StartDateView] to get start date from trading details - this needs validating as the right way to do it :-(
            // Default to existing value for now
            Future.successful(startDateView)
          case _ => Future.successful(startDateView)
        }
      }

      for {
        frs          <- fetchFlatRateScheme
        resolvedView <- resolveStartDate(startDateView)
        savedFRS     <- saveFRS(frs.copy(frsStartDate = Some(resolvedView)))
      } yield savedFRS
    }

    def saveAnnualCostsInclusive(annualCostsInclusive: AnnualCostsInclusiveView)
                                (implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {
      saveFRS(S4LFlatRateScheme(joinFrs = Some(JoinFrsView(true)), annualCostsInclusive = Some(annualCostsInclusive)))
    }

    def saveAnnualCostsLimited(annualCostsLimitedView: AnnualCostsLimitedView)
                              (implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {
      fetchFlatRateScheme flatMap { frs =>
        annualCostsLimitedView.selection match {
          case NO                         => saveFRS(frs.copy(annualCostsLimited = Some(annualCostsLimitedView)))
          case YES | YES_WITHIN_12_MONTHS => saveFRS(frs.copy(annualCostsLimited = Some(annualCostsLimitedView), frsStartDate = None, categoryOfBusiness = None))
        }
      }
    }

    def isOverLimitedCostTraderThreshold(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Boolean] = {
      getFlatRateSchemeThreshold map (_ > LIMITED_COST_TRADER_THRESHOLD)
    }

    @Deprecated
    // TODO remove once other FRS controllers are refactored to not use this
    def submitVatFlatRateScheme()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[VatFlatRateScheme] = {
      def merge(fresh: Option[S4LFlatRateScheme], vs: VatScheme): VatFlatRateScheme = fresh.fold(
        vs.vatFlatRateScheme.getOrElse(throw fail("VatFlatRateScheme"))
      )(s4l => S4LFlatRateScheme.apiT.toApi(s4l))

      for {
        vs        <- vatService.getVatScheme
        frs       <- vatService.s4l[S4LFlatRateScheme] // TODO ??? s4L via service?
        response  <- vatRegConnector.upsertVatFlatRateScheme(profile.registrationId, merge(frs, vs))
      } yield response
    }

    private[services] def frsApiToView(vs: VatScheme): S4LFlatRateScheme = S4LFlatRateScheme(
      joinFrs               = ApiModelTransformer[JoinFrsView].toViewModel(vs),
      annualCostsInclusive  = ApiModelTransformer[AnnualCostsInclusiveView].toViewModel(vs),
      annualCostsLimited    = ApiModelTransformer[AnnualCostsLimitedView].toViewModel(vs),
      registerForFrs        = ApiModelTransformer[RegisterForFrsView].toViewModel(vs),
      frsStartDate          = ApiModelTransformer[FrsStartDateView].toViewModel(vs),
      categoryOfBusiness    = ApiModelTransformer[BusinessSectorView].toViewModel(vs)
    )

    private[services] def frsViewToApi(view: S4LFlatRateScheme): VatFlatRateScheme = VatFlatRateScheme(
      joinFrs                 = view.joinFrs.exists(_.selection),
      annualCostsInclusive    = view.annualCostsInclusive.map(_.selection),
      annualCostsLimited      = view.annualCostsLimited.map(_.selection),
      doYouWantToUseThisRate  = view.registerForFrs.map(_.selection),
      whenDoYouWantToJoinFrs  = view.frsStartDate.map(_.dateType),
      startDate               = view.frsStartDate.flatMap(_.date),
      categoryOfBusiness      = view.categoryOfBusiness.map(_.businessSector),
      percentage              = view.categoryOfBusiness.map(_.flatRatePercentage)
    )

    private[services] def fetchFRSFromS4L(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Option[S4LFlatRateScheme]] = {
      s4LService.fetchAndGetNoAux(flatRateSchemeS4LKey)
    }

    private[services] def fetchFRSFromAPI(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Option[VatFlatRateScheme]] = {
      vatService.getVatScheme map (_.vatFlatRateScheme)
    }

    private[services] def saveFRS(frs: S4LFlatRateScheme)(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {
      // TODO defaulting to S4L
      // TODO check needed to determine whether block is full is unreliable - awaiting API changes
      // TODO (options with no way of identifying whether they should be populated)
      // TODO on save to backend, wipe S4L for safety like PAYE?
      saveFRSToS4L(frs) map Left.apply
    }

    private[services] def saveFRSToS4L(frsView: S4LFlatRateScheme)(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[S4LFlatRateScheme] = {
      s4LService.saveNoAux[S4LFlatRateScheme](frsView, flatRateSchemeS4LKey) map (_ => frsView)
    }

    private[services] def saveFRStoAPI(frs: VatFlatRateScheme)(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[VatFlatRateScheme] = {
      vatRegConnector.upsertVatFlatRateScheme(profile.registrationId, frs)
    }
  }
}
