rootProject.name = "CryptoPrice"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.9.0")
            version("coroutines", "1.7.3")
            version("ktor", "2.3.3")
            version("serialization", "1.5.1")
        }
    }
}



