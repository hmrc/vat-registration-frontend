/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

@Singleton
class EmailVerificationStubController @Inject()(mcc: MessagesControllerComponents)
  extends FrontendController(mcc) {

  def requestEmailVerificationPasscode: Action[JsValue] = Action(parse.json) {
     request =>
      val email = (request.body \ "email").as[String]

      email match {
        case "test@test.com" => Conflict
        case "invalid@test.com" => Forbidden
        case _ => Created
      }
  }

  def verifyEmailVerificationPasscode: Action[JsValue] = Action(parse.json) {
     request =>
      val passcode = (request.body \ "passcode").as[String]

      passcode match {
        case "123456" => NoContent
        case "987654" => NotFound(Json.obj("code" -> "PASSCODE_NOT_FOUND"))
        case "666666" => NotFound(Json.obj("code" -> "PASSCODE_MISMATCH"))
        case "234567" => Forbidden(Json.obj("code" -> "MAX_PASSCODE_ATTEMPTS_EXCEEDED"))
        case _ => Created
      }
  }

}
