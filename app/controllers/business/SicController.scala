/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.business

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import featureswitch.core.config.{FeatureSwitching, OtherBusinessInvolvement, StubIcl}
import forms.MainBusinessActivityForm
import models.CurrentProfile
import models.ModelKeys.SIC_CODES_KEY
import models.api.SicCode
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, Call, Result}
import services.BusinessService.MainBusinessActivity
import services._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import views.html.sicandcompliance._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SicController @Inject()(val authConnector: AuthClientConnector,
                              val sessionService: SessionService,
                              val businessService: BusinessService,
                              val frsService: FlatRateService,
                              val iclService: ICLService,
                              val aboutToConfirmSicPage: about_to_confirm_sic,
                              val mainBusinessActivityPage: main_business_activity)
                             (implicit appConfig: FrontendAppConfig,
                                           val executionContext: ExecutionContext,
                                           baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile with FeatureSwitching {

  val iclFEurlwww: String = appConfig.servicesConfig.getConfString("industry-classification-lookup-frontend.www.url",
    throw new RuntimeException("[ICLConnector] Could not retrieve config for 'industry-classification-lookup-frontend.www.url'"))

  private def fetchSicCodeList()(implicit hc: HeaderCarrier): Future[List[SicCode]] =
    sessionService.fetchAndGet[List[SicCode]](SIC_CODES_KEY) map (_.getOrElse(List.empty[SicCode]))

  def showMainBusinessActivity: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          sicCodeList <- fetchSicCodeList
          businessDetails <- businessService.getBusiness
          formFilled = businessDetails.mainBusinessActivity.fold(MainBusinessActivityForm.form)(sicCode => MainBusinessActivityForm.form.fill(sicCode.code))
        } yield Ok(mainBusinessActivityPage(formFilled, sicCodeList))
  }

  def submitMainBusinessActivity: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        fetchSicCodeList flatMap { sicCodeList =>
          MainBusinessActivityForm.form.bindFromRequest().fold(
            badForm => Future.successful(BadRequest(mainBusinessActivityPage(badForm, sicCodeList))),
            data => sicCodeList.find(_.code == data).fold(
              Future.successful(BadRequest(mainBusinessActivityPage(MainBusinessActivityForm.form.fill(data), sicCodeList)))
            )(selected => for {
              _ <- businessService.updateBusiness(MainBusinessActivity(selected))
              _ <- frsService.resetFRSForSAC(selected)
            } yield {
              Redirect(resolveRoute(sicCodeList))
            })
          )
        }
  }

  private def resolveRoute(sicCodeList: List[SicCode]): Call = {
    if (businessService.needComplianceQuestions(sicCodeList)) {
      controllers.business.routes.ComplianceIntroductionController.show
    } else {
      if (isEnabled(OtherBusinessInvolvement)) {
        controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show
      } else {
        controllers.routes.TradingNameResolverController.resolve(false)
      }
    }
  }

  def showSicHalt: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request => _ =>
      Future.successful(Ok(aboutToConfirmSicPage()))
  }

  private def startSelectingNewSicCodes(implicit hc: HeaderCarrier, cp: CurrentProfile, messages: Messages): Future[Result] = {
    if (isEnabled(StubIcl)) {
      Future.successful(Redirect(controllers.test.routes.SicStubController.show))
    } else {
      val customICLMessages: CustomICLMessages = CustomICLMessages(
        messages("pages.icl.heading"),
        messages("pages.icl.lead"),
        messages("pages.icl.hint")
      )

      iclService.journeySetup(customICLMessages) map (redirectUrl => Redirect(iclFEurlwww + redirectUrl, 303))
    }
  }

  def submitSicHalt: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        startSelectingNewSicCodes
  }

  def returnToICL: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        startSelectingNewSicCodes
  }

  def saveIclCodes: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          iclCodes <- iclService.getICLSICCodes()
          _ <- sessionService.cache(SIC_CODES_KEY, iclCodes)
          _ <- businessService.submitSicCodes(iclCodes)
        } yield {
          Redirect(iclCodes match {
            case codes if codes.size > 1 =>
              routes.SicController.showMainBusinessActivity
            case List() =>
              throw new InternalServerException("[SicAndComplianceController][saveIclCodes] tried to save empty list")
            case codes => resolveRoute(codes)
          })
        }
  }
}
