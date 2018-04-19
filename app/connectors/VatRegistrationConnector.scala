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

package connectors

import java.time.LocalDate
import javax.inject.Inject

import common.enums.VatRegStatus
import config.WSHttp
import features.bankAccountDetails.models.BankAccount
import features.officer.models.view.LodgingOfficer
import features.returns.models.Returns
import features.sicAndCompliance.models.SicAndCompliance
import features.tradingDetails.TradingDetails
import features.turnoverEstimates.TurnoverEstimates
import frs.FlatRateScheme
import models.api._
import models.external.IncorporationInfo
import models.{CurrentProfile, TaxableThreshold}
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import utils.RegistrationWhitelist

import scala.concurrent.Future

sealed trait DESResponse
object Success extends DESResponse
object SubmissionFailed extends DESResponse
object SubmissionFailedRetryable extends DESResponse

class VatRegistrationConnector @Inject()(val http: WSHttp, conf: ServicesConfig) extends RegistrationConnector {
  lazy val vatRegUrl   = conf.baseUrl("vat-registration")
  lazy val vatRegElUrl = conf.baseUrl("vat-registration-eligibility-frontend")
}

trait RegistrationConnector extends RegistrationWhitelist {

  val vatRegUrl: String
  val vatRegElUrl: String
  val http: WSHttp

  def createNewRegistration(implicit hc: HeaderCarrier, rds: HttpReads[VatScheme]): Future[VatScheme] = {
    http.POSTEmpty[VatScheme](s"$vatRegUrl/vatreg/new").recover{
      case e => throw logResponse(e, "createNewRegistration")
    }
  }

  def getRegistration(regId: String)(implicit hc: HeaderCarrier, rds: HttpReads[VatScheme]): Future[VatScheme] = {
    http.GET[VatScheme](s"$vatRegUrl/vatreg/$regId/get-scheme").recover{
      case e => throw logResponse(e, "getRegistration")
    }
  }

  def getAckRef(regId: String)(implicit hc: HeaderCarrier): Future[String] = {
    ifRegIdNotWhitelisted(regId) {
      http.GET[String](s"$vatRegUrl/vatreg/$regId/acknowledgement-reference").recover {
        case e: Exception => throw logResponse(e, "getAckRef")
      }
    }(returnDefaultAckRef)
  }

  def getTaxableThreshold(date: LocalDate)(implicit hc: HeaderCarrier): Future[TaxableThreshold] = {
    http.GET[TaxableThreshold](s"$vatRegUrl/vatreg/threshold/$date") recover {
      case e => throw logResponse(e, "getTaxableThreshold")
    }
  }

  def getLodgingOfficer(regId: String)(implicit hc: HeaderCarrier): Future[Option[JsValue]] = {
    http.GET[HttpResponse](s"$vatRegUrl/vatreg/$regId/officer") map { response =>
      if(response.status == NO_CONTENT) None else Some(response.json)
    } recover {
      case e => throw logResponse(e, "getLodgingOfficer")
    }
  }

  def getThreshold(regId: String)(implicit hc: HeaderCarrier): Future[Option[Threshold]] = {
    http.GET[HttpResponse](s"$vatRegUrl/vatreg/$regId/threshold").map {
      result => if(result.status == NO_CONTENT) None else result.json.validateOpt[Threshold].get
    }.recover {
      case e => throw logResponse(e, "getThreshold")
    }
  }

