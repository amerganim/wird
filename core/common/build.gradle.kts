plugins {
    id("wird.android.library")
}

android {
    namespace = "com.wird.core.common"
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.javax.inject)
}
