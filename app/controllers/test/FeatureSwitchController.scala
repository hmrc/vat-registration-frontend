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

package controllers.test

import config.AuthClientConnector
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FeatureSwitchController @Inject()(mcc: MessagesControllerComponents,
                                        val featureManager: FeatureSwitchManager,
                                        val vatRegFeatureSwitch: VATRegFeatureSwitches,
                                        val authConnector: AuthClientConnector)
                                       (implicit ec: ExecutionContext) extends FrontendController(mcc) {

  def switcher(name: String, state: String): Action[AnyContent] = Action.async {
    implicit request =>
      def feature: FeatureSwitch = state match {
        case "true" => featureManager.enable(BooleanFeatureSwitch(name, enabled = true))
        case x if x.matches(featureManager.datePatternRegex) => featureManager.setSystemDate(ValueSetFeatureSwitch(name, state))
        case x@"time-clear" => featureManager.clearSystemDate(ValueSetFeatureSwitch(name, x))
        case _ => featureManager.disable(BooleanFeatureSwitch(name, enabled = false))
      }

      vatRegFeatureSwitch(name) match {
        case Some(_) => Future.successful(Ok(feature.toString))
        case None => Future.successful(BadRequest)
      }
  }
}
