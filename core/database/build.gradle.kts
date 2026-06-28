plugins {
    id("wird.android.library")
    id("wird.android.hilt")
}

android {
    namespace = "com.wird.core.database"
}

// KSP is applied by the Hilt convention; configure Room schema export here.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
