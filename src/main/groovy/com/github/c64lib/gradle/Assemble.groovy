import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction

class Assemble extends DefaultTask {

	final Property<File> kaJar = project.objects.property(File)
	final Property<String> libDir = project.objects.property(String)

	@TaskAction
	def assemble() {
		project.fileTree(dir: '.', include: '**/*.asm', exclude: 'cpm_modules').each { file ->
			println file.path
			project.javaexec {
				main '-jar'
				args kaJar.get(), '-libdir', libDir.get(), file.path
			}
		}
	}
}
