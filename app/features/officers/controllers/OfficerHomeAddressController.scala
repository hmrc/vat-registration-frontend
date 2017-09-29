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

package models.view.vatLodgingOfficer {

  import models._
  import models.api.{ScrsAddress, VatScheme}
  import play.api.libs.json.Json

  case class OfficerHomeAddressView(addressId: String, address: Option[ScrsAddress] = None)

  object OfficerHomeAddressView {

    implicit val format = Json.format[OfficerHomeAddressView]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatLodgingOfficer) => group.officerHomeAddress,
      updateF = (c: OfficerHomeAddressView, g: Option[S4LVatLodgingOfficer]) =>
        g.getOrElse(S4LVatLodgingOfficer()).copy(officerHomeAddress = Some(c))
    )

    // return a view model from a VatScheme instance
    implicit val modelTransformer = ApiModelTransformer[OfficerHomeAddressView] { vs: VatScheme =>
      vs.lodgingOfficer.map(_.currentAddress).collect {
        case address => OfficerHomeAddressView(address.id, Some(address))
      }
    }

  }

}

package controllers.vatLodgingOfficer {

  import javax.inject.Inject

  import cats.data.OptionT
  import connectors.AddressLookupConnect
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatLodgingOfficer.OfficerHomeAddressForm
  import models.api.ScrsAddress
  import models.view.vatLodgingOfficer.OfficerHomeAddressView
  import play.api.Logger
  import play.api.mvc.{Action, AnyContent}
  import services._
  import uk.gov.hmrc.play.http.HeaderCarrier

  class OfficerHomeAddressController @Inject()(ds: CommonPlayDependencies)
                                              (implicit s4l: S4LService,
                                               vrs: VatRegistrationService,
                                               prePopService: PrePopulationService,
                                               alfConnector: AddressLookupConnect)
    extends VatRegistrationController(ds) with CommonService with SessionProfile {

    import cats.syntax.flatMap._
    import models.AddressLookupJourneyId.homeAddressJourneyId

    private val form = OfficerHomeAddressForm.form
    private val addressListKey = "OfficerAddressList"

    private def fetchAddressList()(implicit headerCarrier: HeaderCarrier) =
      OptionT(keystoreConnector.fetchAndGet[Seq[ScrsAddress]](addressListKey))

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            for {
              addresses <- prePopService.getOfficerAddressList()
              _ <- keystoreConnector.cache[Seq[ScrsAddress]](addressListKey, addresses)
              res <- viewModel[OfficerHomeAddressView]().fold(form)(form.fill)
            } yield Ok(features.officers.views.html.officer_home_address(res, addresses))
          }
    }


    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            form.bindFromRequest().fold(
              badForm => fetchAddressList().getOrElse(Seq()).map(
                addressList => BadRequest(features.officers.views.html.officer_home_address(badForm, addressList))),
              data => (data.addressId == "other").pure.ifM(
                ifTrue = alfConnector.getOnRampUrl(routes.OfficerHomeAddressController.acceptFromTxm()),
                ifFalse = for {
                  addressList <- fetchAddressList().getOrElse(Seq())
                  address = addressList.find(_.id == data.addressId)
                  _ <- save(OfficerHomeAddressView(data.addressId, address))
                } yield controllers.vatLodgingOfficer.routes.PreviousAddressController.show()
              ).map(Redirect))
          }
    }


    def acceptFromTxm(id: String): Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            alfConnector.getAddress(id).flatMap { address =>
              Logger.debug(s"address received: $address")
              save(OfficerHomeAddressView(address.id, Some(address.normalise())))
            }.map(_ => Redirect(controllers.vatLodgingOfficer.routes.PreviousAddressController.show()))
          }
    }

  }

}

package forms.vatLodgingOfficer {

  import forms.FormValidation.textMapping
  import models.view.vatLodgingOfficer.OfficerHomeAddressView
  import play.api.data.Form
  import play.api.data.Forms._

  object OfficerHomeAddressForm {

    val ADDRESS_ID: String = "homeAddressRadio"

    val form = Form(
      mapping(
        ADDRESS_ID -> textMapping()("officerHomeAddress")
      )(OfficerHomeAddressView(_))(view => Option(view.addressId))
    )
  }

}
