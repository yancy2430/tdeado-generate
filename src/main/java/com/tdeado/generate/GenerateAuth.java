package com.tdeado.generate;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.tdeado.generate.entity.VueAuth;
import com.tdeado.generate.entity.VueBean;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 更新扫描权限
 * @goal js
 */
public class GenerateAuth extends AbstractMojo {
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
        List<File> list = FileUtil.loopFiles(uiPath + "src/views", pathname -> FileUtil.pathEndsWith(pathname, ".vue"));
        for (File file : list) {
            try {
                scanVue(file.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static VueBean scanVue(String vuePath) throws IOException {
        String body = FileUtil.readString(new File(vuePath), StandardCharsets.UTF_8);
        Document jsoup = Jsoup.parse(body);
        Elements elements = jsoup.getElementsByAttributeStarting("v-auth");
        List<VueAuth> auth = new ArrayList<>();
        for (Element element : elements) {
            String json = element.attr("v-auth")
                    .replace("'", "")
                    .replace("\"", "")
                    .replace(":", "\":\"")
                    .replace(",", "\",\"")
                    .replace("{", "{\"")
                    .replace("}", "\"}")
                    .replace("\"[", "[\"")
                    .replace("]\"", "\"]");
            JSON jsonObj = JSONUtil.parse(json);
            String title = JSONUtil.getByPath(jsonObj, "title").toString();
            auth.add(new VueAuth()
                    .setCode(Jsoup.connect("https://www.chtml.cn/w?word=" + title).get().select(".list_box .list_table_info .code_name_line>a").get(0).attr("data-val"))
                    .setTitle(title)
                    .setApi(jsonObj.getByPath("api", List.class))
            );
        }
        List<String> apis = new ArrayList<>();
        Pattern r = Pattern.compile("import \\{([\\s\\S]*?)} from (\"|').*?(\"|')");
        Matcher m = r.matcher(body);
        while (m.find()) {
            if (m.group(0).contains("@/api/")) {
                for (String s : m.group(1).split(",")) {
                    apis.add(s.trim());
                }
            }
        }
        return new VueBean()
                .setApi(apis)
                .setAuth(auth);
    }
}
