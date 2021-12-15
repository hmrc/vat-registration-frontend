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

import models.api.{LtdLiabilityPartnership, LtdPartnership, PartyType, ScotLtdPartnership}
import models.external.partnershipid.PartnershipIdJourneyConfig
import models.external.{BvPass, PartnershipIdEntity}
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class PartnershipIdentificationStubController @Inject()(mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  final val excludeBv = "EXCLUDE_BV"

  def createJourney(partyType: String): Action[PartnershipIdJourneyConfig] = Action(parse.json[PartnershipIdJourneyConfig]) {
    journeyConfig =>
      val journeyId = if (journeyConfig.body.businessVerificationCheck) s"$partyType:$excludeBv" else partyType

      Created(Json.obj("journeyStartUrl" -> JsString(journeyConfig.body.continueUrl + s"?journeyId=$journeyId")))
  }

  def retrieveValidationResult(journeyId: String): Action[AnyContent] = Action.async {
    Future.successful(
      Ok(partnershipResponse(journeyId))
    )
  }

  private def partnershipResponse(journeyId: String): JsValue = {
    val partnershipEntity: JsValue = Json.toJson(PartnershipIdEntity(
      sautr = Some("1234567890"),
      postCode = Some("AA11AA"),
      registration = "REGISTERED",
      businessVerification = if (journeyId.contains(excludeBv)) None else Some(BvPass),
      bpSafeId = Some("testBpId"),
      identifiersMatch = true
    ))(PartnershipIdEntity.apiFormat)

    val partyType: PartyType = PartyType.fromString(journeyId.split(":").head)
    partyType match {
      case LtdPartnership | ScotLtdPartnership | LtdLiabilityPartnership =>
        val companyProfile = Json.obj(
        "companyProfile" -> Json.obj(
          "companyName" -> "TestPartnership",
          "companyNumber" -> "01234567",
          "dateOfIncorporation" -> "2020-01-01",
          "unsanitisedCHROAddress" -> Json.obj(
            "address_line_1" -> "testLine1",
            "address_line_2" -> "test town",
            "care_of" -> "test name",
            "country" -> "United Kingdom",
            "locality" -> "test city",
            "po_box" -> "123",
            "postal_code" -> "AA11AA",
            "premises" -> "1",
            "region" -> "test region")
          )
        )
        partnershipEntity.as[JsObject] ++ companyProfile
      case _ => partnershipEntity
    }
  }

}
