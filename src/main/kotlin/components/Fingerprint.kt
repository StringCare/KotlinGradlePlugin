package components

private class Fingerprint {

    companion object {
        private var key: String? = null
        private var until: String? = null
        private var error: String? = null
        private var variantLocated = false
        private var moduleLocated = false
    }

    fun extract(module: String, variant: String, debug: Boolean, trace: String): String {
        val lines = trace.split("\n")
        lines.forEach { line ->
            when {
                line.toLowerCase().contains("downloading") -> if (debug) {
                    PrintUtils.print(module, line, debug)
                }
                line.toLowerCase().contains("unzipping") -> if (debug) {
                    PrintUtils.print(module, line, debug)
                }
                line.toLowerCase().contains("permissions") -> if (debug) {
                    PrintUtils.print(module, line, debug)
                }
                line.toLowerCase().contains("config:") && moduleLocated && variantLocated -> {
                    val k = line.split(": ")[1].trim()
                    val valid = !k.equals("none", ignoreCase = true)
                    if (!valid) {
                        key = k
                        PrintUtils.print(module, "\uD83E\uDD2F no config defined for variant $variant", true)
                        if (debug) {
                            until = key
                        }
                    } else if (debug) {
                        PrintUtils.print(module, "Module: $module", true)
                        PrintUtils.print(module, "Variant: $variant", true)
                    }

                }
                line.toLowerCase().contains("sha1") && moduleLocated && variantLocated -> {
                    key = line.split(" ")[1]
                    if (debug) {
                        PrintUtils.print(module, line, debug)
                    }
                }
                line.toLowerCase().contains("error") -> {
                    error = line.split(": ")[1]
                }
                line.toLowerCase().contains("valid until") && moduleLocated && variantLocated -> {
                    until = line.split(": ")[1]
                    if (debug) {
                        PrintUtils.print(module, line, debug)
                    }
                }
                line.toLowerCase().contains("store") && moduleLocated && variantLocated -> if (debug) {
                    PrintUtils.print(module, line, debug)
                }
                line.toLowerCase().contains("variant") && moduleLocated -> {
                    val locV = line.split(" ")[1]
                    if (locV == variant) {
                        variantLocated = true
                    }
                }
                line.toLowerCase().contains(":$module") -> moduleLocated = true
            }
            if (key != null && (!debug || debug && until != null)) {
                return key!!
            }
        }
        return ""
    }
}

/**
 * Gets the signing report trace and extracts the fingerprint
 */
fun fingerPrint(
    variantMap: MutableMap<String, StringCare.VariantApplicationId>,
    module: String,
    variant: String,
    debug: Boolean,
    keyFound: (key: String) -> Unit
) {
    if (variantMap.containsKey(variant)) {
        if (variantMap[variant]!!.mockedFingerprint.isNotEmpty()) {
            keyFound(variantMap[variant]!!.mockedFingerprint)
            return
        }
    }
    signingReportTask().runCommand { _, report ->
        keyFound(report.extractFingerprint(module, variant, debug))
    }
}

/**
 * Returns the SHA1 fingerprint for the given trace
 */
fun String.extractFingerprint(module: String = "app", variant: String = "debug", debug: Boolean = false): String {
    return Fingerprint().extract(module, variant, debug, this)
}