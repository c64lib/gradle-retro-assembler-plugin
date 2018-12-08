import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskAction

class CleanKickFiles extends Delete {

	@TaskAction
	def void clean() {
		delete project.buildDir
		delete project.fileTree(".").matching {
			include "**/*.prg", "**/*.sym"
		}
		super.clean()
	}
}
