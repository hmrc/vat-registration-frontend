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

import bars.model.response.BarsAssessmentType._

sealed trait BarsResponse

final case class VerifyResponse(barsVerifyResponse: BarsVerifyResponse) extends BarsResponse

final case class SortCodeOnDenyList(error: BarsError) extends BarsResponse


object VerifyResponse {

  import cats.syntax.eq._

  object verifySuccess {
    def unapply(response: VerifyResponse): Boolean = {
      val resp = response.barsVerifyResponse
      (resp.accountNumberIsWellFormatted == Yes || resp.accountNumberIsWellFormatted == Indeterminate) &&
      resp.sortCodeIsPresentOnEISCD == Yes &&
      (resp.accountExists == Yes || resp.accountExists == Indeterminate) &&
      (resp.nameMatches == Yes || resp.nameMatches == Partial || (resp.nameMatches == Indeterminate && resp.accountExists == Indeterminate)) &&
      resp.sortCodeSupportsDirectDebit == Yes
    }
  }

  object thirdPartyError {
    def unapply(response: VerifyResponse): Boolean = {
      val resp = response.barsVerifyResponse
      resp.accountExists == Error || resp.nameMatches == Error
    }
  }

  object nameMatchesNo {
    def unapply(response: VerifyResponse): Boolean = {
      val resp = response.barsVerifyResponse
      resp.nameMatches == No && (resp.accountExists == Yes || resp.accountExists == Indeterminate)

    }
  }

  object accountNumberIsWellFormattedNo {
    def unapply(response: VerifyResponse): Boolean =
      response.barsVerifyResponse.accountNumberIsWellFormatted == No
  }

  object sortCodeIsPresentOnEiscdNo {
    def unapply(response: VerifyResponse): Boolean =
      response.barsVerifyResponse.sortCodeIsPresentOnEISCD == No
  }

  object sortCodeSupportsDirectDebitNo {
    def unapply(response: VerifyResponse): Boolean =
      response.barsVerifyResponse.sortCodeSupportsDirectDebit == No
  }

  object accountDoesNotExist {
    def unapply(response: VerifyResponse): Boolean = {
      val resp = response.barsVerifyResponse

      (resp.accountNumberIsWellFormatted == Yes ||
        resp.accountNumberIsWellFormatted == Indeterminate) &&
      resp.accountExists == No &&
      resp.sortCodeIsPresentOnEISCD == Yes &&
      resp.sortCodeSupportsDirectDebit == Yes
    }
  }

}
