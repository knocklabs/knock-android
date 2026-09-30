plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("maven-publish")
}

// Keep in sync with KnockEnvironment.clientVersion (enforced by KnockEnvironmentTests).
val knockVersion = "1.2.6"

group = "app.knock"
version = knockVersion

publishing {
    publications {
        register<MavenPublication>("release") {
            artifactId = "knock-android"
            groupId = "app.knock"
            version = knockVersion
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

android {
    namespace = "app.knock.client"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.all { it.systemProperty("knock.sdkVersion", knockVersion) }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    // Newer AndroidX/Compose releases require consumers to use compileSdk 37 and AGP 9.1; stay on the
    // latest versions that work with AGP 8.6+ and compileSdk 35+.
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.github.jeziellago:compose-markdown:0.4.1")

    testImplementation("org.mockito:mockito-core:5.24.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.robolectric:robolectric:4.17")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("androidx.test:core:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // okHTTP
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Phoenix Socket
    api("com.github.dsrees:JavaPhoenixClient:1.3.1")

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.4")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.4")

    // Arrow
    api("io.arrow-kt:arrow-core:1.2.4")

    // Swipable Rows
    implementation("me.saket.swipe:swipe:1.3.0")

    // KnockMessagingService extends FirebaseMessagingService, so Firebase Messaging is part of the public API.
    api("com.google.firebase:firebase-messaging:25.1.3")
}
