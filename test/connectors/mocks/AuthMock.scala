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

package connectors.mocks

import config.AuthClientConnector
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment, Enrolments, InsufficientConfidenceLevel, InvalidBearerToken}

import scala.concurrent.Future

trait AuthMock {
  this: MockitoSugar =>

  lazy val mockAuthClientConnector: AuthClientConnector = mock[AuthClientConnector]
  def agentEnrolment(arn: String) = Enrolment("HMRC-AS-AGENT").withIdentifier("AgentReferenceNumber", arn)

  def mockAuthenticatedBasic: OngoingStubbing[Future[Unit]] =
    when(mockAuthClientConnector.authorise[Unit](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful({}))

  def mockAuthenticated(arn: Option[String] = None): OngoingStubbing[Future[Enrolments]] = {
    val enrolments = arn.map(ref => Set(agentEnrolment(ref))).getOrElse(Set())
    when(
      mockAuthClientConnector.authorise(ArgumentMatchers.any(), ArgumentMatchers.eq(Retrievals.allEnrolments))(ArgumentMatchers.any(), ArgumentMatchers.any())
    ).thenReturn(Future.successful(Enrolments(enrolments)))
  }

  def mockNotAuthenticated(): OngoingStubbing[Future[Unit]] = {
    when(mockAuthClientConnector.authorise[Unit](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.failed(new InsufficientConfidenceLevel))
  }

  def mockNoActiveSession(): OngoingStubbing[Future[Unit]] = {
    when(mockAuthClientConnector.authorise[Unit](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.failed(new InvalidBearerToken))
  }

  def mockAuthenticatedOrg(): OngoingStubbing[Future[Option[AffinityGroup]]] = {
    when(
      mockAuthClientConnector.authorise[Option[AffinityGroup]](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())
    ) thenReturn Future.successful(Some(Organisation))
  }

  def mockAuthenticatedAgent(): OngoingStubbing[Future[Option[AffinityGroup]]] = {
    when(
      mockAuthClientConnector.authorise[Option[AffinityGroup]](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())
    ) thenReturn Future.successful(Some(Agent))
  }

  def mockAuthenticatedIndividual(): OngoingStubbing[Future[Option[AffinityGroup]]] = {
    when(
      mockAuthClientConnector.authorise[Option[AffinityGroup]](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())
    ) thenReturn Future.successful(Some(Individual))
  }

  def mockAuthenticatedInternalId(internalId: Option[String]): OngoingStubbing[Future[Option[String]]] = {
    when(
      mockAuthClientConnector.authorise[Option[String]](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())
    ) thenReturn Future.successful(internalId)
  }
}
