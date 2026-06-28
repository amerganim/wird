plugins {
    id("wird.android.feature")
}

android {
    namespace = "com.wird.feature.alarm"
}

dependencies {
    // Pragmatic reuse of prayer location/method + calculator for Fajr-time alarms.
    // TODO: extract PrayerCalculator/PrayerSettings into a shared :core module.
    implementation(project(":feature:prayer"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.datetime)
    implementation(libs.adhan2)
}
