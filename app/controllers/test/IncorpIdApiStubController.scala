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

import java.time.LocalDate
import config.FrontendAppConfig
import models.external.{BvPass, IncorporatedEntity}

import javax.inject.{Inject, Singleton}
import models.external.incorporatedentityid.IncorpIdJourneyConfig
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

@Singleton
class IncorpIdApiStubController @Inject()(mcc: MessagesControllerComponents,
                                          appConfig: FrontendAppConfig)
  extends FrontendController(mcc) {

  def createJourney: Action[IncorpIdJourneyConfig] = Action(parse.json[IncorpIdJourneyConfig]) (
    req =>
      Created(Json.obj("journeyStartUrl" -> JsString(appConfig.incorpIdCallbackUrl + "?journeyId=1")))
  )

  def getDetails(journeyId: String): Action[AnyContent] = Action.async { _ =>
    Future.successful(
      Ok(Json.toJson(IncorporatedEntity(
        companyName = "Test company",
        companyNumber = "12345678",
        ctutr = "1234567890",
        dateOfIncorporation = LocalDate.of(2020,1,1),
        identifiersMatch = true,
        registration = Some("REGISTERED"),
        businessVerification = Some(BvPass),
        bpSafeId = Some("testBpId")
      ))(IncorporatedEntity.apiFormat))
    )
  }
}
