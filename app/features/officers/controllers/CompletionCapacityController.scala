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

  import models.api.{CompletionCapacity, VatLodgingOfficer, VatScheme}
  import models.{ApiModelTransformer, _}
  import play.api.libs.json.Json

  case class CompletionCapacityView(id: String, completionCapacity: Option[CompletionCapacity] = None)

  object CompletionCapacityView {

    def apply(cc: CompletionCapacity): CompletionCapacityView = new CompletionCapacityView(cc.name.id, Some(cc))

    implicit val format = Json.format[CompletionCapacityView]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatLodgingOfficer) => group.completionCapacity,
      updateF = (c: CompletionCapacityView, g: Option[S4LVatLodgingOfficer]) =>
        g.getOrElse(S4LVatLodgingOfficer()).copy(completionCapacity = Some(c))
    )

    // return a view model from a VatScheme instance
    implicit val modelTransformer = ApiModelTransformer[CompletionCapacityView] { vs: VatScheme =>
      vs.lodgingOfficer match{
        case Some(VatLodgingOfficer(_,_,_,Some(b),Some(a),_,_,_,_)) => Some(CompletionCapacityView(a.id, Some(CompletionCapacity(a, b))))
        case _ => None
      }
    }

  }

}

package features.officers.controllers {

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
              officerList <- prePopService.getOfficerList
              officer     <- lodgingOfficerService.getLodgingOfficer
              filledForm  = officer.completionCapacity.fold(form)(form.fill)
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
              cc => lodgingOfficerService.updateLodgingOfficer(cc) map {
                _ => Redirect(features.officers.controllers.routes.OfficerSecurityQuestionsController.show())
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
