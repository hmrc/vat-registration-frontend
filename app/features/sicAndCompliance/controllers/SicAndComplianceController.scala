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

package features.sicAndCompliance.controllers

import javax.inject.Inject

import config.AuthClientConnector
import connectors.KeystoreConnector
import controllers.BaseController
import features.frs.services.FlatRateService
import features.sicAndCompliance.forms.{BusinessActivityDescriptionForm, MainBusinessActivityForm}
import features.sicAndCompliance.models.MainBusinessActivityView
import features.sicAndCompliance.services.{CustomICLMessages, ICLService, SicAndComplianceService}
import features.sicAndCompliance.views.html._
import models.CurrentProfile
import models.ModelKeys.SIC_CODES_KEY
import models.api.SicCode
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import services.SessionProfile
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.config.inject.ServicesConfig
import utils.VATRegFeatureSwitches

import scala.concurrent.Future

class SicAndComplianceControllerImpl @Inject()(val messagesApi: MessagesApi,
                                               val authConnector: AuthClientConnector,
                                               val keystoreConnector: KeystoreConnector,
                                               val sicAndCompService: SicAndComplianceService,
                                               val frsService: FlatRateService,
                                               val vatRegFeatureSwitch: VATRegFeatureSwitches,
                                               val config: ServicesConfig,
                                               val iclService: ICLService) extends SicAndComplianceController {

  val iclFEurlwww: String = config.getConfString("industry-classification-lookup-frontend.www.url",
    throw new RuntimeException("[ICLConnector] Could not retrieve config for 'industry-classification-lookup-frontend.www.url'"))
}

trait SicAndComplianceController extends BaseController with SessionProfile {

  val sicAndCompService: SicAndComplianceService
  val frsService: FlatRateService
  val vatRegFeatureSwitch: VATRegFeatureSwitches
  val iclService: ICLService
  val iclFEurlwww: String
  def useICLStub: Boolean = vatRegFeatureSwitch.useIclStub.enabled

  private def fetchSicCodeList()(implicit hc: HeaderCarrier): Future[List[SicCode]] =
    keystoreConnector.fetchAndGet[List[SicCode]](SIC_CODES_KEY) map (_.getOrElse(List.empty[SicCode]))

  def showBusinessActivityDescription: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          sicCompliance <- sicAndCompService.getSicAndCompliance
          formFilled = sicCompliance.description.fold(BusinessActivityDescriptionForm.form)(BusinessActivityDescriptionForm.form.fill)
        } yield Ok(business_activity_description(formFilled))
      }
  }

  def submitBusinessActivityDescription: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        BusinessActivityDescriptionForm.form.bindFromRequest().fold(
          badForm => Future.successful(BadRequest(business_activity_description(badForm))),
          data => sicAndCompService.updateSicAndCompliance(data).map {
            _ => Redirect(routes.SicAndComplianceController.showSicHalt())
          }
        )
      }
  }

  def showMainBusinessActivity: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          sicCodeList <- fetchSicCodeList
          sicCompliance <- sicAndCompService.getSicAndCompliance
          formFilled = sicCompliance.mainBusinessActivity.fold(MainBusinessActivityForm.form)(MainBusinessActivityForm.form.fill)
        } yield Ok(main_business_activity(formFilled, sicCodeList))
      }
  }

  def submitMainBusinessActivity: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        fetchSicCodeList flatMap { sicCodeList =>
          MainBusinessActivityForm.form.bindFromRequest().fold(
            badForm => Future.successful(BadRequest(main_business_activity(badForm, sicCodeList))),
            data => sicCodeList.find(_.code == data.id).fold(
              Future.successful(BadRequest(main_business_activity(MainBusinessActivityForm.form.fill(data), sicCodeList)))
            )(selected => for {
              _ <- sicAndCompService.updateSicAndCompliance(MainBusinessActivityView(selected))
              _ <- frsService.resetFRSForSAC(selected)
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

  def showComplianceIntro: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        Future.successful(Ok(compliance_introduction()))
      }
  }

  def submitComplianceIntro: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        Future.successful(Redirect(routes.LabourComplianceController.showProvideWorkers()))
      }
  }

  def showSicHalt: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        Future.successful(Ok(about_to_confirm_sic()))
      }
  }

  private def startSelectingNewSicCodes(implicit hc : HeaderCarrier, cp : CurrentProfile) : Future[Result] = {
    if (useICLStub) {
      Future.successful(Redirect(test.routes.SicStubController.show()))
    } else {
      val customICLMessages: CustomICLMessages = CustomICLMessages(
        messagesApi("pages.icl.heading"),
        messagesApi("pages.icl.lead"),
        messagesApi("pages.icl.hint")
      )

      iclService.journeySetup(customICLMessages) map(redirectUrl => Redirect(iclFEurlwww + redirectUrl, 303))
    }
  }

  def submitSicHalt: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        startSelectingNewSicCodes
      }
  }

  def returnToICL: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        startSelectingNewSicCodes
      }
  }

  def saveIclCodes: Action[AnyContent] = isAuthenticatedWithProfile {
    implicit request => implicit profile =>
      ivPassedCheck {
        for {
          iclCodes    <-  iclService.getICLSICCodes()
          cacheCodes  <-  keystoreConnector.cache(SIC_CODES_KEY, iclCodes)
          saveCodes   <-  sicAndCompService.submitSicCodes(iclCodes)
        } yield {
          Redirect(iclCodes match {
            case codes if sicAndCompService.needComplianceQuestions(codes) =>
              features.sicAndCompliance.controllers.routes.SicAndComplianceController.showComplianceIntro()
            case List(onlyOneCode) =>
              controllers.routes.TradingDetailsController.tradingNamePage()
            case _ =>
              routes.SicAndComplianceController.showMainBusinessActivity()
          })
        }
      }
  }
}
