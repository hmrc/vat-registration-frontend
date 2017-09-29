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

package controllers.test

import javax.inject.{Inject, Singleton}
import controllers.{CommonPlayDependencies, VatRegistrationController}
import play.api.mvc.{Action, AnyContent}
import utils._
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

/**
  * Created by eric on 26/09/17.
  */
@Singleton
class FeatureSwitchController  @Inject()(val injFeatureManager: FeatureSwitchManager,
                                         val injVatFeatureSwitch: VATRegFeatureSwitches,
                                         ds: CommonPlayDependencies)
  extends VatRegistrationController(ds) with FeatureSwitchCtrl {

  val featureManager: FeatureSwitchManager = injFeatureManager
  val vatRegFeatureSwitch: VATRegFeatureSwitches = injVatFeatureSwitch
}

trait FeatureSwitchCtrl extends FrontendController{
  val featureManager: FeatureManager
  val vatRegFeatureSwitch: VATRegFeatureSwitches

  def switcher(name: String, state: String): Action[AnyContent] = Action.async{
    implicit request =>

      def feature: FeatureSwitch = state match {
        case "true" => featureManager.enable(BooleanFeatureSwitch(name, enabled = true))
        case _ => featureManager.disable(BooleanFeatureSwitch(name, enabled = false))
      }

      vatRegFeatureSwitch(name) match {
        case Some(_) => Future.successful(Ok(feature.toString))
        case None => Future.successful(BadRequest)
      }
  }

}