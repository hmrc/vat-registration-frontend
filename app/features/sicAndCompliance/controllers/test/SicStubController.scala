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

import javax.inject.{Inject, Singleton}

import connectors.{ConfigConnector, KeystoreConnect}
import controllers.{CommonPlayDependencies, VatRegistrationController}
import features.sicAndCompliance.services.SicAndComplianceService
import forms.test.SicStubForm
import models.ModelKeys.SIC_CODES_KEY
import features.sicAndCompliance.models.MainBusinessActivityView
import models.view.test.SicStub
import features.sicAndCompliance.views.html.test._
import play.api.mvc.{Action, AnyContent}
import services.{RegistrationService, S4LService, SessionProfile}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

@Singleton
class SicStubController @Inject()(ds: CommonPlayDependencies,
                                  configConnect: ConfigConnector,
                                  val keystoreConnector: KeystoreConnect,
                                  implicit val s4LService: S4LService,
                                  val sicAndCompService: SicAndComplianceService,
                                  val authConnector: AuthConnector,
                                  implicit val vrs: RegistrationService) extends VatRegistrationController(ds) with SessionProfile {

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
            badForm => BadRequest(sic_stub(badForm)).pure,
            data    => s4LService.save[SicStub](data).flatMap { _ =>
              val sicCodesList = data.fullSicCodes.map(configConnect.getSicCodeDetails)
              keystoreConnector.cache(SIC_CODES_KEY, sicCodesList).flatMap { _ =>
                if (data.sicCodes.lengthCompare(1) == 0) {
                  sicAndCompService.updateSicAndCompliance(MainBusinessActivityView(sicCodesList.head)) map { _ =>
                    if (sicAndCompService.needComplianceQuestions(sicCodesList)) {
                      features.sicAndCompliance.controllers.routes.SicAndComplianceController.submitComplianceIntro()
                    } else {
                      features.bankAccountDetails.routes.BankAccountDetailsController.showHasCompanyBankAccountView()
                    }
                  }
                } else {
                  Future.successful(features.sicAndCompliance.controllers.routes.SicAndComplianceController.showMainBusinessActivity())
                }
              } map Redirect
            }
          )
        }
  }
}
