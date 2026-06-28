plugins {
    id("wird.android.feature")
}

android {
    namespace = "com.wird.feature.prayer"
}

dependencies {
    implementation(libs.adhan2)
    implementation(libs.kotlinx.datetime)
}
