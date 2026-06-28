plugins {
    id("wird.android.feature")
}

android {
    namespace = "com.wird.feature.alarm"
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.datetime)
}
