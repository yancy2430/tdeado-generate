package com.tdeado.generate;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * 生成DAO代码
 * @goal js
 */
public class GenerateJS extends AbstractMojo {
    /**
     * @parameter expression="${basedir}"
     */
    private String basedir;
    /**
     * @parameter expression="${project.groupId}"
     */
    private String groupId;
    /**
     * @parameter expression="${project.artifactId}"
     */
    private String artifactId;
    /**
     * @parameter expression="${project.build.directory}"
     */
    private String outobjpath;

    /**
     * @parameter expression="${jsPath}"
     */
    private String jsPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            InitDoc.generateApiJs(basedir,jsPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

}
