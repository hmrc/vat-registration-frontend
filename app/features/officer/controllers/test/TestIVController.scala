/*
 * Copyright 2018 HM Revenue & Customs
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

package features.officer.controllers.test

import javax.inject.{Inject, Singleton}

import common.enums.IVResult
import connectors.KeystoreConnect
import connectors.test.BusinessRegDynamicStubConnector
import controllers.{CommonPlayDependencies, VatRegistrationController}
import features.officer.services.IVService
import forms.test.TestIVForm
import models.view.test.TestIVResponse
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent}
import services.{CurrentProfileSrv, SessionProfile}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

@Singleton
class TestIVController @Inject()(ds: CommonPlayDependencies,
                                 busRegDynStub: BusinessRegDynamicStubConnector,
                                 ivService: IVService,
                                 cpService: CurrentProfileSrv,
                                 val authConnector: AuthConnector,
                                 val keystoreConnector: KeystoreConnect) extends VatRegistrationController(ds) with SessionProfile {

  def setIVStatus(ivPassed:String):Action[AnyContent] = authorised.async{
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          val ivp = ivPassed.toBoolean
          for {
            _ <- ivService.setIvStatus(if(ivp) IVResult.Success else IVResult.FailedIV)
            _ <- cpService.updateIVStatusInCurrentProfile(passed = Some(ivp))
          } yield Ok("ivPassed set to true, the current Profile has been refreshed with this data")
        }
  }


  def show(journeyId:String): Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          val testIVResponse = TestIVResponse(journeyId, IVResult.Success)
          Future.successful(Ok(features.officer.views.html.test.testIVResponse(TestIVForm.form.fill(testIVResponse))))
        }
  }

  def submit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          TestIVForm.form.bindFromRequest().fold(
            badForm =>
              Future.successful(BadRequest(features.officer.views.html.test.testIVResponse(badForm))),
            success => for {
              _ <- busRegDynStub.setupIVOutcome(success.journeyId, success.ivResult)
              _ <- ivService.saveJourneyID(JsObject(Map("journeyLink" -> Json.toJson(s"""/${success.journeyId}"""))))
            } yield success.ivResult match {
              case IVResult.Success => Redirect(features.officer.controllers.routes.IdentityVerificationController.completedIVJourney())
              case _                => Redirect(features.officer.controllers.routes.IdentityVerificationController.failedIVJourney(success.journeyId))
            }
          )
        }
  }
}
