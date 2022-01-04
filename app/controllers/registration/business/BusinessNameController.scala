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

package controllers.registration.business

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.BusinessNameForm
import models.external.{IncorporatedEntity, MinorEntity}
import play.api.mvc.{Action, AnyContent}
import services.{ApplicantDetailsService, SessionProfile, SessionService}
import uk.gov.hmrc.http.InternalServerException
import views.html.BusinessName

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessNameController @Inject()(val sessionService: SessionService,
                                       val authConnector: AuthClientConnector,
                                       val applicantDetailsService: ApplicantDetailsService,
                                       view: BusinessName)
                                      (implicit appConfig: FrontendAppConfig,
                                       val executionContext: ExecutionContext,
                                       baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        applicantDetailsService.getCompanyName.map {
          case Some(companyName) => Ok(view(BusinessNameForm().fill(companyName)))
          case None => Ok(view(BusinessNameForm()))
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        BusinessNameForm().bindFromRequest.fold(
          errors =>
            Future.successful(BadRequest(view(errors))),
          success => {
            for {
              entity <- applicantDetailsService.getApplicantDetails.map(_.entity)
              updatedEntity = entity match {
                case Some(entity: IncorporatedEntity) => entity.copy(companyName = Some(success))
                case Some(entity: MinorEntity) => entity.copy(companyName = Some(success))
                case _ => throw new InternalServerException("Invalid entity for business name capture page")
              }
              _ <- applicantDetailsService.saveApplicantDetails(updatedEntity)
            } yield {
              Redirect(routes.TradingNameController.show)
            }
          }
        )
  }

}
