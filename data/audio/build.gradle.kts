plugins {
    id("wird.android.library")
    id("wird.android.hilt")
}

android {
    namespace = "com.wird.data.audio"
}

dependencies {
    implementation(project(":core:common"))
}
