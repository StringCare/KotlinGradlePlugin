package models

class StringEntity(
    var name: String,
    var attributes: List<SAttribute>,
    var value: String,
    val tag: String,
    val index: Int
)

data class SAttribute(val name: String, val value: String)