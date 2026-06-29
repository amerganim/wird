plugins {
    id("wird.android.feature")
}

android {
    namespace = "com.wird.feature.qibla"
}

dependencies {
    // Reuse the user's chosen prayer location (lat/lng) for the qibla bearing.
    implementation(project(":core:prayertimes"))
}
