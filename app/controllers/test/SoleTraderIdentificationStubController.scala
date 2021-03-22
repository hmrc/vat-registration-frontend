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
import models.external.incorporatedentityid.IncorpIdJourneyConfig
import models.external.soletraderid.SoleTraderIdJourneyConfig
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

@Singleton
class SoleTraderIdentificationStubController @Inject()(mcc: MessagesControllerComponents,
                                                       appConfig: FrontendAppConfig) extends FrontendController(mcc) {

  def createJourney: Action[SoleTraderIdJourneyConfig] = Action(parse.json[SoleTraderIdJourneyConfig]) { _ =>
    Created(Json.obj("journeyStartUrl" -> JsString(appConfig.getSoleTraderIdentificationCallbackUrl + "?journeyId=1")))
  }

  def retrieveValidationResult(journeyId: String): Action[AnyContent] = Action {
    Ok(
      Json.obj(
        "personalDetails" -> Json.obj(
          "firstName" -> "testFirstName",
          "lastName" -> "testLastName",
          "nino" -> "AA123456A",
          "dateOfBirth" -> LocalDate.of(1990, 1, 1)
        )
      )
    )
  }

}