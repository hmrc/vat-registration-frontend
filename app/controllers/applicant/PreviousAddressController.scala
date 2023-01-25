/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.applicant

import common.enums.AddressLookupJourneyIdentifier._
import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import controllers.applicant.{routes => applicantRoutes}
import forms.PreviousAddressForm
import models.api.{NETP, NonUkNonEstablished}
import play.api.mvc.{Action, AnyContent}
import services.ApplicantDetailsService._
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.applicant.PreviousAddress

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PreviousAddressController @Inject()(val authConnector: AuthConnector,
                                          val sessionService: SessionService,
                                          val applicantDetailsService: ApplicantDetailsService,
                                          val addressLookupService: AddressLookupService,
                                          val vatRegistrationService: VatRegistrationService,
                                          previousAddressPage: PreviousAddress
                                         )(implicit appConfig: FrontendAppConfig,
                                           val executionContext: ExecutionContext,
                                           baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile {

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          isTransactor <- vatRegistrationService.isTransactor
          applicantDetails <- applicantDetailsService.getApplicantDetails
          optName <- if (isTransactor) applicantDetailsService.getApplicantNameForTransactorFlow else Future.successful(None)
          errorCode = if (isTransactor) "previousAddressQuestionThirdParty" else "previousAddressQuestion"
          filledForm = applicantDetails.noPreviousAddress.fold(PreviousAddressForm.form(errorCode))(PreviousAddressForm.form(errorCode).fill)
        } yield {
          applicantDetails.currentAddress.fold(
            Redirect(routes.HomeAddressController.redirectToAlf)
          )(address =>
            Ok(previousAddressPage(filledForm, optName, address))
          )
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        applicantDetailsService.getApplicantNameForTransactorFlow.flatMap { optName =>
          for {
            isTransactor <- vatRegistrationService.isTransactor
            optName <- if (isTransactor) applicantDetailsService.getApplicantNameForTransactorFlow else Future.successful(None)
            errorCode = if (isTransactor) "previousAddressQuestionThirdParty" else "previousAddressQuestion"
            form = PreviousAddressForm.form(errorCode)
            result <- form.bindFromRequest.fold(
              badForm =>
                applicantDetailsService.getApplicantDetails.map { applicant =>
                  applicant.currentAddress.fold(
                    Redirect(routes.HomeAddressController.redirectToAlf)
                  )(address =>
                    BadRequest(previousAddressPage(badForm, optName, address))
                  )
                },
              data =>
                applicantDetailsService.saveApplicantDetails(NoPreviousAddress(data)).flatMap { _ =>
                  if (data) {
                    Future.successful(Redirect(controllers.routes.TaskListController.show))
                  } else {
                    vatRegistrationService.getEligibilitySubmissionData.map(data => (data.partyType, data.fixedEstablishmentInManOrUk)).map {
                      case (NETP | NonUkNonEstablished, false) =>
                        Redirect(routes.InternationalPreviousAddressController.show)
                      case _ =>
                        Redirect(routes.PreviousAddressController.previousAddress)
                    }
                  }
                }
            )
          } yield result
        }
  }

  def addressLookupCallback(id: String): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        for {
          address <- addressLookupService.getAddressById(id)
          _ <- applicantDetailsService.saveApplicantDetails(PreviousAddress(address))
        } yield Redirect(controllers.routes.TaskListController.show)
  }

  def previousAddress: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatRegistrationService.isTransactor.flatMap { isTransactor =>
          val journeyId = if (isTransactor) {
            applicantAddressThreeYearsOrLess
          } else {
            addressThreeYearsOrLess
          }
          addressLookupService.getJourneyUrl(journeyId, applicantRoutes.PreviousAddressController.addressLookupCallback()) map Redirect
        }
  }
}
