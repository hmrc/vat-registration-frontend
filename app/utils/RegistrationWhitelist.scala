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

package utils

import java.time.LocalDate

import config.{AppConfig, FrontendAppConfig}
import connectors.DESResponse
import connectors.Success
import models.external._
import play.api.Logger
import play.api.libs.json.JsValue

import scala.concurrent.Future

trait RegistrationWhitelist {

  val config: AppConfig

  val returnDefaultIncorpInfo: (String) => Option[IncorporationInfo] = {
    case regId if config.whitelistedPostIncorpRegIds.contains(regId) =>
      Some(IncorporationInfo(
        subscription = IncorpSubscription(transactionId = s"fakeTxId-$regId", regime = "vat", subscriber = "scrs", callbackUrl = "#"),
        statusEvent  = IncorpStatusEvent(status = "accepted", crn = Some("90000001"), incorporationDate = Some(LocalDate.parse("2016-08-05")), description = None)
      ))
    case _ => None
  }

  val returnDefaultCompanyName: (String) => JsValue                         = _ => config.defaultCompanyName
  val returnDefaultPassedIV: (String) => Option[Boolean]                    = _ => Some(true)
  val returnDefaultCohoROA: (String) => Option[CoHoRegisteredOfficeAddress] = _ => Some(config.defaultCohoROA)
  val returnDefaultOfficerList: (String) => Option[OfficerList]             = _ => Some(config.defaultOfficerList)
  val returnDefaultCompRegProfile: (String) => Option[CompanyRegistrationProfile] =
    _ => Some(CompanyRegistrationProfile(status = "accepted", ctStatus = None))
  val returnDefaultTransId: (String) => String                              = regId => s"fakeTxId-$regId"
  val returnDefaultAckRef: (String) => String                               = _ => "fooBarWizzFAKEAckRef"


  val preventSubmissionForWhitelist: (String) => DESResponse = regId => {
    Logger.info(s"[RegistrationWhitelist] [submission] User submitted with regId: $regId which is whitelisted, no information has been sent to DES")
    Success
  }

  def ifRegIdNotWhitelisted[T](regId: String)(f: => Future[T])(implicit default: String => T): Future[T] = {
    if(config.whitelistedRegIds.contains(regId)) {
      Logger.info(s"Registration ID $regId is in the whitelist")
      Future.successful(default(regId))
    } else {
      f
    }
  }
}