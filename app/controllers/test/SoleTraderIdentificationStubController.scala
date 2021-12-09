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

import models.api.{Individual, Partnership, PartyType, NETP => PartyTypeNETP}
import models.external.soletraderid.SoleTraderIdJourneyConfig
import models.external.{BusinessVerificationStatus, BvPass}
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

@Singleton
class SoleTraderIdentificationStubController @Inject()(mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  val soleTraderExcludeBv = "SOLE_TRADER_EXCLUDE_BV"
  val soleTrader = "SOLE_TRADER"
  val netpExcludeBv = "NETP_EXCLUDE_BV"
  val netp = "NETP"
  val individual = "INDIVIDUAL"


  def createJourney(optPartyType: Option[String]): Action[SoleTraderIdJourneyConfig] = Action(parse.json[SoleTraderIdJourneyConfig]) { request =>
    def json(id: String): JsObject = Json.obj("journeyStartUrl" -> JsString(request.body.continueUrl + s"?journeyId=$id"))

    optPartyType.map(PartyType.fromString) match {
      case Some(Individual | Partnership) if !request.body.businessVerificationCheck =>
        Created(json(soleTraderExcludeBv))
      case Some(Individual | Partnership) =>
        Created(json(soleTrader))
      case Some(PartyTypeNETP) if !request.body.businessVerificationCheck =>
        Created(json(netpExcludeBv))
      case Some(PartyTypeNETP) =>
        Created(json(netp))
      case None =>
        Created(json(individual))
    }

  }

  def retrieveValidationResult(journeyId: String): Action[AnyContent] = Action {
    Ok(
      journeyId match {
        case `soleTraderExcludeBv` => soleTraderValidationResult(businessVerification = false)
        case `soleTrader` => soleTraderValidationResult(businessVerification = true)
        case `netpExcludeBv` => netpValidationResult(businessVerification = false)
        case `netp` => netpValidationResult(businessVerification = true)
        case `individual` => individualValidationResult
      }
    )
  }

  def soleTraderValidationResult(businessVerification: Boolean): JsObject = {
    val json = Json.obj(
      "fullName" -> Json.obj(
        "firstName" -> "testFirstName",
        "lastName" -> "testLastName"
      ),
      "nino" -> "AA123456A",
      "dateOfBirth" -> LocalDate.of(1990, 1, 1),
      "sautr" -> "1234567890",
      "registration" -> Json.obj(
        "registrationStatus" -> "REGISTERED",
        "registeredBusinessPartnerId" -> "X00000123456789"
      )
    )
    businessVerificationStatus(businessVerification, json)
  }

  def netpValidationResult(businessVerification: Boolean): JsObject = {
    val json = Json.obj(
      "fullName" -> Json.obj(
        "firstName" -> "testFirstName",
        "lastName" -> "testLastName"
      ),
      "dateOfBirth" -> LocalDate.of(1990, 1, 1),
      "sautr" -> "1234567890",
      "trn" -> "testTrn",
      "registration" -> Json.obj(
        "registrationStatus" -> "REGISTERED",
        "registeredBusinessPartnerId" -> "X00000123456789"
      )
    )
    businessVerificationStatus(businessVerification, json)
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

  private def businessVerificationStatus(businessVerification: Boolean, json: JsObject): JsObject = {
    if (businessVerification) {
      json ++ Json.obj(
        "businessVerification" -> Json.obj(
          "verificationStatus" -> Json.toJson[BusinessVerificationStatus](BvPass)
        )
      )
    } else {
      json
    }
  }
}