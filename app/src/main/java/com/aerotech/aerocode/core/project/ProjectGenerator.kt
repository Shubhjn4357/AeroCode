package com.aerotech.aerocode.core.project

import com.aerotech.aerocode.core.filesystem.FileSystem
import java.io.File

object ProjectGenerator {

    suspend fun generateProject(
        fileSystem: FileSystem,
        projectDir: String,
        appName: String,
        packageName: String,
        template: ProjectTemplate,
        javaVersion: Int = 17,
        gradleVersion: String = "8.7",
        minSdk: Int = 26
    ): String {
        // Create root project folder
        fileSystem.createDirectory(projectDir)

        // 1. settings.gradle.kts
        val settingsGradle = """
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "$appName"
include(":app")
        """.trimIndent()
        fileSystem.write("$projectDir/settings.gradle.kts", settingsGradle)

        // 2. build.gradle.kts (root)
        val rootBuildGradle = """
plugins {
    id("com.android.application") version "8.7.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    ${if (!template.isXmlViews) "id(\"org.jetbrains.kotlin.plugin.compose\") version \"2.0.20\" apply false" else ""}
}
        """.trimIndent()
        fileSystem.write("$projectDir/build.gradle.kts", rootBuildGradle)

        // 3. gradle.properties
        val gradleProps = """
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
        """.trimIndent()
        fileSystem.write("$projectDir/gradle.properties", gradleProps)

        // 4. Gradle Wrapper
        val wrapperDir = "$projectDir/gradle/wrapper"
        fileSystem.createDirectory(wrapperDir)
        val wrapperProps = """
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
        """.trimIndent()
        fileSystem.write("$wrapperDir/gradle-wrapper.properties", wrapperProps)

        // 5. gradlew bash wrapper
        val gradlewScript = """
#!/usr/bin/env sh
exec gradle "${'$'}@"
        """.trimIndent()
        fileSystem.write("$projectDir/gradlew", gradlewScript)

        // 6. app/build.gradle.kts
        val appBuildGradle = if (template.isXmlViews) {
            """
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "$packageName"
    compileSdk = 35

    defaultConfig {
        applicationId = "$packageName"
        minSdk = $minSdk
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_$javaVersion
        targetCompatibility = JavaVersion.VERSION_$javaVersion
    }

    kotlinOptions {
        jvmTarget = "$javaVersion"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
            """.trimIndent()
        } else {
            """
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "$packageName"
    compileSdk = 35

    defaultConfig {
        applicationId = "$packageName"
        minSdk = $minSdk
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_$javaVersion
        targetCompatibility = JavaVersion.VERSION_$javaVersion
    }

    kotlinOptions {
        jvmTarget = "$javaVersion"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.core:core-ktx:1.13.1")
}
            """.trimIndent()
        }
        fileSystem.write("$projectDir/app/build.gradle.kts", appBuildGradle)

        // 7. app/src/main/AndroidManifest.xml
        val manifestTheme = if (template.isXmlViews) "@style/Theme.AppCompat.Light.NoActionBar" else "@android:style/Theme.Material.NoActionBar"
        val manifest = """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="$manifestTheme">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
        """.trimIndent()
        fileSystem.write("$projectDir/app/src/main/AndroidManifest.xml", manifest)

        // 8. Kotlin MainActivity.kt
        val packagePath = packageName.replace('.', '/')
        val kotlinDir = "$projectDir/app/src/main/kotlin/$packagePath"
        fileSystem.createDirectory(kotlinDir)

        val mainActivityContent = template.initialComposeContent(packageName, appName)
        val mainActivityPath = "$kotlinDir/MainActivity.kt"
        fileSystem.write(mainActivityPath, mainActivityContent)

        // 9. XML Layout for XML Views templates
        if (template.isXmlViews && template.initialLayoutXml != null) {
            val resLayoutDir = "$projectDir/app/src/main/res/layout"
            fileSystem.createDirectory(resLayoutDir)
            val layoutContent = template.initialLayoutXml.invoke(packageName, appName)
            fileSystem.write("$resLayoutDir/activity_main.xml", layoutContent)
        }

        // 10. res/values/strings.xml & colors.xml
        val resValuesDir = "$projectDir/app/src/main/res/values"
        fileSystem.createDirectory(resValuesDir)

        val stringsXml = """
<resources>
    <string name="app_name">$appName</string>
</resources>
        """.trimIndent()
        fileSystem.write("$resValuesDir/strings.xml", stringsXml)

        val colorsXml = """
<resources>
    <color name="primary">#3574F0</color>
    <color name="surface">#1E1F22</color>
</resources>
        """.trimIndent()
        fileSystem.write("$resValuesDir/colors.xml", colorsXml)

        return mainActivityPath
    }
}
