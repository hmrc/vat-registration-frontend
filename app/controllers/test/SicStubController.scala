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

import javax.inject.Inject

import controllers.CommonPlayDependencies
import controllers.sicAndCompliance.ComplianceExitController
import forms.test.SicStubForm
import models.ModelKeys.{REGISTERING_OFFICER_KEY, SIC_CODES_KEY}
import models._
import models.api.{CompletionCapacity, SicCode}
import models.view.test.SicStub
import models.view.vatLodgingOfficer.CompletionCapacityView
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, S4LService, VatRegistrationService}
import cats.data.OptionT
import connectors.ConfigConnect
class SicStubController @Inject()(ds: CommonPlayDependencies)
                                 (implicit s4LService: S4LService, vrs: VatRegistrationService, configConnect: ConfigConnect)
  extends ComplianceExitController(ds) {
  import cats.syntax.flatMap._

  def show: Action[AnyContent] = authorised.async(body = implicit user => implicit request =>
    for {
      sicCodes <- s4LService.fetchAndGet[SicStub]()
      sicStub = SicStub(
        sicCodes.map(_.sicCode1.getOrElse("")),
        sicCodes.map(_.sicCode2.getOrElse("")),
        sicCodes.map(_.sicCode3.getOrElse("")),
        sicCodes.map(_.sicCode4.getOrElse(""))
      )
      form = SicStubForm.form.fill(sicStub)
    } yield Ok(views.html.pages.test.sic_stub(form)))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
      SicStubForm.form.bindFromRequest().fold(
        badForm => BadRequest(views.html.pages.test.sic_stub(badForm)).pure,
        data => {
          keystoreConnector.cache(SIC_CODES_KEY,  configConnect.getSicCodesListFromCodes(data.fullSicCodes))
          data.sicCodes match {
            case head :: Nil =>  Redirect(controllers.sicAndCompliance.routes.MainBusinessActivityController.show()).pure
            case _ :: tail => Redirect(controllers.sicAndCompliance.routes.MainBusinessActivityController.show()).pure
            case Nil => Redirect(controllers.sicAndCompliance.routes.ComplianceIntroductionController.show()).pure
          }
        }))


}
