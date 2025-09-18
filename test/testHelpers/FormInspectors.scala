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

package testHelpers

import org.scalatest.Inspectors
import org.scalatest.compatible.Assertion
import org.scalatest.matchers.should.Matchers
import play.api.data.{Form, FormError}

object FormInspectors extends Matchers with Inspectors {

  val toErrorSeq: FormError => (String, String) = (fe: FormError) => (fe.key, fe.message)

  implicit class FormErrorOps[T](form: Form[T]) {

    def shouldHaveErrors(es: Seq[(String, String)]): Assertion = {
      form.errors shouldBe Symbol("nonEmpty")
      form.errors.size shouldBe es.size
      form.errors.map(toErrorSeq) shouldBe es
    }

    def shouldHaveGlobalErrors(es: String*): Assertion = {
      form.globalErrors shouldBe Symbol("nonEmpty")
      form.globalErrors.size shouldBe es.size
      form.globalErrors.map(toErrorSeq).map(_._2) should contain only (es: _*)
    }

    def shouldContainValue(value: T): Assertion = {
      form.errors shouldBe Symbol("empty")
      form.value shouldBe Some(value)
    }

  }

}
