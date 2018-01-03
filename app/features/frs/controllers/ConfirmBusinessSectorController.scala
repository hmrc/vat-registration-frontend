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

package controllers.frs {

  import javax.inject.Inject

  import connectors.{ConfigConnector, KeystoreConnect}
  import play.api.i18n.MessagesApi
  import play.api.mvc.{Action, AnyContent}
  import services.{RegistrationService, SessionProfile}
  import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

  class ConfirmBusinessSectorControllerImpl @Inject()(val messagesApi: MessagesApi,
                                                      val configConnect: ConfigConnector,
                                                      val service: RegistrationService,
                                                      val authConnector: AuthConnector,
                                                      val keystoreConnector: KeystoreConnect) extends ConfirmBusinessSectorController

  trait ConfirmBusinessSectorController extends BusinessSectorAwareController with SessionProfile {

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              businessSectorView() map { view =>
                Ok(features.frs.views.html.frs_confirm_business_sector(view))
              }
            }
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            ivPassedCheck {
              businessSectorView() flatMap {
                service.saveBusinessSector(_) map { _ =>
                  Redirect(controllers.frs.routes.RegisterForFrsWithSectorController.show())
                }
              }
            }
          }
    }
  }
}
