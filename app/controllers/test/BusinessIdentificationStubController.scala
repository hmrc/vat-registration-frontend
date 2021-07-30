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

import models.api.{PartyType, Trust, UnincorpAssoc}
import models.external.businessid.BusinessIdJourneyConfig
import models.external.{BusinessIdEntity, BvPass}
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class BusinessIdentificationStubController @Inject()(mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  def createJourney(partyType: String): Action[BusinessIdJourneyConfig] = Action(parse.json[BusinessIdJourneyConfig]) {
    journeyConfig =>
      val journeyId = PartyType.fromString(partyType) match {
        case UnincorpAssoc => "1"
        case Trust => "2"
      }

      Created(Json.obj("journeyStartUrl" -> JsString(journeyConfig.body.continueUrl + s"?journeyId=$journeyId")))
  }

  def retrieveValidationResult(journeyId: String): Action[AnyContent] = Action.async {
    Future.successful(
      Ok(Json.toJson(BusinessIdEntity(
        sautr = Some("1234567890"),
        postCode = Some("AA11AA"),
        chrn = Some("1234567890"),
        casc = if (journeyId.equals("1")) Some("1234567890") else None,
        registration = "REGISTERED",
        businessVerification = BvPass,
        bpSafeId = Some("testBpId"),
        identifiersMatch = true
      ))(BusinessIdEntity.apiFormat))
    )
  }

}
