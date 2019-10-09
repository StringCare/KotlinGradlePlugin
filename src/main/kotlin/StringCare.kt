import components.*
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.plugins.DslObject

open class StringCare : Plugin<Project> {

    companion object {
        @JvmStatic
        internal lateinit var absoluteProjectPath: String

        private var internalTempDir: String? = null
        @JvmStatic
        var tempFolder: String
            get() = internalTempDir ?: tempPath()
            set(value) {
                internalTempDir = value
            }

        fun resetFolder() {
            internalTempDir = null
        }

        @JvmStatic
        internal val moduleMap: MutableMap<String, Configuration> = mutableMapOf()

        @JvmStatic
        internal val variantMap: MutableMap<String, VariantApplicationId> = mutableMapOf()

        @JvmStatic
        internal var mainModule: String = defaultMainModule

        @JvmStatic
        internal var debug: Boolean = defaultDebug
    }

    private lateinit var project: Project
    private lateinit var extension: Extension

    override fun apply(target: Project) {
        this@StringCare.project = target

        extension = project.createExtension()
        absoluteProjectPath = project.absolutePath()

        this.project.afterEvaluate {
            extension.modules.forEach { module ->
                moduleMap[module.name] = Configuration(module.name).apply {
                    debug = extension.debug
                }
                if (module.srcFolders.isNotEmpty()) {
                    moduleMap[module.name]!!.srcFolders.addAll(module.srcFolders)
                }
                if (module.stringFiles.isNotEmpty()) {
                    moduleMap[module.name]!!.stringFiles.addAll(module.stringFiles)
                }
                if (module.assetsFiles.isNotEmpty()) {
                    moduleMap[module.name]!!.assetsFiles.addAll(module.assetsFiles)
                }

                if (moduleMap[module.name]!!.srcFolders.isEmpty()) {
                    moduleMap[module.name]!!.srcFolders.addAll(defaultConfig().srcFolders)
                }
                if (moduleMap[module.name]!!.stringFiles.isEmpty()) {
                    moduleMap[module.name]!!.stringFiles.addAll(defaultConfig().stringFiles)
                }
            }
            extension.variants.forEach { variant ->
                variantMap[variant.name] = VariantApplicationId(variant.name).apply {
                    applicationId = variant.applicationId
                    mockedFingerprint = variant.mockedFingerprint
                    skip = variant.skip
                }
            }
            this.project.registerTask()
        }
        this.project.gradle.addBuildListener(ExecutionListener(
            debug = extension.debug,
            dataFound = { _, _ ->
                // nothing to do here
            },
            mergeResourcesStart = { module, variant ->
                fingerPrint(variantMap, module, variant, extension.debug) { key ->
                    if ("none" == key) {
                        return@fingerPrint
                    }
                    when {
                        moduleMap.containsKey(module) -> {
                            val variantOrFlavor = extension.variants.find {
                                variant.toLowerCase().contains(it.name.toLowerCase())
                            }
                            if (variantOrFlavor != null && variantOrFlavor.skip) {
                                PrintUtils.print(module, "Skipping $variant")
                                return@fingerPrint
                            }

                            PrintUtils.print(module, "$variant:$key")
                            PrintUtils.print(module, backupStringRes)
                            moduleMap[module]?.let { configuration ->
                                backupResourceFiles(absoluteProjectPath, configuration)
                            }

                            moduleMap[module]?.let { configuration ->
                                val files = locateResourceFiles(absoluteProjectPath, configuration)
                                files.forEach { file ->
                                    modifyXML(
                                        file.file, extension.main_module, key, extension.debug,
                                        variantOrFlavor?.applicationId ?: ""
                                    )
                                }
                            }
                            PrintUtils.print(module, obfuscateStringRes)
                        }
                        else -> {
                            val defaultConfiguration = defaultConfig().apply {
                                name = module
                            }
                            PrintUtils.print(module, "$variant:$key")
                            PrintUtils.print(module, backupStringRes)
                            backupResourceFiles(absoluteProjectPath, defaultConfiguration)
                            PrintUtils.print(module, obfuscateStringRes)
                            val files = locateResourceFiles(absoluteProjectPath, defaultConfiguration)
                            files.forEach { file ->
                                modifyXML(file.file, extension.main_module, key, extension.debug)
                            }
                        }
                    }
                }

            },
            mergeResourcesFinish = { module, variant ->
                PrintUtils.print(module, restoreStringRes)
                val variantOrFlavor = extension.variants.find {
                    variant.toLowerCase().contains(it.name.toLowerCase())
                }
                if (variantOrFlavor != null && variantOrFlavor.skip) {
                    return@ExecutionListener
                }
                restoreResourceFiles(absoluteProjectPath, module)
            },
            mergeAssetsStart = { module, variant ->
                fingerPrint(variantMap, module, variant, extension.debug) { key ->
                    if ("none" == key) {
                        return@fingerPrint
                    }
                    when {
                        moduleMap.containsKey(module) -> {
                            val variantOrFlavor = extension.variants.find {
                                variant.toLowerCase().contains(it.name.toLowerCase())
                            }
                            if (variantOrFlavor != null && variantOrFlavor.skip) {
                                PrintUtils.print(module, "Skipping $variant")
                                return@fingerPrint
                            }

                            PrintUtils.print(module, "$variant:$key")
                            PrintUtils.print(module, backupAssets)
                            moduleMap[module]?.let { configuration ->
                                backupAssetsFiles(absoluteProjectPath, configuration)
                            }

                            moduleMap[module]?.let { configuration ->
                                val files = locateAssetsFiles(absoluteProjectPath, configuration)
                                files.forEach { file ->
                                    if (extension.debug) {
                                        PrintUtils.print(null, file.file.getContent())
                                    }
                                    obfuscateFile(
                                        extension.main_module,
                                        key,
                                        file.file,
                                        variantOrFlavor?.applicationId ?: ""
                                    )
                                    if (extension.debug) {
                                        PrintUtils.print(null, file.file.getContent())
                                    }
                                }
                            }
                            PrintUtils.print(module, obfuscateAssets)
                        }
                    }
                }

            },
            mergeAssetsFinish = { module, variant ->
                PrintUtils.print(module, restoreAssets)
                val variantOrFlavor = extension.variants.find {
                    variant.toLowerCase().contains(it.name.toLowerCase())
                }
                if (variantOrFlavor != null && variantOrFlavor.skip) {
                    return@ExecutionListener
                }
                restoreAssetsFiles(absoluteProjectPath, module)
            }

        ))
    }

    open class Extension {
        var debug: Boolean = false
        var main_module: String = "app"
        var modules: NamedDomainObjectContainer<Configuration>
            @Suppress("UNCHECKED_CAST")
            get() = DslObject(this).extensions.getByName("modules") as NamedDomainObjectContainer<Configuration>
            internal set(value) {
                DslObject(this).extensions.add("modules", value)
            }
        var variants: NamedDomainObjectContainer<VariantApplicationId>
            @Suppress("UNCHECKED_CAST")
            get() = DslObject(this).extensions.getByName("variants") as NamedDomainObjectContainer<VariantApplicationId>
            internal set(value) {
                DslObject(this).extensions.add("variants", value)
            }
    }

    open class Configuration(var name: String) {
        var assetsFiles = mutableListOf<String>()
        var stringFiles = mutableListOf<String>()
        var srcFolders = mutableListOf<String>()
        var debug = false
    }

    open class VariantApplicationId(var name: String) {
        var applicationId = ""
        var mockedFingerprint = ""
        var skip = false
    }

}


