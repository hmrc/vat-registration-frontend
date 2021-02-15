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

package utils

import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar

trait MockBase64Util extends MockitoSugar {
  self: Suite =>

  val mockBase64Util = mock[Base64Util]

  def mockEncodeBase64(inStr: String)(response: String): OngoingStubbing[String] =
    when(mockBase64Util.encodeString(inStr)) thenReturn response

  def mockDecodeBase64(inStr: String)(response: String): OngoingStubbing[String] =
    when(mockBase64Util.decodeString(inStr)) thenReturn response

}
