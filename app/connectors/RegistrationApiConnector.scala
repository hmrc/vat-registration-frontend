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
import models._
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

// scalastyle:off
@Singleton
class RegistrationApiConnector @Inject()(val http: HttpClient,
                                         val config: FrontendAppConfig)
                                        (implicit ec: ExecutionContext) {

  def getSection[T: ApiKey](regId: String, idx: Option[Int] = None)(implicit hc: HeaderCarrier, format: Format[T]): Future[Option[T]] = {
    implicit object GetSectionHttpReads extends HttpReads[Option[T]] {
      override def read(method: String, url: String, response: HttpResponse): Option[T] = {
        response.status match {
          case OK => response.json.asOpt[T]
          case NOT_FOUND => None
          case _ => throw new InternalServerException(s"Unexpected response: ${response.body}")
        }
      }
    }

    val url = appendIndexToUrl(s"${config.backendHost}/vatreg/registrations/$regId/sections/${ApiKey[T]}", idx)
    http.GET[Option[T]](url)
  }

  def getListSection[T: ApiKey](regId: String)(implicit hc: HeaderCarrier, format: Format[List[T]]): Future[List[T]] = {
    implicit object GetSectionsHttpReads extends HttpReads[List[T]] {
      override def read(method: String, url: String, response: HttpResponse): List[T] = {
        response.status match {
          case OK => response.json.as[List[T]]
          case NOT_FOUND => Nil
          case _ => throw new InternalServerException(s"Unexpected response: ${response.body}")
        }
      }
    }
    http.GET[List[T]](s"${config.backendHost}/vatreg/registrations/$regId/sections/${ApiKey[T]}")
  }

  def replaceSection[T: ApiKey](regId: String, section: T, idx: Option[Int] = None)(implicit hc: HeaderCarrier, format: Format[T]): Future[T] = {
    implicit object ReplaceSectionHttpReads extends HttpReads[T] {
      override def read(method: String, url: String, response: HttpResponse): T = {
        response.status match {
          case OK => response.json.as[T]
          case _ => throw new InternalServerException(s"Unexpected response: ${response.body}")
        }
      }
    }

    val url = appendIndexToUrl(s"${config.backendHost}/vatreg/registrations/$regId/sections/${ApiKey[T]}", idx)
    http.PUT[T, T](url, section)
  }

  def deleteSection[T: ApiKey](regId: String, idx: Option[Int] = None)(implicit hc: HeaderCarrier, format: Format[T]): Future[Boolean] = {

    val url = appendIndexToUrl(s"${config.backendHost}/vatreg/registrations/$regId/sections/${ApiKey[T]}", idx)

    http.DELETE[HttpResponse](url).map {
      _.status match {
        case NO_CONTENT => true
        case status => throw new InternalServerException(s"[RegistrationApiConnector][deleteSection] unexpected status from backend: $status")
      }
    }
  }

  private def appendIndexToUrl(url: String, idx: Option[Int]): String = {
    idx match {
      case Some(index) => s"$url/$index"
      case None => url
    }
  }
}