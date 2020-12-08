/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.registration.flatratescheme

import config.{AuthClientConnector, FrontendAppConfig}
import connectors.{ConfigConnector, KeystoreConnector}
import controllers.BaseController
import forms.genericForms.{YesOrNoAnswer, YesOrNoFormFactory}
import javax.inject.Inject
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{FlatRateService, SessionProfile, SicAndComplianceService, TimeService, VatRegistrationService}
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.{ExecutionContext, Future}
import views.html.frs_join

class JoinFlatRateSchemeController  @Inject()(mcc: MessagesControllerComponents,
                                              val flatRateService: FlatRateService,
                                              val vatRegistrationService: VatRegistrationService,
                                              val authConnector: AuthClientConnector,
                                              val keystoreConnector: KeystoreConnector,
                                              view: frs_join)
                                             (implicit val appConfig: FrontendAppConfig,
                                              val executionContext: ExecutionContext) extends BaseController(mcc) with SessionProfile {

  val joinFrsForm: Form[YesOrNoAnswer] = YesOrNoFormFactory.form()("frs.join")

  def show: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        vatRegistrationService.fetchTurnoverEstimates flatMap { res =>
          val turnoverEstimates = res.getOrElse(throw new InternalServerException("[JoinFRSController][show] Missing turnover estimates"))
          if (turnoverEstimates.turnoverEstimate > 150000L) {
            Future.successful(Redirect(controllers.routes.SummaryController.show()))
          } else {
            flatRateService.getFlatRate map { flatRateScheme =>
              val form = flatRateScheme.joinFrs.fold(joinFrsForm)(v => joinFrsForm.fill(YesOrNoAnswer(v)))
              Ok(view(form))
            }
          }
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request =>
      implicit profile =>
        joinFrsForm.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(view(badForm))),
          joiningFRS => flatRateService.saveJoiningFRS(joiningFRS.answer) map { _ =>
            if (joiningFRS.answer) {
              Redirect(controllers.routes.FlatRateController.annualCostsInclusivePage())
            } else {
              Redirect(controllers.routes.SummaryController.show())
            }
          }
        )
  }

}
