/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.Inject
import config.AuthClientConnector
import connectors.KeystoreConnector
import connectors.test.BusinessRegDynamicStubConnector
import controllers.BaseController
import forms.test.TestIVForm
import models.{CurrentProfile, IVResult}
import models.view.test.TestIVResponse
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, SessionProfile}

import scala.concurrent.Future

class TestIVControllerImpl @Inject()(val busRegDynStub: BusinessRegDynamicStubConnector,
                                     val s4lService: S4LService,
                                     val messagesApi: MessagesApi,
                                     val authConnector: AuthClientConnector,
                                     val keystoreConnector: KeystoreConnector) extends TestIVController

trait TestIVController extends BaseController with SessionProfile {
  val busRegDynStub: BusinessRegDynamicStubConnector
  val s4lService: S4LService

  def setIVStatus(ivPassed: Boolean):Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      keystoreConnector.cache[CurrentProfile]("CurrentProfile", profile.copy(ivPassed = Some(ivPassed))) map {
        _ => Ok(s"ivPassed set to $ivPassed in Current Profile (keystore)")
      }
  }


  def show(journeyId:String): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      val testIVResponse = TestIVResponse(journeyId, IVResult.Success)
      Future.successful(Ok(views.html.test.testIVResponse(TestIVForm.form.fill(testIVResponse))))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      TestIVForm.form.bindFromRequest().fold(
        badForm =>
          Future.successful(BadRequest(views.html.test.testIVResponse(badForm))),
        success => for {
          _ <- busRegDynStub.setupIVOutcome(success.journeyId, success.ivResult)
          _ <- s4lService.save("IVJourneyID", success.journeyId)
        } yield success.ivResult match {
          case IVResult.Success => Redirect(controllers.routes.IdentityVerificationController.completedIVJourney())
          case _                => Redirect(controllers.routes.IdentityVerificationController.failedIVJourney(success.journeyId))
        }
      )
  }
}
