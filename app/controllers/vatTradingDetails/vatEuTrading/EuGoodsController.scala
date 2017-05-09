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

package controllers.vatTradingDetails.vatEuTrading

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatTradingDetails.vatEuTrading.EuGoodsForm
import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}

import scala.concurrent.Future


class EuGoodsController @Inject()(ds: CommonPlayDependencies)
                                 (implicit s4LService: S4LService, vrs: VatRegistrationService) extends VatRegistrationController(ds) {

  import cats.instances.future._

  val form = EuGoodsForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel[EuGoods].fold(form)(form.fill)
      .map(f => Ok(views.html.pages.vatTradingDetails.vatEuTrading.eu_goods(f)))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    form.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(views.html.pages.vatTradingDetails.vatEuTrading.eu_goods(formWithErrors))),
      (data: EuGoods) =>
        s4LService.saveForm[EuGoods](data) flatMap {  _ =>
          if (EuGoods.EU_GOODS_NO == data.yesNo) {
            for {
              _ <- s4LService.saveForm[ApplyEori](ApplyEori(ApplyEori.APPLY_EORI_NO))
            } yield Redirect(controllers.vatLodgingOfficer.routes.OfficerHomeAddressController.show())
          } else {
            Future.successful(Redirect(controllers.vatTradingDetails.vatEuTrading.routes.ApplyEoriController.show()))
          }
        }
    )
  })

}
