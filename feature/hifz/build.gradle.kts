plugins {
    id("wird.android.feature")
}

android {
    namespace = "com.wird.feature.hifz"
}

dependencies {
    implementation(project(":core:database"))
    implementation(libs.kotlinx.datetime)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.datasource)
    implementation(libs.androidx.media3.database)

    testImplementation(libs.junit)
}
