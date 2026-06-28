plugins {
    id("wird.android.feature")
}

android {
    namespace = "com.wird.feature.prayer"
}

dependencies {
    implementation(project(":core:prayertimes"))
    implementation(libs.adhan2)
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.activity.compose)
}
