/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package utils

import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import javax.inject.{Inject, Singleton}

/** Repeating the pattern which was brought originally by play-framework and putting some more data which can be derived
  * from a request
  *
  * Use it to provide HeaderCarrier, Lang, or Messages
  */
@Singleton
class RequestSupport @Inject()(override val messagesApi: MessagesApi) extends I18nSupport {

  implicit def headerCarrier(implicit request: RequestHeader): HeaderCarrier =
    RequestSupport.headerCarrier

  def lang(implicit messages: Messages): Lang =
    messages.lang
}

object RequestSupport {

  implicit def headerCarrier(implicit request: RequestHeader): HeaderCarrier =
    HcProvider.headerCarrier(request)

  /** This is because we want to give responsibility of creation of [[HeaderCarrier]] to the platform code. If they
   * refactor how hc is created our code will pick it up automatically.
   */
  private object HcProvider extends FrontendHeaderCarrierProvider {
    def headerCarrier(request: RequestHeader): HeaderCarrier =
      hc(request)
  }
}
