plugins {
    id("wird.android.library")
    id("wird.android.hilt")
}

android {
    namespace = "com.wird.core.prayertimes"
}

dependencies {
    // Exposed so consumers get the prayer-calc types (CalculationMethod, Instant…).
    api(libs.adhan2)
    api(libs.kotlinx.datetime)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
}
