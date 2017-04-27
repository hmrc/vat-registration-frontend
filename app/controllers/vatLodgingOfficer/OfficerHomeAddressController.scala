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

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatLodgingOfficer.OfficerHomeAddressForm
import models.view.vatLodgingOfficer.OfficerHomeAddressView
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}

class OfficerHomeAddressController @Inject()(ds: CommonPlayDependencies)
                                            (implicit s4l: S4LService, vrs: VatRegistrationService)
  extends VatRegistrationController(ds) {

  import cats.instances.future._
  import cats.syntax.applicative._

  val form = OfficerHomeAddressForm.form

  // TODO get list of addresses (ScrsAddressType?) from PrePop service
  // TODO and make available to the page
  // "For addresses: the shared addresses will be accessible as a single resource
  // for a given regId via a URL shaped like /business-registration/data-sharing/${regId}/addresses"
  // https://confluence.tools.tax.service.gov.uk/display/SCRS/Data+Sharing+Feature
  val prePopAddressMap: Seq[(String, String)] = List(
    "addressId1" -> "5 Romford Road, Wellington, Telford, TF1 4ER",
    "addressId2" -> "7 Romford Road, Wellington, Telford, TF1 4ER"
  )


  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel[OfficerHomeAddressView].fold(form)(form.fill)
      .map(f => Ok(views.html.pages.vatLodgingOfficer.officer_home_address(f, prePopAddressMap)))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    form.bindFromRequest().fold(
      copyGlobalErrorsToFields("daytimePhone", "mobile")
        .andThen(form => BadRequest(views.html.pages.vatLodgingOfficer.officer_home_address(form, prePopAddressMap)).pure),
      s4l.saveForm(_).map(_ => Redirect(controllers.sicAndCompliance.routes.BusinessActivityDescriptionController.show()))
    )
  })

}
