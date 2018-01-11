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

package models

import models.api.SicCode
import play.api.mvc.Call

sealed trait ComplianceQuestions {

  def firstQuestion: Call

}


case object LabourComplianceQuestions extends ComplianceQuestions {

  override def firstQuestion: Call = controllers.sicAndCompliance.labour.routes.CompanyProvideWorkersController.show()

}

case object NoComplianceQuestions extends ComplianceQuestions {

  override def firstQuestion: Call = features.bankAccountDetails.routes.BankAccountDetailsController.showHasCompanyBankAccountView()

}

object ComplianceQuestions {

  type SicCodeMap = Map[ComplianceQuestions, Vector[String]]

  implicit val sicCodeMaps: SicCodeMap = Map(
    LabourComplianceQuestions -> Vector(
      "01610", "41201", "42110", "42910", "42990",
      "43120", "43999", "78200", "80100", "81210",
      "81221", "81222", "81223", "81291", "81299"))

  def apply(sicCodes: Array[String])(implicit m: SicCodeMap): ComplianceQuestions = m.collectFirst {
    case (q, cs) if sicCodes.nonEmpty && sicCodes.forall(cs.contains) => q
  }.getOrElse(NoComplianceQuestions)

  def apply(sicCodes: List[SicCode])(implicit m: SicCodeMap): ComplianceQuestions = m.collectFirst {
    case (q, cs) if sicCodes.nonEmpty && sicCodes.forall(code =>
      code.id.length == 8  && cs.contains(code.id.substring(0, 5))) => q
  }.getOrElse(NoComplianceQuestions)

}
