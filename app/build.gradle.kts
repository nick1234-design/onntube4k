import java.util.Base64

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.nick1234.onntube4k"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.nick1234.onntube4k"
        minSdk = 23
        targetSdk = 28
        versionCode = 1
        versionName = "0.1.0"
        val apiKey = providers.gradleProperty("youtubeApiKey").orElse(providers.environmentVariable("YOUTUBE_API_KEY").orElse("")).get()
        val encodedApiKey = Base64.getEncoder().encodeToString(apiKey.toByteArray(Charsets.UTF_8))
        buildConfigField("String", "YOUTUBE_API_KEY_B64", "\"$encodedApiKey\"")
    }
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
