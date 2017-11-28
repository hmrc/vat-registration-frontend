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

package controllers.sicAndCompliance.labour {

  import javax.inject.{Inject, Singleton}

  import connectors.KeystoreConnect
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.sicAndCompliance.labour.CompanyProvideWorkersForm
  import models.S4LVatSicAndCompliance
  import models.S4LVatSicAndCompliance.{dropFromCompanyProvideWorkers, labourOnly}
  import models.view.sicAndCompliance.labour.CompanyProvideWorkers
  import models.view.sicAndCompliance.labour.CompanyProvideWorkers.PROVIDE_WORKERS_YES
  import play.api.mvc.{Action, AnyContent}
  import services.{RegistrationService, S4LService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  @Singleton
  class CompanyProvideWorkersController @Inject()(ds: CommonPlayDependencies,
                                                  val keystoreConnector: KeystoreConnect,
                                                  val authConnector: AuthConnector,
                                                  implicit val s4lService: S4LService,
                                                  implicit val vrs: RegistrationService) extends VatRegistrationController(ds) with SessionProfile {

    val form = CompanyProvideWorkersForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              viewModel[CompanyProvideWorkers]().fold(form)(form.fill)
                .map(f => Ok(features.sicAndCompliance.views.html.labour.company_provide_workers(f)))
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              form.bindFromRequest().fold(
                badForm => BadRequest(features.sicAndCompliance.views.html.labour.company_provide_workers(badForm)).pure,
                view => (if (PROVIDE_WORKERS_YES == view.yesNo) {
                  for {
                    container <- s4lContainer[S4LVatSicAndCompliance]()
                    _         <- s4lService.save(labourOnly(container.copy(companyProvideWorkers = Some(view))))
                  } yield controllers.sicAndCompliance.labour.routes.WorkersController.show()
                } else {
                  for {
                    container <- s4lContainer[S4LVatSicAndCompliance]()
                    _         <- s4lService.save(dropFromCompanyProvideWorkers(labourOnly(container.copy(companyProvideWorkers = Some(view)))))
                    _         <- vrs.submitSicAndCompliance
                  } yield controllers.vatTradingDetails.vatEuTrading.routes.EuGoodsController.show()
                }).map(Redirect))
            }
          }
    }
  }

}

package forms.sicAndCompliance.labour {

  import forms.FormValidation._
  import models.view.sicAndCompliance.labour.CompanyProvideWorkers
  import play.api.data.Form
  import play.api.data.Forms._

  object CompanyProvideWorkersForm {

    val RADIO_YES_NO: String = "companyProvideWorkersRadio"
    implicit val errorCode: ErrorCode = "companyProvideWorkers"

    val form = Form(
      mapping(
        RADIO_YES_NO -> textMapping().verifying(CompanyProvideWorkers.valid)
      )(CompanyProvideWorkers.apply)(CompanyProvideWorkers.unapply)
    )

  }

}
