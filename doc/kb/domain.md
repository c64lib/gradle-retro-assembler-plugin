# Domain
The project is divided into several business domains.

The `compilers` domain contains code that is supposed to compile source files into binary files.

The `dependencies` domain contains code that is supposed to manage dependencies that are needed to build sources, like external libraries that can be downloaded from internet.

The `emulators` domain contains code that is supposed to emulate hardware.
It is used to run compiled binary files for automated testing.

The `testing` domain contains code that is supposed to support automated testing of the project source.

The `processors` domain contains code that is supposed to process asset files such as graphics or music so that it can be used in source files of the project.
The `charpad` subdomain of the `processors` domain contains code that is supposed to process CharPad files to extract graphical assets that are consumed by the source files of the project.
The `spritepad` subdomain of the `processors` domain contains code that is supposed to process SpritePad files to extract graphical assets that are consumed by the source files of the project.
The `goattracker` subdomain of the `processors` domain contains code that is supposed to process GoatTracker files to extract musical assets that are consumed by the source files of the project.
The `image` subdomain of the `processors` domain contains code that is supposed to process image files to extract graphical assets that are consumed by the source files of the project.

The `flows` domain contains code that is supposed to orchestrate and execute build pipelines using the Pipeline DSL.
It provides the infrastructure for defining complex build workflows with multiple steps, dependencies, and data transformations.
The flows domain integrates with processors and compilers to execute coordinated build tasks.
 