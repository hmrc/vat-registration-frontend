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
import java.time.format.DateTimeFormatter
import javax.inject.Inject

import cats.data.OptionT
import com.google.inject.ImplementedBy
import connectors.CTConnector
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[IncorporationInformationService])
trait IIService {

  def getCTActiveDate()(implicit headerCarrier: HeaderCarrier): OptionT[Future, LocalDate]

}

class IncorporationInformationService @Inject()(ctConnector: CTConnector) extends IIService with CommonService {

  import cats.instances.future._

  val formatter = DateTimeFormatter.ofPattern("YYYY-mm-dd")

  def getCTActiveDate()(implicit headerCarrier: HeaderCarrier): OptionT[Future, LocalDate] =
    OptionT.liftF(fetchRegistrationId.flatMap(ctConnector.getRegistration)).flatMap {
      ctr => OptionT.fromOption(ctr.accountingDetails.flatMap(_.activeDate.map(LocalDate.parse(_, formatter))))
    }

  //    OptionT.pure(LocalDate.of(2017, 8, 4)) // is filtered out, no option is shown
  //      OptionT.pure(LocalDate.now()) // will be shown as radio button
  //    OptionT.none // we couldn't get the CT active date, no radio button will be shown

}
