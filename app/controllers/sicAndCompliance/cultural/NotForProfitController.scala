/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.sicAndCompliance.cultural

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.sicAndCompliance.cultural.NotForProfitForm
import models.S4LVatSicAndCompliance
import models.S4LVatSicAndCompliance.culturalOnly
import models.view.sicAndCompliance.cultural.NotForProfit
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, S4LService, SessionProfile, VatRegistrationService}


class NotForProfitController @Inject()(ds: CommonPlayDependencies,
                                       implicit val s4lService: S4LService,
                                       implicit val vrs: VatRegistrationService)
  extends VatRegistrationController(ds) with CommonService with SessionProfile {

  val form = NotForProfitForm.form

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          viewModel[NotForProfit]().fold(form)(form.fill)
            .map(f => Ok(views.html.pages.sicAndCompliance.cultural.not_for_profit(f)))
        }
  }

  def submit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          form.bindFromRequest().fold(
            badForm => BadRequest(views.html.pages.sicAndCompliance.cultural.not_for_profit(badForm)).pure,
            view => for {
              container <- s4lContainer[S4LVatSicAndCompliance]()
              _ <- s4lService.save(culturalOnly(container.copy(notForProfit = Some(view))))
              _ <- vrs.submitSicAndCompliance()
            } yield Redirect(controllers.vatTradingDetails.vatEuTrading.routes.EuGoodsController.show())
          )
        }
  }

}
