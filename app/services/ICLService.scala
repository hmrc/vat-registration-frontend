/*
 * Copyright 2020 HM Revenue & Customs
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

import connectors.{ICLConnector, KeystoreConnector, RegistrationConnector}
import javax.inject.Inject
import models.CurrentProfile
import models.api.SicCode
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class ICLServiceImpl @Inject()(val iclConnector: ICLConnector,
                               config: ServicesConfig,
                               val keystore: KeystoreConnector,
                               val sicAndCompliance : SicAndComplianceService,
                               val registrationConnector: RegistrationConnector,
                               val iiService: IncorporationInformationService) extends ICLService {
  lazy val vatFeUrl:String = config.getConfString("vat-registration-frontend.www.url",
    throw new RuntimeException("[ICLService] Could not retrieve config for 'vat-registration-frontend'"))
  lazy val vatFeUri:String = config.getConfString("vat-registration-frontend.www.uri",
    throw new RuntimeException("[ICLService] Could not retrieve config for 'vat-registration-frontend.uri'"))
  lazy val iclReturnUrl: String = config.getConfString("vat-registration-frontend.redirect.url",
    throw new RuntimeException("[ICLService] Could not retrieve config for 'vat-registration-lookup-frontend.redirect.url'"))

  lazy val vatRedirectUrl = vatFeUrl + vatFeUri + iclReturnUrl

}

case class CustomICLMessages(
                              heading : String,
                              lead: String,
                              hint: String
                            )

object CustomICLMessages {
  implicit val writes = Json.writes[CustomICLMessages]
}

trait ICLService {
  val iclConnector: ICLConnector
  val vatRedirectUrl: String
  val keystore: KeystoreConnector
  val sicAndCompliance: SicAndComplianceService
  val iiService: IncorporationInformationService
  val registrationConnector : RegistrationConnector

  def prepopulateSicCodes(implicit hc : HeaderCarrier, cp : CurrentProfile) : Future[List[String]] = {
    sicAndCompliance.getSicAndCompliance flatMap {sac =>
      sac.otherBusinessActivities match {
        case Some(res) => Future.successful(res.sicCodes map (_.code))
        case None      => iiService.retrieveSicCodes(cp.transactionId)
      }
    } recover {
      case e =>
        Logger.warn(s"[ICLServiceImpl] [prepopulateSicCodes] Retrieving S4L/VR sic codes failed: ${e.getMessage}")
        Nil
    }
  }

  def journeySetup(customICLMessages: CustomICLMessages)(implicit hc: HeaderCarrier, cp: CurrentProfile): Future[String] = {
    def extractFromJsonSetup(jsonSetup : JsObject, item : String) = {
      (jsonSetup \ item).validate[String].getOrElse {
        Logger.error(s"[ICLServiceImpl] [journeySetup] $item couldn't be parsed from Json object")
        throw new Exception
      }
    }

    for {
      codes           <- prepopulateSicCodes
      jsonSetup       <- iclConnector.iclSetup(constructJsonForJourneySetup(codes, customICLMessages))
      fetchResultsUri = extractFromJsonSetup(jsonSetup, "fetchResultsUri")
      storeFetch      <- keystore.cache[String]("ICLFetchResultsUri", fetchResultsUri)
    } yield {
      extractFromJsonSetup(jsonSetup, "journeyStartUri")
    }
  }

  private[services] def constructJsonForJourneySetup(sicCodes: List[String], customICLMessages: CustomICLMessages): JsObject = {
    Json.obj(
      "redirectUrl" -> vatRedirectUrl,
      "journeySetupDetails" -> Json.obj(
        "customMessages" -> Json.obj(
          "summary" -> customICLMessages
        ),
        "sicCodes" -> sicCodes
      )
    )
  }

  def getICLSICCodes()(implicit hc:HeaderCarrier, cp:CurrentProfile): Future[List[SicCode]] = {
    for {
      url   <- keystore.fetchAndGet[String]("ICLFetchResultsUri").map(_.getOrElse(throw new Exception(s"[ICLService] [getICLCodes] No URL in keystore for key ICLFetchResultsUri for reg id ${cp.registrationId}")))
      js    <- iclConnector.iclGetResult(url)
      list  = Json.fromJson[List[SicCode]](js)(SicCode.readsList).get
    } yield {
      if (list.isEmpty) {
        Logger.error(s"[ICLService] [getICLCodes] ICLGetResult returned no sicCodes for regId: ${cp.registrationId}")
        throw new Exception
      }
      list
    }
  }
}
