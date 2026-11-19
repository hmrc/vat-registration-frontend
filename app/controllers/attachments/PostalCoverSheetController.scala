/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.attachments

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import featuretoggle.FeatureSwitch.VrsNewAttachmentJourney
import featuretoggle.FeatureToggleSupport
import forms.CoverLetterPrintedForm
import models.CurrentProfile
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Request, Result}
import play.twirl.api.Html
import services._
import views.html.attachments.{PostalCoverSheet, PostalCoverSheetNewJourney}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PostalCoverSheetController @Inject()(view: PostalCoverSheet,
                                           viewNewJourney: PostalCoverSheetNewJourney,
                                           val authConnector: AuthClientConnector,
                                           val sessionService: SessionService,
                                           attachmentsService: AttachmentsService,
                                           vatRegistrationService: VatRegistrationService,
                                           applicantDetailsService: ApplicantDetailsService,
                                           transactorDetailsService: TransactorDetailsService
                                          )(implicit appConfig: FrontendAppConfig,
                                            val executionContext: ExecutionContext,
                                            baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with FeatureToggleSupport {

  private val prefixLength = 3
  private val groupSize = 4
  private val separator = " "
  private val coverLetterConfirmedKey = "coverLetterConfirmed"

  val show: Action[AnyContent] = isAuthenticatedAndSubmitted {
    implicit request =>
      implicit profile =>
        whenCoverLetterNotConfirmed {
          renderPage(CoverLetterPrintedForm()).map(Ok(_))
        }
  }

  val submit: Action[AnyContent] = isAuthenticatedAndSubmitted {
    implicit request =>
      implicit profile =>
        if (isEnabled(VrsNewAttachmentJourney)) {
          whenCoverLetterNotConfirmed {
            CoverLetterPrintedForm().bindFromRequest().fold(
              formWithErrors => {
                infoLog("[PostalCoverSheetController][submit] - Cover letter printed checkbox not ticked")
                renderPage(formWithErrors).map(BadRequest(_))
              },
              _ =>
                sessionService.cache[Boolean](coverLetterConfirmedKey, true).map { _ =>
                  infoLog("[PostalCoverSheetController][submit] - Cover letter confirmed as printed or saved, redirecting to the application submitted page")
                  Redirect(controllers.routes.ApplicationSubmissionController.show)
                }
            )
          }
        } else {
          Future.successful(Redirect(routes.PostalCoverSheetController.show))
        }
  }

  private def whenCoverLetterNotConfirmed(f: => Future[Result])(implicit request: Request[_]): Future[Result] =
    if (isEnabled(VrsNewAttachmentJourney)) {
      sessionService.fetchAndGet[Boolean](coverLetterConfirmedKey).flatMap {
        case Some(true) =>
          infoLog("[PostalCoverSheetController] - Cover letter already confirmed, redirecting to the application submitted page")
          Future.successful(Redirect(controllers.routes.ApplicationSubmissionController.show))
        case _ =>
          f
      }
    } else {
      f
    }

  private def renderPage(printedForm: Form[Boolean])(implicit request: Request[_], profile: CurrentProfile): Future[Html] =
    for {
      attachments <- attachmentsService.getAttachmentList(profile.registrationId)
      acknowledgementRef <- vatRegistrationService.getAckRef(profile.registrationId)
      formattedRef = formatAckRef(acknowledgementRef)
      isTransactor <- vatRegistrationService.isTransactor
      applicantName <- if (isTransactor) {
        applicantDetailsService.getApplicantDetails.map(_.personalDetails.map(_.fullName))
      } else {
        Future.successful(None)
      }
      transactorName <- if (isTransactor) {
        transactorDetailsService.getTransactorDetails.map(_.personalDetails.map(_.fullName))
      } else {
        Future.successful(None)
      }
    } yield {
      if (isEnabled(VrsNewAttachmentJourney)) {
        viewNewJourney(printedForm, formattedRef, attachments, applicantName, transactorName)
      } else {
        view(formattedRef, attachments, applicantName, transactorName)
      }
    }

  private def formatAckRef(acknowledgementRef: String): String = {
    val prefix = acknowledgementRef.take(prefixLength)
    val groups = acknowledgementRef.drop(prefixLength).grouped(groupSize).toList
    prefix +: groups mkString separator
  }
}
