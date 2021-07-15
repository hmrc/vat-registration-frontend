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

package services

import connectors.{KeystoreConnector, PartnersConnector}
import models.PartnerEntity
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnersService @Inject()(val s4LService: S4LService,
                                val partnersConnector: PartnersConnector,
                                val keystoreConnector: KeystoreConnector
                               )(implicit ec: ExecutionContext) {

  def getAllPartners(regId: String)(implicit hc: HeaderCarrier): Future[List[PartnerEntity]] =
    partnersConnector.getAllPartners(regId)

  def getLeadPartner(regId: String)(implicit hc: HeaderCarrier): Future[Option[PartnerEntity]] =
    partnersConnector.getAllPartners(regId).map { partners =>
      partners.find(_.isLeadPartner)
    }

  def getPartner(regId: String, index: Int)(implicit hc: HeaderCarrier): Future[Option[PartnerEntity]] =
    partnersConnector.getPartner(regId, index)

  def upsertPartner(regId: String, index: Int, partner: PartnerEntity)(implicit hc: HeaderCarrier): Future[PartnerEntity] =
    partnersConnector.upsertPartner(regId, index, partner)

  def deletePartner(regId: String, index: Int)(implicit hc: HeaderCarrier): Future[Boolean] =
    partnersConnector.deletePartner(regId, index)
}