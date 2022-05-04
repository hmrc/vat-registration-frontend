
package controllers.business

import itutil.ControllerISpec
import models.BusinessContact
import models.api.{Address, Country}
import org.jsoup.Jsoup
import org.scalatest.Assertion
import play.api.http.HeaderNames
import play.api.test.Helpers._

class InternationalPpobAddressControllerISpec extends ControllerISpec {

  val url = "/principal-place-business/international"
  val testForeignCountry = Country(Some("NO"), Some("Norway"))
  val testShortForeignAddress = Address(testLine1, Some(testLine2), country = Some(testForeignCountry))
  val testForeignAddress = address.copy(country = Some(testForeignCountry))

  "GET /principal-place-business/international" when {
    "reading from S4L" must {
      "return OK when the ApplicantDetails block is empty" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getSection[BusinessContact](None, testRegId)
          .s4lContainer[BusinessContact].contains(BusinessContact())

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get())

        res.status mustBe OK
      }
      "return OK and pre-populate when the ApplicantDetails block contains an address" in new Setup {
        given
          .user.isAuthorised()
          .s4lContainer[BusinessContact].contains(BusinessContact(ppobAddress = Some(testForeignAddress)))
          .registrationApi.getSection[BusinessContact](None, testRegId)

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
        val businessContact = BusinessContact(ppobAddress = Some(testForeignAddress))
        given
          .user.isAuthorised()
          .s4lContainer[BusinessContact].isEmpty
          .registrationApi.getSection[BusinessContact](Some(businessContact))(BusinessContact.apiKey, BusinessContact.apiFormat)

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

  "POST /principal-place-business/international" must {
    "Store the address and redirect to the previous address page if a minimal address is provided" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[BusinessContact](None, testRegId)
        .s4lContainer[BusinessContact].isEmpty
        .s4lContainer[BusinessContact].isUpdatedWith(BusinessContact(ppobAddress = Some(testShortForeignAddress)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map(
        "line1" -> "testLine1",
        "country" -> "Norway"
      )))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessContactDetailsController.show.url)
    }
    "Store the address and redirect to the previous address page if a full address is provided" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[BusinessContact](None, testRegId)
        .s4lContainer[BusinessContact].contains(BusinessContact())
        .s4lContainer[BusinessContact].isUpdatedWith(BusinessContact(ppobAddress = Some(testForeignAddress)))

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
      res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessContactDetailsController.show.url)
    }
    "return BAD_REQUEST if line 1 is missing" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[BusinessContact](None, testRegId)
        .s4lContainer[BusinessContact].contains(BusinessContact())
        .s4lContainer[BusinessContact].isUpdatedWith(BusinessContact(ppobAddress = Some(testForeignAddress)))

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
        .registrationApi.getSection[BusinessContact](None, testRegId)
        .s4lContainer[BusinessContact].contains(BusinessContact())
        .s4lContainer[BusinessContact].isUpdatedWith(BusinessContact(ppobAddress = Some(testForeignAddress)))

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
    "return BAD_REQUEST if country is UK" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[BusinessContact](None, testRegId)
        .s4lContainer[BusinessContact].contains(BusinessContact())

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map(
        "line1" -> "testLine1",
        "line2" -> "testLine2",
        "line3" -> "testLine3",
        "line4" -> "testLine4",
        "line5" -> "testLine5",
        "postcode" -> "AB12 3YZ",
        "country" -> "United Kingdom"
      )))

      res.status mustBe BAD_REQUEST
    }

    "return BAD_REQUEST if postcode missing and country requires postcode" in new Setup {

      def assertMissingPostcode(country: String): Assertion = {
        given
          .user.isAuthorised()
          .vatScheme.contains(emptyVatSchemeNetp)
          .registrationApi.getSection[BusinessContact](None, testRegId)
          .s4lContainer[BusinessContact].contains(BusinessContact())

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).post(Map(
          "line1" -> "testLine1",
          "line2" -> "testLine2",
          "line3" -> "testLine3",
          "line4" -> "testLine4",
          "line5" -> "testLine5",
          "country" -> country
        )))

        res.status mustBe BAD_REQUEST
      }

      List("Isle of Man", "Guernsey", "Jersey").foreach(assertMissingPostcode)
    }
  }

}
