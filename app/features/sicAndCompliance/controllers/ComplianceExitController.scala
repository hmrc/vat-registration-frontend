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

package controllers.sicAndCompliance {

  import javax.inject.Singleton

  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import features.sicAndCompliance.services.SicAndComplianceService
  import models.S4LVatSicAndCompliance.dropLabour
  import models._
  import models.api.SicCode
  import models.view.sicAndCompliance.BusinessActivityDescription
  import play.api.mvc._
  import services.{RegistrationService, S4LService}
  import uk.gov.hmrc.http.HeaderCarrier
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  import scala.concurrent.Future

  @Singleton
  class


  ComplianceExitController(ds: CommonPlayDependencies,
                                 val authConnector: AuthConnector,
                                 implicit val vrs: RegistrationService,
                                 val sicAndCompService: SicAndComplianceService,
                                 implicit val s4lService: S4LService) extends VatRegistrationController(ds) {

    def selectNextPage(sicCodesList: List[SicCode])(implicit hc: HeaderCarrier, currentProfile: CurrentProfile): Future[Result] = {
      ComplianceQuestions(sicCodesList) match {
        case NoComplianceQuestions => for {
          container <- sicAndCompService.dropLabour
          _         <- sicAndCompService.updateSicAndCompliance(None)
        } yield Redirect(features.bankAccountDetails.routes.BankAccountDetailsController.showHasCompanyBankAccountView())
        case _ => Redirect(controllers.sicAndCompliance.routes.ComplianceIntroductionController.show()).pure
      }
    }
  }
}
