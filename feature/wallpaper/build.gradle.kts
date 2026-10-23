plugins { id("com.android.library"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "studio.nox.wallpaper"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
}
dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:renderer"))
    implementation(project(":core:data"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}
