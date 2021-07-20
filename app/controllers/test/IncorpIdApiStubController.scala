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

import models.api._
import models.external.incorporatedentityid.IncorpIdJourneyConfig
import models.external.{BvPass, IncorporatedEntity}
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class IncorpIdApiStubController @Inject()(mcc: MessagesControllerComponents)
  extends FrontendController(mcc) {

  def createJourney(partyType: String): Action[IncorpIdJourneyConfig] = Action(parse.json[IncorpIdJourneyConfig]) {
    request =>
      val journeyId = PartyType.fromString(partyType) match {
        case UkCompany => "1"
        case RegSociety => "2"
        case CharitableOrg => "3"
      }

      Created(Json.obj("journeyStartUrl" -> JsString(request.body.continueUrl + s"?journeyId=$journeyId")))
  }

  def getDetails(journeyId: String): Action[AnyContent] = Action.async { _ =>
    Future.successful(
      Ok(Json.toJson(IncorporatedEntity(
        companyName = "Test company",
        companyNumber = "12345678",
        ctutr = if (!journeyId.equals("3")) Some("123567890") else None,
        chrn = if (journeyId.equals("3")) Some("123567890") else None,
        dateOfIncorporation = LocalDate.of(2020, 1, 1),
        identifiersMatch = true,
        registration = Some("REGISTERED"),
        businessVerification = Some(BvPass),
        bpSafeId = Some("testBpId")
      ))(IncorporatedEntity.apiFormat))
    )
  }
}
