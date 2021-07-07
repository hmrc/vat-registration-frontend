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

package controllers.test

import config.FrontendAppConfig
import models.external.partnershipid.PartnershipIdJourneyConfig
import models.external.{BusinessVerificationStatus, BvPass}
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}

@Singleton
class PartnershipIdentificationStubController @Inject()(mcc: MessagesControllerComponents,
                                                        appConfig: FrontendAppConfig) extends FrontendController(mcc) {

  def createJourney: Action[PartnershipIdJourneyConfig] = Action(parse.json[PartnershipIdJourneyConfig]) (_ =>
    Created(Json.obj("journeyStartUrl" -> JsString(appConfig.partnershipIdCallbackUrl + s"?journeyId=1")))
  )

  def retrieveValidationResult(journeyId: String): Action[AnyContent] = Action {
    Ok(Json.obj(
      "sautr" -> "1234567890",
      "postcode" -> "AA11AA",
      "businessVerification" -> Json.obj(
        "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
      ),
      "registration" -> Json.obj(
        "registrationStatus" -> "REGISTERED",
        "registeredBusinessPartnerId" -> "X00000123456789"
      ),
      "identifiersMatch" -> true
    )
    )
  }

}
