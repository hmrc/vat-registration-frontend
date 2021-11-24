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

package controllers.registration.business

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.BaseController
import forms.CompanyContactDetailsForm
import javax.inject.{Inject, Singleton}
import models.CompanyContactDetails
import play.api.mvc.{Action, AnyContent}
import services.{AddressLookupService, BusinessContactService, PrePopulationService, SessionProfile}
import views.html.business_contact_details

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessContactDetailsController @Inject()(val authConnector: AuthClientConnector,
                                                 val keystoreConnector: KeystoreConnector,
                                                 val businessContactService: BusinessContactService,
                                                 val prepopService: PrePopulationService,
                                                 val addressLookupService: AddressLookupService,
                                                 business_contact_details: business_contact_details
                                                )
                                                (implicit appConfig: FrontendAppConfig,
                                                 val executionContext: ExecutionContext,
                                                 baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  lazy val dropoutUrl: String = appConfig.servicesConfig.getString("microservice.services.otrs.url")
  private val companyContactForm = CompanyContactDetailsForm.form

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          businessContact <- businessContactService.getBusinessContact
          form = businessContact.companyContactDetails.fold(companyContactForm)(x => companyContactForm.fill(x))
        } yield Ok(business_contact_details(form))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        companyContactForm.bindFromRequest.fold(
          formError => {
            Future.successful(BadRequest(business_contact_details(CompanyContactDetailsForm.transformErrors(formError))))
          },
          contact => businessContactService.updateBusinessContact[CompanyContactDetails](contact) map {
            _ => Redirect(controllers.routes.ContactPreferenceController.showContactPreference)
          }
        )
  }
}
