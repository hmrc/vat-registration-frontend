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

package forms

import models.{Email, Letter}
import play.api.data.FormError
import testHelpers.VatRegSpec

class ContactPreferenceFormSpec extends VatRegSpec {
  val form = ContactPreferenceForm()

  "contactPreferenceForm" must {

    val contactPreference = "value"

    val contactPreferenceErrorKey = "contactPreference.error.required"


    "successfully parse a Email entity" in {
      val res = form.bind(Map(contactPreference -> "email"))
      res.value must contain(Email)
    }

    "successfully parse a Letter entity" in {
      val res = form.bind(Map(contactPreference -> "letter"))
      res.value must contain(Letter)
    }

    "fail when nothing has been entered in the view" in {
      val res = form.bind(Map.empty[String, String])
      res.errors must contain(FormError(contactPreference, contactPreferenceErrorKey))
    }

    "fail when it is not an expected value in the view" in {
      val res = form.bind(Map(contactPreference -> "invalid"))
      res.errors must contain(FormError(contactPreference, contactPreferenceErrorKey))
    }
  }

}
