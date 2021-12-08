/*
 * Copyright 2021 HM Revenue & Customs
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

import common.enums.VatRegStatus
import config.FrontendAppConfig
import models.api._
import models.api.returns.Returns
import models.{ApplicantDetails, _}
import play.api.http.Status._
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import uk.gov.hmrc.http._

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

// scalastyle:off
@Singleton
class VatRegistrationConnector @Inject()(val http: HttpClient,
                                         val config: FrontendAppConfig)
                                        (implicit ec: ExecutionContext) {

  lazy val vatRegUrl: String = config.servicesConfig.baseUrl("vat-registration")
  lazy val vatRegElUrl: String = config.servicesConfig.baseUrl("vat-registration-eligibility-frontend")

  def createNewRegistration(implicit hc: HeaderCarrier, rds: HttpReads[VatScheme]): Future[VatScheme] = {
    http.POSTEmpty[VatScheme](s"$vatRegUrl/vatreg/new").recover {
      case e => throw logResponse(e, "createNewRegistration")
    }
  }

  def getAllRegistrations(implicit hc: HeaderCarrier, rds: HttpReads[VatSchemeHeader]): Future[List[VatSchemeHeader]] =
    http.GET[List[JsValue]](s"$vatRegUrl/vatreg/registrations").recover {
      case e => throw logResponse(e, "getRegistration")
    }.map { list =>
      list.flatMap { json =>
        json.validate[VatSchemeHeader] match {
          case JsSuccess(header, _) => Some(header)
          case JsError(_) =>
            logger.error(s"[getAllRegistrations] Failed to parse VatSchemeHeader out of VatScheme for user")
            None
        }
      }
    }

  def getRegistration(regId: String)(implicit hc: HeaderCarrier, rds: HttpReads[VatScheme]): Future[VatScheme] = {
    http.GET[VatScheme](s"$vatRegUrl/vatreg/$regId/get-scheme").recover {
      case e => throw logResponse(e, "getRegistration")
    }
  }

  def getRegistrationJson(regId: String)(implicit hc: HeaderCarrier, rds: HttpReads[VatScheme]): Future[JsValue] = {
    http.GET[JsValue](s"$vatRegUrl/vatreg/$regId/get-scheme").recover {
      case e => throw logResponse(e, "getRegistrationJson")
    }
  }

  def getAckRef(regId: String)(implicit hc: HeaderCarrier): Future[String] = {
    http.GET[String](s"$vatRegUrl/vatreg/$regId/acknowledgement-reference")
      .recover {
        case e: Exception => throw logResponse(e, "getAckRef")
      }
  }

  def getTaxableThreshold(date: LocalDate)(implicit hc: HeaderCarrier): Future[TaxableThreshold] = {
    http.GET[TaxableThreshold](s"$vatRegUrl/vatreg/threshold/$date") recover {
      case e => throw logResponse(e, "getTaxableThreshold")
    }
  }

  def getEligibilityData(implicit hc: HeaderCarrier, cp: CurrentProfile): Future[JsObject] = {
    http.GET[HttpResponse](s"$vatRegUrl/vatreg/${cp.registrationId}/eligibility-data") map {
      _.json.as[JsObject]
    } recover {
      case e => throw logResponse(e, "getEligibilityData")
    }
  }

  def getApplicantDetails(regId: String, partyType: PartyType)(implicit hc: HeaderCarrier): Future[Option[ApplicantDetails]] = {
    implicit val reads: Reads[ApplicantDetails] = ApplicantDetails.reads(partyType)

    http.GET[Option[ApplicantDetails]](s"$vatRegUrl/vatreg/$regId/applicant-details").recover {
      case e => throw logResponse(e, "getApplicantDetails")
    }
  }

  def getThreshold(regId: String)(implicit hc: HeaderCarrier): Future[Option[Threshold]] = {
    http.GET[HttpResponse](s"$vatRegUrl/vatreg/$regId/threshold-data").map {
      result => if (result.status == NO_CONTENT) None else result.json.validateOpt[Threshold].get
    }.recover {
      case e => throw logResponse(e, "getThreshold")
    }
  }

  def patchApplicantDetails(data: ApplicantDetails)(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[JsValue] = {
    val json = Json.toJson(data)(ApplicantDetails.writes).as[JsObject]
    http.PATCH[JsValue, JsValue](s"$vatRegUrl/vatreg/${profile.registrationId}/applicant-details", json) map {
      _ => json
    } recover {
      case e: Exception => throw logResponse(e, "patchApplicantDetails")
    }
  }

  def upsertPpob(regId: String, address: Address)(implicit hc: HeaderCarrier, rds: HttpReads[Address]): Future[Address] = {
    http.PATCH[Address, Address](s"$vatRegUrl/vatreg/$regId/ppob", address).recover {
      case e: Exception => throw logResponse(e, "upsertPpob")
    }
  }

  def deleteVatScheme(regId: String)(implicit hc: HeaderCarrier, rds: HttpReads[HttpResponse]): Future[Boolean] = {
    http.DELETE[HttpResponse](s"$vatRegUrl/vatreg/registrations/$regId").map(_.status == OK)
  }

  def deleteVREFESession(regId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.DELETE[HttpResponse](s"$vatRegElUrl/internal/$regId/delete-session") //Doesn't actually exist on VRS-EL-FE
  }

  def getStatus(regId: String)(implicit hc: HeaderCarrier): Future[VatRegStatus.Value] = {
    http.GET[JsObject](s"$vatRegUrl/vatreg/$regId/status") map { json =>
      (json \ "status").as[VatRegStatus.Value]
    } recover {
      case e: Exception => throw logResponse(e, "getStatus")
    }
  }

  def getTurnoverEstimates(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Option[TurnoverEstimates]] = {
    http.GET[HttpResponse](s"$vatRegUrl/vatreg/${profile.registrationId}/turnover-estimates-data") map { res =>
      if (res.status.equals(OK)) Some(res.json.as[TurnoverEstimates]) else None
    } recover {
      case e: Exception => throw logResponse(e, "getTurnoverEstimates")
    }
  }

  def submitRegistration(regId: String, userHeaders: Map[String, String])(implicit hc: HeaderCarrier): Future[DESResponse] = {
    val jsonBody = Json.obj("userHeaders" -> userHeaders)

    http.PUT[JsObject, HttpResponse](s"$vatRegUrl/vatreg/$regId/submit-registration", jsonBody) map {
      _.status match {
        case OK => Success
      }
    } recover {
      case e: Upstream5xxResponse => SubmissionFailedRetryable
      case _ => SubmissionFailed
    }
  }

  def getTradingDetails(regId: String)(implicit hc: HeaderCarrier): Future[Option[TradingDetails]] = {
    implicit val frmt: Format[TradingDetails] = TradingDetails.apiFormat
    http.GET[HttpResponse](s"$vatRegUrl/vatreg/$regId/trading-details") map { res =>
      if (res.status.equals(OK)) Some(res.json.as[TradingDetails]) else None
    } recover {
      case e: Exception => throw logResponse(e, "getTradingDetails")
    }
  }

  def upsertTradingDetails(regId: String, tradingDetails: TradingDetails)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    implicit val frmt: Format[TradingDetails] = TradingDetails.apiFormat
    http.PATCH[TradingDetails, HttpResponse](s"$vatRegUrl/vatreg/$regId/trading-details", tradingDetails) recover {
      case e: Exception => throw logResponse(e, "upsertTradingDetails")
    }
  }

  def getFlatRate(regId: String)(implicit hc: HeaderCarrier): Future[Option[FlatRateScheme]] = {
    implicit val frmt: Format[FlatRateScheme] = FlatRateScheme.apiFormat
    http.GET[HttpResponse](s"$vatRegUrl/vatreg/$regId/flat-rate-scheme") map { res =>
      if (res.status.equals(OK)) Some(res.json.as[FlatRateScheme]) else None
    } recover {
      case e: Exception => throw logResponse(e, "getFlatRate")
    }
  }

  def upsertFlatRate(regId: String, flatRate: FlatRateScheme)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    implicit val frmt: Format[FlatRateScheme] = FlatRateScheme.apiFormat
    http.PATCH[FlatRateScheme, HttpResponse](s"$vatRegUrl/vatreg/$regId/flat-rate-scheme", flatRate) recover {
      case e: Exception => throw logResponse(e, "upsertFlatRate")
    }
  }

  def clearFlatRate(regId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.DELETE[HttpResponse](s"$vatRegUrl/vatreg/$regId/flat-rate-scheme") recover {
      case e: Exception => throw logResponse(e, "deleteFlatRate")
    }
  }

  def getSicAndCompliance(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Option[JsValue]] = {
    http.GET[HttpResponse](s"$vatRegUrl/vatreg/${profile.registrationId}/sicAndComp").map { res =>
      if (res.status.equals(OK)) Some(res.json) else None
    }.recover {
      case e: Exception => throw logResponse(e, "getSicAndCompliance")
    }
  }

  def updateSicAndCompliance(sac: SicAndCompliance)(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[JsValue] = {
    http.PATCH[JsValue, JsValue](s"$vatRegUrl/vatreg/${profile.registrationId}/sicAndComp", Json.toJson(sac)(SicAndCompliance.toApiWrites)).recover {
      case e: Exception => throw logResponse(e, "updateSicAndCompliance")
    }
  }

  def getBusinessContact(implicit hc: HeaderCarrier, cp: CurrentProfile): Future[Option[BusinessContact]] = {
    http.GET[HttpResponse](s"$vatRegUrl/vatreg/${cp.registrationId}/business-contact").map { res =>
      if (res.status.equals(OK)) Some(res.json.as[BusinessContact](BusinessContact.apiFormat)) else None
    }.recover {
      case e: Exception => throw logResponse(e, "getBusinessContact")
    }
  }

  def upsertBusinessContact(businessContact: BusinessContact)(implicit hc: HeaderCarrier, cp: CurrentProfile): Future[JsValue] = {
    http.PATCH[JsValue, JsValue](s"$vatRegUrl/vatreg/${cp.registrationId}/business-contact", Json.toJson(businessContact)(BusinessContact.apiFormat)) recover {
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
                  (implicit hc: HeaderCarrier): Future[HttpResponse] = {
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
                      (implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.PATCH[BankAccount, HttpResponse](s"$vatRegUrl/vatreg/$regId/bank-account", bankAccount) recover {
      case e: Exception => throw logResponse(e, "patchBankAccount")
    }
  }

  def submitHonestyDeclaration(regId: String, honestyDeclaration: Boolean)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val js = Json.obj("honestyDeclaration" -> honestyDeclaration)
    http.PATCH[JsObject, HttpResponse](s"$vatRegUrl/vatreg/$regId/honesty-declaration", js)
  }

  def upsertVatScheme(regId: String, partialVatScheme: JsValue)(implicit hc: HeaderCarrier): Future[JsValue] = {
    http.POST(s"$vatRegUrl/vatreg/insert-s4l-scheme", partialVatScheme).map {
      response => response.json
    }
  }
}

sealed trait DESResponse

object Success extends DESResponse

object SubmissionFailed extends DESResponse

object SubmissionFailedRetryable extends DESResponse
