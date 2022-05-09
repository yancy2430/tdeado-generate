package com.tdeado.generate;

import cn.hutool.core.io.FileUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;

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
     * @parameter expression="${uiPath}"
     */
    private String uiPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            File api = new File(uiPath+"/src/api/");
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
            InitDoc.generateApiJs(api.getPath()+"/");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

}
