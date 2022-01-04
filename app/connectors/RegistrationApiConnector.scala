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

  def getSection[T: ApiKey](regId: String)(implicit hc: HeaderCarrier, format: Format[T]): Future[Option[T]] = {
    implicit object GetSectionHttpReads extends HttpReads[Option[T]] {
      override def read(method: String, url: String, response: HttpResponse): Option[T] = {
        response.status match {
          case OK => (response.json \ "data").asOpt[T]
          case NOT_FOUND => None
          case _ => throw new InternalServerException(s"Unexpected response: ${response.body}")
        }
      }
    }

    http.GET[Option[T]](s"${config.backendHost}/vatreg/registrations/$regId/sections/${ApiKey[T]}")
  }

  def replaceSection[T: ApiKey](regId: String, section: T)(implicit hc: HeaderCarrier, format: Format[T]): Future[T] = {
    implicit object ReplaceSectionHttpReads extends HttpReads[T] {
      override def read(method: String, url: String, response: HttpResponse): T = {
        response.status match {
          case OK => (response.json \ "data").as[T]
          case _ => throw new InternalServerException(s"Unexpected response: ${response.body}")
        }
      }
    }

    http.PUT[T, T](s"${config.backendHost}/vatreg/registrations/$regId/sections/${ApiKey[T]}", section)
  }
}