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

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.fileUpload.{callback_check, file_upload}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class FileUploadController @Inject()(fileUploadView: file_upload,
                                     callbackCheck: callback_check,
                                     upscanService: UpscanService,
                                     val authConnector: AuthConnector,
                                     val keystoreConnector: KeystoreConnector
                                    )(implicit appConfig: FrontendAppConfig,
                                      val executionContext: ExecutionContext,
                                      baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show(): Action[AnyContent] = isAuthenticatedWithProfile() { implicit request => profile =>
    upscanService.initiateUpscan(profile.registrationId).map{ upscanResponse =>
      Ok(fileUploadView(upscanResponse))
    }
  }

  def callbackCheck(reference: String): Action[AnyContent] = isAuthenticatedWithProfile() { implicit request => profile =>
    upscanService.fetchUpscanFileDetails(profile.registrationId, reference).map{ upscanDetails =>
      Ok(callbackCheck(upscanDetails))
    }
  }

}
