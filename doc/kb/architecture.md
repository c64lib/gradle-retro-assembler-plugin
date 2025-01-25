# General architecture
The project uses hexagonal architecture.

Top level split is based on domain analysis. The following domains are idenfified: compilers, dependencies, emulators, processors, and testing.
Domain "shared" is reserved for shared kernel elements.

Inside each domain there is a room for optional subdomains. Each leaf subdomain consists src folder that implements business logic.
Inside business logic we always identify domain folder that contains domain data structures and usecases folder that contains domain functions.
A use case is a class with single public method that is called by inbound adapter.

In adapters subfolder we contain submodules divided into in and out subfolder denoting inbound and outbound adapters.
Each leaf module inside adapters implements single concern of the technology.
As this is a gradle plugin project, gradle itself is considered as a concern, could be either inbnound or outbound adapter.

## Domain part
### Use cases
Use case should be always a kotlin class with single method.
Use case class should be named so that it always ends with `UseCase.kt` suffix.
Method should always consume payload parameter and be named apply.
Method can return an use case result object.

# Technology stack
Programming language of this project is Kotlin.
This project is a gradle plugin code.
