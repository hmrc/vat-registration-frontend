/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package utils

import play.api.libs.json.Reads
import uk.gov.hmrc.http.HttpResponse

import scala.util.{Failure, Success, Try}

object HttpResponseUtils {
  implicit class HttpResponseOps(private val response: HttpResponse) extends AnyVal {

    def parseJSON[A](implicit reads: Reads[A]): Either[String, A] =
      Try(response.json) match {
        case Success(jsValue) ⇒
          jsValue
            .validate[A]
            .fold[Either[String, A]](
              _ ⇒
                // there was JSON in the response but we couldn't read it
                Left("Could not parse http response JSON"),
              Right(_)
            )

        case Failure(_) ⇒
          // response.json failed in this case - there was no JSON in the response
          Left(s"Could not read http response as JSON")
      }

  }

}