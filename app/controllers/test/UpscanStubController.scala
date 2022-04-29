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

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.UpscanConnector
import connectors.test.TestUpscanCallbackConnector
import controllers.BaseController
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.InternalServerException

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UpscanStubController @Inject()(val authConnector: AuthConnector,
                                     val sessionService: SessionService,
                                     upscanCallbackConnector: TestUpscanCallbackConnector,
                                     upscanConnector: UpscanConnector)
                                    (implicit appConfig: FrontendAppConfig,
                                     val executionContext: ExecutionContext,
                                     val bcc: BaseControllerComponents) extends BaseController with SessionProfile {

  private val testOnlyUpscanCallbackUrl = "TEST_UPSCAN_CALLBACK"
  private val testOnlyUpscanSuccessUrl = "TEST_UPSCAN_SUCCESS"

  def upscanInitiate(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val callbackUrl = (request.body \ "callbackUrl").as[String]
    val successUrl = (request.body \ "successRedirect").as[String]

    for {
      _ <- sessionService.cache(testOnlyUpscanCallbackUrl, callbackUrl)
      _ <- sessionService.cache(testOnlyUpscanSuccessUrl, successUrl)
    } yield Ok(Json.obj(
      "reference" -> UUID.randomUUID(),
      "uploadRequest" -> Json.obj(
        "href" -> "/register-for-vat/test-only/upscan/upload-response",
        "fields" -> Json.obj("test" -> "field")
      )
    ))
  }

  def uploadResponse: Action[AnyContent] = isAuthenticatedWithProfile() { implicit request => implicit profile =>
    for {
      optCallbackUrl <- sessionService.fetchAndGet[String](testOnlyUpscanCallbackUrl)
      optSuccessUrl <- sessionService.fetchAndGet[String](testOnlyUpscanSuccessUrl)
      reference = request.session.get("reference").get
      callbackUrl = optCallbackUrl.getOrElse(throw new InternalServerException("Callback URL couldn't be retrieved from session"))
      successUrl = optSuccessUrl.getOrElse(throw new InternalServerException("Success URL couldn't be retrieved from session"))
      unfinishedUpscan <- upscanConnector.fetchUpscanFileDetails(profile.registrationId, reference)
      _ <- upscanCallbackConnector.postUpscanResponse(callbackUrl, reference, unfinishedUpscan.attachmentType)
    } yield Redirect(successUrl)
  }

}
