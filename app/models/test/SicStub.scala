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

package models.test

import play.api.libs.json.Json

case class SicStub(selection: SicStubSelection,
                   sicCode1: Option[String] = None,
                   sicCode2: Option[String] = None,
                   sicCode3: Option[String] = None,
                   sicCode4: Option[String] = None) {

  def fullSicCodes: List[String] = (sicCode1 ++ sicCode2 ++ sicCode3 ++ sicCode4).toList

}

object SicStub {
  implicit val format = Json.format[SicStub]
}
