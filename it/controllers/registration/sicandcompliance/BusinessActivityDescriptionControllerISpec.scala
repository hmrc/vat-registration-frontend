
package controllers.registration.sicandcompliance

import helpers.RequestsFinder
import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.{BusinessActivityDescription, SicAndCompliance}
import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures
import play.api.http.HeaderNames
import play.api.test.Helpers._
import play.api.libs.json.{JsValue, Json}
import repositories.SessionRepository
import support.AppAndStubs
import uk.gov.hmrc.http.cache.client.CacheMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import controllers.registration.sicandcompliance.{routes => sicRoutes}

class BusinessActivityDescriptionControllerISpec extends IntegrationSpecBase
  with AppAndStubs
  with ScalaFutures
  with RequestsFinder
  with ITRegistrationFixtures {

  class Setup {
    def customAwait[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)

    val repo = app.injector.instanceOf[SessionRepository]
    val defaultTimeout: FiniteDuration = 5 seconds

    customAwait(repo.ensureIndexes)(defaultTimeout)
    customAwait(repo.drop)(defaultTimeout)

    def insertCurrentProfileIntoDb(currentProfile: models.CurrentProfile, sessionId: String): Boolean = {
      val preawait = customAwait(repo.count)(defaultTimeout)
      val currentProfileMapping: Map[String, JsValue] = Map("CurrentProfile" -> Json.toJson(currentProfile))
      val res = customAwait(repo.upsert(CacheMap(sessionId, currentProfileMapping)))(defaultTimeout)
      customAwait(repo.count)(defaultTimeout) mustBe preawait + 1
      res
    }

  }

  "GET /what-company-does" must {
    "return OK" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[SicAndCompliance].contains(sicAndCompliance)
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(sicRoutes.BusinessActivityDescriptionController.show.url).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  "POST /what-company-does" must {
    "redirect to ICL on submit" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[SicAndCompliance].contains(sicAndCompliance)
        .vatScheme.isUpdatedWith[SicAndCompliance](sicAndCompliance.copy(description = Some(BusinessActivityDescription("foo"))))
        .s4lContainer[SicAndCompliance].cleared
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(sicRoutes.BusinessActivityDescriptionController.submit().url)
        .post(Map("description" -> Seq("foo")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SicAndComplianceController.submitSicHalt().url)
      }
    }
  }

}
