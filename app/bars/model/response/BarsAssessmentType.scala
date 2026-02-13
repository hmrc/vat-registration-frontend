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

package bars.model.response

import enumeratum._
import enumeratum.PlayInsensitiveJsonEnum

sealed trait BarsAssessmentType extends EnumEntry with EnumEntry.Uncapitalised

object BarsAssessmentType
  extends Enum[BarsAssessmentType] with PlayInsensitiveJsonEnum[BarsAssessmentType] {

  val values = findValues

  case object Yes extends BarsAssessmentType

  case object No extends BarsAssessmentType

  case object Error extends BarsAssessmentType

  case object Indeterminate extends BarsAssessmentType

  case object Inapplicable extends BarsAssessmentType

  case object Partial extends BarsAssessmentType
}
