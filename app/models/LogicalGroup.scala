package models

import play.api.libs.json.OFormat

trait LogicalGroup {

  type Type

  val key: String

  val gFormat: OFormat[Type]

}


object LogicalGroup {

  val vatLodgingOfficer: LogicalGroup = new LogicalGroup {
    override type Type = S4LVatLodgingOfficer
    override val key: String = "VatLodgingOfficer"
    override val gFormat: OFormat[S4LVatLodgingOfficer] = S4LVatLodgingOfficer.format
  }

}
