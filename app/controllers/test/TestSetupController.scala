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

import javax.inject.Inject

import connectors.{KeystoreConnector, VatRegistrationConnector}
import controllers.{CommonPlayDependencies, VatRegistrationController}
import enums.CacheKeys
import forms.vatDetails.TradingNameForm
import forms.vatDetails.test.TestSetupForm
import models.api._
import models.view._
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, S4LService}

import scala.concurrent.Future

class TestSetupController @Inject()(s4LService: S4LService, vatRegistrationConnector: VatRegistrationConnector,
                                    ds: CommonPlayDependencies) extends VatRegistrationController(ds) with CommonService {

  override val keystoreConnector: KeystoreConnector = KeystoreConnector

  def show: Action[AnyContent] = authorised(implicit user => implicit request => {
    Ok(views.html.pages.test_setup(TestSetupForm.form))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    TestSetupForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.test_setup(formWithErrors)))
      }, {
        data: TestSetup => {
          for {
            regId <- fetchRegistrationId

            _ <- vatRegistrationConnector.upsertVatChoice(regId,
              VatChoice(StartDate("",Some(data.startDate.substring(0,2).toInt),
                Some(data.startDate.substring(3,4).toInt),
                Some(data.startDate.substring(4,8).toInt)).toDateTime,
                data.necessity))

            _ <- vatRegistrationConnector.upsertVatTradingDetails(regId,
              VatTradingDetails(data.tradingName))

            _ <- vatRegistrationConnector.upsertVatFinancials(regId,
              VatFinancials(Some(VatBankAccount.default),
                data.vatTurnover.toLong,
                data.zeroRatedSalesEstimate match {
                  case "" => None
                  case _ => Some(data.zeroRatedSalesEstimate.toLong)
                },
                true,
                VatAccountingPeriod.default))

          } yield Ok
        }
      })
  })


}
