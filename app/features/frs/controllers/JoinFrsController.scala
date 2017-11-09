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

package models.view.frs {

  import models.ApiModelTransformer
  import models.api.VatScheme
  import play.api.libs.json.Json

  case class JoinFrsView(selection: Boolean)

  object JoinFrsView {
    implicit val format = Json.format[JoinFrsView]

//    implicit val viewModelFormat = ViewModelFormat(
//      readF = (_: S4LFlatRateScheme).joinFrs,
//      updateF = (c: JoinFrsView, g: Option[S4LFlatRateScheme]) =>
//        g.getOrElse(S4LFlatRateScheme()).copy(joinFrs = Some(c))
//    )

    implicit val modelTransformer = ApiModelTransformer[JoinFrsView] { (vs: VatScheme) =>
      vs.vatFlatRateScheme.map(_.joinFrs).map(JoinFrsView(_))
    }
  }
}

package controllers.frs {

  import javax.inject.Inject

  import models.{S4LFlatRateScheme, S4LKey, S4LModelTransformer}
  import play.api.data.Form

//  import cats.data.OptionT
//  import cats.syntax.FlatMapSyntax
  import connectors.KeystoreConnector
  import controllers.{CommonPlayDependencies, VatRegistrationController}
  import forms.genericForms.{YesOrNoAnswer, YesOrNoFormFactory}
  import models.view.frs.JoinFrsView
  import play.api.mvc.{Action, AnyContent}
  import services.{S4LService, SessionProfile, VatRegistrationService}

  import scala.concurrent.Future

  class JoinFrsController @Inject()(ds: CommonPlayDependencies,
                                    formFactory: YesOrNoFormFactory,
                                    s4LService: S4LService,
                                    vrs: VatRegistrationService)
    extends VatRegistrationController(ds) with SessionProfile {

    val keystoreConnector: KeystoreConnector = KeystoreConnector
    val form: Form[YesOrNoAnswer] = formFactory.form("joinFrs")("frs.join")
    val s4lFRSKey: S4LKey[S4LFlatRateScheme] = S4LFlatRateScheme.vatFlatRateScheme
    val apiToS4LViewTransformer: S4LModelTransformer[S4LFlatRateScheme] = S4LFlatRateScheme.modelT


//    def show: Action[AnyContent] = authorised.async {
//      implicit user =>
//        implicit request =>
//          withCurrentProfile { implicit profile =>
//            viewModel[JoinFrsView]().map(vm => YesOrNoAnswer(vm.selection)).fold(form)(form.fill)
//              .map(f => Ok(features.frs.views.html.frs_join(f)))
//          }
//    }

    def show: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>

            val fetchedS4LFlatRatScheme = s4LService.fetchAndGetNoAux(s4lFRSKey)

            val s4lFlatRateSchemeLogicalBlock = fetchedS4LFlatRatScheme.flatMap(_.fold(
              vrs.getVatScheme map apiToS4LViewTransformer.toS4LModel
            )(s4LFlatRateScheme => Future.successful(s4LFlatRateScheme)))

            val joinFrsViewOpt = s4lFlatRateSchemeLogicalBlock.map(_.joinFrs) //old ViewModelFormat readF

            val yesOrNoAnswerForm = joinFrsViewOpt.map(_.fold(form)(joinFrsView => form.fill(YesOrNoAnswer(joinFrsView.selection))))

            yesOrNoAnswerForm.map(f => Ok(features.frs.views.html.frs_join(f)))
          }
    }

//    def submit: Action[AnyContent] = authorised.async {
//      implicit user =>
//        implicit request =>
//          withCurrentProfile { implicit profile =>
//            form.bindFromRequest().fold(
//              badForm => Future.successful(BadRequest(features.frs.views.html.frs_join(badForm))),
//              view => (if (view.answer) {
//                save(JoinFrsView(view.answer)).map(_ =>
//                  controllers.frs.routes.AnnualCostsInclusiveController.show())
//              } else {
//                for {
//                  _ <- s4LService.save(S4LFlatRateScheme(joinFrs = Some(JoinFrsView(false))))
//                  _ <- vrs.submitVatFlatRateScheme()
//                } yield controllers.routes.SummaryController.show()
//              }).map(Redirect))
//          }
//    }

    def submit: Action[AnyContent] = authorised.async {
      implicit user =>
        implicit request =>
          withCurrentProfile { implicit profile =>
            form.bindFromRequest().fold(
              badForm => Future.successful(BadRequest(features.frs.views.html.frs_join(badForm))),
              view => if (view.answer) {

                val newJoinFRSView = JoinFrsView(view.answer)

                val s4lFRSLogicalBlock = s4LService.fetchAndGetNoAux(s4lFRSKey)
                  .flatMap(_.fold(vrs.getVatScheme map apiToS4LViewTransformer.toS4LModel)(s => Future.successful(s)))

                val updatedS4LFRSLogicalBlock =
                  s4lFRSLogicalBlock.map(_.copy(joinFrs = Some(newJoinFRSView))) // old ViewModelFormat updateF

                val savedNewLogicalBlock = updatedS4LFRSLogicalBlock flatMap { newFrsLogicalBlock =>
                  s4LService.saveNoAux(newFrsLogicalBlock, s4lFRSKey)
                }

                savedNewLogicalBlock.map(_ => Redirect(controllers.frs.routes.AnnualCostsInclusiveController.show()))

              } else {
                for {
                  _ <- s4LService.saveNoAux(S4LFlatRateScheme(joinFrs = Some(JoinFrsView(false))), s4lFRSKey)
                  _ <- vrs.submitVatFlatRateScheme()
                } yield Redirect(controllers.routes.SummaryController.show())
              }
            )
          }
    }
  }
}
