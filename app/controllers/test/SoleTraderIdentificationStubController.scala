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

import models.api.{Individual, NETP, Partnership, PartyType}
import models.external.soletraderid.SoleTraderIdJourneyConfig
import models.external.{BusinessVerificationStatus, BvPass}
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

@Singleton
class SoleTraderIdentificationStubController @Inject()(mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  val soleTraderJourney = "1"
  val netpJourney = "2"
  val individualJourney = "3"

  def createJourney(optPartyType: Option[String]): Action[SoleTraderIdJourneyConfig] = Action(parse.json[SoleTraderIdJourneyConfig]) { request =>
    def json(id: String): JsObject = Json.obj("journeyStartUrl" -> JsString(request.body.continueUrl + s"?journeyId=$id"))

    optPartyType.map(PartyType.fromString) match {
      case Some(Individual | Partnership) =>
        Created(json(soleTraderJourney))
      case Some(NETP) =>
        Created(json(netpJourney))
      case None =>
        Created(json(individualJourney))
    }

  }

  def retrieveValidationResult(journeyId: String): Action[AnyContent] = Action {
    Ok(
      journeyId match {
        case `soleTraderJourney` => soleTraderValidationResult
        case `netpJourney` => netpValidationResult
        case `individualJourney` => individualValidationResult
      }
    )
  }

  val soleTraderValidationResult: JsObject = {
    Json.obj(
      "fullName" -> Json.obj(
        "firstName" -> "testFirstName",
        "lastName" -> "testLastName"
      ),
      "nino" -> "AA123456A",
      "dateOfBirth" -> LocalDate.of(1990, 1, 1),
      "sautr" -> "1234567890",
      "businessVerification" -> Json.obj(
        "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
      ),
      "registration" -> Json.obj(
        "registrationStatus" -> "REGISTERED",
        "registeredBusinessPartnerId" -> "X00000123456789"
      )
    )
  }

  val netpValidationResult: JsObject = {
    Json.obj(
      "fullName" -> Json.obj(
        "firstName" -> "testFirstName",
        "lastName" -> "testLastName"
      ),
      "dateOfBirth" -> LocalDate.of(1990, 1, 1),
      "sautr" -> "1234567890",
      "trn" -> "testTrn",
      "businessVerification" -> Json.obj(
        "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
      ),
      "registration" -> Json.obj(
        "registrationStatus" -> "REGISTERED",
        "registeredBusinessPartnerId" -> "X00000123456789"
      )
    )
  }

  val individualValidationResult: JsObject = {
    Json.obj(
      "fullName" -> Json.obj(
        "firstName" -> "testFirstName",
        "lastName" -> "testLastName"
      ),
      "nino" -> "AA123456A",
      "dateOfBirth" -> LocalDate.of(1990, 1, 1)
    )
  }
}