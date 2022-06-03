package com.tdeado.generate;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.power.doc.builder.ApiDataBuilder;
import com.power.doc.builder.HtmlApiDocBuilder;
import com.power.doc.model.ApiAllData;
import com.power.doc.model.ApiConfig;
import com.power.doc.model.ApiDoc;
import com.power.doc.model.SourceCodePath;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 接口文档+npm接口依赖生成
 */
public class InitDoc {
    static String json = "{\n" +
            "  \"projectName\": \"项目名称\",\n" +
            "  \"serverUrl\": \"http://127.0.0.1\",\n" +
            "  \"outPath\": \"src/main/resources/doc\",\n" +
            "  \"allInOne\": true,\n" +
            "  \"isStrict\": false,\n" +
            "  \"coverOld\": true,\n" +
            "  \"sortByTitle\": true,\n" +
            "  \"packageFilters\": \"com.tdeado.*\",\n" +
            "  \"style\": \"hopscotch\",\n" +
            "  \"inlineEnum\": true,\n" +
            "  \"requestExample\": false,\n" +
            "  \"responseExample\": false,\n" +
            "  \"displayActualType\": true,\n" +
            "  \"ignoreRequestParams\": [\n" +
            "    \"org.springframework.ui.ModelMap\"\n" +
            "  ]\n" +
            "}";

    public static void generateApiJs(String codePath,String jsPath) throws Exception {
        System.out.println("开始生成api JS文件");
        ApiConfig config = new Gson().fromJson(json, ApiConfig.class);
        config.setSourceCodePaths(new SourceCodePath().setPath(codePath));
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
            model.put("module", apiDoc.getName().replace("Controller", ""));
            System.out.println("开始生成" + jsPath + apiDoc.getName().replace("Controller", "") + ".js");
            createFile(template, model, jsPath + apiDoc.getName().replace("Controller", "") + ".js");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("version", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy.Md.HHmm")));
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

    public static String readFileContent(InputStream inputStream) {
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
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
