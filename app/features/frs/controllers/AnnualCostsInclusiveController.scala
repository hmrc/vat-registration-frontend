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

import javax.inject.Inject

package models.view.frs {
  
  import models.ApiModelTransformer
  import models.api.VatScheme
  import play.api.libs.json.Json

  case class AnnualCostsInclusiveView(selection: String)

  object AnnualCostsInclusiveView {

    val YES = "yes"
    val YES_WITHIN_12_MONTHS = "yesWithin12months"
    val NO = "no"

    val valid: (String) => Boolean = List(YES, YES_WITHIN_12_MONTHS, NO).contains

    implicit val format = Json.format[AnnualCostsInclusiveView]

//    implicit val viewModelFormat = ViewModelFormat(
//      readF = (group: S4LFlatRateScheme) => group.annualCostsInclusive,
//      updateF = (c: AnnualCostsInclusiveView, g: Option[S4LFlatRateScheme]) =>
//        g.getOrElse(S4LFlatRateScheme()).copy(annualCostsInclusive = Some(c))
//    )

    implicit val modelTransformer = ApiModelTransformer[AnnualCostsInclusiveView] { vs: VatScheme =>
      vs.vatFlatRateScheme.flatMap(_.annualCostsInclusive).collect {
        case choice@(YES | YES_WITHIN_12_MONTHS | NO) => AnnualCostsInclusiveView(choice)
      }
    }
  }
}

package controllers.frs {

  import config.FrontendAuthConnector
  import connectors.KeystoreConnector
  import controllers.VatRegistrationControllerNoAux
  import forms.frs.AnnualCostsInclusiveForm
  import models.view.frs.AnnualCostsInclusiveView.NO
  import models.view.frs.AnnualCostsInclusiveView
  import play.api.data.Form
  import play.api.i18n.MessagesApi
  import play.api.mvc.{Action, AnyContent}
  import services.{S4LService, SessionProfile, VatRegistrationService}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  import scala.concurrent.Future

  class AnnualCostsInclusiveControllerImpl @Inject()(val messagesApi: MessagesApi,
                                                     val service: VatRegistrationService) extends AnnualCostsInclusiveController {
    override val authConnector: AuthConnector = FrontendAuthConnector
    override val keystoreConnector: KeystoreConnector = KeystoreConnector
  }

  trait AnnualCostsInclusiveController extends VatRegistrationControllerNoAux with SessionProfile {

    val service: VatRegistrationService

    val annualCostsInclusiveForm: Form[AnnualCostsInclusiveView] = AnnualCostsInclusiveForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            service.fetchFlatRateScheme.map { flatRateScheme =>
              val form = flatRateScheme.annualCostsInclusive match {
                case Some(view) => annualCostsInclusiveForm.fill(view)
                case None       => annualCostsInclusiveForm
              }
              Ok(features.frs.views.html.annual_costs_inclusive(form))
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            annualCostsInclusiveForm.bindFromRequest().fold(
              badForm => Future.successful(BadRequest(features.frs.views.html.annual_costs_inclusive(badForm))),
              view =>
                if (view.selection == NO) {
                  service.isOverLimitedCostTraderThreshold map {
                    case true  => Redirect(controllers.frs.routes.AnnualCostsLimitedController.show())
                    case false => Redirect(controllers.frs.routes.ConfirmBusinessSectorController.show())
                  }
                } else {
                  service.saveAnnualCostsInclusive(view) map { _ =>
                    Redirect(controllers.frs.routes.RegisterForFrsController.show())
                  }
                }
            )

//                if (view.selection == NO) {
//                save(view).flatMap(_ =>
//                  getFlatRateSchemeThreshold().map {
//                    case n if n > PREVIOUS_QUESTION_THRESHOLD => controllers.frs.routes.AnnualCostsLimitedController.show()
//                    case _ => controllers.frs.routes.ConfirmBusinessSectorController.show()
//                  })
//              } else {
//                for {
//                // save annualCostsInclusive and delete all later elements
//                  _ <- s4LService.save(S4LFlatRateScheme(joinFrs = Some(JoinFrsView(true)), annualCostsInclusive = Some(view)))
//                } yield controllers.frs.routes.RegisterForFrsController.show()
//              }
          }
    }
  }
}

package forms.frs {

  import forms.FormValidation.textMapping
  import models.view.frs.AnnualCostsInclusiveView
  import play.api.data.Form
  import play.api.data.Forms.mapping

  object AnnualCostsInclusiveForm {

    val RADIO_INCLUSIVE: String = "annualCostsInclusiveRadio"

    val form = Form(
      mapping(
        RADIO_INCLUSIVE -> textMapping()("frs.costsInclusive")
          .verifying(AnnualCostsInclusiveView.valid)
      )(AnnualCostsInclusiveView.apply)(AnnualCostsInclusiveView.unapply)
    )
  }
}
