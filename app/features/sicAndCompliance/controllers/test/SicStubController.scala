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

package features.sicAndCompliance.controllers.test

import javax.inject.Inject

import connectors.{ConfigConnector, KeystoreConnect}
import controllers.VatRegistrationControllerNoAux
import features.sicAndCompliance.forms.test.SicStubForm
import features.sicAndCompliance.models.MainBusinessActivityView
import features.sicAndCompliance.models.test.SicStub
import features.sicAndCompliance.services.SicAndComplianceService
import features.sicAndCompliance.views.html.test._
import models.ModelKeys.SIC_CODES_KEY
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, SessionProfile}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class SicStubControllerImpl @Inject()(implicit val messagesApi: MessagesApi,
                                      val configConnect: ConfigConnector,
                                      val keystoreConnector: KeystoreConnect,
                                      val s4LService: S4LService,
                                      val sicAndCompService: SicAndComplianceService,
                                      val authConnector: AuthConnector) extends SicStubController

trait SicStubController extends VatRegistrationControllerNoAux with SessionProfile {
  val configConnect: ConfigConnector
  val keystoreConnector: KeystoreConnect
  val s4LService: S4LService
  val sicAndCompService: SicAndComplianceService

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          for {
            sicCodes  <- s4LService.fetchAndGet[SicStub]
            sicStub   =  SicStub(
              sicCodes.map(_.sicCode1.getOrElse("")),
              sicCodes.map(_.sicCode2.getOrElse("")),
              sicCodes.map(_.sicCode3.getOrElse("")),
              sicCodes.map(_.sicCode4.getOrElse(""))
            )
            form       =  SicStubForm.form.fill(sicStub)
          } yield Ok(sic_stub(form))
        }
  }

  def submit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          SicStubForm.form.bindFromRequest().fold(
            badForm => Future.successful(BadRequest(sic_stub(badForm))),
            data    => for {
              _ <- s4LService.save[SicStub](data)
              sicCodesList = data.fullSicCodes.map(configConnect.getSicCodeDetails)
              _ <- keystoreConnector.cache(SIC_CODES_KEY, sicCodesList)
              _ <- sicAndCompService.submitSicCodes(sicCodesList)
            } yield {
              if (data.sicCodes.lengthCompare(1) == 0) {
                if (sicAndCompService.needComplianceQuestions(sicCodesList)) {
                  Redirect(features.sicAndCompliance.controllers.routes.SicAndComplianceController.showComplianceIntro())
                } else {
                  Redirect(controllers.routes.TradingDetailsController.tradingNamePage())
                }
              } else {
                Redirect(features.sicAndCompliance.controllers.routes.SicAndComplianceController.showMainBusinessActivity())
              }
            }
          )
        }
  }
}