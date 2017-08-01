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

import connectors.AddressLookupConnect
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatLodgingOfficer.PreviousAddressForm
import models.view.vatLodgingOfficer.PreviousAddressView
import play.api.Logger
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}


class PreviousAddressController @Inject()(ds: CommonPlayDependencies)
                                         (implicit s4l: S4LService,
                                          vrs: VatRegistrationService,
                                          alfConnector: AddressLookupConnect)
  extends VatRegistrationController(ds) {

  import cats.syntax.flatMap._
  import models.AddressLookupJourneyId.previousAddressId

  val form: Form[PreviousAddressView] = PreviousAddressForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[PreviousAddressView]().fold(form)(form.fill)
      .map(f => Ok(views.html.pages.vatLodgingOfficer.previous_address(f))))

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      badForm => BadRequest(views.html.pages.vatLodgingOfficer.previous_address(badForm)).pure,
      data => (!data.yesNo).pure.ifM(
        ifTrue = alfConnector.getOnRampUrl(routes.PreviousAddressController.acceptFromTxm()),
        ifFalse = for {
          _ <- save(PreviousAddressView(true))
          _ <- vrs.submitVatLodgingOfficer()
        } yield controllers.vatContact.ppob.routes.PpobController.show()
      ).map(Redirect)
    )
  )

  def acceptFromTxm(id: String): Action[AnyContent] = authorised.async(implicit user => implicit request =>
    for {
      address <- alfConnector.getAddress(id)
      _ <- save(PreviousAddressView(false, Some(address)))
      _ <- vrs.submitVatLodgingOfficer()
    } yield Redirect(controllers.vatContact.ppob.routes.PpobController.show()))

  def changeAddress: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    alfConnector.getOnRampUrl(routes.PreviousAddressController.acceptFromTxm()).map(Redirect))
}


