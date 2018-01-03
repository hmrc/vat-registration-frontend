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

  import java.time.LocalDate

  import models._
  import models.api._
  import play.api.libs.json.Json

  case class FormerNameDateView(date: LocalDate)

  object FormerNameDateView {

    def bind(dateModel: DateModel): FormerNameDateView =
      FormerNameDateView(dateModel.toLocalDate.get) // form ensures valid date

    def unbind(formerNameDate: FormerNameDateView): Option[DateModel] =
      Some(DateModel.fromLocalDate(formerNameDate.date)) // form ensures valid date

    implicit val format = Json.format[FormerNameDateView]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatLodgingOfficer) => group.formerNameDate,
      updateF = (c: FormerNameDateView, g: Option[S4LVatLodgingOfficer]) =>
        g.getOrElse(S4LVatLodgingOfficer()).copy(formerNameDate = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer[FormerNameDateView] { vs: VatScheme =>
      vs.lodgingOfficer match{
        case Some(VatLodgingOfficer(_,_,_,_,_,Some(a),_,_,_)) =>
          a.formerName match {
            case Some(FormerName(_,Some(b))) => Some(FormerNameDateView(b))
            case _ => None
          }
        case _ => None
      }
    }
  }
}

package controllers.vatLodgingOfficer {

  import javax.inject.{Inject, Singleton}

  import cats.syntax.FlatMapSyntax
  import connectors.KeystoreConnect
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatLodgingOfficer.FormerNameDateForm
  import models.view.vatLodgingOfficer.{FormerNameDateView, FormerNameView}
  import play.api.data.Form
  import play.api.mvc._
  import services.{RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  @Singleton
  class FormerNameDateController @Inject()(ds: CommonPlayDependencies,
                                           val keystoreConnector: KeystoreConnect,
                                           val authConnector: AuthConnector,
                                           implicit val s4LService: S4LService,
                                           implicit val vrs: RegistrationService) extends VatRegistrationController(ds) with FlatMapSyntax with SessionProfile {

    val form: Form[FormerNameDateView] = FormerNameDateForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              for {
                formerName <- viewModel[FormerNameView]().subflatMap(_.formerName).getOrElse("")
                res <- viewModel[FormerNameDateView]().fold(form)(form.fill)
              } yield Ok(features.officers.views.html.former_name_date(res, formerName))
            }
          }
    }


    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              form.bindFromRequest().fold(
                badForm => viewModel[FormerNameView]().subflatMap(_.formerName).getOrElse("")
                  .map(formerName => BadRequest(features.officers.views.html.former_name_date(badForm, formerName))),
                data => save(data).map(_ => Redirect(controllers.vatLodgingOfficer.routes.OfficerContactDetailsController.show()))
              )
            }
          }
    }

  }

}

package forms.vatLodgingOfficer {

  import java.time.LocalDate

  import forms.FormValidation.Dates.{nonEmptyDateModel, validDateModel}
  import forms.FormValidation._
  import models.DateModel
  import models.view.vatLodgingOfficer.FormerNameDateView
  import play.api.data.Form
  import play.api.data.Forms.{mapping, text}

  object FormerNameDateForm {

    implicit val errorCode: ErrorCode = "formerNameDate"

    implicit object LocalDateOrdering extends Ordering[LocalDate] {
      override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
    }

    val minDate: LocalDate = LocalDate.of(1900, 1, 1)
    val maxDate: LocalDate = LocalDate.now()

    val form = Form(
      mapping(
        "formerNameDate" -> mapping(
          "day" -> text,
          "month" -> text,
          "year" -> text
        )(DateModel.apply)(DateModel.unapply).verifying(nonEmptyDateModel(validDateModel(inRange(minDate, maxDate))))
      )(FormerNameDateView.bind)(FormerNameDateView.unbind)
    )
  }

}
