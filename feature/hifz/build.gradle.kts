plugins {
    id("wird.android.feature")
}

android {
    namespace = "com.wird.feature.hifz"
}

dependencies {
    implementation(project(":core:database"))
    implementation(libs.kotlinx.datetime)
}
