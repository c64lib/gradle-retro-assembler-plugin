rootProject.name = "retro-assembler"

include(":infra:gradle")

include(":shared:domain")
include(":shared:filedownload")
include(":shared:gradle")
include(":shared:binary-utils")
include(":shared:processor")
include(":shared:testutils")

include(":flows")

include(":compilers:kickass")
include(":compilers:kickass:adapters:in:gradle")
include(":compilers:kickass:adapters:out:gradle")
include(":compilers:kickass:adapters:out:filedownload")

include(":emulators:vice")
include(":emulators:vice:adapters:out:gradle")

include(":testing:64spec")
include(":testing:64spec:adapters:in:gradle")

include(":dependencies")
include(":dependencies:adapters:in:gradle")
include(":dependencies:adapters:out:gradle")

include(":processors:goattracker")
include(":processors:goattracker:adapters:in:gradle")
include(":processors:goattracker:adapters:out:gradle")

include(":processors:spritepad")
include(":processors:spritepad:adapters:in:gradle")

include(":processors:charpad")
include(":processors:charpad:adapters:in:gradle")

include(":processors:image")
include(":processors:image:adapters:in:gradle")
include(":processors:image:adapters:out:png")
include(":processors:image:adapters:out:file")

include(":doc")
