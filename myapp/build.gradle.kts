// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // 使用正确的语法应用Android应用程序插件
    id("com.android.application") version "8.3.0" apply false
}

task("clean", Delete::class) {
    delete(rootProject.buildDir)
}

// 添加Android构建插件配置
