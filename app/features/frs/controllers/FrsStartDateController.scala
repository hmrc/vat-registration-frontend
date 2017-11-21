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

package models.view.frs {

  import java.time.LocalDate

  import models._
  import models.api.{VatFlatRateScheme, VatScheme}
  import play.api.libs.json.Json

  import scala.util.Try

  case class FrsStartDateView(dateType: String = "", date: Option[LocalDate] = None)

  object FrsStartDateView {

    def bind(dateType: String, dateModel: Option[DateModel]): FrsStartDateView =
      FrsStartDateView(dateType, dateModel.flatMap(_.toLocalDate))

    def unbind(frsStartDate: FrsStartDateView): Option[(String, Option[DateModel])] =
      Try {
        frsStartDate.date.fold((frsStartDate.dateType, Option.empty[DateModel])) {
          d => (frsStartDate.dateType, Some(DateModel.fromLocalDate(d)))
        }
      }.toOption

    val VAT_REGISTRATION_DATE = "VAT_REGISTRATION_DATE"
    val DIFFERENT_DATE = "DIFFERENT_DATE"

    val validSelection: String => Boolean = Seq(VAT_REGISTRATION_DATE, DIFFERENT_DATE).contains

    implicit val format = Json.format[FrsStartDateView]

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer[FrsStartDateView] { vs: VatScheme =>
      vs.vatFlatRateScheme.collect {
        case VatFlatRateScheme(_, _, _, _, Some(dateType), date, _, _) => FrsStartDateView(dateType, date) //TODO review if such collect necessary
      }
    }
  }
}

package controllers.frs {

  import javax.inject.Inject

  import config.FrontendAuthConnector
  import connectors.KeystoreConnector
  import controllers.VatRegistrationControllerNoAux
  import forms.frs.FrsStartDateFormFactory
  import models.view.frs.FrsStartDateView
  import play.api.data.Form
  import play.api.i18n.MessagesApi
  import play.api.mvc._
  import services.{SessionProfile, VatRegistrationService}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  import scala.concurrent.Future

  class FrsStartDateControllerImpl @Inject()(frsStartDateFormFactory: FrsStartDateFormFactory,
                                             val messagesApi: MessagesApi,
                                             val service: VatRegistrationService) extends FrsStartDateController {
    override val keystoreConnector: KeystoreConnector = KeystoreConnector
    override val authConnector: AuthConnector = FrontendAuthConnector
    val startDateForm: Form[FrsStartDateView] = frsStartDateFormFactory.form()
  }

  trait FrsStartDateController extends VatRegistrationControllerNoAux with SessionProfile {

    val service: VatRegistrationService
    val startDateForm: Form[FrsStartDateView]

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              service.fetchFlatRateScheme map { flatRateScheme =>
                val viewForm = flatRateScheme.frsStartDate.fold(startDateForm)(startDateForm.fill)
                Ok(features.frs.views.html.frs_start_date(viewForm))
              }
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              startDateForm.bindFromRequest().fold(
                badForm => Future.successful(BadRequest(features.frs.views.html.frs_start_date(badForm))),
                view => service.saveFRSStartDate(view) map { _ =>
                  Redirect(controllers.routes.SummaryController.show())
                }
              )
            }
          }
    }
  }
}

package forms.frs {

  import java.time.LocalDate
  import javax.inject.Inject

  import common.Now
  import forms.FormValidation.Dates.{nonEmptyDateModel, validDateModel}
  import forms.FormValidation.{onOrAfter, textMapping}
  import models.DateModel
  import models.view.frs.FrsStartDateView
  import play.api.data.Form
  import play.api.data.Forms._
  import services.DateService
  import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

  class FrsStartDateFormFactory @Inject()(dateService: DateService, today: Now[LocalDate]) {

    implicit object LocalDateOrdering extends Ordering[LocalDate] {
      override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
    }

    val RADIO_INPUT_NAME = "frsStartDateRadio"

    def form(): Form[FrsStartDateView] = {

      val minDate: LocalDate = dateService.addWorkingDays(today(), 2)

      implicit val specificErrorCode: String = "frs.startDate"

      Form(
        mapping(
          RADIO_INPUT_NAME -> textMapping()("frs.startDate.choice").verifying(FrsStartDateView.validSelection),
          "frsStartDate" -> mandatoryIf(
            isEqual(RADIO_INPUT_NAME, FrsStartDateView.DIFFERENT_DATE),
            mapping(
              "day" -> text,
              "month" -> text,
              "year" -> text
            )(DateModel.apply)(DateModel.unapply).verifying(
              nonEmptyDateModel(validDateModel(onOrAfter(minDate))))
          )
        )(FrsStartDateView.bind)(FrsStartDateView.unbind)
      )
    }
  }
}
