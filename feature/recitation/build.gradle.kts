plugins {
    id("wird.android.feature")
}

android {
    namespace = "com.wird.feature.recitation"
}

dependencies {
    implementation(project(":core:database"))

    implementation(libs.androidx.activity.compose)

    // On-device Arabic speech recognition (offline). Model is downloaded on demand.
    implementation(libs.vosk.android)

    testImplementation(libs.junit)
}
