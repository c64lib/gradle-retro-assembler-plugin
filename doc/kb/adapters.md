# General
All adapters, that is, objects that are passed to the use cases, cannot leak into the domain code.
Leak in should be prevented by building an abstraction in form of a port (interface).

# Gradle
For parallel execution of tasks always use standard gradle approach to parallism using Workers API.
