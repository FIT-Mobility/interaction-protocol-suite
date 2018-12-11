package de.fraunhofer.fit.ips.project2wsdl;


import de.fraunhofer.fit.ips.model.IllegalDocumentStructureException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Mojo(name = "project2wsdl", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class MyMojo extends AbstractMojo {
    /**
     * Location of the result file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;
    /**
     * Location of the proto file containing a delimited SchemaAndProjectStructure instance.
     */
    @Parameter(property = "jsonProject", required = true)
    private String protoSchemaAndProjectStructurePath;
    /**
     * Hostname prefix, e.g. http://localhost:8080/ws
     */
    @Parameter(defaultValue = "http://localhost:8080/ws", property = "hostname", required = true)
    private String hostname;

    public void execute()
            throws MojoExecutionException {
        outputDirectory.mkdirs();
        try {
            ProjectToWsdlConverter.createWSDL(getUrl(), hostname, outputDirectory);
        } catch (final IOException | TransformerException | ParserConfigurationException | IllegalDocumentStructureException e) {
            throw new MojoExecutionException("error", e);
        }
    }

    private URL getUrl() throws MojoExecutionException {
        try {
            return new URL(new URL(protoSchemaAndProjectStructurePath).toExternalForm());
        } catch (MalformedURLException e) {
            try {
                return new File(protoSchemaAndProjectStructurePath).toURI().toURL();
            } catch (MalformedURLException e2) {
                throw new MojoExecutionException("invalid url", e);
            }
        }
    }
}
