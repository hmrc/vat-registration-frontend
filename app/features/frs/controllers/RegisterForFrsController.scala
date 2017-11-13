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

  import models.api.VatScheme
  import models.{ApiModelTransformer, S4LFlatRateScheme, ViewModelFormat}
  import play.api.libs.json.Json

  final case class RegisterForFrsView(selection: Boolean)

  object RegisterForFrsView {
    implicit val format = Json.format[RegisterForFrsView]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LFlatRateScheme) => group.registerForFrs,
      updateF = (c: RegisterForFrsView, g: Option[S4LFlatRateScheme]) =>
        g.getOrElse(S4LFlatRateScheme()).copy(registerForFrs = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer[RegisterForFrsView] { (vs: VatScheme) =>
      vs.vatFlatRateScheme.flatMap(answers => answers.doYouWantToUseThisRate.map(RegisterForFrsView.apply))
    }
  }
}

package controllers.frs {

  import javax.inject.Inject

  import cats.syntax.FlatMapSyntax
  import connectors.KeystoreConnector
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.genericForms.YesOrNoFormFactory
  import models.S4LFlatRateScheme
  import models.view.frs.{BusinessSectorView, RegisterForFrsView}
  import play.api.mvc.{Action, AnyContent}
  import services.{S4LService, SessionProfile, VatRegistrationService}

  class RegisterForFrsController @Inject()(ds: CommonPlayDependencies, formFactory: YesOrNoFormFactory)
                                          (implicit s4LService: S4LService, vrs: VatRegistrationService)
    extends VatRegistrationController(ds) with FlatMapSyntax with SessionProfile {

    val keystoreConnector: KeystoreConnector = KeystoreConnector

    val defaultFlatRate: BigDecimal = 16.5

    val form = formFactory.form("registerForFrs")("frs.registerFor")

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              Ok(features.frs.views.html.frs_register_for(form)).pure
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              form.bindFromRequest().fold(
                badForm => BadRequest(features.frs.views.html.frs_register_for(badForm)).pure,
                view => (for {
                  _ <- save(RegisterForFrsView(view.answer))
                  _ <- save(BusinessSectorView("", defaultFlatRate))
                } yield view.answer).ifM(
                  ifTrue = controllers.frs.routes.FrsStartDateController.show().pure,
                  ifFalse = for {
                    frs <- s4lContainer[S4LFlatRateScheme]()
                    _ <- s4LService.save(frs.copy(frsStartDate = None))
                    _ <- vrs.submitVatFlatRateScheme()
                  } yield controllers.routes.SummaryController.show()
                ).map(Redirect))
            }
          }
    }
  }
}
