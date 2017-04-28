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

package models.api

sealed abstract class EligibilityQuestion(val name: String) extends Product with Serializable

object EligibilityQuestion {

  case object HaveNinoQuestion extends EligibilityQuestion("haveNino")

  case object DoingBusinessAbroadQuestion extends EligibilityQuestion("doingBusinessAbroad")

  case object DoAnyApplyToYouQuestion extends EligibilityQuestion("doAnyApplyToYou")

  case object ApplyingForAnyOfQuestion extends EligibilityQuestion("applyingForAnyOf")

  case object CompanyWillDoAnyOfQuestion extends EligibilityQuestion("companyWillDoAnyOf")

  private val questions = Seq(HaveNinoQuestion,
    DoingBusinessAbroadQuestion,
    DoAnyApplyToYouQuestion,
    ApplyingForAnyOfQuestion,
    CompanyWillDoAnyOfQuestion)

  def apply(s: String): EligibilityQuestion = questions.find(_.name == s)
    .getOrElse(throw new IllegalArgumentException("unexpected question identifier!"))

}