  def patchLodgingOfficer(data: LodgingOfficer)(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[JsValue] = {
    val json = Json.toJson(data)(LodgingOfficer.apiWrites)
    http.PATCH[JsValue, JsValue](s"$vatRegUrl/vatreg/${profile.registrationId}/officer", json) map {
      _ => json
    } recover {
      case e: Exception => throw logResponse(e, "patchLodgingOfficer")
    }
  }

  def upsertPpob(regId: String, address: ScrsAddress)(implicit hc: HeaderCarrier, rds: HttpReads[ScrsAddress]): Future[ScrsAddress] = {
    http.PATCH[ScrsAddress, ScrsAddress](s"$vatRegUrl/vatreg/$regId/ppob", address).recover{
      case e: Exception => throw logResponse(e, "upsertPpob")
    }
  }

  def deleteVatScheme(regId: String)(implicit hc: HeaderCarrier, rds: HttpReads[HttpResponse]): Future[Boolean] = {
    http.DELETE[HttpResponse](s"$vatRegUrl/vatreg/$regId/delete-scheme").map(_.status == OK)
  }

  def getIncorporationInfo(regId: String, transactionId: String)(implicit hc: HeaderCarrier): Future[Option[IncorporationInfo]] = {
    ifRegIdNotWhitelisted[Option[IncorporationInfo]](regId) {
      http.GET[IncorporationInfo](s"$vatRegUrl/vatreg/incorporation-information/$transactionId").map(Some(_)).recover {
        case _ => Option.empty[IncorporationInfo]
      }
    }(returnDefaultIncorpInfo)
  }

  def deleteVREFESession(regId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.DELETE[HttpResponse](s"$vatRegElUrl/internal/$regId/delete-session")
  }

  def getStatus(regId: String)(implicit hc: HeaderCarrier, rds: HttpReads[VatScheme]): Future[VatRegStatus.Value] = {
    http.GET[JsObject](s"$vatRegUrl/vatreg/$regId/status") map { json =>
      (json \ "status").as[VatRegStatus.Value]
    } recover {
      case e: Exception => throw logResponse(e, "getStatus")
    }
  }

  def updateIVStatus(regId: String, ivData: Boolean)(implicit hc:HeaderCarrier):Future[HttpResponse] = {
    http.PATCH[JsValue, HttpResponse](s"$vatRegUrl/vatreg/$regId/update-iv-status/${ivData.toString}", Json.obj()).recover {
      case e: Exception => throw logResponse(e, "updateIVStatus")
    }
  }

  def getTurnoverEstimates(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Option[TurnoverEstimates]] = {
    http.GET[HttpResponse](s"$vatRegUrl/vatreg/${profile.registrationId}/turnover-estimates") map { res =>
      if(res.status.equals(OK)) Some(res.json.as[TurnoverEstimates]) else None
    } recover {
      case e: Exception => throw logResponse(e, "getTurnoverEstimates")
    }
  }

  def patchTurnoverEstimates(turnoverEstimates: TurnoverEstimates)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[HttpResponse] = {
    http.PATCH[TurnoverEstimates, HttpResponse](s"$vatRegUrl/vatreg/${profile.registrationId}/turnover-estimates", turnoverEstimates) recover {
      case e: Exception => throw logResponse(e, "patchTurnoverEstimates")
    }
  }

  def submitRegistration(regId:String)(implicit hc:HeaderCarrier) : Future[DESResponse] = {
    ifRegIdNotWhitelisted[DESResponse](regId) {
      http.PUT[String, HttpResponse](s"$vatRegUrl/vatreg/$regId/submit-registration", "") map {
        _.status match {
          case OK => Success
        }
      } recover {
        case e: Upstream4xxResponse => SubmissionFailed
        case _ => SubmissionFailedRetryable
      }
    }(preventSubmissionForWhitelist)
  }

  def getTradingDetails(regId: String)(implicit hc: HeaderCarrier): Future[Option[TradingDetails]] = {
    implicit val frmt = TradingDetails.apiFormat
    http.GET[HttpResponse](s"$vatRegUrl/vatreg/$regId/trading-details") map { res =>
      if(res.status.equals(OK)) Some(res.json.as[TradingDetails]) else None
    } recover {
      case e: Exception => throw logResponse(e, "getTradingDetails")
    }
  }

  def upsertTradingDetails(regId: String, tradingDetails: TradingDetails)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    implicit val frmt = TradingDetails.apiFormat
    http.PATCH[TradingDetails, HttpResponse](s"$vatRegUrl/vatreg/$regId/trading-details", tradingDetails) recover {
      case e: Exception => throw logResponse(e, "upsertTradingDetails")
    }
  }

