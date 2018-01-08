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

import javax.inject.Inject

import cats.data.OptionT
import cats.instances.FutureInstances
import common.enums.VatRegStatus
import config.WSHttp
import features.officer.models.view.LodgingOfficer
import models.CurrentProfile
import models.api._
import models.external.IncorporationInfo
import play.api.libs.json._
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.http._
import play.api.http.Status._

import scala.concurrent.Future

sealed trait DESResponse
object Success extends DESResponse

class VatRegistrationConnector @Inject()(val http: WSHttp, config: ServicesConfig) extends RegistrationConnector {
  lazy val vatRegUrl   = config.baseUrl("vat-registration")
  lazy val vatRegElUrl = config.baseUrl("vat-registration-eligibility-frontend")
}

trait RegistrationConnector extends FlatRateConnector with TradingDetailsConnector with FinancialsConnector with FutureInstances {

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

  def getAckRef(regId: String)(implicit hc: HeaderCarrier): OptionalResponse[String] = OptionT(
    http.GET[Option[String]](s"$vatRegUrl/vatreg/$regId/acknowledgement-reference").recover{
      case e: Exception => throw logResponse(e, "getAckRef")
    }
  )


  def upsertSicAndCompliance(regId: String, sicAndCompliance: VatSicAndCompliance)
                            (implicit hc: HeaderCarrier, rds: HttpReads[VatSicAndCompliance]): Future[VatSicAndCompliance] = {
    http.PATCH[VatSicAndCompliance, VatSicAndCompliance](s"$vatRegUrl/vatreg/$regId/sic-and-compliance", sicAndCompliance).recover{
      case e: Exception => throw logResponse(e, "upsertSicAndCompliance")
    }
  }

  def upsertVatContact(regId: String, vatContact: VatContact)(implicit hc: HeaderCarrier, rds: HttpReads[VatContact]): Future[VatContact] = {
    http.PATCH[VatContact, VatContact](s"$vatRegUrl/vatreg/$regId/vat-contact", vatContact).recover{
      case e: Exception => throw logResponse(e, "upsertVatContact")
    }
  }

  def getLodgingOfficer(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[Option[JsValue]] = {
    http.GET[HttpResponse](s"$vatRegUrl/vatreg/${profile.registrationId}/officer") map { response =>
      if(response.status == NO_CONTENT) None else Some(response.json)
    } recover {
      case e => throw logResponse(e, "getLodgingOfficer")
    }
  }

  def patchLodgingOfficer(data: LodgingOfficer, writes: Writes[LodgingOfficer])(implicit profile: CurrentProfile, hc: HeaderCarrier): Future[JsValue] = {
    val json = Json.toJson(data)(writes)
    http.PATCH[JsValue, JsValue](s"$vatRegUrl/vatreg/${profile.registrationId}/officer", json) map {
      _ => json
    } recover {
      case e: Exception => throw logResponse(e, "patchLodgingOfficer")
    }
  }

  def upsertVatLodgingOfficer(regId: String, vatLodgingOfficer: VatLodgingOfficer)
                             (implicit hc: HeaderCarrier, rds: HttpReads[VatLodgingOfficer]): Future[VatLodgingOfficer] = {
    http.PATCH[VatLodgingOfficer, VatLodgingOfficer](s"$vatRegUrl/vatreg/$regId/lodging-officer", vatLodgingOfficer).recover{
      case e: Exception => throw logResponse(e, "upsertVatLodgingOfficer")
    }
  }

  def upsertVatEligibility(regId: String, vatServiceEligibility: VatServiceEligibility)
                          (implicit hc: HeaderCarrier, rds: HttpReads[VatServiceEligibility]): Future[VatServiceEligibility] = {
    http.PATCH[VatServiceEligibility, VatServiceEligibility](s"$vatRegUrl/vatreg/$regId/service-eligibility", vatServiceEligibility).recover{
      case e: Exception => throw logResponse(e, "upsertVatEligibility")
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

  def getIncorporationInfo(transactionId: String)(implicit hc: HeaderCarrier): OptionalResponse[IncorporationInfo] = OptionT(
    http.GET[IncorporationInfo](s"$vatRegUrl/vatreg/incorporation-information/$transactionId").map(Some(_)).recover {
      case _ => Option.empty[IncorporationInfo]
    }
  )

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

  def submitRegistration(regId:String)(implicit hc:HeaderCarrier) : Future[DESResponse] = {
    http.PUT[String, HttpResponse](s"$vatRegUrl/vatreg/$regId/submit-registration", "") map {
      _.status match {
        case OK => Success
      }
    }
  }
}
