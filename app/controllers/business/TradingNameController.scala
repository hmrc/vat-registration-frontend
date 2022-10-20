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

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import forms.TradingNameForm
import models.api.{NETP, NonUkNonEstablished}
import play.api.mvc.{Action, AnyContent}
import services.BusinessService.{HasTradingName, TradingName}
import services._
import uk.gov.hmrc.http.InternalServerException
import views.html.business.trading_name

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TradingNameController @Inject()(val sessionService: SessionService,
                                      val authConnector: AuthClientConnector,
                                      val applicantDetailsService: ApplicantDetailsService,
                                      val businessService: BusinessService,
                                      val vatRegistrationService: VatRegistrationService,
                                      view: trading_name)
                                     (implicit appConfig: FrontendAppConfig,
                                      val executionContext: ExecutionContext,
                                      baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          companyName <- applicantDetailsService.getCompanyName.map(_.getOrElse(throw new InternalServerException("Missing company name")))
          business <- businessService.getBusiness
          form = TradingNameForm.fillWithPrePop(business)
        } yield Ok(view(form, companyName))
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        TradingNameForm.form.bindFromRequest.fold(
          errors =>
            for {
              companyName <- applicantDetailsService.getCompanyName.map(_.getOrElse(throw new InternalServerException("Missing company name")))
            } yield BadRequest(view(errors, companyName)),
          success => {
            val (hasName, optName) = success
            for {
              business <- businessService.updateBusiness(HasTradingName(hasName))
              _ <- optName.fold(Future.successful(business))(name => businessService.updateBusiness(TradingName(name)))
              partyType <- vatRegistrationService.partyType
            } yield partyType match {
              case NETP | NonUkNonEstablished => Redirect(controllers.business.routes.InternationalPpobAddressController.show)
              case _ => Redirect(controllers.business.routes.PpobAddressController.startJourney)
            }
          }
        )
  }

}
