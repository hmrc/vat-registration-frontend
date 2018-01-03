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

package models.view.vatFinancials {

  import models.api.VatScheme
  import models.{ApiModelTransformer, S4LVatFinancials, ViewModelFormat}
  import play.api.libs.json.{Json, OFormat}

  case class EstimateVatTurnover(vatTurnoverEstimate: Long)

  object EstimateVatTurnover {

    implicit val format: OFormat[EstimateVatTurnover] = Json.format[EstimateVatTurnover]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatFinancials) => group.estimateVatTurnover,
      updateF = (c: EstimateVatTurnover, g: Option[S4LVatFinancials]) =>
        g.getOrElse(S4LVatFinancials()).copy(estimateVatTurnover = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer[EstimateVatTurnover] { (vs: VatScheme) =>
      vs.financials.map(_.turnoverEstimate).collect {
        case turnoverEstimate => EstimateVatTurnover(turnoverEstimate)
      }
    }
  }
}

package controllers.vatFinancials {

  import javax.inject.{Inject, Singleton}

  import connectors.KeystoreConnect
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.vatFinancials.EstimateVatTurnoverForm
  import models.view.vatFinancials.EstimateVatTurnover
  import play.api.mvc.{Action, AnyContent}
  import services.{RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  @Singleton
  class EstimateVatTurnoverController @Inject()(ds: CommonPlayDependencies,
                                                val keystoreConnector: KeystoreConnect,
                                                val authConnector: AuthConnector,
                                                implicit val s4LService: S4LService,
                                                implicit val vrs: RegistrationService)
    extends VatRegistrationController(ds) with SessionProfile {

    val form = EstimateVatTurnoverForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              viewModel[EstimateVatTurnover]().fold(form)(form.fill)
                .map(f => Ok(features.financials.views.html.estimate_vat_turnover(f)))
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              EstimateVatTurnoverForm.form.bindFromRequest().fold(
                badForm => BadRequest(features.financials.views.html.estimate_vat_turnover(badForm)).pure,
                view => for {
                  originalTurnover <- viewModel[EstimateVatTurnover]().fold[Long](0)(_.vatTurnoverEstimate)
                  _ <- keystoreConnector.cache[Long](EstimateVatTurnoverKey.lastKnownValueKey, originalTurnover)
                  _ <- save(view)
                } yield Redirect(controllers.vatFinancials.routes.ZeroRatedSalesController.show())
              )
            }
          }
    }
  }

  object EstimateVatTurnoverKey {
    val lastKnownValueKey = "lastKnownEstimatedVatTurnover"
  }
}

package forms.vatFinancials {

  import forms.FormValidation._
  import models.view.vatFinancials.EstimateVatTurnover
  import play.api.data.Form
  import play.api.data.Forms._

  object EstimateVatTurnoverForm {

    val TURNOVER_ESTIMATE: String = "turnoverEstimate"

    implicit val errorCode: ErrorCode = "estimate.vat.turnover"

    val form = Form(
      mapping(
        TURNOVER_ESTIMATE -> text.verifying(mandatoryNumericText).
          transform(taxEstimateTextToLong, longToText).verifying(boundedLong)
      )(EstimateVatTurnover.apply)(EstimateVatTurnover.unapply)
    )
  }
}
