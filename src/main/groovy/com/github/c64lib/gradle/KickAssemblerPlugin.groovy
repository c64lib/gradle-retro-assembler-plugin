import org.gradle.api.Plugin
import org.gradle.api.Project

class KickAssemblerPlugin implements Plugin<Project> {

	void apply(Project project) {
		def extension = project.extensions.create('kickAssembler', KickAssemblerPluginExtension, project)
		project.tasks.create('assemble', Assemble) {
			kaJar = extension.kaJar;
			libDir = extension.libDir;
		}
		project.tasks.create('clean', CleanKickFiles)
	}
}

