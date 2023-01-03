/*
 * Copyright 2023 HM Revenue & Customs
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

import models.{English, Welsh}
import play.api.data.FormError
import testHelpers.VatRegSpec

class VatCorrespondenceFormSpec extends VatRegSpec {
  val form = VatCorrespondenceForm()

  "vatCorrespondenceForm" must {

    val vatCorrespondence = "value"

    val vatCorrespondenceErrorKey = "vatCorrespondence.error.required"


    "successfully parse a English entity" in {
      val res = form.bind(Map(vatCorrespondence -> "english"))
      res.value must contain(English)
    }

    "successfully parse a Welsh entity" in {
      val res = form.bind(Map(vatCorrespondence -> "welsh"))
      res.value must contain(Welsh)
    }

    "fail when nothing has been entered in the view" in {
      val res = form.bind(Map.empty[String, String])
      res.errors must contain(FormError(vatCorrespondence, vatCorrespondenceErrorKey))
    }

    "fail when it is not an expected value in the view" in {
      val res = form.bind(Map(vatCorrespondence -> "invalid"))
      res.errors must contain(FormError(vatCorrespondence, vatCorrespondenceErrorKey))
    }
  }

}
