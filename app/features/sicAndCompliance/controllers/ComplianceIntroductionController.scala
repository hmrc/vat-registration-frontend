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

package controllers.sicAndCompliance {

  import javax.inject.Inject

  import cats.data.OptionT
  import connectors.KeystoreConnector
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import models.ComplianceQuestions
  import models.view.test.SicStub
  import play.api.mvc._
  import services.{S4LService, SessionProfile}

  import scala.concurrent.Future

  class ComplianceIntroductionController @Inject()(s4LService: S4LService, ds: CommonPlayDependencies)
    extends VatRegistrationController(ds) with SessionProfile {

    val keystoreConnector: KeystoreConnector = KeystoreConnector

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { _ =>
            Future.successful(Ok(features.sicAndCompliance.views.html.compliance_introduction()))
          }
    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            OptionT(s4LService.fetchAndGet[SicStub]()).map(
              ss =>
                ComplianceQuestions(ss.sicCodes.toArray))
              .fold(controllers.test.routes.SicStubController.show())(_.firstQuestion).map(Redirect)
          }
    }

  }

}