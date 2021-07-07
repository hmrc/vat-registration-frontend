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

package controllers.registration.applicant

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import forms.LeadPartnerForm
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, SessionProfile}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.lead_partner_entity_type

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LeadPartnerEntityController @Inject()(val authConnector: AuthConnector,
                                            val keystoreConnector: KeystoreConnector,
                                            val applicantDetailsService: ApplicantDetailsService,
                                            leadPartnerEntityPage: lead_partner_entity_type
                                           )(implicit appConfig: FrontendAppConfig,
                                             val executionContext: ExecutionContext,
                                             baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def showLeadPartnerEntityType: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        Future.successful(Ok(leadPartnerEntityPage(form = LeadPartnerForm.form))) // TO DO This will be updated when the new API is implemented
  }

  def submitLeadPartnerEntity: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        LeadPartnerForm.form.bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(leadPartnerEntityPage(formWithErrors))),
          leadPartnerEntity =>
            Future.successful(NotImplemented) // TO DO This will be updated to use the new API
        )
  }


}
