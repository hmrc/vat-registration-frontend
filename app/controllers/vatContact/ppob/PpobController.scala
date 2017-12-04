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

package controllers.vatContact.ppob

import javax.inject.{Inject, Singleton}

import cats.data.OptionT
import connectors.KeystoreConnect
import common.enums.AddressLookupJourneyIdentifier.businessActivities
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.ppob.PpobForm
import models.api.ScrsAddress
import models.view.vatContact.ppob.PpobView
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

@Singleton
class PpobController @Inject()(ds: CommonPlayDependencies,
                               prePopService: PrePopService,
                               addressLookupService: AddressLookupService,
                               val authConnector: AuthConnector,
                               val keystoreConnector: KeystoreConnect,
                               implicit val s4l: S4LService,
                               implicit val vrs: RegistrationService) extends VatRegistrationController(ds) with SessionProfile {

  private val form = PpobForm.form
  private val addressListKey = "PpobAddressList"

  private def fetchAddressList(implicit headerCarrier: HeaderCarrier) = OptionT(keystoreConnector.fetchAndGet[Seq[ScrsAddress]](addressListKey))

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            for {
              addresses <- prePopService.getPpobAddressList
              _ <- keystoreConnector.cache[Seq[ScrsAddress]](addressListKey, addresses)
              res <- viewModel[PpobView]().fold(form)(form.fill)
            } yield Ok(views.html.pages.vatContact.ppob.ppob(res, addresses))
          }
        }
  }


  def submit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            form.bindFromRequest.fold(
              badForm => fetchAddressList.getOrElse(Seq()) map {
                addressList => BadRequest(views.html.pages.vatContact.ppob.ppob(badForm, addressList))
              },
              data    => if(data.addressId == "other") {
                addressLookupService.getJourneyUrl(businessActivities, routes.PpobController.acceptFromTxm()) map Redirect
              } else {
                for {
                  addressList <- fetchAddressList.getOrElse(Seq())
                  adddress    =  addressList.find(_.id == data.addressId)
                  _           <- save(PpobView(data.addressId, adddress))
                } yield Redirect(controllers.vatContact.routes.BusinessContactDetailsController.show())
              }
            )
          }
        }
  }

  def acceptFromTxm(id: String): Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            for {
              address <- addressLookupService.getAddressById(id)
              _ <- save(PpobView(address.id, Some(address)))
            } yield Redirect(controllers.vatContact.routes.BusinessContactDetailsController.show())
          }
        }
  }
}
