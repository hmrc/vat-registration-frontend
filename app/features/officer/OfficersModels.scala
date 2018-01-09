/*
 * Copyright 2018 HM Revenue & Customs
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

  case class VatLodgingOfficer(currentAddress: Option[ScrsAddress],
                               dob: Option[LocalDate],
                               nino: Option[String],
                               role: Option[String],
                               name: Option[Name],
                               changeOfName: Option[ChangeOfName],
                               currentOrPreviousAddress: Option[CurrentOrPreviousAddress],
                               contact: Option[OfficerContactDetails],
                               ivPassed: Option[Boolean] = None)

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

  case class Name(forename: Option[String],
                  otherForenames: Option[String],
                  surname: String,
                  title: Option[String] = None) {

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
    implicit val format: OFormat[Name] = (
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

  import models.api._
  import play.api.libs.json.{Json, OFormat}
  import features.officer.models.view._
  import models.view.vatLodgingOfficer.CompletionCapacityView

  final case class S4LVatLodgingOfficer
  (
    officerHomeAddress: Option[HomeAddressView] = None,
    officerSecurityQuestions: Option[SecurityQuestionsView] = None,
    completionCapacity: Option[CompletionCapacityView] = None,
    officerContactDetails: Option[ContactDetailsView] = None,
    formerName: Option[FormerNameView] = None,
    formerNameDate: Option[FormerNameDateView] = None,
    previousAddress: Option[PreviousAddressView] = None,
    ivPassed: Option[Boolean] = None
  )

  object S4LVatLodgingOfficer {
    implicit val format: OFormat[S4LVatLodgingOfficer] = Json.format[S4LVatLodgingOfficer]

    implicit val viewModelFormatCompletionCapacity = ViewModelFormat(
      readF = (group: S4LVatLodgingOfficer) => group.completionCapacity,
      updateF = (c: CompletionCapacityView, g: Option[S4LVatLodgingOfficer]) =>
        g.getOrElse(S4LVatLodgingOfficer()).copy(completionCapacity = Some(c))
    )

    // return a view model from a VatScheme instance
    implicit val modelTransformerCompletionCapacity = ApiModelTransformer[CompletionCapacityView] { vs: VatScheme =>
      vs.lodgingOfficer match{
        case Some(VatLodgingOfficer(_,_,_,Some(b),Some(a),_,_,_,_)) => Some(CompletionCapacityView(a.id, Some(CompletionCapacity(a, b))))
        case _ => None
      }
    }

    implicit val viewModelFormatSecuQuestions = ViewModelFormat(
      readF = (group: S4LVatLodgingOfficer) => group.officerSecurityQuestions,
      updateF = (c: SecurityQuestionsView, g: Option[S4LVatLodgingOfficer]) =>
        g.getOrElse(S4LVatLodgingOfficer()).copy(officerSecurityQuestions = Some(c))
    )

    // return a view model from a VatScheme instance
    implicit val modelTransformerSecuQuestions = ApiModelTransformer[SecurityQuestionsView] { vs: VatScheme =>
      vs.lodgingOfficer.collect {
        case VatLodgingOfficer(_,Some(a),Some(b),_,Some(c),_,_,_,_) => SecurityQuestionsView(a, b, Some(c))
      }
    }

    implicit val viewModelFormatFormerName = ViewModelFormat(
      readF = (group: S4LVatLodgingOfficer) => group.formerName,
      updateF = (c: FormerNameView, g: Option[S4LVatLodgingOfficer]) =>
        g.getOrElse(S4LVatLodgingOfficer()).copy(formerName = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformerFormerName = ApiModelTransformer[FormerNameView] { vs: VatScheme =>
      vs.lodgingOfficer match {
        case Some(VatLodgingOfficer(_,_,_,_,_,Some(a),_,_,_)) =>  Some(FormerNameView(a.nameHasChanged, a.formerName.map(_.formerName)))
        case _ => None
      }
    }

    implicit val viewModelFormatFormerNameDate = ViewModelFormat(
      readF = (group: S4LVatLodgingOfficer) => group.formerNameDate,
      updateF = (c: FormerNameDateView, g: Option[S4LVatLodgingOfficer]) =>
        g.getOrElse(S4LVatLodgingOfficer()).copy(formerNameDate = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformerFormerNameDate = ApiModelTransformer[FormerNameDateView] { vs: VatScheme =>
      vs.lodgingOfficer match{
        case Some(VatLodgingOfficer(_,_,_,_,_,Some(a),_,_,_)) =>
          a.formerName match {
            case Some(FormerName(_,Some(b))) => Some(FormerNameDateView(b))
            case _ => None
          }
        case _ => None
      }
    }

    implicit val viewModelFormatContactDetails = ViewModelFormat(
      readF = (group: S4LVatLodgingOfficer) => group.officerContactDetails,
      updateF = (c: ContactDetailsView, g: Option[S4LVatLodgingOfficer]) =>
        g.getOrElse(S4LVatLodgingOfficer()).copy(officerContactDetails = Some(c))
    )

    implicit val modelTransformerContactDetails = ApiModelTransformer[ContactDetailsView] { (vs: VatScheme) =>
      vs.lodgingOfficer.map(_.contact).collect {
        case Some(OfficerContactDetails(e, t, m)) =>
          ContactDetailsView(email = e, daytimePhone = t, mobile = m)
      }
    }

    implicit val viewModelFormatHomeAddress = ViewModelFormat(
      readF = (group: S4LVatLodgingOfficer) => group.officerHomeAddress,
      updateF = (c: HomeAddressView, g: Option[S4LVatLodgingOfficer]) =>
        g.getOrElse(S4LVatLodgingOfficer()).copy(officerHomeAddress = Some(c))
    )

    // return a view model from a VatScheme instance
    implicit val modelTransformerHomeAddress = ApiModelTransformer[HomeAddressView] { vs: VatScheme =>
      vs.lodgingOfficer.map(_.currentAddress).collect {
        case address => HomeAddressView(address.map(_.id).getOrElse(""), address)
      }
    }

    implicit val modelT = new S4LModelTransformer[S4LVatLodgingOfficer] {
      override def toS4LModel(vs: VatScheme): S4LVatLodgingOfficer =
        S4LVatLodgingOfficer(
          officerHomeAddress = ApiModelTransformer[HomeAddressView].toViewModel(vs),
          officerSecurityQuestions = ApiModelTransformer[SecurityQuestionsView].toViewModel(vs),
          completionCapacity = ApiModelTransformer[CompletionCapacityView].toViewModel(vs),
          officerContactDetails = ApiModelTransformer[ContactDetailsView].toViewModel(vs),
          formerName = ApiModelTransformer[FormerNameView].toViewModel(vs),
          formerNameDate = ApiModelTransformer[FormerNameDateView].toViewModel(vs),
          previousAddress = ApiModelTransformer[PreviousAddressView].toViewModel(vs),
          ivPassed = vs.lodgingOfficer.flatMap(_.ivPassed)
        )
    }

    implicit val viewModelFormatPreviousAddress = ViewModelFormat(
      readF = (group: S4LVatLodgingOfficer) => group.previousAddress,
      updateF = (c: PreviousAddressView, g: Option[S4LVatLodgingOfficer]) =>
        g.getOrElse(S4LVatLodgingOfficer()).copy(previousAddress = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformerPreviousAddress = ApiModelTransformer[PreviousAddressView] { vs: VatScheme =>
      vs.lodgingOfficer match {
        case Some(VatLodgingOfficer(_, _, _, _, _, _, Some(a), _, _)) => Some(PreviousAddressView(a.currentAddressThreeYears, a.previousAddress))
        case _ => None
      }

    }

    implicit val apiT = new S4LApiTransformer[S4LVatLodgingOfficer, VatLodgingOfficer] {
      override def toApi(c: S4LVatLodgingOfficer): VatLodgingOfficer =
        VatLodgingOfficer(
          currentAddress = c.officerHomeAddress.flatMap(_.address),
          dob = c.officerSecurityQuestions.map(d => d.dob),
          nino = c.officerSecurityQuestions.map(n => n.nino),
          role = c.completionCapacity.flatMap(_.completionCapacity.map(_.role)),
          name = c.completionCapacity.flatMap(_.completionCapacity.map(_.name)),
          changeOfName =
            c.formerName.map((fnv: FormerNameView) =>
              ChangeOfName(
                nameHasChanged = fnv.yesNo,
                formerName = fnv.formerName.map(fn =>
                    FormerName(formerName = fn, dateOfNameChange = c.formerNameDate.map(_.date))))),
          currentOrPreviousAddress = c.previousAddress.map(cpav =>
              CurrentOrPreviousAddress(currentAddressThreeYears = cpav.yesNo, previousAddress = cpav.address)),
          contact = c.officerContactDetails.map(ocd =>
              OfficerContactDetails(email = ocd.email, tel = ocd.daytimePhone, mobile = ocd.mobile)),
          ivPassed = c.ivPassed
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
