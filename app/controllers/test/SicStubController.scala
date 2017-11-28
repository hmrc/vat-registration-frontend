/*
 * Copyright 2017 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}

import connectors.{ConfigConnector, KeystoreConnect}
import controllers.CommonPlayDependencies
import controllers.sicAndCompliance.ComplianceExitController
import forms.test.SicStubForm
import models.ModelKeys.SIC_CODES_KEY
import models.view.test.SicStub
import play.api.mvc.{Action, AnyContent}
import services.{RegistrationService, S4LService, SessionProfile}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

@Singleton
class SicStubController @Inject()(ds: CommonPlayDependencies,
                                  configConnect: ConfigConnector,
                                  val keystoreConnector: KeystoreConnect,
                                  implicit val s4LService: S4LService,
                                  override val authConnector: AuthConnector,
                                  override implicit val vrs: RegistrationService) extends ComplianceExitController(ds, authConnector, vrs, s4LService) with SessionProfile {

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
          } yield Ok(views.html.pages.test.sic_stub(form))
        }
  }

  def submit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          SicStubForm.form.bindFromRequest().fold(
            badForm => BadRequest(views.html.pages.test.sic_stub(badForm)).pure,
            data    => s4LService.save[SicStub](data).flatMap { _ =>
              val sicCodesList = data.fullSicCodes.map(configConnect.getSicCodeDetails)
              keystoreConnector.cache(SIC_CODES_KEY, sicCodesList).flatMap { _ =>
                data.sicCodes match {
                  case head :: Nil  => Redirect(controllers.sicAndCompliance.routes.MainBusinessActivityController.redirectToNext()).pure
                  case _ :: tail    => Redirect(controllers.sicAndCompliance.routes.MainBusinessActivityController.show()).pure
                  case Nil          => selectNextPage(sicCodesList)
                }
              }
            }
          )
        }
  }
}
