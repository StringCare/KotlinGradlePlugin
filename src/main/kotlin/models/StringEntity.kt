package models

class StringEntity(var name: String, var attributes: List<SAttribute>, var value: String) {

}
data class SAttribute(val name: String, val value: String)