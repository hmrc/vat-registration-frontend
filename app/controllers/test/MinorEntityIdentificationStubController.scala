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

import models.api.{NonUkNonEstablished, PartyType, Trust, UnincorpAssoc}
import models.external.minorentityid.MinorEntityIdJourneyConfig
import models.external.soletraderid.OverseasIdentifierDetails
import models.external.{BvPass, MinorEntity}
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class MinorEntityIdentificationStubController @Inject()(mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  def createJourney(partyType: String): Action[MinorEntityIdJourneyConfig] = Action(parse.json[MinorEntityIdJourneyConfig]) {
    journeyConfig =>
      val journeyId = PartyType.fromString(partyType) match {
        case UnincorpAssoc => "1"
        case Trust => "2"
        case NonUkNonEstablished => "3"
      }

      Created(Json.obj("journeyStartUrl" -> JsString(journeyConfig.body.continueUrl + s"?journeyId=$journeyId")))
  }

  def retrieveValidationResult(journeyId: String): Action[AnyContent] = Action.async {
    Future.successful(
      Ok(Json.toJson(
        journeyId match {
          case "1" => unincorpAssocEntity
          case "2" => trustEntity
          case "3" => nonUKCompanyEntity
        }
      )(MinorEntity.apiFormat))
    )
  }

  val unincorpAssocEntity: MinorEntity = MinorEntity(
    sautr = Some("1234567890"),
    ctutr = None,
    postCode = Some("AA11AA"),
    chrn = Some("1234567890"),
    casc = Some("1234567890"),
    registration = "REGISTERED",
    businessVerification = BvPass,
    bpSafeId = Some("testBpId"),
    identifiersMatch = true
  )

  val trustEntity: MinorEntity = MinorEntity(
    sautr = Some("1234567890"),
    ctutr = None,
    postCode = Some("AA11AA"),
    chrn = Some("1234567890"),
    casc = None,
    registration = "REGISTERED",
    businessVerification = BvPass,
    bpSafeId = Some("testBpId"),
    identifiersMatch = true
  )

  val nonUKCompanyEntity: MinorEntity = MinorEntity(
    sautr = None,
    ctutr = Some("1234567890"),
    overseas = Some(OverseasIdentifierDetails("1234567890", "EE")),
    postCode = None,
    chrn = None,
    casc = None,
    registration = "REGISTERED",
    businessVerification = BvPass,
    bpSafeId = Some("testBpId"),
    identifiersMatch = true
  )

}
