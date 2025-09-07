// Root build.gradle.kts

import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
}

// Ensure Java 17 toolchain is used by any module that applies the Java plugin
allprojects {
    extensions.findByType(org.gradle.api.plugins.JavaPluginExtension::class.java)?.apply {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}
