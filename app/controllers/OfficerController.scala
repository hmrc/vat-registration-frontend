/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import common.enums.AddressLookupJourneyIdentifier.{addressThreeYearsOrLess, homeAddress}
import config.AuthClientConnector
import connectors.KeystoreConnector
import models.view.{HomeAddressView, PreviousAddressView}
import forms._
import javax.inject.Inject
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{AddressLookupService, LodgingOfficerService, PrePopService, SessionProfile}

import scala.concurrent.Future

class OfficerControllerImpl @Inject()(val messagesApi: MessagesApi,
                                      val authConnector: AuthClientConnector,
                                      val keystoreConnector: KeystoreConnector,
                                      val lodgingOfficerService: LodgingOfficerService,
                                      val prePopService: PrePopService,
                                      val addressLookupService: AddressLookupService) extends OfficerController

trait OfficerController extends BaseController with SessionProfile {
  val prePopService: PrePopService
  val lodgingOfficerService: LodgingOfficerService
  val addressLookupService: AddressLookupService
  implicit val messagesApi: MessagesApi

  def showSecurityQuestions: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      for {
        officer    <- lodgingOfficerService.getLodgingOfficer
        filledForm = officer.securityQuestions.fold(SecurityQuestionsForm.form)(SecurityQuestionsForm.form.fill)
      } yield Ok(views.html.officer_security_questions(filledForm))
  }

  def submitSecurityQuestions: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      SecurityQuestionsForm.form.bindFromRequest().fold(
        badForm => Future.successful(BadRequest(views.html.officer_security_questions(badForm))),
        data => lodgingOfficerService.saveLodgingOfficer(data) map {
          _ => Redirect(controllers.routes.IdentityVerificationController.redirectToIV())
        }
      )
  }

  def showFormerName: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          officer    <- lodgingOfficerService.getLodgingOfficer
          applicant  <- lodgingOfficerService.getApplicantName
          ccId       = applicant.id
          filledForm = officer.formerName.fold(FormerNameForm.form(ccId))(FormerNameForm.form(ccId).fill)
        } yield Ok(views.html.former_name(filledForm))
      }
  }

  def submitFormerName: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        lodgingOfficerService.getApplicantName flatMap { applicant =>
          FormerNameForm.form(applicant.id).bindFromRequest().fold(
            badForm => Future.successful(BadRequest(views.html.former_name(badForm))),
            data => lodgingOfficerService.saveLodgingOfficer(data) map { _ =>
              if (data.yesNo) Redirect(controllers.routes.OfficerController.showFormerNameDate()) else Redirect(controllers.routes.OfficerController.showContactDetails())
            }
          )
        }
      }
  }

  def showFormerNameDate: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          officer <- lodgingOfficerService.getLodgingOfficer
          formerName = officer.formerName.flatMap(_.formerName).getOrElse(throw new IllegalStateException("Missing officer former name"))
          dateOfBirth = officer.securityQuestions.map(_.dob).getOrElse(throw new IllegalStateException("Missing Officer date of birth"))
          filledForm = officer.formerNameDate.fold(FormerNameDateForm.form(dateOfBirth))(FormerNameDateForm.form(dateOfBirth).fill)
        } yield Ok(views.html.former_name_date(filledForm, formerName))
      }
  }

  def submitFormerNameDate: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        lodgingOfficerService.getLodgingOfficer flatMap { officer =>
          val dateOfBirth = officer.securityQuestions.map(_.dob).getOrElse(throw new IllegalStateException("Missing Officer date of birth"))
          FormerNameDateForm.form(dateOfBirth).bindFromRequest().fold(
            badForm => for {
              officer <- lodgingOfficerService.getLodgingOfficer
              formerName = officer.formerName.flatMap(_.formerName).getOrElse(throw new IllegalStateException("Missing officer former name"))
            } yield BadRequest(views.html.former_name_date(badForm, formerName)),
            data => lodgingOfficerService.saveLodgingOfficer(data) map {
              _ => Redirect(controllers.routes.OfficerController.showContactDetails())
            }
          )
        }
      }
  }

  def showContactDetails: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          officer    <- lodgingOfficerService.getLodgingOfficer
          filledForm = officer.contactDetails.fold(ContactDetailsForm.form)(ContactDetailsForm.form.fill)
        } yield Ok(views.html.officer_contact_details(filledForm))
      }
  }

  def submitContactDetails: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ContactDetailsForm.form.bindFromRequest().fold(
        badForm => Future.successful(BadRequest(views.html.officer_contact_details(badForm))),
        data => lodgingOfficerService.saveLodgingOfficer(data) map {
          _ => Redirect(controllers.routes.OfficerController.showHomeAddress())
        }
      )
  }

  def showHomeAddress: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          officer    <- lodgingOfficerService.getLodgingOfficer
          addresses  <- prePopService.getOfficerAddressList(officer)
          filledForm = officer.homeAddress.fold(HomeAddressForm.form)(HomeAddressForm.form.fill)
        } yield Ok(views.html.officer_home_address(filledForm, addresses))
      }
  }


  def submitHomeAddress: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      lodgingOfficerService.getLodgingOfficer.flatMap {
        officer => HomeAddressForm.form.bindFromRequest.fold(
          badForm => prePopService.getOfficerAddressList(officer) map { addresses =>
            BadRequest(views.html.officer_home_address(badForm, addresses))
          },
          data    => if(data.addressId == "other") {
            addressLookupService.getJourneyUrl(homeAddress, controllers.routes.OfficerController.acceptFromTxmHomeAddress()) map Redirect
          } else {
            for {
              addresses <- prePopService.getOfficerAddressList(officer)
              address   =  addresses.find(_.id == data.addressId)
              _         <- lodgingOfficerService.saveLodgingOfficer(HomeAddressView(data.addressId, address))
            } yield Redirect(controllers.routes.OfficerController.showPreviousAddress())
          }
        )
      }
  }

  def acceptFromTxmHomeAddress(id: String): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      for {
        address <- addressLookupService.getAddressById(id)
        _ <- lodgingOfficerService.saveLodgingOfficer(HomeAddressView(address.id, Some(address.normalise())))
      } yield Redirect(controllers.routes.OfficerController.showPreviousAddress())
  }

  def showPreviousAddress: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          officer    <- lodgingOfficerService.getLodgingOfficer
          filledForm = officer.previousAddress.fold(PreviousAddressForm.form)(PreviousAddressForm.form.fill)
        } yield Ok(views.html.previous_address(filledForm))
      }
  }

  def submitPreviousAddress: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        PreviousAddressForm.form.bindFromRequest.fold(
          badForm => Future.successful(BadRequest(views.html.previous_address(badForm))),
          data    => if (!data.yesNo) {
            addressLookupService.getJourneyUrl(addressThreeYearsOrLess, controllers.routes.OfficerController.acceptFromTxmPreviousAddress()) map Redirect
          } else {
            lodgingOfficerService.saveLodgingOfficer(data) map {
             _ => Redirect(controllers.routes.BusinessContactDetailsController.showPPOB())
            }
          }
        )
      }
  }

  def acceptFromTxmPreviousAddress(id: String): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          address <- addressLookupService.getAddressById(id)
          _       <- lodgingOfficerService.saveLodgingOfficer(PreviousAddressView(false, Some(address)))
        } yield Redirect(controllers.routes.BusinessContactDetailsController.showPPOB())
      }
  }

  def changePreviousAddress: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        addressLookupService.getJourneyUrl(addressThreeYearsOrLess, controllers.routes.OfficerController.acceptFromTxmPreviousAddress()) map Redirect
      }
  }
}
