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

package controllers.vatLodgingOfficer

import javax.inject.Inject

import cats.data.OptionT
import connectors.AddressLookupConnect
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatLodgingOfficer.PreviousAddressForm
import models.view.vatLodgingOfficer.{OfficerHomeAddressView, PreviousAddressView}
import play.api.Logger
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{PrePopulationService, S4LService, VatRegistrationService}

import scala.concurrent.Future


class PreviousAddressController @Inject()(ds: CommonPlayDependencies)
                                         (implicit s4l: S4LService,
                                                  vrs: VatRegistrationService,
                                                  alfConnector: AddressLookupConnect)
  extends VatRegistrationController(ds) {

  import cats.syntax.flatMap._

  val form: Form[PreviousAddressView] = PreviousAddressForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[PreviousAddressView]().fold(form)(form.fill)
      .map(f => Ok(views.html.pages.vatLodgingOfficer.previous_address(f))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.vatLodgingOfficer.previous_address(badForm)).pure,

      data => (!data.yesNo).pure.ifM(
        ifTrue = alfConnector.getOnRampUrl(routes.OfficerHomeAddressController.acceptFromTxm()),
        ifFalse = controllers.vatContact.routes.BusinessContactDetailsController.show().pure
      ).map(Redirect)
    )
  )

  def acceptFromTxm(id: String): Action[AnyContent] = authorised.async(implicit user => implicit request =>
    alfConnector.getAddress(id).flatMap { address =>
      Logger.debug(s"address received: $address")
          save(PreviousAddressView(false, Some(address)))
    }.map(_ => Redirect(controllers.vatContact.routes.BusinessContactDetailsController.show())))
}


