/*
 * Copyright 2022 HM Revenue & Customs
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
import models.external.{BusinessVerificationStatus, BvPass, MinorEntity}
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class MinorEntityIdentificationStubController @Inject()(mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  final val unincorpAssoc = "UNINCORP_ASSOC"
  final val unincorpAssocExcludeBv = "UNINCORP_ASSOC_EXCLUDE_BV"
  final val trust = "TRUST"
  final val trustExcludeBv = "TRUST_EXCLUDE_BV"
  final val nonUkNonEstablished = "NON_UK_NON_ESTABLISHED"
  final val nonUkNonEstablishedExcludeBv = "NON_UK_NON_ESTABLISHED_EXCLUDE_BV"

  def createJourney(partyType: String): Action[MinorEntityIdJourneyConfig] = Action(parse.json[MinorEntityIdJourneyConfig]) {
    journeyConfig =>
      val journeyId = PartyType.fromString(partyType) match {
        case UnincorpAssoc if !journeyConfig.body.businessVerificationCheck => unincorpAssocExcludeBv
        case UnincorpAssoc => unincorpAssoc
        case Trust if !journeyConfig.body.businessVerificationCheck => trustExcludeBv
        case Trust => trust
        case NonUkNonEstablished if !journeyConfig.body.businessVerificationCheck => nonUkNonEstablishedExcludeBv
        case NonUkNonEstablished => nonUkNonEstablished
      }

      Created(Json.obj("journeyStartUrl" -> JsString(journeyConfig.body.continueUrl + s"?journeyId=$journeyId")))
  }

  def retrieveValidationResult(journeyId: String): Action[AnyContent] = Action.async {
    Future.successful(
      Ok(Json.toJson(
        journeyId match {
          case `unincorpAssocExcludeBv` => unincorpAssocEntity(businessVerification = None)
          case `unincorpAssoc` => unincorpAssocEntity(businessVerification = Some(BvPass))
          case `trustExcludeBv` => trustEntity(businessVerification = None)
          case `trust` => trustEntity(businessVerification = Some(BvPass))
          case `nonUkNonEstablishedExcludeBv` => nonUKCompanyEntity(businessVerification = None)
          case `nonUkNonEstablished` => nonUKCompanyEntity(businessVerification = Some(BvPass))
        }
      )(MinorEntity.apiFormat))
    )
  }

  def unincorpAssocEntity(businessVerification: Option[BusinessVerificationStatus]): MinorEntity = MinorEntity(
    sautr = Some("1234567890"),
    ctutr = None,
    postCode = Some("AA11AA"),
    chrn = Some("1234567890"),
    casc = Some("1234567890"),
    registration = "REGISTERED",
    businessVerification = businessVerification,
    bpSafeId = Some("testBpId"),
    identifiersMatch = true
  )

  def trustEntity(businessVerification: Option[BusinessVerificationStatus]): MinorEntity = MinorEntity(
    sautr = Some("1234567890"),
    ctutr = None,
    postCode = Some("AA11AA"),
    chrn = Some("1234567890"),
    casc = None,
    registration = "REGISTERED",
    businessVerification = businessVerification,
    bpSafeId = Some("testBpId"),
    identifiersMatch = true
  )

  def nonUKCompanyEntity(businessVerification: Option[BusinessVerificationStatus]): MinorEntity = MinorEntity(
    sautr = None,
    ctutr = Some("1234567890"),
    overseas = Some(OverseasIdentifierDetails("1234567890", "EE")),
    postCode = None,
    chrn = None,
    casc = None,
    registration = "REGISTERED",
    businessVerification = businessVerification,
    bpSafeId = Some("testBpId"),
    identifiersMatch = true
  )

}
