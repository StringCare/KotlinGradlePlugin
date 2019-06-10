package models

class Configuration(var name: String?) {
    val stringFiles = mutableListOf<String>()
    val srcFolders = mutableListOf<String>()
}