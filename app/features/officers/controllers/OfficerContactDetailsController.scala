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

  import models.api._
  import models.{ApiModelTransformer, S4LVatLodgingOfficer, ViewModelFormat}
  import play.api.libs.json.{Json, OFormat}

  case class OfficerContactDetailsView(
                                        email: Option[String] = None,
                                        daytimePhone: Option[String] = None,
                                        mobile: Option[String] = None
                                      )


  object OfficerContactDetailsView {

    def apply(ocd: OfficerContactDetails): OfficerContactDetailsView =
      OfficerContactDetailsView(email = ocd.email, daytimePhone = ocd.tel, mobile = ocd.mobile)

    implicit val format: OFormat[OfficerContactDetailsView] = Json.format[OfficerContactDetailsView]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatLodgingOfficer) => group.officerContactDetails,
      updateF = (c: OfficerContactDetailsView, g: Option[S4LVatLodgingOfficer]) =>
        g.getOrElse(S4LVatLodgingOfficer()).copy(officerContactDetails = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer[OfficerContactDetailsView] { (vs: VatScheme) =>
      vs.lodgingOfficer.map(_.contact).collect {
        case OfficerContactDetails(e, t, m) =>
          OfficerContactDetailsView(email = e, daytimePhone = t, mobile = m)
      }
    }
  }
}

package controllers.vatLodgingOfficer {

  import javax.inject.Inject

  import cats.syntax.FlatMapSyntax
  import connectors.KeystoreConnector
  import controllers.vatTradingDetails.vatChoice.{routes => vatChoiceRoutes}
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatLodgingOfficer.OfficerContactDetailsForm
  import models.view.vatLodgingOfficer.OfficerContactDetailsView
  import play.api.mvc.{Action, AnyContent}
  import services.{S4LService, SessionProfile, VatRegistrationService}

  class OfficerContactDetailsController @Inject()(ds: CommonPlayDependencies)
                                                 (implicit s4l: S4LService, vrs: VatRegistrationService)
    extends VatRegistrationController(ds) with FlatMapSyntax with SessionProfile {

    val form = OfficerContactDetailsForm.form
    val keystoreConnector: KeystoreConnector = KeystoreConnector

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            viewModel[OfficerContactDetailsView]().fold(form)(form.fill)
              .map(f => Ok(features.officers.views.html.officer_contact_details(f)))
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            form.bindFromRequest().fold(
              copyGlobalErrorsToFields("email", "daytimePhone", "mobile")
                .andThen(form => BadRequest(features.officers.views.html.officer_contact_details(form)).pure),
              data => save(data).map(_ => Redirect(controllers.vatLodgingOfficer.routes.OfficerHomeAddressController.show())))
          }
    }

  }

}

package forms.vatLodgingOfficer {

  import forms.FormValidation._
  import models.view.vatLodgingOfficer.OfficerContactDetailsView
  import play.api.data.Form
  import play.api.data.Forms._
  import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

  object OfficerContactDetailsForm {

    val EMAIL_MAX_LENGTH = 70
    val EMAIL_PATTERN = """^([A-Za-z0-9\-_.]+)@([A-Za-z0-9\-_.]+)\.[A-Za-z0-9\-_.]{2,3}$""".r
    val PHONE_NUMBER_PATTERN = """[\d]{1,20}""".r

    private val FORM_NAME = "officerContactDetails"

    private val EMAIL = "email"
    private val DAYTIME_PHONE = "daytimePhone"
    private val MOBILE = "mobile"

    implicit val errorCode: ErrorCode = "officerContactDetails.email"

    private def validationError(field: String) = ValidationError(s"validation.officerContact.missing", field)

    val form = Form(
      mapping(
        EMAIL -> optional(text.verifying(regexPattern(EMAIL_PATTERN)(s"$FORM_NAME.$EMAIL")).verifying(maxLenText(EMAIL_MAX_LENGTH))),
        DAYTIME_PHONE -> optional(text.transform(removeSpaces, identity[String]).verifying(regexPattern(PHONE_NUMBER_PATTERN)(s"$FORM_NAME.$DAYTIME_PHONE"))),
        MOBILE -> optional(text.transform(removeSpaces, identity[String]).verifying(regexPattern(PHONE_NUMBER_PATTERN)(s"$FORM_NAME.$MOBILE")))
      )(OfficerContactDetailsView.apply)(OfficerContactDetailsView.unapply).verifying(atLeastOneContactDetail)
    )

    def atLeastOneContactDetail: Constraint[OfficerContactDetailsView] = Constraint {
      case OfficerContactDetailsView(None, None, None) =>
        Invalid(Seq(EMAIL, MOBILE, DAYTIME_PHONE).map(validationError))
      case _ => Valid
    }

  }

}