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

 
  import java.time.LocalDate

  import models.api.{VatScheme, _}
  import models.{ApiModelTransformer, DateModel, _}
  import play.api.libs.json.Json

  case class OfficerSecurityQuestionsView(dob: LocalDate, nino: String, officerName: Option[Name] = None)

  object OfficerSecurityQuestionsView {

    def bind(dateModel: DateModel, nino: String): OfficerSecurityQuestionsView =
      OfficerSecurityQuestionsView(dateModel.toLocalDate.get, nino) // form ensures valid date

    def unbind(dobView: OfficerSecurityQuestionsView): Option[(DateModel, String)] =
      Some(DateModel.fromLocalDate(dobView.dob) -> dobView.nino) // form ensures valid date

    implicit val format = Json.format[OfficerSecurityQuestionsView]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatLodgingOfficer) => group.officerSecurityQuestions,
      updateF = (c: OfficerSecurityQuestionsView, g: Option[S4LVatLodgingOfficer]) =>
        g.getOrElse(S4LVatLodgingOfficer()).copy(officerSecurityQuestions = Some(c))
    )

    // return a view model from a VatScheme instance
    implicit val modelTransformer = ApiModelTransformer[OfficerSecurityQuestionsView] { vs: VatScheme =>
      vs.lodgingOfficer.collect {
        case VatLodgingOfficer(_,Some(a),Some(b),_,Some(c),_,_,_,_) => OfficerSecurityQuestionsView(a, b, Some(c))
      }
    }
  }
}

package controllers.vatLodgingOfficer {

  import javax.inject.{Inject, Singleton}

  import cats.syntax.CartesianSyntax
  import connectors.KeystoreConnect
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatLodgingOfficer.OfficerSecurityQuestionsForm
  import models.ModelKeys._
  import models.external.Officer
  import models.view.vatLodgingOfficer.OfficerSecurityQuestionsView
  import play.api.mvc._
  import services._
  import uk.gov.hmrc.http.HeaderCarrier
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  @Singleton
  class OfficerSecurityQuestionsController @Inject()(ds: CommonPlayDependencies,
                                                     val keystoreConnector: KeystoreConnect,
                                                     val authConnector: AuthConnector,
                                                     implicit val s4l: S4LService,
                                                     implicit val vrs: RegistrationService)
    extends VatRegistrationController(ds) with CartesianSyntax with SessionProfile {

    val form = OfficerSecurityQuestionsForm.form

    private def fetchOfficer()(implicit headerCarrier: HeaderCarrier) = keystoreConnector.fetchAndGet[Officer](REGISTERING_OFFICER_KEY)

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
              for {
                officer <- fetchOfficer()
                view <- viewModel[OfficerSecurityQuestionsView]().value
              } yield Ok(features.officers.views.html.officer_security_questions(getView(officer, view).fold(form)(form.fill)))

          }
    }

    def getView(officer: Option[Officer], view: Option[OfficerSecurityQuestionsView]): Option[OfficerSecurityQuestionsView] =
      (officer.map(_.name) == view.flatMap(_.officerName), officer.flatMap(_.dateOfBirth), view) match {
        case (_, None, None) => None
        case (true, _, Some(v)) => Some(v)
        case (false, None, Some(v)) if officer.isEmpty => Some(v)
        case (false, None, Some(_)) => None
        case (false, Some(dob), None) => Some(OfficerSecurityQuestionsView(dob, "", Some(officer.get.name)))
        case (false, Some(dob), Some(v)) => Some(OfficerSecurityQuestionsView(dob, v.nino, Some(officer.get.name)))
      }


    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            form.bindFromRequest().fold(
              badForm => BadRequest(features.officers.views.html.officer_security_questions(badForm)).pure,
              data => for {
                officer <- fetchOfficer()
                _ <- save(officer.fold(data)(officer => data.copy(officerName = Some(officer.name))))
              } yield Redirect(controllers.iv.routes.IdentityVerificationController.redirectToIV()))
          }
    }
  }
}

package forms.vatLodgingOfficer {

  import java.time.LocalDate

  import forms.FormValidation.Dates.{nonEmptyDateModel, validDateModel}
  import forms.FormValidation._
  import models.DateModel
  import models.view.vatLodgingOfficer.OfficerSecurityQuestionsView
  import play.api.data.Form
  import play.api.data.Forms.{mapping, text}

  object OfficerSecurityQuestionsForm {

    val NINO_REGEX = """^[[A-Z]&&[^DFIQUV]][[A-Z]&&[^DFIQUVO]] ?\d{2} ?\d{2} ?\d{2} ?[A-D]{1}$""".r

    implicit object LocalDateOrdering extends Ordering[LocalDate] {
      override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
    }

    val minDate: LocalDate = LocalDate.of(1900, 1, 1)
    val maxDate: LocalDate = LocalDate.now()

    val form = Form(
      mapping(
        "dob" -> {
          implicit val errorCodeDob: ErrorCode = "security.questions.dob"
          mapping(
            "day" -> text,
            "month" -> text,
            "year" -> text
          )(DateModel.apply)(DateModel.unapply).verifying(nonEmptyDateModel(validDateModel(inRange(minDate, maxDate))))
        }
        ,
        "nino" -> {
          implicit val errorCodeNino: ErrorCode = "security.questions.nino"
          text.verifying(nonEmptyValidText(NINO_REGEX))
        }
      )(OfficerSecurityQuestionsView.bind)(OfficerSecurityQuestionsView.unbind)
    )
  }

}
