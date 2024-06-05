plugins {
    id("com.android.library")
    kotlin("android")
    id("maven-publish")
}

group = "app.knock"
version = "1.1.1"

publishing {
    publications {
        register<MavenPublication>("release") {
            artifactId = "knock-android"
            groupId = "app.knock"
            version = "1.1.1"
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

android {
    namespace = "app.knock.client"
    compileSdk = 34

    defaultConfig {
        minSdk = 28
        aarMetadata {
            minCompileSdk = 28
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.02.01"))
    implementation("androidx.compose.foundation:foundation:1.6.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.1")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.github.jeziellago:compose-markdown:0.4.1")
    implementation("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-rc04")
    implementation("androidx.test:core-ktx:1.5.0")
    implementation("androidx.test.ext:junit-ktx:1.1.5")
    implementation("androidx.test:runner:1.5.2")

    testImplementation("org.mockito:mockito-core:3.12.4")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")


    // okHTTP
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Phoenix Socket
    api("com.github.dsrees:JavaPhoenixClient:1.2.0")

    // Jackson
    implementation ("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation ("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")

    // Arrow
    api("io.arrow-kt:arrow-core:1.2.0")
    implementation("io.arrow-kt:arrow-integrations-jackson-module:0.14.1")

    implementation("com.google.code.gson:gson:2.10.1")

    api("com.google.firebase:firebase-messaging-ktx:23.4.1")
}