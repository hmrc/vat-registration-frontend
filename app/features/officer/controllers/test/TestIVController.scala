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
import forms.test.TestIVForm
import models.CurrentProfile
import models.view.test.TestIVResponse
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, SessionProfile}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

@Singleton
class TestIVController @Inject()(ds: CommonPlayDependencies,
                                 busRegDynStub: BusinessRegDynamicStubConnector,
                                 val s4lService: S4LService,
                                 val authConnector: AuthConnector,
                                 val keystoreConnector: KeystoreConnect) extends VatRegistrationController(ds) with SessionProfile {

  def setIVStatus(ivPassed: Boolean):Action[AnyContent] = authorised.async{
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          keystoreConnector.cache[CurrentProfile]("CurrentProfile", profile.copy(ivPassed = Some(ivPassed))) map {
            _ => Ok(s"ivPassed set to $ivPassed in Current Profile (keystore)")
          }
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
              _ <- s4lService.save("IVJourneyID", success.journeyId)
            } yield success.ivResult match {
              case IVResult.Success => Redirect(features.officer.controllers.routes.IdentityVerificationController.completedIVJourney())
              case _                => Redirect(features.officer.controllers.routes.IdentityVerificationController.failedIVJourney(success.journeyId))
            }
          )
        }
  }
}
