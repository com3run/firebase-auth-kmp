package az.random.testauth

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform