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
import forms.vatLodgingOfficer.OfficerHomeAddressForm
import models.api.ScrsAddress
import models.view.vatLodgingOfficer.OfficerHomeAddressView
import play.api.Logger
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, PrePopulationService, S4LService, VatRegistrationService}
import uk.gov.hmrc.play.http.HeaderCarrier

class OfficerHomeAddressController @Inject()(ds: CommonPlayDependencies)
                                            (implicit s4l: S4LService,
                                             vrs: VatRegistrationService,
                                             prePopService: PrePopulationService,
                                             val alfConnector: AddressLookupConnect)
  extends VatRegistrationController(ds) with CommonService {

  import cats.instances.future._
  import cats.syntax.applicative._
  import cats.syntax.flatMap._

  private val form = OfficerHomeAddressForm.form
  private val addressListKey = "OfficerAddressList"

  private def fetchAddressList()(implicit headerCarrier: HeaderCarrier) =
    OptionT(keystoreConnector.fetchAndGet[Seq[ScrsAddress]](addressListKey))

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    for {
      addresses <- prePopService.getOfficerAddressList()
      _ <- keystoreConnector.cache[Seq[ScrsAddress]](addressListKey, addresses)
      res <- viewModel[OfficerHomeAddressView]().fold(form)(form.fill)
    } yield Ok(views.html.pages.vatLodgingOfficer.officer_home_address(res, addresses))
  )

  def submit: Action[AnyContent] = authorised.async { implicit user =>
    implicit request =>
      form.bindFromRequest().fold(
        badForm => fetchAddressList().getOrElse(Seq()).map(
          addressList => BadRequest(views.html.pages.vatLodgingOfficer.officer_home_address(badForm, addressList))),
        (form: OfficerHomeAddressView) =>
          (form.addressId == "other").pure.ifM(
            alfConnector.getOnRampUrl(routes.OfficerHomeAddressController.acceptFromTxm())
            ,
            for {
              addressList <- fetchAddressList().getOrElse(Seq())
              address = addressList.find(_.id == form.addressId)
              _ <- s4l.save(OfficerHomeAddressView(form.addressId, address))
            } yield controllers.vatContact.routes.BusinessContactDetailsController.show()
          ).map(Redirect))
  }


  def acceptFromTxm(id: String): Action[AnyContent] = authorised.async { implicit user =>
    implicit request =>
      alfConnector.getAddress(id).flatMap { address =>
        Logger.debug(s"address received: $address")
        s4l.save(OfficerHomeAddressView(address.id, Some(address)))
      }.map(_ => Redirect(controllers.vatContact.routes.BusinessContactDetailsController.show()))
  }

}
