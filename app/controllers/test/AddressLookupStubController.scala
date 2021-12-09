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

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.ConfigConnector
import controllers.BaseController
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, SessionProfile, SessionService, SicAndComplianceService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupStubController @Inject()(val configConnect: ConfigConnector,
                                            val sessionService: SessionService,
                                            val s4LService: S4LService,
                                            val sicAndCompService: SicAndComplianceService,
                                            val authConnector: AuthClientConnector)
                                           (implicit appConfig: FrontendAppConfig,
                                            val executionContext: ExecutionContext,
                                            baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def createJourney: Action[JsValue] = Action.async(parse.json) { implicit request =>
    (request.body \ "options" \ "continueUrl").validate[String].asOpt match {
      case Some(continueUrl) =>
        Future.successful(Accepted("").withHeaders(LOCATION -> s"$continueUrl?id=1"))
      case None =>
        Future.successful(BadRequest("[AddressLookupStubController][createJourney] No continue url was provided"))
    }
  }

  def retrieve(id: String): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(
      Ok(Json.obj(
        "auditRef" -> "auditRef",
        "id" -> id,
        "address" -> Json.obj(
          "lines" -> Json.arr("line1", "line2"),
          "postcode" -> "AB12 3YZ",
          "country" -> Json.obj(
            "code" -> "GB",
            "name" -> "United Kingdom"
          )
        )
      ))
    )
  }

}
