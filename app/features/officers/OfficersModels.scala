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

package models.api {

  import java.time.LocalDate

  import cats.Show
  import cats.Show.show
  import models.Formatters
  import org.apache.commons.lang3.text.WordUtils
  import play.api.libs.functional.syntax._
  import play.api.libs.json.{Json, OFormat, __}

  case class FormerName(formerName: String, dateOfNameChange: Option[LocalDate] = None)

  object FormerName {
    implicit val format: OFormat[FormerName] = Json.format[FormerName]
  }

  case class ChangeOfName(nameHasChanged: Boolean, formerName: Option[FormerName] = None)

  object ChangeOfName {
    implicit val format: OFormat[ChangeOfName] = Json.format[ChangeOfName]
  }

  case class VatLodgingOfficer(currentAddress: ScrsAddress,
                               dob: DateOfBirth,
                               nino: String,
                               role: String,
                               name: Name,
                               changeOfName: ChangeOfName,
                               currentOrPreviousAddress: CurrentOrPreviousAddress,
                               contact: OfficerContactDetails)

  object VatLodgingOfficer {
    implicit val format: OFormat[VatLodgingOfficer] = Json.format[VatLodgingOfficer]
  }


  case class CurrentOrPreviousAddress(currentAddressThreeYears: Boolean,
                                      previousAddress: Option[ScrsAddress] = None)

  object CurrentOrPreviousAddress {
    implicit val format: OFormat[CurrentOrPreviousAddress] = Json.format[CurrentOrPreviousAddress]
  }

  //TODO: Explore Bringing in DOB from DOB.scala without duplication
  case class DateOfBirth(day: Int, month: Int, year: Int)

  object DateOfBirth {
    import models.StringToNumberReaders._

    implicit def toLocalDate(dob: DateOfBirth): LocalDate = LocalDate.of(dob.year, dob.month, dob.day)

    def apply(localDate: LocalDate): DateOfBirth = DateOfBirth(
      day = localDate.getDayOfMonth,
      month = localDate.getMonthValue,
      year = localDate.getYear
    )

    implicit val formatter = (
      (__ \ "day").format(__.readStringifiedInt) and
        (__ \ "month").format(__.readStringifiedInt) and
        (__ \ "year").format(__.readStringifiedInt)
      ) (DateOfBirth.apply, unlift(DateOfBirth.unapply))
  }

  case class OfficerContactDetails(email: Option[String], tel: Option[String], mobile: Option[String])

  object OfficerContactDetails {

    implicit val format: OFormat[OfficerContactDetails] = (
      (__ \ "email").formatNullable[String] and
        (__ \ "tel").formatNullable[String] and
        (__ \ "mobile").formatNullable[String]
      ) (OfficerContactDetails.apply, unlift(OfficerContactDetails.unapply))

  }

  case class CompletionCapacity(name: Name, role: String)

  object CompletionCapacity {
    implicit val format = Json.format[CompletionCapacity]
  }

  case class Name(
                   forename: Option[String],
                   otherForenames: Option[String],
                   surname: String,
                   title: Option[String] = None
                 ){

    import cats.instances.option._
    import cats.syntax.applicative._
    import models.api.Name.inlineShow.inline

    val id: String = List(forename,
      surname.pure,
      otherForenames,
      title
    ).flatten.mkString.replaceAll(" ", "")

    val asLabel: String = inline show this

  }

  object Name {
    implicit val format = (
      (__ \ "forename").formatNullable[String] and
        (__ \ "other_forenames").formatNullable[String] and
        (__ \ "surname").format[String] and
        (__ \ "title").formatNullable[String]
      ) (Name.apply, unlift(Name.unapply))

    val normalizeNameReads = (
      (__ \ "forename").readNullable[String](Formatters.normalizeReads) and
        (__ \ "other_forenames").readNullable[String](Formatters.normalizeReads) and
        (__ \ "surname").read[String](Formatters.normalizeReads) and
        (__ \ "title").readNullable[String](Formatters.normalizeReads)
      ) (Name.apply _)


    private def normalisedSeq(name: Name): Seq[String] = {
      import cats.instances.option._
      import cats.syntax.applicative._

      Seq[Option[String]](
        name.title,
        name.forename,
        name.otherForenames,
        name.surname.pure
      ) flatMap (_ map WordUtils.capitalizeFully)

    }

    object htmlShow {
      implicit val html: Show[Name] = show((name: Name) => normalisedSeq(name).mkString("<br/>"))
    }

    object inlineShow {
      implicit val inline = show((name: Name) => normalisedSeq(name).mkString(" "))
    }


  }
}

