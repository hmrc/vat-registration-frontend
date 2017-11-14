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

package features.frs.controllers

import javax.inject.{Inject, Singleton}

import config.FrontendAuthConnector
import connectors.KeystoreConnector
import controllers.VatRegistrationControllerNoAux
import forms.genericForms.{YesOrNoAnswer, YesOrNoFormFactory}
import models.view.frs.JoinFrsView
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{SessionProfile, VatRegistrationService}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

@Singleton
class FlatRateSchemeControllerImpl @Inject()(val messagesApi: MessagesApi,
                                             val service: VatRegistrationService) extends FlatRateSchemeController {
  override val keystoreConnector: KeystoreConnector = KeystoreConnector
  override val authConnector: AuthConnector = FrontendAuthConnector
}

trait FlatRateSchemeController extends VatRegistrationControllerNoAux with SessionProfile {

  val service: VatRegistrationService

  //Join Frs
  val joinFrsForm: Form[YesOrNoAnswer] = YesOrNoFormFactory.form("joinFrs")("frs.join")

  def frsShow: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          service.fetchFlatRateScheme map { flatRateScheme =>
            val form = flatRateScheme.joinFrs match {
              case Some(joinFrs) => joinFrsForm.fill(YesOrNoAnswer(joinFrs.selection))
              case None => joinFrsForm
            }
            Ok(features.frs.views.html.frs_join(form))
          }
        }
  }

  def frsSubmit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          joinFrsForm.bindFromRequest().fold(
            badForm => Future.successful(BadRequest(features.frs.views.html.frs_join(badForm))),
            joiningFRS => {
              service.saveJoinFRS(JoinFrsView(joiningFRS.answer)) map { _ =>
                if (joiningFRS.answer) {
                  Redirect(controllers.frs.routes.AnnualCostsInclusiveController.show())
                } else {
                  Redirect(controllers.routes.SummaryController.show())
                }
              }
            }
          )
        }
  }
}
