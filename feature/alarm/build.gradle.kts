plugins {
    id("wird.android.feature")
}

android {
    namespace = "com.wird.feature.alarm"
}

dependencies {
    // Shared prayer location/method + calculator for Fajr-time alarms.
    implementation(project(":core:prayertimes"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.datetime)
    implementation(libs.adhan2)
}
