package com.tdeado.generate;

import com.google.gson.Gson;
import com.power.doc.builder.ApiDataBuilder;
import com.power.doc.builder.HtmlApiDocBuilder;
import com.power.doc.model.ApiAllData;
import com.power.doc.model.ApiConfig;
import com.power.doc.model.ApiDoc;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 接口文档+npm接口依赖生成
 */
public class InitDoc {
    /**
     * 流程
     */
    public static void prod(String objPath,String jsPath) throws Exception {
        String jsonConfig = readFileContent(objPath + "/src/main/resources/smart-doc.json");
        ApiConfig config = new Gson().fromJson(jsonConfig, ApiConfig.class);
        ApiAllData apiAllData = ApiDataBuilder.getApiData(config);
        System.err.println("检测完成 无异常");
        HtmlApiDocBuilder.buildApiDoc(config);
        System.err.println("生成文档完成");
        Configuration configuration = new Configuration(Configuration.getVersion());
        configuration.setClassForTemplateLoading(InitDoc.class, "/templates/");
        Template template = configuration.getTemplate("api.ftl");
        for (ApiDoc apiDoc : apiAllData.getApiDocList()) {
            HashMap<String, Object> model = new HashMap<>();
            model.put("api", apiDoc);
            createFile(template, model, jsPath + apiDoc.getName().replace("Controller", "") + ".js");
        }
        System.err.println("JS生成完成");
    }

    /**
     * 检测注释
     *
     * @param objPath
     * @throws IOException
     */
    public static void check(String objPath) throws IOException {
        String jsonConfig = readFileContent(objPath + "/src/main/resources/smart-doc.json");
        ApiConfig config = new Gson().fromJson(jsonConfig, ApiConfig.class);
        ApiDataBuilder.getApiData(config);
        System.err.println("检测完成 无异常");
    }

    /**
     * 生成HTML文档
     *
     * @param objPath
     * @throws IOException
     */
    public static void generateApiDoc(String objPath) throws IOException {
        LocalDateTime vTime = LocalDateTime.now();
        System.out.println("开始生成api文档");
        String jsonConfig = readFileContent(objPath + "/src/main/resources/smart-doc.json");
        ApiConfig config = new Gson().fromJson(jsonConfig, ApiConfig.class);
        config.setOutPath(objPath + "/src/main/resources/"+ config.getOutPath());
        HtmlApiDocBuilder.buildApiDoc(config);
        System.out.println("api文档生成完成 版本" + vTime.format(DateTimeFormatter.ofPattern("yy.Md.HHmm")));
    }


    public static void generateApiJs(String objPath,String jsPath) throws Exception {
        String jsonConfig = readFileContent(objPath + "/src/main/resources/smart-doc.json");
        ApiConfig config = new Gson().fromJson(jsonConfig, ApiConfig.class);
        ApiAllData apiAllData = ApiDataBuilder.getApiData(config);
        Configuration configuration = new Configuration(Configuration.getVersion());
        configuration.setClassForTemplateLoading(InitDoc.class, "/templates/");
        //设置字符集
//        configuration.setDefaultEncoding("UTF‐8");
        //加载模板
        Template template = configuration.getTemplate("api.ftl");
        //数据模型
        for (ApiDoc apiDoc : apiAllData.getApiDocList()) {
            HashMap<String, Object> model = new HashMap<>();
            model.put("api", apiDoc);
            createFile(template, model, jsPath + apiDoc.getName().replace("Controller", "") + ".js");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("version", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy.Md.HHmm")));
    }

    public static void localJs(String objPath, String smbPath) throws IOException, TemplateException {
        System.out.println("开始生成api JS文件");
        String jsonConfig = readFileContent(objPath + "/src/main/resources/smart-doc.json");
        ApiConfig config = new Gson().fromJson(jsonConfig, ApiConfig.class);
        ApiAllData apiAllData = ApiDataBuilder.getApiData(config);
        HtmlApiDocBuilder.buildApiDoc(config);
        Configuration configuration = new Configuration(Configuration.getVersion());
        configuration.setClassForTemplateLoading(InitDoc.class, "/templates/");
//            configuration.setDefaultEncoding("UTF‐8");
        //加载模板
        Template template = configuration.getTemplate("api.ftl");
        //数据模型
        for (ApiDoc apiDoc : apiAllData.getApiDocList()) {
            HashMap<String, Object> model = new HashMap<>();
            model.put("api", apiDoc);
            String content = processTemplateIntoString(template, model);

            SmbFile remoteFile = new SmbFile(smbPath + apiDoc.getName().replace("Controller", "") + ".js");
            if (!remoteFile.exists()) {
                remoteFile.createNewFile();
            }
            System.out.println("开始生成" + remoteFile.getPath());
            remoteFile.connect();
            InputStream in = IOUtils.toInputStream(content, StandardCharsets.UTF_8);
            OutputStream out = new BufferedOutputStream(new SmbFileOutputStream(remoteFile));
            try {
                byte[] buffer = new byte[4096];
                int len = 0; //读取长度
                while ((len = in.read(buffer, 0, buffer.length)) != -1) {
                    out.write(buffer, 0, len);
                }
            } finally {
                try {
                    out.flush();//刷新缓冲的输出流
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void createFile(Template template, Map<String, Object> map, String name) throws Exception {
        String content = processTemplateIntoString(template, map);
        InputStream inputStream = IOUtils.toInputStream(content, StandardCharsets.UTF_8);
        //输出文件
        FileOutputStream fileOutputStream = new FileOutputStream(name);
        int copy = IOUtils.copy(inputStream, fileOutputStream);
    }

    public static String processTemplateIntoString(Template template, Object model) throws IOException, freemarker.template.TemplateException {
        StringWriter result = new StringWriter(1024);
        template.process(model, result);
        return result.toString();
    }

    public static String readFileContent(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr);
            }
            reader.close();
            return sbf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return sbf.toString();
    }
}
