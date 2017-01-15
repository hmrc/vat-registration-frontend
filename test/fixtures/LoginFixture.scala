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

package fixtures

import java.net.URLEncoder

trait LoginFixture {
  val signInUri: String = """http://localhost:9025/gg/sign-in"""
  val continueUrlHtmlEncoded: String = URLEncoder.encode(s"http://localhost:9895/vat-registration/post-sign-in", "UTF-8")
  lazy val authUrl = s"$signInUri?accountType=organisation&continue=$continueUrlHtmlEncoded&origin=vat-registration-frontend"
}
