import com.wird.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * Convention for `:feature:*` modules: an Android library that uses Compose and
 * Hilt, and depends on the shared `:core` modules plus the common UI/nav deps.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("wird.android.library")
                apply("wird.android.compose")
                apply("wird.android.hilt")
            }

            dependencies {
                add("implementation", project(":core:common"))
                add("implementation", project(":core:ui"))

                add("implementation", libs.findLibrary("androidx-core-ktx").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                add("implementation", libs.findLibrary("androidx-navigation-compose").get())
                add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
            }
        }
    }
}
