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

package features.businessContact.controllers

import javax.inject.Inject

import common.enums.AddressLookupJourneyIdentifier
import config.AuthClientConnector
import connectors.KeystoreConnector
import controllers.BaseController
import features.businessContact.BusinessContactService
import features.businessContact.forms.{CompanyContactDetailsForm, PpobForm}
import features.businessContact.models.CompanyContactDetails
import features.businessContact.views.html.{business_contact_details, ppob, ppob_drop_out}
import models.api.ScrsAddress
import models.view.vatContact.ppob.PpobView
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{AddressLookupService, PrePopService, SessionProfile}

import scala.concurrent.Future

class BusinessContactDetailsControllerImpl @Inject()(val messagesApi: MessagesApi,
                                                     val authConnector: AuthClientConnector,
                                                     val keystoreConnector: KeystoreConnector,
                                                     val businessContactService: BusinessContactService,
                                                     val prepopService: PrePopService,
                                                     val addressLookupService: AddressLookupService,
                                                     val conf: Configuration) extends BusinessContactDetailsController {
  val dropoutUrl = conf.getString("microservice.services.otrs.url").getOrElse("")
}

trait BusinessContactDetailsController extends BaseController with SessionProfile {

  val businessContactService: BusinessContactService
  val prepopService: PrePopService
  val addressLookupService: AddressLookupService
  val dropoutUrl: String

  private val ppobForm            = PpobForm.form
  private val companyContactForm  = CompanyContactDetailsForm.form

  def showPPOB: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          businessContact <- businessContactService.getBusinessContact
          form            =  businessContact.ppobAddress.fold(ppobForm)(x => ppobForm.fill(PpobView(x.id, Some(x))))
          addressList     <- prepopService.getPpobAddressList
        } yield Ok(ppob(form, addressList))
      }
  }


  def submitPPOB: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        ppobForm.bindFromRequest.fold(
          hasErrors => prepopService.getPpobAddressList map { addressList =>
            BadRequest(ppob(hasErrors, addressList))
          },
          address   => if(address.addressId.equals("other")) {
            addressLookupService.getJourneyUrl(
              AddressLookupJourneyIdentifier.businessActivities,
              routes.BusinessContactDetailsController.returnFromTxm()
            ) map Redirect
          } else if (address.addressId.equals("non-uk")) {
            Future.successful(Redirect(routes.BusinessContactDetailsController.showPPOBDropOut()))
          } else {
        for {
              addressList <- prepopService.getPpobAddressList
              _           <- businessContactService.updateBusinessContact[ScrsAddress](addressList.find(_.id == address.addressId).get)
            } yield Redirect(features.businessContact.controllers.routes.BusinessContactDetailsController.showCompanyContactDetails())
          }
        )
      }
  }

  def showPPOBDropOut: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        Future.successful(Ok(ppob_drop_out(dropoutUrl)))
      }
  }

  def returnFromTxm(id: String): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          address <- addressLookupService.getAddressById(id)
          _       <- businessContactService.updateBusinessContact[ScrsAddress](address)
        } yield Redirect(features.businessContact.controllers.routes.BusinessContactDetailsController.showCompanyContactDetails)
      }
  }



  def showCompanyContactDetails: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          businessContact <- businessContactService.getBusinessContact
          form            =  businessContact.companyContactDetails.fold(companyContactForm)(x => companyContactForm.fill(x))
        } yield Ok(business_contact_details(form))
      }
  }

  def submitCompanyContactDetails: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        companyContactForm.bindFromRequest.fold(
          hasErrors => Future.successful(BadRequest(business_contact_details(hasErrors))),
          contact   => businessContactService.updateBusinessContact[CompanyContactDetails](contact) map {
            _ => Redirect(features.sicAndCompliance.controllers.routes.SicAndComplianceController.showBusinessActivityDescription())
          }
        )
      }
  }
}
