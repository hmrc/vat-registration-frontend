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

package services

import java.time.LocalDate
import javax.inject.Inject

import cats.data.OptionT
import com.google.inject.ImplementedBy
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[IncorporationInformationService])
trait IIService {

  def getCTActiveDate()(implicit headerCarrier: HeaderCarrier): OptionT[Future, LocalDate]

}

class IncorporationInformationService @Inject()() extends IIService {

  import cats.instances.future._
  import cats.syntax.applicative._


  def getCTActiveDate()(implicit headerCarrier: HeaderCarrier): OptionT[Future, LocalDate] =
    OptionT(Option(LocalDate.now()).pure)

}
