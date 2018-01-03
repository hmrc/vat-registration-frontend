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

package controllers.sicAndCompliance.labour {

  import javax.inject.{Inject, Singleton}

  import connectors.KeystoreConnect
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.sicAndCompliance.labour.SkilledWorkersForm
  import models.view.sicAndCompliance.labour.SkilledWorkers
  import play.api.mvc.{Action, AnyContent}
  import services.{RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  @Singleton
  class SkilledWorkersController @Inject()(ds: CommonPlayDependencies,
                                           val keystoreConnector: KeystoreConnect,
                                           val authConnector: AuthConnector,
                                           implicit val vrs: RegistrationService,
                                           implicit val s4lService: S4LService) extends VatRegistrationController(ds) with SessionProfile {

    val form = SkilledWorkersForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              viewModel[SkilledWorkers]().fold(form)(form.fill)
                .map(f => Ok(features.sicAndCompliance.views.html.labour.skilled_workers(f)))
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            SkilledWorkersForm.form.bindFromRequest().fold(
              badForm => BadRequest(features.sicAndCompliance.views.html.labour.skilled_workers(badForm)).pure,
              view => for {
                _ <- save(view)
                _ <- vrs.submitSicAndCompliance
              } yield Redirect(controllers.vatTradingDetails.vatEuTrading.routes.EuGoodsController.show()))
          }
    }

  }

}

package forms.sicAndCompliance.labour {

  import forms.FormValidation._
  import models.view.sicAndCompliance.labour.SkilledWorkers
  import play.api.data.Form
  import play.api.data.Forms._

  object SkilledWorkersForm {
    val RADIO_YES_NO: String = "skilledWorkersRadio"
    implicit val errorCode: ErrorCode = "skilledWorkers"

    val form = Form(
      mapping(
        RADIO_YES_NO -> textMapping().verifying(SkilledWorkers.valid)
      )(SkilledWorkers.apply)(SkilledWorkers.unapply)
    )

  }

}
