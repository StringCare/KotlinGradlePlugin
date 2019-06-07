package models

data class StringEntity(val name: String, val attributes: List<SAttribute>, val value: String)
data class SAttribute(val name: String, val value: String)