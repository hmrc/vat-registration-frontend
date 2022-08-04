/*
 * Copyright 2022 HM Revenue & Customs
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

import config.FrontendAppConfig
import models.api._
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatRegistrationConnector @Inject()(val http: HttpClient,
                                         val config: FrontendAppConfig)
                                        (implicit ec: ExecutionContext) {

  lazy val vatRegUrl: String = config.servicesConfig.baseUrl("vat-registration")
  lazy val vatRegElUrl: String = config.servicesConfig.baseUrl("vat-registration-eligibility-frontend")

  def createNewRegistration(implicit hc: HeaderCarrier, rds: HttpReads[VatScheme]): Future[VatScheme] = {
    http.POSTEmpty[VatScheme](s"$vatRegUrl/vatreg/registrations").recover {
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

  def getRegistration[T](regId: String)(implicit hc: HeaderCarrier, reads: HttpReads[T]): Future[T] = {
    http.GET[T](s"$vatRegUrl/vatreg/registrations/$regId").recover {
      case e => throw logResponse(e, "getRegistration")
    }
  }

  def upsertRegistration(regId: String, vatScheme: VatScheme)(implicit hc: HeaderCarrier, writes: Writes[VatScheme]): Future[VatScheme] = {
    http.PUT[VatScheme, VatScheme](url = s"$vatRegUrl/vatreg/registrations/$regId", body = vatScheme).recover {
      case e => throw logResponse(e, "upsertRegistration")
    }
  }

  def submitRegistration(regId: String, userHeaders: Map[String, String])(implicit hc: HeaderCarrier): Future[DESResponse] = {
    val jsonBody = Json.obj("userHeaders" -> userHeaders)

    http.PUT[JsObject, HttpResponse](s"$vatRegUrl/vatreg/$regId/submit-registration", jsonBody).map {
      _.status match {
        case OK => Success
      }
    }.recover {
      case UpstreamErrorResponse(_, CONFLICT, _, _) => AlreadySubmitted
      case UpstreamErrorResponse(_, UNPROCESSABLE_ENTITY, _, _) => Contact
      case UpstreamErrorResponse(_, TOO_MANY_REQUESTS, _, _) => SubmissionInProgress
      case ex: BadRequestException => SubmissionFailed
      case ex => SubmissionFailedRetryable
    }
  }

}

sealed trait DESResponse
object Success extends DESResponse
object SubmissionFailed extends DESResponse
object SubmissionFailedRetryable extends DESResponse
object AlreadySubmitted extends DESResponse
object SubmissionInProgress extends DESResponse

object Contact extends DESResponse
