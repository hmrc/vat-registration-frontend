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
import models.ModelKeys.SIC_CODES_KEY
import models._
import models.api.SicCode
import models.view.sicAndCompliance.BusinessActivityDescription
import models.view.test.SicStub
import play.api.mvc._
import services.{CommonService, RegistrationService, S4LService}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class ComplianceExitController (ds: CommonPlayDependencies)(implicit vrs: RegistrationService, s4LService: S4LService)
  extends VatRegistrationController(ds) with CommonService{

  def submitAndExit(elements: List[ElementPath])(implicit hc: HeaderCarrier): Future[Result] =
    for {
      _ <- vrs.deleteElements(elements)
      _ <- vrs.submitSicAndCompliance()
    } yield Redirect(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show())


  def clearComplianceContainer(implicit hc: HeaderCarrier): Future[S4LVatSicAndCompliance] =
    viewModel[BusinessActivityDescription]().
      fold(S4LVatSicAndCompliance())(bad => S4LVatSicAndCompliance(description = Some(bad)))

  def selectNextPage(sicCodesList: List[SicCode])(implicit hc: HeaderCarrier):  Future[Result] = {
    ComplianceQuestions(sicCodesList) match {
      case NoComplianceQuestions => submitAndExit(List(CulturalCompliancePath, FinancialCompliancePath, LabourCompliancePath))
      case _ =>
        Redirect(controllers.sicAndCompliance.routes.ComplianceIntroductionController.show()).pure
    }
  }

}
