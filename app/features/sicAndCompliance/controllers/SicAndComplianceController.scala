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

package features.sicAndCompliance.controllers

import javax.inject.Inject

import connectors.KeystoreConnect
import controllers.VatRegistrationControllerNoAux
import features.sicAndCompliance.forms.{BusinessActivityDescriptionForm, MainBusinessActivityForm}
import features.sicAndCompliance.models.MainBusinessActivityView
import features.sicAndCompliance.services.SicAndComplianceService
import features.sicAndCompliance.views.html._
import models.ModelKeys.SIC_CODES_KEY
import models.api.SicCode
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{FlatRateService, SessionProfile}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class SicAndComplianceControllerImpl @Inject()(implicit val messagesApi: MessagesApi,
                                               val authConnector: AuthConnector,
                                               val keystoreConnector: KeystoreConnect,
                                               val sicAndCompService: SicAndComplianceService,
                                               val frsService: FlatRateService) extends SicAndComplianceController {

}

trait SicAndComplianceController extends VatRegistrationControllerNoAux with SessionProfile {
  val sicAndCompService: SicAndComplianceService
  val frsService: FlatRateService

  private def fetchSicCodeList()(implicit hc: HeaderCarrier): Future[List[SicCode]] =
    keystoreConnector.fetchAndGet[List[SicCode]](SIC_CODES_KEY) map (_.getOrElse(List.empty[SicCode]))

  def showBusinessActivityDescription: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            for {
              sicCompliance <- sicAndCompService.getSicAndCompliance
              formFilled    = sicCompliance.description.fold(BusinessActivityDescriptionForm.form)(BusinessActivityDescriptionForm.form.fill)
            } yield Ok(business_activity_description(formFilled))
          }
        }
  }

  def submitBusinessActivityDescription: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            BusinessActivityDescriptionForm.form.bindFromRequest().fold(
              badForm => Future.successful(BadRequest(business_activity_description(badForm))),
              data => sicAndCompService.updateSicAndCompliance(data).map {
                _ => Redirect(test.routes.SicStubController.show())
              }
            )
          }
        }
  }

  def showMainBusinessActivity: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            for {
              sicCodeList   <- fetchSicCodeList
              sicCompliance <- sicAndCompService.getSicAndCompliance
              formFilled    = sicCompliance.mainBusinessActivity.fold(MainBusinessActivityForm.form)(MainBusinessActivityForm.form.fill)
            } yield Ok(main_business_activity(formFilled, sicCodeList))
          }
        }
  }

  def submitMainBusinessActivity: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            fetchSicCodeList flatMap { sicCodeList =>
              MainBusinessActivityForm.form.bindFromRequest().fold(
                badForm => Future.successful(BadRequest(main_business_activity(badForm, sicCodeList))),
                data => sicCodeList.find(_.id == data.id).fold(
                  Future.successful(BadRequest(main_business_activity(MainBusinessActivityForm.form.fill(data), sicCodeList)))
                )(selected => for {
                  _ <- sicAndCompService.updateSicAndCompliance(MainBusinessActivityView(selected))
                  _ <- frsService.resetFRS(selected)
                } yield {
                  if (sicAndCompService.needComplianceQuestions(sicCodeList)) {
                    Redirect(routes.SicAndComplianceController.showComplianceIntro())
                  } else {
                    Redirect(controllers.routes.TradingDetailsController.tradingNamePage())
                  }
                })
              )
            }
          }
        }
  }

  def showComplianceIntro: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            Future.successful(Ok(compliance_introduction()))
          }
        }
  }

  def submitComplianceIntro: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          ivPassedCheck {
            Future.successful(Redirect(routes.LabourComplianceController.showProvideWorkers()))
          }
        }
  }
}
