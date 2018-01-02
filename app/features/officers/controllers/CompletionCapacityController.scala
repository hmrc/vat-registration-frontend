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


package controllers.vatLodgingOfficer{

  import javax.inject.Inject

  import connectors.KeystoreConnect
  import controllers.VatRegistrationControllerNoAux
  import features.officers.services.LodgingOfficerService
  import forms.vatLodgingOfficer.CompletionCapacityForm
  import play.api.i18n.MessagesApi
  import play.api.mvc.{Action, AnyContent}
  import services._
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  class CompletionCapacityControllerImpl @Inject()(implicit val messagesApi: MessagesApi,
                                                   val authConnector: AuthConnector,
                                                   val keystoreConnector: KeystoreConnect,
                                                   val lodgingOfficerService: LodgingOfficerService,
                                                   val prePopService: PrePopService) extends CompletionCapacityController

  trait CompletionCapacityController extends VatRegistrationControllerNoAux with SessionProfile {
    val prePopService: PrePopService
    val lodgingOfficerService: LodgingOfficerService

    private val form = CompletionCapacityForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            for {
              officerList     <- prePopService.getOfficerList
              selectedOfficer <- lodgingOfficerService.getCompletionCapacity
              filledForm      =  selectedOfficer.fold(form)(x => form.fill(x))
            } yield Ok(features.officers.views.html.completion_capacity(filledForm, officerList))
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            form.bindFromRequest.fold(
              formErrors => prePopService.getOfficerList map { officerList =>
                BadRequest(features.officers.views.html.completion_capacity(formErrors, officerList))
              },
              ccView => lodgingOfficerService.submitCompletionCapacity(ccView) map {
                _ => Redirect(routes.OfficerSecurityQuestionsController.show())
              }
            )
          }
    }
  }
}

package forms.vatLodgingOfficer {

  import forms.FormValidation.textMapping
  import models.view.vatLodgingOfficer.CompletionCapacityView
  import play.api.data.Form
  import play.api.data.Forms._

  object CompletionCapacityForm {
    val NAME_ID: String = "completionCapacityRadio"

    val form = Form(
      single(
        NAME_ID -> textMapping()("completionCapacity")
      )
    )
  }

}
