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
import models.external.soletraderid.SoleTraderIdJourneyConfig
import models.external.{BusinessVerificationStatus, BvPass, BvUnchallenged}
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

@Singleton
class SoleTraderIdentificationStubController @Inject()(mcc: MessagesControllerComponents,
                                                       appConfig: FrontendAppConfig) extends FrontendController(mcc) {

  val ukCompanyJourney = "1"
  val soleTraderJourney = "2"

  def createJourney: Action[SoleTraderIdJourneyConfig] = Action(parse.json[SoleTraderIdJourneyConfig]) { config =>
    def json(id: String): JsObject = Json.obj("journeyStartUrl" -> JsString(appConfig.getSoleTraderIdentificationCallbackUrl + s"?journeyId=$id"))

    if (config.body.enableSautrCheck) {
      Created(json(soleTraderJourney))
    } else {
      Created(json(ukCompanyJourney))
    }
  }

  def retrieveValidationResult(journeyId: String): Action[AnyContent] = Action {
    Ok(
      Json.obj(
        "fullName" -> Json.obj(
          "firstName" -> "testFirstName",
          "lastName" -> "testLastName"
        ),
        "nino" -> "AA123456A",
        "dateOfBirth" -> LocalDate.of(1990, 1, 1)
      ) ++ (
        if (journeyId == soleTraderJourney) {
          Json.obj(
            "sautr" -> "1234567890",
            "businessVerification" -> Json.obj(
              "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
            ),
            "registration" -> Json.obj(
              "registrationStatus" -> "REGISTERED",
              "registeredBusinessPartnerId" -> "X00000123456789"
            )
          )
        } else {
          Json.obj(
            "businessVerification" -> Json.obj(
              "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvUnchallenged)
            ),
            "registration" -> Json.obj(
              "registrationStatus" -> "REGISTRATION_NOT_CALLED"
            )
          )
        }
        )
    )
  }

}