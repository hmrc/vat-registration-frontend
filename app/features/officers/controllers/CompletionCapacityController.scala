/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.vatLodgingOfficer{

import javax.inject.Inject

import cats.data.OptionT
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatLodgingOfficer.CompletionCapacityForm
import models.ModelKeys._
import models.api.CompletionCapacity
import models.external.Officer
import models.view.vatLodgingOfficer.CompletionCapacityView
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.play.http.HeaderCarrier

class CompletionCapacityController @Inject()(ds: CommonPlayDependencies)
                                            (implicit s4l: S4LService,
                                             vrs: VatRegistrationService,
                                             prePopService: PrePopulationService)
  extends VatRegistrationController(ds) with CommonService with SessionProfile {

    private val form = CompletionCapacityForm.form

    private def fetchOfficerList()(implicit hc: HeaderCarrier) =
      OptionT(keystoreConnector.fetchAndGet[Seq[Officer]](OFFICER_LIST_KEY))

    def show: Action[AnyContent] = authorised.async{
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            for {
              officerList <- prePopService.getOfficerList()
              _ <- keystoreConnector.cache(OFFICER_LIST_KEY, officerList)
              res <- viewModel[CompletionCapacityView]().fold(form)(form.fill)
            } yield Ok(features.officers.views.html.completion_capacity(res, officerList))
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            form.bindFromRequest().fold(
              badForm => fetchOfficerList().getOrElse(Seq()).map(
                officerList => BadRequest(features.officers.views.html.completion_capacity(badForm, officerList))),
              view => for {
                officerSeq <- fetchOfficerList().getOrElse(Seq())
                selectedOfficer = officerSeq.find(_.name.id == view.id).getOrElse(Officer.empty)
                _ <- keystoreConnector.cache(REGISTERING_OFFICER_KEY, selectedOfficer)
                _ <- save(CompletionCapacityView(view.id, Some(CompletionCapacity(selectedOfficer.name, selectedOfficer.role))))
              } yield Redirect(controllers.vatLodgingOfficer.routes.OfficerSecurityQuestionsController.show()))
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
      mapping(
        NAME_ID -> textMapping()("completionCapacity")
      )(CompletionCapacityView(_))(view => Option(view.id))

    )
  }

}
