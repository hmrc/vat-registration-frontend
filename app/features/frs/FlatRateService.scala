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
  import models._
  import models.api.{VatFlatRateScheme, VatScheme}
  import models.view.frs._
  import models.view.frs.AnnualCostsLimitedView.{NO, YES_WITHIN_12_MONTHS, YES}

  trait FlatRateService extends CommonService {
    self: RegistrationService =>

    private val flatRateSchemeS4LKey: S4LKey[S4LFlatRateScheme] = S4LFlatRateScheme.vatFlatRateScheme
    private val LIMITED_COST_TRADER_THRESHOLD = 1000L
    private val defaultFlatRate: BigDecimal = 16.5

    type SavedFlatRateScheme = Either[S4LFlatRateScheme, VatFlatRateScheme]

    def getFlatRateSchemeThreshold()(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Long] = {
      fetchFinancials map {
        _.estimateVatTurnover match {
          case Some(estimate) => Math.round(estimate.vatTurnoverEstimate * 0.02)
          case None => 0L
        }
      }
    }

    def fetchFlatRateScheme(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[S4LFlatRateScheme] = {
      fetchFRSFromS4L flatMap {
        case Some(frs) => Future.successful(frs)
        case None => getVatScheme map { vatScheme =>
          vatScheme.vatFlatRateScheme match {
            case Some(_) => frsApiToView(vatScheme)
            case None => S4LFlatRateScheme()
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

    def saveAnnualCostsInclusive(annualCostsInclusive: AnnualCostsInclusiveView)
                                (implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {
      if(annualCostsInclusive.selection == NO) {

      }
      saveFRS(S4LFlatRateScheme(joinFrs = Some(JoinFrsView(true)), annualCostsInclusive = Some(annualCostsInclusive)))
    }

    def saveAnnualCostsLimited(annualCostsLimitedView: AnnualCostsLimitedView)
                              (implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {
      fetchFlatRateScheme flatMap { frs =>
        annualCostsLimitedView.selection match {
          case NO => saveFRS(frs.copy(annualCostsLimited = Some(annualCostsLimitedView)))
          case YES | YES_WITHIN_12_MONTHS =>
            saveFRS(frs.copy(annualCostsLimited = Some(annualCostsLimitedView), frsStartDate = None, categoryOfBusiness = None))
        }
      }
    }

    def saveBusinessSector(businessSectorView: BusinessSectorView)
                          (implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {
      fetchFlatRateScheme flatMap { frs =>
        saveFRS(frs.copy(categoryOfBusiness = Some(businessSectorView)))
      }
    }

    def saveRegisterForFRS(registerForFrs: Boolean, sector: Option[BusinessSectorView] = None)
                          (implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {

      val businessSector = sector.fold(BusinessSectorView("", defaultFlatRate))(identity)

      def buildRegisterForFRS(frs: S4LFlatRateScheme) = {
        if(registerForFrs) {
          saveFRS(frs.copy(registerForFrs = Some(RegisterForFrsView(true))))
        } else {
          saveFRS(frs.copy(registerForFrs = None, frsStartDate = None))
        }
      }

      for {
        frs      <- fetchFlatRateScheme
        _        <- saveBusinessSector(businessSector)
        savedFRS <- buildRegisterForFRS(frs)
      } yield savedFRS
    }

    def saveFRSStartDate(frsStartDateView: FrsStartDateView)
                        (implicit profile: CurrentProfile, hc: HeaderCarrier): Future[SavedFlatRateScheme] = {

      fetchFlatRateScheme flatMap { frs =>
        if(frsStartDateView.dateType == FrsStartDateView.VAT_REGISTRATION_DATE){
          fetchVatStartDate flatMap { vatStartDate =>
            saveFRS(frs.copy(frsStartDate = Some(frsStartDateView.copy(date = vatStartDate))))
          }
        } else {
          saveFRS(frs.copy(frsStartDate = Some(frsStartDateView)))
        }
      }
    }

    def isOverLimitedCostTraderThreshold(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Boolean] = {
      getFlatRateSchemeThreshold map (_ > LIMITED_COST_TRADER_THRESHOLD)
    }

    @Deprecated
    // TODO other FRS controllers still use this
    def submitVatFlatRateScheme()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[VatFlatRateScheme] = {
      def merge(fresh: Option[S4LFlatRateScheme], vs: VatScheme): VatFlatRateScheme =
        fresh.fold(
          vs.vatFlatRateScheme.getOrElse(throw fail("VatFlatRateScheme"))
        )(s4l => S4LFlatRateScheme.apiT.toApi(s4l))

      for {
        vs <- getVatScheme()
        frs <- s4l[S4LFlatRateScheme]()
        response <- vatRegConnector.upsertVatFlatRateScheme(profile.registrationId, merge(frs, vs))
      } yield response
    }

    private[services] def frsApiToView(vs: VatScheme): S4LFlatRateScheme =
      S4LFlatRateScheme(
        joinFrs = ApiModelTransformer[JoinFrsView].toViewModel(vs),
        annualCostsInclusive = ApiModelTransformer[AnnualCostsInclusiveView].toViewModel(vs),
        annualCostsLimited = ApiModelTransformer[AnnualCostsLimitedView].toViewModel(vs),
        registerForFrs = ApiModelTransformer[RegisterForFrsView].toViewModel(vs),
        frsStartDate = ApiModelTransformer[FrsStartDateView].toViewModel(vs),
        categoryOfBusiness = ApiModelTransformer[BusinessSectorView].toViewModel(vs)
      )

    private[services] def frsViewToApi(view: S4LFlatRateScheme): VatFlatRateScheme = {
      VatFlatRateScheme(
        joinFrs = view.joinFrs.map(_.selection).getOrElse(false),
        annualCostsInclusive = view.annualCostsInclusive.map(_.selection),
        annualCostsLimited = view.annualCostsLimited.map(_.selection),
        doYouWantToUseThisRate = view.registerForFrs.map(_.selection),
        whenDoYouWantToJoinFrs = view.frsStartDate.map(_.dateType),
        startDate = view.frsStartDate.flatMap(_.date),
        categoryOfBusiness = view.categoryOfBusiness.map(_.businessSector),
        percentage = view.categoryOfBusiness.map(_.flatRatePercentage)
      )
    }

    private[services] def fetchFRSFromS4L(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Option[S4LFlatRateScheme]] = {
      s4LService.fetchAndGetNoAux(flatRateSchemeS4LKey)
    }

    private[services] def fetchFRSFromAPI(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Option[VatFlatRateScheme]] = {
      getVatScheme() map (_.vatFlatRateScheme)
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

package connectors {

  import cats.instances.FutureInstances
  import models.api.VatFlatRateScheme
  import uk.gov.hmrc.play.http.HttpReads

  trait FlatRateConnector extends FutureInstances {
    self: RegistrationConnector =>

    // TODO - check why this is here twice? update vs upsert?
    def updateVatFlatRateScheme(regId: String, vatFlatRateScheme: VatFlatRateScheme)
                               (implicit hc: HeaderCarrier, rds: HttpReads[VatFlatRateScheme]): Future[VatFlatRateScheme] =
      http.PATCH[VatFlatRateScheme, VatFlatRateScheme](s"$vatRegUrl/vatreg/$regId/flat-rate-scheme", vatFlatRateScheme).recover{
        case e: Exception => throw logResponse(e, className, "vatFlatRateScheme")
      }

    def upsertVatFlatRateScheme(regId: String, vatFrs: VatFlatRateScheme)
                               (implicit hc: HeaderCarrier, rds: HttpReads[VatFlatRateScheme]): Future[VatFlatRateScheme] =
      http.PATCH[VatFlatRateScheme, VatFlatRateScheme](s"$vatRegUrl/vatreg/$regId/flat-rate-scheme", vatFrs).recover{
        case e: Exception => throw logResponse(e, className, "upsertVatFrsAnswers")
      }
  }
}
