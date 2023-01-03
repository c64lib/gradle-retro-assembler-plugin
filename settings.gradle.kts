rootProject.name = "retro-assembler"

include(":domain")
include(":app:binary-utils")
include(":app:processor-commons")
include(":app:charpad-processor")
include(":app:spritepad-processor")
include(":app:binary-interleaver")
include(":app:nybbler")
include(":infra:gradle")

include(":shared:domain")
include(":shared:filedownload")
include(":shared:gradle")

include(":compilers:kickass")
include(":compilers:kickass:adapters:in:gradle")
include(":compilers:kickass:adapters:out:gradle")
include(":compilers:kickass:adapters:out:filedownload")

include(":emulators:vice")
include(":emulators:vice:adapters:out:gradle")

include(":testing:64spec")
include(":testing:64spec:adapters:in:gradle")

include(":doc")
