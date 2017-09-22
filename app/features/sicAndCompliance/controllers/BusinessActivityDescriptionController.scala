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

  import connectors.KeystoreConnector
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.sicAndCompliance.BusinessActivityDescriptionForm
  import models.view.sicAndCompliance.BusinessActivityDescription
  import play.api.data.Form
  import play.api.mvc.{Action, AnyContent}
  import services.{S4LService, SessionProfile, VatRegistrationService}


  class BusinessActivityDescriptionController @Inject()(ds: CommonPlayDependencies)
                                                       (implicit s4l: S4LService, vrs: VatRegistrationService)
    extends VatRegistrationController(ds) with SessionProfile {

    val keystoreConnector: KeystoreConnector = KeystoreConnector

    val form: Form[BusinessActivityDescription] = BusinessActivityDescriptionForm.form

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            viewModel[BusinessActivityDescription]().fold(form)(form.fill)
              .map(f => Ok(features.sicAndCompliance.views.html.business_activity_description(f)))
          }
    }


    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            form.bindFromRequest().fold(
              badForm => BadRequest(features.sicAndCompliance.views.html.business_activity_description(badForm)).pure,
              data => save(data.copy(description = data.description.trim)).map(_ =>
                Redirect(controllers.test.routes.SicStubController.show())))
          }
    }

  }

}

package forms.sicAndCompliance {

  import forms.FormValidation._
  import models.view.sicAndCompliance.BusinessActivityDescription
  import play.api.data.Form
  import play.api.data.Forms._

  object BusinessActivityDescriptionForm {
    val INPUT_DESCRIPTION: String = "description"
    val PartPattern = """^[A-Za-z0-9\-',/& ]{1,250}$""".r

    val form = Form(
      mapping(
        INPUT_DESCRIPTION -> text.transform(removeNewlineAndTrim, identity[String]).verifying(regexPattern(PartPattern)("BusinessActivity.description"))
      )(BusinessActivityDescription.apply)(BusinessActivityDescription.unapply)
    )
  }

}