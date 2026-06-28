// Top-level build file. Plugins are declared here with `apply false` so the
// versions are known to all subprojects; convention plugins in :build-logic
// apply them by id where needed.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
