/*
 * Copyright 2026 HM Revenue & Customs
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

package services

import connectors.{ICLConnector, VatRegistrationConnector}
import models.CurrentProfile
import models.api.SicCode
import play.api.libs.json.{JsObject, JsValue, Json, OWrites}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.Request

@Singleton
class ICLService @Inject()(val iclConnector: ICLConnector,
                           config: ServicesConfig,
                           val keystore: SessionService,
                           val businessService: BusinessService,
                           val registrationConnector: VatRegistrationConnector
                          )(implicit ec: ExecutionContext) {
  lazy val vatFeUrl: String = config.getConfString("vat-registration-frontend.www.url",
    throw new RuntimeException("[ICLService] Could not retrieve config for 'vat-registration-frontend'"))
  lazy val vatFeUri: String = config.getConfString("vat-registration-frontend.www.uri",
    throw new RuntimeException("[ICLService] Could not retrieve config for 'vat-registration-frontend.uri'"))
  lazy val iclReturnUrl: String = config.getConfString("vat-registration-frontend.redirect.url",
    throw new RuntimeException("[ICLService] Could not retrieve config for 'vat-registration-lookup-frontend.redirect.url'"))

  lazy val vatRedirectUrl: String = vatFeUrl + vatFeUri + iclReturnUrl

  def prepopulateSicCodes(implicit hc: HeaderCarrier, cp: CurrentProfile, request: Request[_]): Future[List[String]] = {
    businessService.getBusiness flatMap { businessDetails =>
      businessDetails.businessActivities match {
        case Some(sicCodes) => Future.successful(sicCodes map (_.code))
        case _ => Future.successful(Nil)
      }
    } recover {
      case e =>
        logger.warn(s"[ICLServiceImpl] [prepopulateSicCodes] Retrieving VR sic codes failed: ${e.getMessage}")
        Nil
    }
  }

  def journeySetup(customICLMessages: CustomICLMessages, welshCustomICLMessages: CustomICLMessages)
                  (implicit hc: HeaderCarrier, cp: CurrentProfile, request: Request[_]): Future[String] = {

    def extractFromJsonSetup(jsonSetup: JsValue, item: String) = {
      (jsonSetup \ item).validate[String].getOrElse {
        logger.error(s"[ICLServiceImpl] [journeySetup] $item couldn't be parsed from Json object")
        throw new Exception
      }
    }

    for {
      codes <- prepopulateSicCodes
      jsonSetup <- iclConnector.iclSetup(constructJsonForJourneySetup(codes, customICLMessages, welshCustomICLMessages))
      fetchResultsUri = extractFromJsonSetup(jsonSetup, "fetchResultsUri")
      _ <- keystore.cache[String]("ICLFetchResultsUri", fetchResultsUri)
    } yield {
      extractFromJsonSetup(jsonSetup, "journeyStartUri")
    }
  }

  private[services] def constructJsonForJourneySetup(sicCodes: List[String], customICLMessages: CustomICLMessages, welshCustomICLMessages: CustomICLMessages): JsObject = {
    Json.obj(
      "redirectUrl" -> vatRedirectUrl,
      "journeySetupDetails" -> Json.obj(
        "customMessages" -> Json.obj(
          "summary" -> customICLMessages,
          "summaryCy" -> welshCustomICLMessages
        ),
        "sicCodes" -> sicCodes
      )
    )
  }

  def getICLSICCodes()(implicit hc: HeaderCarrier, cp: CurrentProfile, request: Request[_]): Future[List[SicCode]] = {
    for {
      url <- keystore.fetchAndGet[String]("ICLFetchResultsUri").map(_.getOrElse(throw new Exception(s"[ICLService] [getICLCodes] No URL in keystore for key ICLFetchResultsUri for reg id ${cp.registrationId}")))
      js <- iclConnector.iclGetResult(url)
      list = Json.fromJson[List[SicCode]](js)(SicCode.readsList).get
    } yield {
      if (list.isEmpty) {
        logger.error(s"[ICLService] [getICLCodes] ICLGetResult returned no sicCodes for regId: ${cp.registrationId}")
        throw new Exception
      }
      list
    }
  }
}

case class CustomICLMessages(heading: Option[String],
                             lead: Option[String],
                             hint: Option[String])

object CustomICLMessages {
  implicit val writes: OWrites[CustomICLMessages] = Json.writes[CustomICLMessages]
}
