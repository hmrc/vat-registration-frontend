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

package features.businessContact

import javax.inject.Inject

import _root_.models.api.ScrsAddress
import _root_.models.view.vatContact.ppob.PpobView
import common.enums.AddressLookupJourneyIdentifier
import connectors.KeystoreConnect
import controllers.VatRegistrationControllerNoAux
import features.businessContact.forms.{CompanyContactDetailsForm, PpobForm}
import features.businessContact.models.CompanyContactDetails
import features.businessContact.views.html.{business_contact_details => BusinessContactDetailsView, ppob => PPOBView}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Call}
import services.{AddressLookupService, PrePopService, SessionProfile}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class BusinessContactDetailsControllerImpl @Inject()(implicit val messagesApi: MessagesApi,
                                                     val authConnector: AuthConnector,
                                                     val keystoreConnector: KeystoreConnect,
                                                     val businessContactService: BusinessContactService,
                                                     val prepopService: PrePopService,
                                                     val addressLookupService: AddressLookupService) extends BusinessContactDetailsController

trait BusinessContactDetailsController extends VatRegistrationControllerNoAux with SessionProfile {

  val businessContactService: BusinessContactService
  val prepopService: PrePopService
  val addressLookupService: AddressLookupService

  private val ppobForm            = PpobForm.form
  private val companyContactForm  = CompanyContactDetailsForm.form

  def showPPOB: Action[AnyContent] = authorisedWithCurrentProfile {
    implicit user => implicit request => implicit profile =>
      ivPassedCheck {
        for {
          businessContact <- businessContactService.getBusinessContact
          form            =  businessContact.ppobAddress.fold(ppobForm)(x => ppobForm.fill(PpobView(x.id, Some(x))))
          addressList     <- prepopService.getPpobAddressList
        } yield Ok(PPOBView(form, addressList))
      }
  }

  def submitPPOB: Action[AnyContent] = authorisedWithCurrentProfile {
    implicit user => implicit request => implicit profile =>
      ivPassedCheck {
        ppobForm.bindFromRequest.fold(
          hasErrors => prepopService.getPpobAddressList map { addressList =>
            BadRequest(PPOBView(hasErrors, addressList))
          },
          address   => if(address.addressId.equals("other")) {
            addressLookupService.getJourneyUrl(AddressLookupJourneyIdentifier.businessActivities, Call("", "")) map Redirect
          } else {
            for {
              addressList <- prepopService.getPpobAddressList
              _           <- businessContactService.updateBusinessContact[ScrsAddress](addressList.find(_.id == address.addressId).get)
            } yield Redirect(features.businessContact.routes.BusinessContactDetailsController.showCompanyContactDetails())
          }
        )
      }
  }

  def returnFromTxm(id: String): Action[AnyContent] = authorisedWithCurrentProfile {
    implicit user => implicit request => implicit profile =>
      ivPassedCheck {
        for {
          address <- addressLookupService.getAddressById(id)
          _       <- businessContactService.updateBusinessContact[ScrsAddress](address)
        } yield Redirect(features.businessContact.routes.BusinessContactDetailsController.showCompanyContactDetails)
      }
  }



  def showCompanyContactDetails: Action[AnyContent] = authorisedWithCurrentProfile {
    implicit user => implicit request => implicit profile =>
      ivPassedCheck {
        for {
          businessContact <- businessContactService.getBusinessContact
          form            =  businessContact.companyContactDetails.fold(companyContactForm)(x => companyContactForm.fill(x))
        } yield Ok(BusinessContactDetailsView(form))
      }
  }

  def submitCompanyContactDetails: Action[AnyContent] = authorisedWithCurrentProfile {
    implicit user => implicit request => implicit profile =>
      ivPassedCheck {
        companyContactForm.bindFromRequest.fold(
          hasErrors => Future.successful(BadRequest(BusinessContactDetailsView(hasErrors))),
          contact   => businessContactService.updateBusinessContact[CompanyContactDetails](contact) map {
            _ => Redirect(controllers.sicAndCompliance.routes.BusinessActivityDescriptionController.show())
          }
        )
      }
  }
}
