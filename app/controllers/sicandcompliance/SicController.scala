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

package controllers.sicandcompliance

import config.{AuthClientConnector, BaseControllerComponents, FrontendAppConfig}
import controllers.BaseController
import featureswitch.core.config.{FeatureSwitching, StubIcl}
import models.CurrentProfile
import models.ModelKeys.SIC_CODES_KEY
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{Action, AnyContent, Result}
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
                              val aboutToConfirmSicPage: about_to_confirm_sic)
                             (implicit appConfig: FrontendAppConfig,
                              val executionContext: ExecutionContext,
                              baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile with FeatureSwitching {

  val iclFEurlwww: String = appConfig.servicesConfig.getConfString("industry-classification-lookup-frontend.www.url",
    throw new RuntimeException("[ICLConnector] Could not retrieve config for 'industry-classification-lookup-frontend.www.url'"))

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      _ =>
        Future.successful(Ok(aboutToConfirmSicPage()))
  }

  def startICLJourney: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        startSelectingNewSicCodes
  }

  def saveICLCodes: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          iclCodes <- iclService.getICLSICCodes()
          _ <- sessionService.cache(SIC_CODES_KEY, iclCodes)
          _ <- businessService.submitSicCodes(iclCodes)
        } yield {
          iclCodes match {
            case codes if codes.size > 1 =>
              Redirect(controllers.sicandcompliance.routes.MainBusinessActivityController.show)
            case List() =>
              throw new InternalServerException("[SicAndComplianceController][saveIclCodes] tried to save empty list")
            case _ =>
              Redirect(controllers.sicandcompliance.routes.BusinessActivitiesResolverController.resolve)
          }
        }
  }

  private def startSelectingNewSicCodes(implicit hc: HeaderCarrier, cp: CurrentProfile, messages: Messages): Future[Result] = {
    if (isEnabled(StubIcl)) {
      Future.successful(Redirect(controllers.test.routes.SicStubController.show))
    } else {
      val customICLMessages: String => CustomICLMessages = (lang: String) => CustomICLMessages(
        messagesApi.translate("pages.icl.heading", Nil)(Lang(lang)),
        messagesApi.translate("pages.icl.lead", Nil)(Lang(lang)),
        messagesApi.translate("pages.icl.hint", Nil)(Lang(lang))
      )

      iclService.journeySetup(customICLMessages("en"), customICLMessages("cy")) map (redirectUrl => Redirect(iclFEurlwww + redirectUrl, 303))
    }
  }
}