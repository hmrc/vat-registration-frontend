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

package features.officer.controllers

import javax.inject.Inject

import common.enums.AddressLookupJourneyIdentifier.{addressThreeYearsOrLess, homeAddress}
import connectors.KeystoreConnect
import controllers.VatRegistrationControllerNoAux
import features.officer.forms._
import features.officer.models.view.{HomeAddressView, PreviousAddressView}
import features.officer.services.LodgingOfficerService
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{AddressLookupService, PrePopService, SessionProfile}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class OfficerControllerImpl @Inject()(implicit val messagesApi: MessagesApi,
                                      val authConnector: AuthConnector,
                                      val keystoreConnector: KeystoreConnect,
                                      val lodgingOfficerService: LodgingOfficerService,
                                      val prePopService: PrePopService,
                                      val addressLookupService: AddressLookupService) extends OfficerController

trait OfficerController extends VatRegistrationControllerNoAux with SessionProfile {
  val prePopService: PrePopService
  val lodgingOfficerService: LodgingOfficerService
  val addressLookupService: AddressLookupService
  implicit val messagesApi: MessagesApi

  def showCompletionCapacity: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          for {
            officerList <- prePopService.getOfficerList
            officer     <- lodgingOfficerService.getLodgingOfficer
            filledForm  = officer.completionCapacity.fold(CompletionCapacityForm.form)(CompletionCapacityForm.form.fill)
          } yield Ok(features.officer.views.html.completion_capacity(filledForm, officerList))
        }
  }

  def submitCompletionCapacity: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          CompletionCapacityForm.form.bindFromRequest.fold(
            formErrors => prePopService.getOfficerList map { officerList =>
              BadRequest(features.officer.views.html.completion_capacity(formErrors, officerList))
            },
            cc => lodgingOfficerService.updateLodgingOfficer(cc) map {
              _ => Redirect(features.officer.controllers.routes.OfficerController.showSecurityQuestions())
            }
          )
        }
  }

  def showSecurityQuestions: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          for {
            officer    <- lodgingOfficerService.getLodgingOfficer
            filledForm = officer.securityQuestions.fold(SecurityQuestionsForm.form)(SecurityQuestionsForm.form.fill)
          } yield Ok(features.officer.views.html.officer_security_questions(filledForm))
        }
  }

  def submitSecurityQuestions: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          SecurityQuestionsForm.form.bindFromRequest().fold(
            badForm => Future.successful(BadRequest(features.officer.views.html.officer_security_questions(badForm))),
            data => lodgingOfficerService.updateLodgingOfficer(data) map {
              _ => Redirect(controllers.iv.routes.IdentityVerificationController.redirectToIV())
            }
          )
        }
  }

  def showFormerName: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            for {
              officer    <- lodgingOfficerService.getLodgingOfficer
              filledForm = officer.formerName.fold(FormerNameForm.form)(FormerNameForm.form.fill)
            } yield Ok(features.officer.views.html.former_name(filledForm))
          }
        }
  }

  def submitFormerName: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            FormerNameForm.form.bindFromRequest().fold(
              badForm => Future.successful(BadRequest(features.officer.views.html.former_name(badForm))),
              data => lodgingOfficerService.updateLodgingOfficer(data) map {
                _ => if (data.yesNo) {
                  Redirect(routes.OfficerController.showFormerNameDate())
                } else {
                  Redirect(routes.OfficerController.showContactDetails())
                }
              }
            )
          }
        }
  }

  def showFormerNameDate: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            for {
              officer <- lodgingOfficerService.getLodgingOfficer
              formerName = officer.formerName.flatMap(_.formerName).getOrElse(throw new IllegalStateException("Missing officer former name"))
              filledForm = officer.formerNameDate.fold(FormerNameDateForm.form)(FormerNameDateForm.form.fill)
            } yield Ok(features.officer.views.html.former_name_date(filledForm, formerName))
          }
        }
  }


  def submitFormerNameDate: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            FormerNameDateForm.form.bindFromRequest().fold(
              badForm => for {
                officer <- lodgingOfficerService.getLodgingOfficer
                formerName = officer.formerName.flatMap(_.formerName).getOrElse(throw new IllegalStateException("Missing officer former name"))
              } yield BadRequest(features.officer.views.html.former_name_date(badForm, formerName)),
              data => lodgingOfficerService.updateLodgingOfficer(data) map {
                _ => Redirect(routes.OfficerController.showContactDetails())
              }
            )
          }
        }
  }

  def showContactDetails: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            for {
              officer    <- lodgingOfficerService.getLodgingOfficer
              filledForm = officer.contactDetails.fold(ContactDetailsForm.form)(ContactDetailsForm.form.fill)
            } yield Ok(features.officer.views.html.officer_contact_details(filledForm))
          }
        }
  }

  def submitContactDetails: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ContactDetailsForm.form.bindFromRequest().fold(
            badForm => Future.successful(BadRequest(features.officer.views.html.officer_contact_details(badForm))),
            data => lodgingOfficerService.updateLodgingOfficer(data) map {
              _ => Redirect(routes.OfficerController.showHomeAddress())
            }
          )
        }
  }

  def showHomeAddress: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            for {
              addresses  <- prePopService.getOfficerAddressList
              officer    <- lodgingOfficerService.getLodgingOfficer
              filledForm = officer.homeAddress.fold(HomeAddressForm.form)(HomeAddressForm.form.fill)
            } yield Ok(features.officer.views.html.officer_home_address(filledForm, addresses))
          }
        }
  }


  def submitHomeAddress: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          HomeAddressForm.form.bindFromRequest.fold(
            badForm => prePopService.getOfficerAddressList map { addresses =>
              BadRequest(features.officer.views.html.officer_home_address(badForm, addresses))
            },
            data    => if(data.addressId == "other") {
              addressLookupService.getJourneyUrl(homeAddress, routes.OfficerController.acceptFromTxmHomeAddress()) map Redirect
            } else {
              for {
                addresses <- prePopService.getOfficerAddressList
                address   =  addresses.find(_.id == data.addressId)
                _         <- lodgingOfficerService.updateLodgingOfficer(HomeAddressView(data.addressId, address))
              } yield Redirect(features.officer.controllers.routes.OfficerController.showPreviousAddress())
            }
          )
        }
  }

  def acceptFromTxmHomeAddress(id: String): Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          for {
            address <- addressLookupService.getAddressById(id)
            _ <- lodgingOfficerService.updateLodgingOfficer(HomeAddressView(address.id, Some(address.normalise())))
          } yield Redirect(features.officer.controllers.routes.OfficerController.showPreviousAddress())
        }
  }

  def showPreviousAddress: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            for {
              officer    <- lodgingOfficerService.getLodgingOfficer
              filledForm = officer.previousAddress.fold(PreviousAddressForm.form)(PreviousAddressForm.form.fill)
            } yield Ok(features.officer.views.html.previous_address(filledForm))
          }
        }
  }

  def submitPreviousAddress: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            PreviousAddressForm.form.bindFromRequest.fold(
              badForm => Future.successful(BadRequest(features.officer.views.html.previous_address(badForm))),
              data    => if (!data.yesNo) {
                addressLookupService.getJourneyUrl(addressThreeYearsOrLess, routes.OfficerController.acceptFromTxmPreviousAddress()) map Redirect
              } else {
                lodgingOfficerService.updateLodgingOfficer(data) map {
                 _ => Redirect(controllers.vatContact.ppob.routes.PpobController.show())
                }
              }
            )
          }
        }
  }

  def acceptFromTxmPreviousAddress(id: String): Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            for {
              address <- addressLookupService.getAddressById(id)
              _       <- lodgingOfficerService.updateLodgingOfficer(PreviousAddressView(false, Some(address)))
            } yield Redirect(controllers.vatContact.ppob.routes.PpobController.show())
          }
        }
  }

  def changePreviousAddress: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            addressLookupService.getJourneyUrl(addressThreeYearsOrLess, routes.OfficerController.acceptFromTxmPreviousAddress()) map Redirect
          }
        }
  }
}
