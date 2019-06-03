import models.Configuration
import models.Extension
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import utils.absolutePath
import utils.createConfiguration
import utils.createExtension

class StringCare : Plugin<Project> {

    private lateinit var absoluteProjectPath: String
    private lateinit var project: Project
    private lateinit var extension: Extension
    private lateinit var configuration: NamedDomainObjectContainer<Configuration>

    override fun apply(target: Project) {
        this@StringCare.project = target
        defineExtension()
    }

    private fun defineExtension() {
        extension = project.createExtension()
        configuration = project.createConfiguration()
        absoluteProjectPath = project.absolutePath()
    }

}