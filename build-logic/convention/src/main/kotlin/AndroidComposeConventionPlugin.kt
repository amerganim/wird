import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.LibraryExtension
import com.wird.convention.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Applies the Compose compiler plugin and Compose dependencies. Apply this
 * alongside [AndroidLibraryConventionPlugin] or [AndroidApplicationConventionPlugin]
 * (after the Android plugin, so the concrete extension is registered).
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            when {
                pluginManager.hasPlugin("com.android.application") ->
                    extensions.configure<ApplicationExtension> { configureAndroidCompose(this) }

                pluginManager.hasPlugin("com.android.library") ->
                    extensions.configure<LibraryExtension> { configureAndroidCompose(this) }
            }
        }
    }
}
