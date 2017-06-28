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

///*
// * Copyright 2017 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package models.view.frs
//
//import java.time.LocalDate
//
//import fixtures.VatRegistrationFixture
//import models.api.VatTradingDetails
//import models.{DateModel, S4LTradingDetails, ViewModelTransformer}
//import org.scalatest.Inside
//import uk.gov.hmrc.play.test.UnitSpec
//
//class FrsStartDateViewSpec extends UnitSpec with VatRegistrationFixture with Inside {
//
//  val date = LocalDate.of(2017, 3, 21)
//  val frsStartDate = FrsStartDateView(FrsStartDateView.DIFFERENT_DATE, Some(date))
//  val newFrsStartDate = FrsStartDateView(FrsStartDateView.DIFFERENT_DATE, Some(date))
//
//  "unbind" should {
//    "decompose a VAT_REGISTRATION_DATE FrsStartDate" in {
//      val testFrsStartDate = FrsStartDateView(FrsStartDateView.VAT_REGISTRATION_DATE)
//      inside(FrsStartDateView.unbind(testFrsStartDate)) {
//        case Some((dateChoice, odm)) =>
//          dateChoice shouldBe FrsStartDateView.VAT_REGISTRATION_DATE
//          odm shouldBe None
//      }
//    }
//
//    "decompose a DIFFERENT_DATE FrsStartDate" in {
//      val testFrsStartDate = FrsStartDateView(FrsStartDateView.DIFFERENT_DATE, Some(date))
//      inside(FrsStartDateView.unbind(testFrsStartDate)) {
//        case Some((dateChoice, odm)) =>
//          dateChoice shouldBe FrsStartDateView.DIFFERENT_DATE
//          odm shouldBe Some(DateModel.fromLocalDate(date))
//      }
//    }
//  }
//
//  "bind" should {
//    "create FrsStartDate when DateModel is present" in {
//      FrsStartDateView.bind("any", Some(DateModel.fromLocalDate(date))) shouldBe FrsStartDateView("any", Some(date))
//    }
//    "create FrsStartDate when DateModel is NOT present" in {
//      FrsStartDateView.bind("any", None) shouldBe FrsStartDateView("any", None)
//    }
//  }
//
//  "toApi" should {
//    "update a VatChoice a new FrsStartDate" in {
//      val vtd = tradingDetails(FrsStartDate = newStartDate.date)
//      inside(ViewModelTransformer[FrsStartDateView, VatTradingDetails].toApi(frsStartDate, vtd)) {
//        case tradingDetails => tradingDetails.vatChoice.vatStartDate.startDate shouldBe newFrsStartDate.date
//      }
//    }
//
//    "when no date present, StardDateView contains date type selection" in {
//      val c = FrsStartDateView("from S4L", None)
//      val g = tradingDetails()
//      val transformed = ViewModelTransformer[FrsStartDateView, VatTradingDetails].toApi(c, g)
//      transformed shouldBe g.copy(vatChoice = g.vatChoice.copy(vatStartDate = g.vatChoice.vatStartDate.copy(selection = "from S4L")))
//    }
//  }
//
//  "ViewModelFormat" should {
//    val s4LTradingDetails: S4LTradingDetails = S4LTradingDetails(frsStartDate = Some(validFrsStartDateView))
//
//    "extract frsStartDate from vatTradingDetails" in {
//      FrsStartDateView.viewModelFormat.read(s4LTradingDetails) shouldBe Some(validFrsStartDateView)
//    }
//
//    "update empty vatContact with frsStartDate" in {
//      FrsStartDateView.viewModelFormat.update(validFrsStartDateView, Option.empty[S4LTradingDetails]).frsStartDate shouldBe Some(validFrsStartDateView)
//    }
//
//    "update non-empty vatContact with startDate" in {
//      FrsStartDateView.viewModelFormat.update(validFrsStartDateView, Some(s4LTradingDetails)).frsStartDate shouldBe Some(validFrsStartDateView)
//    }
//
//  }
//
//}