package models {

  import common.ErrorUtil.fail
  import models.api._
  import models.view.vatLodgingOfficer._
  import play.api.libs.json.{Json, OFormat}

  final case class S4LVatLodgingOfficer
  (
    officerHomeAddress: Option[OfficerHomeAddressView] = None,
    officerSecurityQuestions: Option[OfficerSecurityQuestionsView] = None,
    completionCapacity: Option[CompletionCapacityView] = None,
    officerContactDetails: Option[OfficerContactDetailsView] = None,
    formerName: Option[FormerNameView] = None,
    formerNameDate: Option[FormerNameDateView] = None,
    previousAddress: Option[PreviousAddressView] = None
  )

  object S4LVatLodgingOfficer {
    implicit val format: OFormat[S4LVatLodgingOfficer] = Json.format[S4LVatLodgingOfficer]


    implicit val modelT = new S4LModelTransformer[S4LVatLodgingOfficer] {
      override def toS4LModel(vs: VatScheme): S4LVatLodgingOfficer =
        S4LVatLodgingOfficer(
          officerHomeAddress = ApiModelTransformer[OfficerHomeAddressView].toViewModel(vs),
          officerSecurityQuestions = ApiModelTransformer[OfficerSecurityQuestionsView].toViewModel(vs),
          completionCapacity = ApiModelTransformer[CompletionCapacityView].toViewModel(vs),
          officerContactDetails = ApiModelTransformer[OfficerContactDetailsView].toViewModel(vs),
          formerName = ApiModelTransformer[FormerNameView].toViewModel(vs),
          formerNameDate = ApiModelTransformer[FormerNameDateView].toViewModel(vs),
          previousAddress = ApiModelTransformer[PreviousAddressView].toViewModel(vs)
        )
    }

    def error = throw fail("VatLodgingOfficer")

    implicit val apiT = new S4LApiTransformer[S4LVatLodgingOfficer, VatLodgingOfficer] {
      override def toApi(c: S4LVatLodgingOfficer): VatLodgingOfficer =
        VatLodgingOfficer(
          currentAddress = c.officerHomeAddress.flatMap(_.address).getOrElse(error),
          dob = c.officerSecurityQuestions.map(d => DateOfBirth(d.dob)).getOrElse(error),
          //$COVERAGE-OFF$
          nino = c.officerSecurityQuestions.map(n => n.nino).getOrElse(error),
          //$COVERAGE-ON$
          role = c.completionCapacity.flatMap(_.completionCapacity.map(_.role)).getOrElse(error),
          //$COVERAGE-OFF$
          name = c.completionCapacity.flatMap(_.completionCapacity.map(_.name)).getOrElse(error),
          //$COVERAGE-ON$

          changeOfName = c.formerName.map((fnv: FormerNameView) =>
            ChangeOfName(nameHasChanged = fnv.yesNo,
              formerName = fnv.formerName.map(fn =>
                FormerName(formerName = fn,
                  dateOfNameChange = c.formerNameDate.map(_.date))))).getOrElse(error),

          currentOrPreviousAddress = c.previousAddress.map(cpav =>
            CurrentOrPreviousAddress(currentAddressThreeYears = cpav.yesNo,
              previousAddress = cpav.address)).getOrElse(error),

          contact = c.officerContactDetails.map(ocd =>
            OfficerContactDetails(email = ocd.email, tel = ocd.daytimePhone, mobile = ocd.mobile)).getOrElse(error)
        )
    }
  }
}

package models.external {

  import models.api.{DateOfBirth, Name}
  import org.joda.time.DateTime
  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  case class Officer(
                      name: Name,
                      role: String,
                      dateOfBirth: Option[DateOfBirth] = None,
                      resignedOn: Option[DateTime] = None,
                      appointmentLink: Option[String] = None // custom read to pick up (if required - TBC)
                    ) {

    override def equals(obj: Any): Boolean = obj match {
      case Officer(nameObj, roleObj, _, _, _)
        if role.equalsIgnoreCase(roleObj) && (nameObj == name) => true
      case _ => false
    }

    override def hashCode: Int = 1 // bit of a hack, but works
  }

  object Officer {

    implicit val rd: Reads[Officer] = (
      (__ \ "name_elements").read[Name](Name.normalizeNameReads) and
        (__ \ "officer_role").read[String] and
        (__ \ "date_of_birth").readNullable[DateOfBirth] and
        (__ \ "resigned_on").readNullable[DateTime] and
        (__ \ "appointment_link").readNullable[String]
      ) (Officer.apply _)

    implicit val wt: Writes[Officer] = (
      (__ \ "name_elements").write[Name] and
        (__ \ "officer_role").write[String] and
        (__ \ "date_of_birth").writeNullable[DateOfBirth] and
        (__ \ "resigned_on").writeNullable[DateTime] and
        (__ \ "appointment_link").writeNullable[String]
      ) (unlift(Officer.unapply))

    private val emptyName = Name(None, None, "", None)
    val empty = Officer(emptyName, "", None, None, None)

  }

  case class OfficerList(items: Seq[Officer])

  object OfficerList {
    implicit val reads: Reads[OfficerList] = (__ \ "officers").read[Seq[Officer]] map OfficerList.apply
  }
}
