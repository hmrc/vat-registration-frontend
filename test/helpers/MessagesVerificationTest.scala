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

package helpers

import org.scalatestplus.play.PlaySpec

import scala.io.Source

class MessagesVerificationTest extends PlaySpec {

  "Messages file" should {

    def key(line: String): String = line.takeWhile(_ != '=')

    "not contain unescaped single quotes" in {
      for {
        line <- Source.fromFile("conf/messages").getLines()
        if line.matches("""^.+[=].*[^']'[^'].*$""")
      } fail(s"Found an unescaped single quote in messages file under key: ${key(line)}")
    }

    "not contain attribute values unenclosed in double quotes" in {
      for {
        line <- Source.fromFile("conf/messages").getLines()
        if line.matches("""^.+=.+=\s*[{].*$""")
      } fail(s"Found an open brace not preceded by double quotes, but preceded by an equals sign: ${key(line)}")
    }
  }
}
