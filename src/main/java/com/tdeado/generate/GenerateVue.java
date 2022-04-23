package com.tdeado.generate;

import cn.hutool.core.io.resource.ClassPathResource;
import com.google.gson.Gson;
import com.power.doc.builder.ApiDataBuilder;
import com.power.doc.model.ApiAllData;
import com.power.doc.model.ApiConfig;
import com.power.doc.model.ApiDoc;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.tdeado.generate.InitDoc.createFile;
import static com.tdeado.generate.InitDoc.readFileContent;

/**
 * 生成DAO代码
 * @goal vue
 */
public class GenerateVue extends AbstractMojo {
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
     * @parameter expression="${vuePath}"
     */
    private String vuePath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream("smart-doc.json");
            String jsonConfig = readFileContent(in);
            ApiConfig config = new Gson().fromJson(jsonConfig, ApiConfig.class);
            ApiAllData apiAllData = ApiDataBuilder.getApiData(config);
            Configuration configuration = new Configuration(Configuration.getVersion());
            configuration.setClassForTemplateLoading(InitDoc.class, "/templates/");
            //设置字符集
//          configuration.setDefaultEncoding("UTF‐8");
            //加载模板
            Template template = configuration.getTemplate("vue.ftl");
            //数据模型
            for (ApiDoc apiDoc : apiAllData.getApiDocList()) {
                HashMap<String, Object> model = new HashMap<>();
                model.put("api", apiDoc);
                createFile(template, model, vuePath + apiDoc.getName().replace("Controller", "") + ".vue");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("version", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy.Md.HHmm")));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

}
