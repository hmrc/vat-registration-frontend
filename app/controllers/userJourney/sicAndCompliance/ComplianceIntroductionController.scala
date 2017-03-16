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

package controllers.userJourney.sicAndCompliance

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import play.api.mvc._
import services.S4LService

class ComplianceIntroductionController @Inject()(s4LService: S4LService, ds: CommonPlayDependencies) extends VatRegistrationController(ds) {

  def show: Action[AnyContent] = authorised(implicit user => implicit request => {
      Ok(views.html.pages.compliance_introduction())
  })

  def submit: Action[AnyContent] = authorised(implicit user => implicit request => {
    Redirect(controllers.userJourney.sicAndCompliance.routes.CulturalComplianceQ1Controller.show())
  })
}
