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
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatLodgingOfficer.OfficerHomeAddressForm
import models.api.ScrsAddress
import models.view.vatLodgingOfficer.OfficerHomeAddressView
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, PrePopulationService, S4LService, VatRegistrationService}

class OfficerHomeAddressController @Inject()(ds: CommonPlayDependencies)
                                            (implicit s4l: S4LService,
                                             vrs: VatRegistrationService,
                                             prePopService: PrePopulationService)
  extends VatRegistrationController(ds) with CommonService {

  import cats.instances.future._

  val form = OfficerHomeAddressForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    for {
      addresses <- prePopService.getOfficerAddressList()
      _ <- keystoreConnector.cache[Seq[ScrsAddress]]("OfficerAddressList", addresses)
      res <- viewModel[OfficerHomeAddressView].fold(form)(form.fill)
        .map(f => Ok(views.html.pages.vatLodgingOfficer.officer_home_address(f, addresses)))
    } yield res

  })

  // TODO keystoreConnector.remove[Seq[ScrsAddress]]("OfficerAddressList")
  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    form.bindFromRequest().fold(
      (formWithErrors: Form[OfficerHomeAddressView]) => for {
        addressList <- prePopService.getOfficerAddressList()
      } yield BadRequest(views.html.pages.vatLodgingOfficer.officer_home_address(formWithErrors, addressList)),
      (form: OfficerHomeAddressView) => (for {
        addressList <- OptionT(keystoreConnector.fetchAndGet[Seq[ScrsAddress]]("OfficerAddressList"))
        address <- OptionT.fromOption(addressList.find(_.getId() == form.addressId))
        _ <- OptionT.liftF(s4l.saveForm[OfficerHomeAddressView](OfficerHomeAddressView(form.addressId, Some(address))))
      } yield Redirect(controllers.sicAndCompliance.routes.BusinessActivityDescriptionController.show()))
        .getOrElse(Redirect(controllers.sicAndCompliance.routes.BusinessActivityDescriptionController.show()))
        // TODO route to address lookup if selected
    )
  })

}
