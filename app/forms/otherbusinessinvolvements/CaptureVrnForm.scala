/*
 * Copyright 2022 HM Revenue & Customs
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

package forms.otherbusinessinvolvements

import forms.FormValidation._
import forms.constraints.VrnConstraints.isValidChecksum
import play.api.data.Form
import play.api.data.Forms.{single, text}
import uk.gov.hmrc.play.mappers.StopOnFirstFail

import scala.util.matching.Regex

object CaptureVrnForm {

  val captureVrnKey = "captureVrn"
  implicit val errorCode: ErrorCode = "obi.captureVrn"
  val regex: Regex = """^[0-9]{9}$""".r

  def apply(): Form[String] = Form(
    single(
      captureVrnKey -> text.transform(removeNewlineAndTrim, identity[String]).verifying(StopOnFirstFail(
        nonEmptyValidText(regex),
        isValidChecksum(s"validation.$errorCode.invalid")
      ))
    )
  )

}
