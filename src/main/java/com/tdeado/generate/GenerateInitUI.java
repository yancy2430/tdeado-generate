package com.tdeado.generate;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * 初始化前端
 * @goal init-ui
 */
public class GenerateInitUI extends AbstractMojo {
    /**
     * @parameter expression="${uiPath}"
     */
    private String uiPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
            URL url = GenerateInitUI.class.getClassLoader().getResource("templates/ui");
            assert url != null;
            System.err.println(url.getPath());
            File file = new File(url.getPath());

    }
}
