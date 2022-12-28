rootProject.name = "retro-assembler"

include(":domain")
include(":app:binary-utils")
include(":app:processor-commons")
include(":app:charpad-processor")
include(":app:spritepad-processor")
include(":app:binary-interleaver")
include(":app:nybbler")
include(":infra:gradle")

include(":emulators:vice")
include(":emulators:vice:adapters:out:gradle")
include(":shared:domain")

include(":doc")
