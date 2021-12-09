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

import models.api.{Partnership, PartyType}
import models.external.partnershipid.PartnershipIdJourneyConfig
import models.external.{BvPass, PartnershipIdEntity}
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class PartnershipIdentificationStubController @Inject()(mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  final val partnership = "PARTNERSHIP"
  final val partnershipExcludeBv = "PARTNERSHIP_EXCLUDE_BV"

  def createJourney(partyType: String): Action[PartnershipIdJourneyConfig] = Action(parse.json[PartnershipIdJourneyConfig]) {
    journeyConfig =>
      val journeyId = PartyType.fromString(partyType) match {
        case Partnership if !journeyConfig.body.businessVerificationCheck => partnershipExcludeBv
        case Partnership => partnership
      }

      Created(Json.obj("journeyStartUrl" -> JsString(journeyConfig.body.continueUrl + s"?journeyId=$journeyId")))
  }

  def retrieveValidationResult(journeyId: String): Action[AnyContent] = Action.async {
    Future.successful(
      Ok(Json.toJson(PartnershipIdEntity(
        sautr = Some("1234567890"),
        postCode = if (journeyId.equals("1")) Some("AA11AA") else None,
        registration = "REGISTERED",
        businessVerification = if (journeyId.equals(partnershipExcludeBv)) None else Some(BvPass),
        bpSafeId = Some("testBpId"),
        identifiersMatch = true
      ))(PartnershipIdEntity.apiFormat))
    )
  }

}
