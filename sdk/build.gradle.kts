plugins {
    id("com.android.library")
    kotlin("android")
    id("maven-publish")
}

group = "app.knock"
version = "1.0.3"

publishing {
    publications {
        register<MavenPublication>("release") {
            artifactId = "knock-android"
            groupId = "app.knock"
            version = "1.0.3"
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
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

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