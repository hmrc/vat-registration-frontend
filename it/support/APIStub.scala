
package support

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsValue, Json}

class APIStub(val url: String)

trait CanGet extends HttpStubMethod {
  object GET {
    implicit val httpAction: MappingBuilder = get(urlMatching(url))
    def respondsWith(status: Int, responseBody: Option[JsValue] = None): StubMapping =
      stubFor(apiResponse(status, responseBody.map(Json.stringify)))
  }
}

trait CanPost extends HttpStubMethod {
  object POST {
    implicit val httpAction: MappingBuilder = post(urlMatching(url))

    def respondsWith(status: Int, responseBody: Option[JsValue] = None): StubMapping =
      stubFor(apiResponse(status, responseBody.map(Json.stringify)))
  }
}

trait CanPut extends HttpStubMethod {
  object PUT {
    implicit val httpAction: MappingBuilder = put(urlMatching(url))

    def putRespondsWith(status: Int, responseBody: Option[JsValue] = None): StubMapping =
      stubFor(apiResponse(status, responseBody.map(Json.stringify)))
  }
}

trait CanPatch extends HttpStubMethod {
  object PATCH {
    implicit val httpAction: MappingBuilder = patch(urlMatching(url))

    def patchRespondsWith(status: Int, responseBody: Option[JsValue] = None): StubMapping =
      stubFor(apiResponse(status, responseBody.map(Json.stringify)))
  }
}

trait CanDelete extends HttpStubMethod {
  object DELETE {
    implicit val httpAction: MappingBuilder = delete(urlMatching(url))

    def deleteRespondsWith(status: Int, responseBody: Option[JsValue] = None): StubMapping =
      stubFor(apiResponse(status, responseBody.map(Json.stringify)))
  }
}

trait HttpStubMethod {
  def url: String
  private val defaultResponse = ""

  def apiResponse(status: Int, response: Option[String])(implicit httpAction: MappingBuilder): MappingBuilder =
    httpAction.willReturn(aResponse()
      .withBody(response.getOrElse(defaultResponse))
      .withStatus(status)
    )

}
