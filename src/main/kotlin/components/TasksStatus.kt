package components

interface TasksStatus {

    fun debug(): Boolean

    fun onDataFound(module: String, variant: String)

    fun onMergeResourcesStarts(module: String, variant: String)

    fun onMergeResourcesFinish(module: String, variant: String)
}