  def getFlatRate(regId: String)(implicit hc: HeaderCarrier): Future[Option[FlatRateScheme]] = {
    implicit val frmt = FlatRateScheme.apiFormat
    http.GET[HttpResponse](s"$vatRegUrl/vatreg/$regId/flat-rate-scheme") map { res =>
      if(res.status.equals(OK)) Some(res.json.as[FlatRateScheme]) else None
    } recover {
      case e: Exception => throw logResponse(e, "getFlatRate")
    }
  }

  def upsertFlatRate(regId: String, flatRate: FlatRateScheme)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    implicit val frmt = FlatRateScheme.apiFormat
    http.PATCH[FlatRateScheme, HttpResponse](s"$vatRegUrl/vatreg/$regId/flat-rate-scheme", flatRate) recover {
      case e: Exception => throw logResponse(e, "upsertFlatRate")
    }
  }

  def clearFlatRate(regId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.DELETE[HttpResponse](s"$vatRegUrl/vatreg/$regId/flat-rate-scheme") recover {
      case e: Exception => throw logResponse(e, "deleteFlatRate")
    }
  }

  def getSicAndCompliance(implicit hc:HeaderCarrier,profile:CurrentProfile): Future[Option[JsValue]] = {
    http.GET[HttpResponse](s"$vatRegUrl/vatreg/${profile.registrationId}/sicAndComp").map{ res =>
      if(res.status.equals(OK)) Some(res.json) else None
    }.recover{
          case e: Exception => throw logResponse(e, "getSicAndCompliance")
    }
  }

  def updateSicAndCompliance(sac: SicAndCompliance)(implicit hc:HeaderCarrier,profile:CurrentProfile): Future[JsValue] = {
    http.PATCH[JsValue, JsValue](s"$vatRegUrl/vatreg/${profile.registrationId}/sicAndComp", Json.toJson(sac)(SicAndCompliance.toApiWrites)).recover{
      case e: Exception => throw logResponse(e,"updateSicAndCompliance")
    }
  }

  def getBusinessContact(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Option[JsValue]] = {
    http.GET[HttpResponse](s"$vatRegUrl/vatreg/${cp.registrationId}/business-contact") map { resp =>
      if(resp.status.equals(OK)) Some(resp.json) else None
    } recover {
      case e: Exception => throw logResponse(e, "getBusinessContact")
    }
  }

  def upsertBusinessContact(businessContactJson: JsValue)(implicit cp: CurrentProfile, hc: HeaderCarrier, rds: HttpReads[JsValue]): Future[JsValue] = {
    http.PATCH[JsValue, JsValue](s"$vatRegUrl/vatreg/${cp.registrationId}/business-contact", businessContactJson) recover {
      case e: Exception => throw logResponse(e, "upsertBusinessContact")
    }
  }

  def getReturns(regId: String)
                (implicit hc: HeaderCarrier, rds: HttpReads[Returns]): Future[Returns] = {
    http.GET[Returns](s"$vatRegUrl/vatreg/$regId/returns") recover {
      case e: Exception => throw logResponse(e, "getReturns")
    }
  }

  def patchReturns(regId: String, returns: Returns)
                  (implicit hc: HeaderCarrier, rds: HttpReads[Returns]): Future[HttpResponse] = {
    http.PATCH[Returns, HttpResponse](s"$vatRegUrl/vatreg/$regId/returns", returns) recover {
      case e: Exception => throw logResponse(e, "patchReturns")
    }
  }

  def getBankAccount(regId: String)(implicit hc: HeaderCarrier): Future[Option[BankAccount]] = {
    http.GET[BankAccount](s"$vatRegUrl/vatreg/$regId/bank-account") map (Some(_)) recover {
      case _: NotFoundException => None
      case e: Exception => throw logResponse(e, "getBankAccount")
    }
  }

  def patchBankAccount(regId: String, bankAccount: BankAccount)
                      (implicit hc: HeaderCarrier, rds: HttpReads[BankAccount]): Future[HttpResponse] = {
    http.PATCH[BankAccount, HttpResponse](s"$vatRegUrl/vatreg/$regId/bank-account", bankAccount) recover {
      case e: Exception => throw logResponse(e, "patchBankAccount")
    }
  }
}
