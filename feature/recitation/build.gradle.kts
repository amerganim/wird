plugins {
    id("wird.android.feature")
}

android {
    namespace = "com.wird.feature.recitation"
}

dependencies {
    implementation(project(":core:database"))

    implementation(libs.androidx.activity.compose)

    testImplementation(libs.junit)
}
