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

package controllers.fileupload

import config.{BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import featuretoggle.FeatureSwitch.VrsNewAttachmentJourney
import featuretoggle.FeatureToggleSupport
import models.CurrentProfile
import models.api.{AttachmentType, TaxRepresentativeAuthorisation, VAT2, VAT51, VAT5L}
import models.external.upscan.UpscanDetails
import play.api.data.Form
import play.api.data.Forms.{single, text}
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.{SessionProfile, SessionService, UpscanService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.fileupload.RemoveDocument

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

// Serves /file-upload/:reference/remove for the new attachment journey's form documents.
// Every other case is handed to RemoveUploadedDocumentController, which still owns the old journey.
@Singleton
class RemoveDocumentController @Inject()(view: RemoveDocument,
                                         oldJourneyController: RemoveUploadedDocumentController,
                                         upscanService: UpscanService,
                                         val authConnector: AuthConnector,
                                         val sessionService: SessionService)
                                        (implicit appConfig: FrontendAppConfig,
                                         val executionContext: ExecutionContext,
                                         baseControllerComponents: BaseControllerComponents)
  extends BaseController with SessionProfile with FeatureToggleSupport {

  private val removableForms: Set[AttachmentType] = Set(VAT2, VAT51, TaxRepresentativeAuthorisation, VAT5L)
  private val removeChoice = "remove"
  private val choiceForm: Form[String] = Form(single("choice" -> text))

  def show(reference: String): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        withRemovableForm(reference, oldJourneyController.show(reference)) { upscanDetails =>
          Future.successful(Ok(view(upscanDetails)))
        }
  }

  def submit(reference: String): Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        withRemovableForm(reference, oldJourneyController.submit(reference)) { _ =>
          choiceForm.bindFromRequest().value match {
            case Some(`removeChoice`) =>
              upscanService.deleteUpscanDetails(profile.registrationId, reference).map(_ => redirectToUploadSummary)
            case _ =>
              Future.successful(redirectToUploadSummary)
          }
        }
  }

  private def redirectToUploadSummary: Result = Redirect(routes.UploadSummaryController.show)

  private def withRemovableForm(reference: String, oldJourney: Action[AnyContent])(newJourney: UpscanDetails => Future[Result])
                               (implicit request: Request[AnyContent], profile: CurrentProfile): Future[Result] =
    if (isEnabled(VrsNewAttachmentJourney)) {
      upscanService.fetchUpscanFileDetails(profile.registrationId, reference)
        .map(Option(_))
        .recover { case NonFatal(_) => None }
        .flatMap {
          case Some(upscanDetails) if removableForms.contains(upscanDetails.attachmentType) => newJourney(upscanDetails)
          case Some(_) => oldJourney(request)
          case None => Future.successful(redirectToUploadSummary)
        }
    } else {
      oldJourney(request)
    }
}
