/*
 * Copyright 2024 HM Revenue & Customs
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

package forms

import models.api.vatapplication.{StoringOverseas, StoringWithinUk}
import testHelpers.VatRegSpec

class StoringGoodsFormSpec extends VatRegSpec {

  val form = app.injector.instanceOf[StoringGoodsForm].form

  val fieldName = "value"
  val uk = "UK"
  val overseas = "OVERSEAS"

  "the StoringGoods form" should {
    "bind the uk answer successfully" in {
      val boundForm = form.bind(Map(fieldName -> uk))
      boundForm.errors.isEmpty mustBe true
      boundForm.value mustBe Some(StoringWithinUk)
    }
    "bind the overseas answer successfully" in {
      val boundForm = form.bind(Map(fieldName -> overseas))
      boundForm.errors.isEmpty mustBe true
      boundForm.value mustBe Some(StoringOverseas)
    }
    "return an error if nothing is selected" in {
      val boundForm = form.bind(Map.empty[String, String])
      boundForm.errors.size mustBe 1
      boundForm.errors.head.message mustBe "pages.netp.warehouseLocation.error"
    }
  }

}
