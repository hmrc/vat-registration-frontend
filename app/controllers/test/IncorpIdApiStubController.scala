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

import config.FrontendAppConfig
import forms.test.IncorpIdStubForm
import models.api._
import models.external.incorporatedentityid.IncorpIdJourneyConfig
import models.external.{BusinessVerificationStatus, BvFail, BvPass, IncorporatedEntity}
import models.test.IncorpStubData
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.test.IncorpIdStubPage

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncorpIdApiStubController @Inject()(mcc: MessagesControllerComponents,
                                          sessionService: SessionService,
                                          stubView: IncorpIdStubPage)
                                         (implicit ex: ExecutionContext, appConfig: FrontendAppConfig)
  extends FrontendController(mcc) {

  final val ukCompany = "UK_COMPANY"
  final val ukCompanyExcludeBv = "UK_COMPANY_EXCLUDE_BV"
  final val regSociety = "REG_SOCIETY"
  final val regSocietyExcludeBv = "REG_SOCIETY_EXCLUDE_BV"
  final val charitableOrg = "CHARITABLE_ORG"
  final val charitableOrgExcludeBv = "CHARITABLE_ORG_EXCLUDE_BV"

  def createJourney(partyType: String): Action[IncorpIdJourneyConfig] = Action(parse.json[IncorpIdJourneyConfig]).async {
    implicit request =>
      val journeyId = PartyType.fromString(partyType) match {
        case UkCompany if !request.body.businessVerificationCheck => ukCompanyExcludeBv
        case UkCompany => ukCompany
        case RegSociety if !request.body.businessVerificationCheck => regSocietyExcludeBv
        case RegSociety => regSociety
        case CharitableOrg if !request.body.businessVerificationCheck => charitableOrgExcludeBv
        case CharitableOrg => charitableOrg
      }

      sessionService.cache("continueUrl", request.body.continueUrl).map { _ =>
        Created(Json.obj("journeyStartUrl" -> JsString(routes.IncorpIdApiStubController.showStubPage(journeyId).url)))
      }
  }

  def showStubPage(journeyId: String): Action[AnyContent] = Action { implicit request =>
    Ok(stubView(IncorpIdStubForm.form, journeyId))
  }

  def submitStubPage(journeyId: String): Action[AnyContent] = Action.async { implicit request =>
    IncorpIdStubForm.form.bindFromRequest.fold(
      errors =>
        Future.successful(BadRequest(stubView(errors, journeyId))),
      values =>
        sessionService.cache("incorpStubResponse", values).flatMap { _ =>
          sessionService.fetchAndGet[String]("continueUrl").collect {
            case Some(continueUrl) =>
              Redirect(continueUrl + s"?journeyId=$journeyId")
          }
        }
    )
  }

  def getDetails(journeyId: String): Action[AnyContent] = Action.async { implicit request =>
    sessionService.fetchAndGet[IncorpStubData]("incorpStubResponse").map {
      case Some(data) =>
        Ok(Json.toJson(IncorporatedEntity(
          companyName = Some("Test company"),
          companyNumber = data.crn,
          ctutr = if (!journeyId.contains(charitableOrg)) Some(data.utr.get) else None,
          chrn = if (journeyId.contains(charitableOrg)) Some("123567890") else None,
          dateOfIncorporation = Some(LocalDate.of(2020, 1, 1)),
          identifiersMatch = true,
          registration = "REGISTERED",
          businessVerification = if (data.passBv) { Some(BvPass) } else { Some(BvFail) },
          bpSafeId = Some("testBpId")
        ))(IncorporatedEntity.apiFormat))
      case _ =>
        Ok(Json.toJson(IncorporatedEntity(
          companyName = Some("Test company"),
          companyNumber = "12345678",
          ctutr = if (!journeyId.contains(charitableOrg)) Some("123567890") else None,
          chrn = if (journeyId.contains(charitableOrg)) Some("123567890") else None,
          dateOfIncorporation = Some(LocalDate.of(2020, 1, 1)),
          identifiersMatch = true,
          registration = "REGISTERED",
          businessVerification = businessVerificationStatus(journeyId),
          bpSafeId = Some("testBpId")
        ))(IncorporatedEntity.apiFormat))
    }
  }

  private def businessVerificationStatus(journeyId: String): Option[BusinessVerificationStatus] = {
    journeyId match {
      case `ukCompanyExcludeBv` | `regSocietyExcludeBv` | `charitableOrgExcludeBv` => None
      case _ => Some(BvPass)
    }
  }
}
