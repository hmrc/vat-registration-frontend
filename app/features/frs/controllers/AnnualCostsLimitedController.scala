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

  import models._
  import models.api.VatScheme
  import play.api.libs.json.Json

  case class AnnualCostsLimitedView(selection: String)

  object AnnualCostsLimitedView {

    val YES = "yes"
    val YES_WITHIN_12_MONTHS = "yesWithin12months"
    val NO = "no"

    val valid: (String) => Boolean = List(YES, YES_WITHIN_12_MONTHS, NO).contains

    implicit val format = Json.format[AnnualCostsLimitedView]

    implicit val modelTransformer = ApiModelTransformer[AnnualCostsLimitedView] { vs: VatScheme =>
      vs.vatFlatRateScheme.flatMap(_.annualCostsLimited).collect {
        case YES => AnnualCostsLimitedView(YES)
        case YES_WITHIN_12_MONTHS => AnnualCostsLimitedView(YES_WITHIN_12_MONTHS)
        case NO => AnnualCostsLimitedView(NO)
      }
    }
  }
}

package controllers.frs {

  import javax.inject.Inject

  import connectors.KeystoreConnect
  import controllers.VatRegistrationControllerNoAux
  import forms.frs.AnnualCostsLimitedFormFactory
  import models.view.frs.AnnualCostsLimitedView
  import play.api.data.Form
  import play.api.i18n.MessagesApi
  import play.api.mvc.{Action, AnyContent}
  import services.{RegistrationService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  import scala.concurrent.Future

  class AnnualCostsLimitedControllerImpl @Inject()(val messagesApi: MessagesApi,
                                                   val service: RegistrationService,
                                                   val authConnector: AuthConnector,
                                                   val keystoreConnector: KeystoreConnect) extends AnnualCostsLimitedController

  trait AnnualCostsLimitedController extends VatRegistrationControllerNoAux with SessionProfile {

    val keystoreConnector: KeystoreConnect
    val service: RegistrationService

    val form: Form[AnnualCostsLimitedView] = AnnualCostsLimitedFormFactory.form()

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              for {
                flatRateScheme <- service.fetchFlatRateScheme
                flatRateSchemeThreshold <- service.getFlatRateSchemeThreshold()
              } yield {
                val viewForm = flatRateScheme.annualCostsLimited.fold(form)(form.fill)
                Ok(features.frs.views.html.annual_costs_limited(viewForm, flatRateSchemeThreshold))
              }
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              service.getFlatRateSchemeThreshold() flatMap { frsThreshold =>
                AnnualCostsLimitedFormFactory.form(Seq(frsThreshold)).bindFromRequest().fold(
                  errors => Future.successful(BadRequest(features.frs.views.html.annual_costs_limited(errors, frsThreshold))),
                  view =>  {
                    service.saveAnnualCostsLimited(view) map { _ =>
                      if (view.selection == AnnualCostsLimitedView.NO) {
                        Redirect(controllers.frs.routes.ConfirmBusinessSectorController.show())
                      } else {
                        Redirect(controllers.frs.routes.RegisterForFrsController.show())
                      }
                    }
                  }
                )
              }
            }
          }
    }
  }
}

package forms.frs {

  import forms.FormValidation.textMappingWithMessageArgs
  import models.view.frs.AnnualCostsLimitedView
  import play.api.data.Form
  import play.api.data.Forms.mapping

  object AnnualCostsLimitedFormFactory {

    val RADIO_COST_LIMITED: String = "annualCostsLimitedRadio"

    def form(msgArgs: Seq[Any] = Seq()): Form[AnnualCostsLimitedView] = {
      Form(mapping(
        RADIO_COST_LIMITED -> textMappingWithMessageArgs()(msgArgs)("frs.costsLimited").verifying(AnnualCostsLimitedView.valid)
      )(AnnualCostsLimitedView.apply)(AnnualCostsLimitedView.unapply))
    }
  }
}
