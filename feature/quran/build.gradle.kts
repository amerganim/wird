plugins {
    id("wird.android.feature")
}

android {
    namespace = "com.wird.feature.quran"
}

dependencies {
    implementation(project(":core:database"))
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.activity.compose)

    testImplementation(libs.junit)
}
