/*
 * Copyright 2018 HM Revenue & Customs
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
  import models.api._
  import play.api.libs.json.Json

  case class PreviousAddressView(yesNo: Boolean, address: Option[ScrsAddress] = None)

  object PreviousAddressView {

    implicit val format = Json.format[PreviousAddressView]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatLodgingOfficer) => group.previousAddress,
      updateF = (c: PreviousAddressView, g: Option[S4LVatLodgingOfficer]) =>
        g.getOrElse(S4LVatLodgingOfficer()).copy(previousAddress = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer[PreviousAddressView] { vs: VatScheme =>
      vs.lodgingOfficer match {
        case Some(VatLodgingOfficer(_, _, _, _, _, _, Some(a), _, _)) => Some(PreviousAddressView(a.currentAddressThreeYears, a.previousAddress))
        case _ => None
      }

    }

  }

}
package controllers.vatLodgingOfficer {

  import javax.inject.{Inject, Singleton}

  import connectors.KeystoreConnect
  import common.enums.AddressLookupJourneyIdentifier.addressThreeYearsOrLess
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import features.officers.controllers.routes
  import forms.vatLodgingOfficer.PreviousAddressForm
  import models.view.vatLodgingOfficer.PreviousAddressView
  import play.api.data.Form
  import play.api.mvc.{Action, AnyContent}
  import services.{AddressLookupService, RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  import scala.concurrent.Future

  @Singleton
  class PreviousAddressController @Inject()(ds: CommonPlayDependencies,
                                            addressLookupService: AddressLookupService,
                                            val keystoreConnector: KeystoreConnect,
                                            val authConnector: AuthConnector,
                                            implicit val s4l: S4LService,
                                            implicit val vrs: RegistrationService) extends VatRegistrationController(ds) with SessionProfile {

    val form: Form[PreviousAddressView] = PreviousAddressForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              viewModel[PreviousAddressView]().fold(form)(form.fill)
                .map(f => Ok(features.officers.views.html.previous_address(f)))
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              form.bindFromRequest.fold(
                badForm => Future.successful(BadRequest(features.officers.views.html.previous_address(badForm))),
                data    => if(!data.yesNo) {
                  addressLookupService.getJourneyUrl(addressThreeYearsOrLess, routes.PreviousAddressController.acceptFromTxm()) map Redirect
                } else {
                  for {
                    _ <- save(PreviousAddressView(true))
                    _ <- vrs.submitVatLodgingOfficer
                  } yield Redirect(controllers.vatContact.ppob.routes.PpobController.show())
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
                _       <- save(PreviousAddressView(false, Some(address)))
                _       <- vrs.submitVatLodgingOfficer
              } yield Redirect(controllers.vatContact.ppob.routes.PpobController.show())
            }
          }
    }

    def changeAddress: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              addressLookupService.getJourneyUrl(addressThreeYearsOrLess, routes.PreviousAddressController.acceptFromTxm()) map Redirect
            }
          }
    }
  }
}

package forms.vatLodgingOfficer {

  import forms.FormValidation.missingBooleanFieldMapping
  import models.view.vatLodgingOfficer.PreviousAddressView
  import play.api.data.Form
  import play.api.data.Forms._

  object PreviousAddressForm {
    val RADIO_YES_NO: String = "previousAddressQuestionRadio"

    val form = Form(
      mapping(
        RADIO_YES_NO -> missingBooleanFieldMapping()("previousAddressQuestion")
      )(PreviousAddressView.apply(_))(view => Option(view.yesNo))
    )

  }
}
