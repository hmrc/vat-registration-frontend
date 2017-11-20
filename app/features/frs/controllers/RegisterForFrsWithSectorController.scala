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

package controllers.frs {

  import javax.inject.Inject

  import config.FrontendAuthConnector
  import connectors.{ConfigConnect, KeystoreConnector}
  import forms.genericForms.{YesOrNoAnswer, YesOrNoFormFactory}
  import models.view.frs.RegisterForFrsView
  import play.api.data.Form
  import play.api.i18n.MessagesApi
  import play.api.mvc.{Action, AnyContent}
  import services.{SessionProfile, VatRegistrationService}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector


  class RegisterForFrsWithSectorControllerImpl @Inject()(val messagesApi: MessagesApi,
                                                         val configConnect: ConfigConnect,
                                                         val service: VatRegistrationService) extends RegisterForFrsWithSectorController {

    override val authConnector: AuthConnector = FrontendAuthConnector
    override val keystoreConnector: KeystoreConnector = KeystoreConnector
  }

  trait RegisterForFrsWithSectorController extends BusinessSectorAwareController with SessionProfile {

    val service: VatRegistrationService
    val formFactory: YesOrNoFormFactory = YesOrNoFormFactory

    val form: Form[YesOrNoAnswer] = formFactory.form("registerForFrsWithSector")("frs.registerForWithSector")

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            businessSectorView().map(sectorInfo => Ok(features.frs.views.html.frs_your_flat_rate(sectorInfo, form)))
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              form.bindFromRequest().fold(
                badForm => businessSectorView map { view =>
                  BadRequest(features.frs.views.html.frs_your_flat_rate(view, badForm))
                },
                view =>
                  for {
                    sector <- businessSectorView
                    _ <- service.saveBusinessSector(sector)
                    _ <- service.saveRegisterForFRS(RegisterForFrsView(view.answer))
                  } yield  {
                    if(view.answer){
                      Redirect(controllers.frs.routes.FrsStartDateController.show())
                    } else {
                      Redirect(controllers.routes.SummaryController.show())
                    }
                  }
              )

//              form.bindFromRequest().fold(
//                badForm => businessSectorView().map(view => BadRequest(features.frs.views.html.frs_your_flat_rate(view, badForm))),
//                view => (for {
//                  sector <- businessSectorView()
//                  _ <- save(sector)
//                  _ <- save(RegisterForFrsView(view.answer))
//                } yield view.answer).ifM(
//                  ifTrue = controllers.frs.routes.FrsStartDateController.show().pure,
//                  ifFalse = for {
//                    frs <- s4lContainer[S4LFlatRateScheme]()
//                    _ <- s4LService.save(frs.copy(frsStartDate = None))
//                    _ <- vrs.submitVatFlatRateScheme()
//                  } yield controllers.routes.SummaryController.show()
//                ).map(Redirect))
            }
          }
    }
  }
}
