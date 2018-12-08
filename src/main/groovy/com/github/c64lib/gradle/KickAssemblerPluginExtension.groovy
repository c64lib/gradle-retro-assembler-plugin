import org.gradle.api.Project
import org.gradle.api.provider.Property

class KickAssemblerPluginExtension {

	final Property<File> kaJar
	final Property<String> libDir
	
	KickAssemblerPluginExtension(Project project) {
		kaJar = project.objects.property(File)
		kaJar.set(new File(project.buildDir,'ka/KickAss.jar'))
		libDir = project.objects.property(String)
		libDir.set('..')
	}
}
