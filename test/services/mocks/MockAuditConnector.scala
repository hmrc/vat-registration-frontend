/*
 * Copyright 2026 HM Revenue & Customs
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

package services.mocks

import org.mockito.Mockito.{reset, times, verify}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.{ExecutionContext, Future}

trait MockAuditConnector extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockAuditConnector: AuditConnector = mock[AuditConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuditConnector)
  }

  val auditEventCaptor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])

  def verifyAuditEvent: Future[AuditResult] =
    verify(mockAuditConnector, times(1)).sendEvent(
      auditEventCaptor.capture()
    )(ArgumentMatchers.any[HeaderCarrier],
      ArgumentMatchers.any[ExecutionContext]
    )

}
