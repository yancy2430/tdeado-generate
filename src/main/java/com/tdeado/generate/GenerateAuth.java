package com.tdeado.generate;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.db.Db;
import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.tdeado.generate.entity.VueAuth;
import com.tdeado.generate.entity.VueBean;
import com.zaxxer.hikari.util.DriverDataSource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 更新扫描权限
 * @goal auth
 * @aggregator true
 * @inheritByDefault false
 */
public class GenerateAuth extends AbstractMojo {
    /**
     * @parameter expression="${host}"
     */
    private String host;
    /**
     * @parameter expression="${schemaName}"
     */
    private String schemaName;
    /**
     * @parameter expression="${username}"
     */
    private String username;
    /**
     * @parameter expression="${password}"
     */
    private String password;
    /**
     * @parameter expression="${resourcesTable}"
     */
    private String resourcesTable;
    /**
     * @parameter expression="${uiPath}"
     */
    private String uiPath;

    public static void main(String[] args) {
        try {
            new GenerateAuth().execute();
        } catch (MojoExecutionException e) {
            throw new RuntimeException(e);
        } catch (MojoFailureException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        MysqlDataSource dsc = new MysqlDataSource();
        dsc.setUrl("jdbc:mysql://"+host+"/"+schemaName+"?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&useSSL=false&useLocalSessionState=true&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true&tinyInt1isBit=false");
        dsc.setUser(username);
        dsc.setPassword(password);
        String path = uiPath + "src/views";
        List<File> list = FileUtil.loopFiles(path, pathname -> FileUtil.pathEndsWith(pathname, ".vue"));
        for (File file : list) {
            try {
                VueBean api = scanVue(file.getPath(),file.getPath().replace(".vue","").replace(path,""));
                api.setPath(file.getPath().replace(".vue","").replace(path,""));
                api.setCode(api.getPath().replace(".vue","").replace("/",""));
                api.setComponent("."+api.getPath());
                api.setName(file.getName().replace(".vue",""));
                System.err.println(JSONUtil.toJsonStr(api));
                Entity e = Db.use(dsc).get(resourcesTable, "code", api.getCode());
                if (null==e){
                    Db.use(dsc).insert(Entity.parse(api,true,true).setTableName(resourcesTable));
                }else {
                    api.setName(null);
                    Db.use(dsc).update(Entity.parse(api,true,true).setTableName(resourcesTable),Entity.create().set("code",api.getCode()));
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static VueBean scanVue(String vuePath,String name) throws IOException {
        File file = new File(vuePath);
        String body = FileUtil.readString(file, StandardCharsets.UTF_8);
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
            String title = JSONUtil.getByPath(jsonObj, "name").toString();
            auth.add(new VueAuth()
                    .setCode(MD5.create().digestHex16(name+title))
                    .setName(title)
                    .setApis(jsonObj.getByPath("apis", List.class))
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
        for (VueAuth vueAuth : auth) {
            apis.removeAll(vueAuth.getApis());
        }
        return new VueBean()
                .setPath(vuePath.replace(".vue",""))
                .setApis(JSONUtil.toJsonStr(apis))
                .setPermissions(JSONUtil.toJsonStr(auth));
    }
}
