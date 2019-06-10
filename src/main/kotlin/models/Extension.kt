package models

import org.gradle.api.NamedDomainObjectContainer

data class Extension(
    val debug: Boolean = false,
    val main_module: String = "app",
    var modules: NamedDomainObjectContainer<Configuration>
)