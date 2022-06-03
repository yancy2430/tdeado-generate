package com.tdeado.generate;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "js",inheritByDefault = false, aggregator = true)
public class GenerateJS extends AbstractMojo {
    /**
     * @parameter expression="${basedir}"
     */
    @Parameter(name = "basedir")
    private String basedir;
    /**
     * @parameter expression="${jsPath}"
     */
    @Parameter(name = "jsPath")
    private String jsPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            for (Object o : getPluginContext().entrySet()) {
                System.err.println(o);
            }
            File api = new File(jsPath);
            if (api.exists()){
                if (!api.isDirectory()) {
                    System.err.println(api.getPath()+" 必须为文件夹");
                    return;
                }
            }else {
                if (api.mkdirs()){
                    System.err.println(api.getPath()+" create ok");
                }
            }
            System.err.println("outPath:"+api.getPath()+"/");
            InitDoc.generateApiJs(basedir,api.getPath()+"/");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

}
