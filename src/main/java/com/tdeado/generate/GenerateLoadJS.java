package com.tdeado.generate;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "load-js",inheritByDefault = false, aggregator = true)
public class GenerateLoadJS extends AbstractMojo {
    /**
     * @parameter expression="${basedir}"
     */
    @Parameter(name = "basedir")
    private String basedir;
    /**
     * @parameter expression="${jsPath}"
     */
    @Parameter(name = "smbPath")
    private String smbPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            InitDoc.localJs(basedir,smbPath);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

}
