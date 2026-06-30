plugins {
    id("wird.android.feature")
}

android {
    namespace = "com.wird.feature.qibla"
}

dependencies {
    // Reuse the user's chosen prayer location (lat/lng) for the qibla bearing.
    implementation(project(":core:prayertimes"))

    implementation(libs.androidx.activity.compose)

    // AR mode
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    testImplementation(libs.junit)
}
