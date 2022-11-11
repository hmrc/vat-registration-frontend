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

package controllers.business

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.VatCorrespondenceForm
import models.{English, Welsh}
import play.api.mvc.{Action, AnyContent}
import services.BusinessService.VatCorrespondenceInWelsh
import services.{BusinessService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.business.VatCorrespondence

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatCorrespondenceController @Inject()(val authConnector: AuthConnector,
                                            val sessionService: SessionService,
                                            val businessService: BusinessService,
                                            view: VatCorrespondence)
                                           (implicit val appConfig: FrontendAppConfig,
                                            val executionContext: ExecutionContext,
                                            baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          business <- businessService.getBusiness
          form = business.welshLanguage match {
            case Some(true) => VatCorrespondenceForm().fill(Welsh)
            case Some(false) => VatCorrespondenceForm().fill(English)
            case _ => VatCorrespondenceForm()
          }
        } yield Ok(view(form))

  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        VatCorrespondenceForm().bindFromRequest().fold(
          errors => Future.successful(BadRequest(view(errors))),
          success => {
            val vatCorrespondenceInWelsh = success match {
              case Welsh => VatCorrespondenceInWelsh(true)
              case _ => VatCorrespondenceInWelsh(false)
            }
            businessService.updateBusiness(vatCorrespondenceInWelsh).map {
              _ =>
                  Redirect(controllers.business.routes.ContactPreferenceController.showContactPreference)
            }
          }
        )
  }

}
