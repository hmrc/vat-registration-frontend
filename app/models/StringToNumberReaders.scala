/*
 * Copyright 2018 HM Revenue & Customs
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
package models

import play.api.libs.json.{JsPath, Reads}

object StringToNumberReaders {

    implicit class StringToNumberReaders(js: JsPath) extends JsPath {
      // Read T element or try to convert from String
      def readStringified[T](toT: String => T)(implicit r: Reads[T], rS: Reads[String]): Reads[T] =
        Reads.at[T](js)(r) orElse Reads.at[String](js)(rS).map(toT(_)) // There is certainly a way to use automatic conversion...
      // Read Int and fallback to String conversion
      def readStringifiedInt = readStringified[Int](_.toInt)
    }
}
