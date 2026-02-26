/*
 * Copyright 2026 HM Revenue & Customs
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
import forms.BusinessWebsiteAddressForm
import models.CurrentProfile
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.BusinessService.Website
import services.{BusinessService, SessionProfile, SessionService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier
import views.html.business.BusinessWebsiteAddress

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessWebsiteAddressController @Inject()(val sessionService: SessionService,
                                                 val authConnector: AuthConnector,
                                                 val businessService: BusinessService,
                                                 view: BusinessWebsiteAddress)
                                                (implicit appConfig: FrontendAppConfig,
                                                 val executionContext: ExecutionContext,
                                                 baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          business <- businessService.getBusiness
          form = business.website.fold(BusinessWebsiteAddressForm.form)(BusinessWebsiteAddressForm.form.fill)
        } yield Ok(view(routes.BusinessWebsiteAddressController.submit, form))


  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        BusinessWebsiteAddressForm.form.bindFromRequest().fold(
          badForm => {
            val invalidWebsite = badForm.data.get(BusinessWebsiteAddressForm.businessWebsiteAddressKey)

            if (invalidWebsite.exists(value => value.endsWith("/"))) {
              val stripped = invalidWebsite.map(_.dropRight(1)).getOrElse("")
              BusinessWebsiteAddressForm.form.bind(Map(BusinessWebsiteAddressForm.businessWebsiteAddressKey -> stripped)).fold(
                badForm => Future.successful(BadRequest(view(routes.BusinessWebsiteAddressController.submit, badForm))),
                businessWebsiteAddress => formSuccess(businessWebsiteAddress)
              )
            } else {
              Future.successful(BadRequest(view(routes.BusinessWebsiteAddressController.submit, badForm)))
            }
          },
          businessWebsiteAddress => formSuccess(businessWebsiteAddress)
        )
  }

  private def formSuccess(businessWebsiteAddress: String)(implicit cp: CurrentProfile, hc: HeaderCarrier, request: Request[_]): Future[Result] =
    businessService.updateBusiness(Website(businessWebsiteAddress)).map {
      _ =>
          Redirect(controllers.business.routes.VatCorrespondenceController.show)
    }

}
