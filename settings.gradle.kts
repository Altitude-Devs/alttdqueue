rootProject.name = "AlttdQueue"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        // Altitude
        maven {
            name = "maven"
            url = uri("https://repo.destro.xyz/snapshots")
            credentials(PasswordCredentials::class)
        }
        // Velocity
        maven("https://nexus.velocitypowered.com/repository/maven-public/")
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
