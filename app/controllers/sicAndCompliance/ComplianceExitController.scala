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

package controllers.sicAndCompliance

import controllers.{CommonPlayDependencies, VatRegistrationController}
import play.api.mvc._
import services.RegistrationService
import uk.gov.hmrc.play.http.HeaderCarrier

class ComplianceExitController (ds: CommonPlayDependencies, vrs: RegistrationService)
  extends VatRegistrationController(ds) {

  def submitAndExit (implicit hc: HeaderCarrier): Call = {
    vrs.submitSicAndCompliance().map(_ => ())
    controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show()
  }

}
