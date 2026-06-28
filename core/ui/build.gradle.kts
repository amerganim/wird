plugins {
    id("wird.android.library")
    id("wird.android.compose")
}

android {
    namespace = "com.wird.core.ui"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    api(libs.androidx.compose.material.icons.extended)
}
