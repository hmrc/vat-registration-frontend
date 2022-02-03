
package controllers.registration.transactor

import itutil.ControllerISpec
import models.TransactorDetails
import models.api.{Address, Country}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.test.Helpers._

class TransactorInternationalAddressControllerISpec extends ControllerISpec {

  val url = "/your-address/international"
  val testForeignCountry = Country(Some("NO"), Some("Norway"))
  val testShortForeignAddress = Address(testLine1, Some(testLine2), country = Some(testForeignCountry))
  val testForeignAddress = address.copy(country = Some(testForeignCountry))

  "GET /your-address/international" when {
    "reading from S4L" must {
      "return OK when the TransactorDetails block is empty" in new Setup {
        given
          .user.isAuthorised()
          .vatScheme.contains(emptyUkCompanyVatScheme)
          .s4lContainer[TransactorDetails].contains(TransactorDetails())

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get())

        res.status mustBe OK
      }
      "return OK and pre-populate when the TransactorDetails block contains an address" in new Setup {
        given
          .user.isAuthorised()
          .vatScheme.contains(emptyUkCompanyVatScheme)
          .s4lContainer[TransactorDetails].contains(TransactorDetails(address = Some(testShortForeignAddress)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get())

        res.status mustBe OK

        val doc = Jsoup.parse(res.body)
        doc.select("input[id=line1]").`val`() mustBe testLine1
        doc.select("input[id=line2]").`val`() mustBe testLine2
        doc.select("option[value=Norway]").hasAttr("selected") mustBe true
      }
    }

    "when reading from the backend" must {
      "return OK and pre-populate the page" in new Setup {
        val trDetails = TransactorDetails(address = Some(testForeignAddress))

        given
          .user.isAuthorised()
          .s4lContainer[TransactorDetails].isEmpty
          .registrationApi.getSection[TransactorDetails](Some(trDetails))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get())

        res.status mustBe OK

        val doc = Jsoup.parse(res.body)
        doc.select("input[id=line1]").`val`() mustBe testLine1
        doc.select("input[id=line2]").`val`() mustBe testLine2
        doc.select("option[value=Norway]").hasAttr("selected") mustBe true
      }
    }
  }

  "POST /your-address/international" must {
    "Store the address and redirect to the next page if a minimal address is provided" in new Setup {
      given
        .user.isAuthorised()
        .vatScheme.contains(emptyUkCompanyVatScheme)
        .vatScheme.doesNotExistForKey("transactor-details")
        .s4lContainer[TransactorDetails].contains(TransactorDetails())
        .s4lContainer[TransactorDetails].isUpdatedWith(TransactorDetails().copy(address = Some(testForeignAddress)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map(
        "line1" -> "testLine1",
        "country" -> "Norway"
      )))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.TelephoneNumberController.show.url)
    }
    "Store the address and redirect to the next page if a full address is provided" in new Setup {
      given
        .user.isAuthorised()
        .vatScheme.contains(emptyUkCompanyVatScheme)
        .vatScheme.doesNotExistForKey("transactor-details")
        .s4lContainer[TransactorDetails].contains(TransactorDetails())
        .s4lContainer[TransactorDetails].isUpdatedWith(TransactorDetails().copy(address = Some(testForeignAddress)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map(
        "line1" -> "testLine1",
        "line2" -> "testLine2",
        "line3" -> "testLine3",
        "line4" -> "testLine4",
        "line5" -> "testLine5",
        "postcode" -> "AB12 3YZ",
        "country" -> "Norway"
      )))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.TelephoneNumberController.show.url)
    }
    "return BAD_REQUEST if line 1 is missing" in new Setup {
      given
        .user.isAuthorised()
        .vatScheme.contains(emptyUkCompanyVatScheme)
        .vatScheme.doesNotExistForKey("transactor-details")
        .s4lContainer[TransactorDetails].contains(TransactorDetails())
        .s4lContainer[TransactorDetails].isUpdatedWith(TransactorDetails().copy(address = Some(testForeignAddress)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map(
        "line2" -> "testLine2",
        "line3" -> "testLine3",
        "line4" -> "testLine4",
        "line5" -> "testLine5",
        "postcode" -> "AB12 3YZ",
        "country" -> "Norway"
      )))

      res.status mustBe BAD_REQUEST
    }
    "return BAD_REQUEST if country is missing" in new Setup {
      given
        .user.isAuthorised()
        .vatScheme.contains(emptyUkCompanyVatScheme)
        .vatScheme.doesNotExistForKey("transactor-details")
        .s4lContainer[TransactorDetails].contains(TransactorDetails())
        .s4lContainer[TransactorDetails].isUpdatedWith(TransactorDetails().copy(address = Some(testForeignAddress)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map(
        "line1" -> "testLine1",
        "line2" -> "testLine2",
        "line3" -> "testLine3",
        "line4" -> "testLine4",
        "line5" -> "testLine5",
        "postcode" -> "AB12 3YZ",
      )))

      res.status mustBe BAD_REQUEST
    }
    "return BAD_REQUEST if country is UK and postcode is missing" in new Setup {
      given
        .user.isAuthorised()
        .vatScheme.contains(emptyUkCompanyVatScheme)
        .vatScheme.doesNotExistForKey("transactor-details")
        .s4lContainer[TransactorDetails].contains(TransactorDetails())
        .s4lContainer[TransactorDetails].isUpdatedWith(TransactorDetails().copy(address = Some(testForeignAddress)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map(
        "line1" -> "testLine1",
        "line2" -> "testLine2",
        "line3" -> "testLine3",
        "line4" -> "testLine4",
        "line5" -> "testLine5",
        "country" -> "United Kingdom"
      )))

      res.status mustBe BAD_REQUEST
    }
  }

}
