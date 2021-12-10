/*
 * Copyright 2021 HM Revenue & Customs
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

import config.{BaseControllerComponents, FrontendAppConfig}
import connectors.ConfigConnector
import controllers.BaseController
import forms.ChooseBusinessTypeForm
import play.api.libs.json.JsObject
import play.api.mvc.{Action, AnyContent}
import services.{FlatRateService, SessionProfile, SessionService, SicAndComplianceService, VatRegistrationService}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.choose_business_type

import javax.inject.{Inject, Singleton}
import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChooseBusinessTypeController @Inject()(val authConnector: AuthConnector,
                                             val sessionService: SessionService,
                                             configConnector: ConfigConnector,
                                             flatRateService: FlatRateService,
                                             chooseBusinessTypeView: choose_business_type)
                                            (implicit appConfig: FrontendAppConfig,
                                             val executionContext: ExecutionContext,
                                             baseControllerComponents: BaseControllerComponents) extends BaseController with SessionProfile {

  lazy val groupingBusinessTypesValues: ListMap[String, Seq[(String, String)]] = ListMap(configConnector.businessTypes.map { jsObj =>
    (
      (jsObj \ "groupLabel").as[String],
      (jsObj \ "categories").as[Seq[JsObject]].map(js => ((js \ "id").as[String], (js \ "businessType").as[String]))
    )
  }.sortBy(_._1): _*)

  lazy val businessTypeIds: Seq[String] = groupingBusinessTypesValues.values.toSeq.flatMap(radioValues => radioValues map Function.tupled((id, _) => id))

  def show: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        for {
          flatRateScheme <- flatRateService.getFlatRate
          form = ChooseBusinessTypeForm.form(businessTypeIds)
          formFilled = flatRateScheme.categoryOfBusiness.fold(form)(v => form.fill(v))
        } yield {
          Ok(chooseBusinessTypeView(formFilled, groupingBusinessTypesValues))
        }
  }

  def submit: Action[AnyContent] = isAuthenticatedWithProfile() {
    implicit request =>
      implicit profile =>
        ChooseBusinessTypeForm.form(businessTypeIds).bindFromRequest().fold(
          badForm => Future.successful(BadRequest(chooseBusinessTypeView(badForm, groupingBusinessTypesValues))),
          data => flatRateService.saveBusinessType(data) map {
            _ => Redirect(controllers.routes.FlatRateController.yourFlatRatePage)
          }
        )
  }


}
