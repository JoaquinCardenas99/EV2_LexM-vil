import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val firmaArchivo = rootProject.file("keystore.properties")
val firma = Properties().apply { if (firmaArchivo.exists()) firmaArchivo.inputStream().use { load(it) } }

android {
    namespace = "duoc.example.EV2_Desarrollo"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "duoc.example.EV2_Desarrollo"
        minSdk = 24
        targetSdk = 37
        versionCode = 3
        versionName = "1.1"

        val apiUrl = providers.gradleProperty("LEXMOVIL_API_URL").orElse("http://10.0.2.2:8080/api/").get()
        require(apiUrl.endsWith("/")) { "LEXMOVIL_API_URL debe terminar en /" }
        buildConfigField("String", "API_BASE_URL", "\"${apiUrl}\"")
        manifestPlaceholders["permitirHttpLocal"] = providers.gradleProperty("LEXMOVIL_ALLOW_HTTP").orElse("false").get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (firmaArchivo.exists()) create("entrega") {
            storeFile = rootProject.file(firma.getProperty("storeFile"))
            storePassword = firma.getProperty("storePassword")
            keyAlias = firma.getProperty("keyAlias")
            keyPassword = firma.getProperty("keyPassword")
        }
    }
    buildTypes {
        release {
            if (firmaArchivo.exists()) signingConfig = signingConfigs.getByName("entrega")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.3")
    // Icons
    implementation("androidx.compose.material:material-icons-extended")
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    testImplementation(libs.junit)
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}