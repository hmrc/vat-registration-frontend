/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BankAccountReputationStubController @Inject()(mcc: MessagesControllerComponents)
                                                   (implicit ec: ExecutionContext) extends FrontendController(mcc) {

  def validateBankDetails(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val accountNumber = (request.body \\ "sortCode").head.as[String]

    val status = accountNumber.take(2) match {
      case "11" => "no"
      case "22" => "indeterminate"
      case _ => "yes"
    }

    Future.successful(
      Ok(Json.obj(
        "accountNumberWithSortCodeIsValid" -> status,
        "nonStandardAccountDetailsRequiredForBacs" -> "no",
        "sortCodeIsPresentOnEISCD" -> "yes",
        "supportsBACS" -> "yes",
        "ddiVoucherFlag" -> "no",
        "directDebitsDisallowed" -> "yes",
        "directDebitInstructionsDisallowed" -> "yes",
        "iban" -> "GB59 HBUK 1234 5678",
        "sortCodeBankName" -> "Lloyds"
      )
    ))
  }

}
