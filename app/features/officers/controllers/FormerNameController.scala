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

  import models.api._
  import models.{ApiModelTransformer, S4LVatLodgingOfficer, ViewModelFormat}
  import play.api.libs.json.Json

  case class FormerNameView(yesNo: Boolean,
                            formerName: Option[String] = None)

  object FormerNameView {

    def apply(changeOfName: ChangeOfName): FormerNameView = new FormerNameView(changeOfName.nameHasChanged, changeOfName.formerName.map(_.formerName))

    implicit val format = Json.format[FormerNameView]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatLodgingOfficer) => group.formerName,
      updateF = (c: FormerNameView, g: Option[S4LVatLodgingOfficer]) =>
        g.getOrElse(S4LVatLodgingOfficer()).copy(formerName = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer[FormerNameView] { vs: VatScheme =>
      vs.lodgingOfficer match {
        case Some(VatLodgingOfficer(_,_,_,_,_,Some(a),_,_,_)) =>  Some(FormerNameView(a.nameHasChanged, a.formerName.map(_.formerName)))
        case _ => None
      }
    }
  }
}

package controllers.vatLodgingOfficer {

  import javax.inject.{Inject, Singleton}

  import connectors.KeystoreConnect
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import features.officers.controllers.routes
  import forms.vatLodgingOfficer.FormerNameForm
  import models.view.vatLodgingOfficer.FormerNameView
  import play.api.data.Form
  import play.api.mvc._
  import services.{RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  @Singleton
  class FormerNameController @Inject()(ds: CommonPlayDependencies,
                                       implicit val s4LService: S4LService,
                                       val keystoreConnector: KeystoreConnect,
                                       val authConnector: AuthConnector,
                                       implicit val vatRegistrationService: RegistrationService) extends VatRegistrationController(ds) with SessionProfile {

    import cats.syntax.flatMap._

    val form: Form[FormerNameView] = FormerNameForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              viewModel[FormerNameView]().fold(form)(form.fill)
                .map(f => Ok(features.officers.views.html.former_name(f)))
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              form.bindFromRequest().fold(
                badForm => BadRequest(features.officers.views.html.former_name(badForm)).pure,
                data => data.yesNo.pure.ifM(
                  ifTrue = save(data).map(_ => Redirect(routes.FormerNameDateController.show())),
                  ifFalse = save(data).map(_ => Redirect(controllers.vatLodgingOfficer.routes.OfficerContactDetailsController.show()))))
            }
          }
    }

  }

}


package forms.vatLodgingOfficer {

  import forms.FormValidation._
  import models.view.vatLodgingOfficer.FormerNameView
  import play.api.data.Form
  import play.api.data.Forms._
  import uk.gov.voa.play.form.ConditionalMappings._

  object FormerNameForm {

    val RADIO_YES_NO: String = "formerNameRadio"
    val INPUT_FORMER_NAME: String = "formerName"

    val FORMER_NAME_REGEX = """^[A-Za-z0-9.,\-()/!"%&*;'<>][A-Za-z0-9 .,\-()/!"%&*;'<>]{0,55}$""".r

    implicit val errorCode: ErrorCode = INPUT_FORMER_NAME

    val form = Form(
      mapping(
        RADIO_YES_NO -> missingBooleanFieldMapping()("formerName"),
        INPUT_FORMER_NAME -> mandatoryIf(
          isEqual(RADIO_YES_NO, "true"),
          text.verifying(nonEmptyValidText(FORMER_NAME_REGEX)("formerName.selected")))
      )(FormerNameView.apply)(FormerNameView.unapply)
    )

  }

}
