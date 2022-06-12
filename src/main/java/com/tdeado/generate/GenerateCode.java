package com.tdeado.generate;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import freemarker.core.ParseException;
import freemarker.template.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.jsoup.Jsoup;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 生成DAO代码
 * @aggregator true
 * @inheritByDefault false
 * @goal code
 */
public class GenerateCode extends AbstractMojo {
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
     * @parameter expression="${driverName}"
     */
    private String driverName;
    /**
     * @parameter expression="${tablePrefix}"
     */
    private String tablePrefix;
    /**
     * @parameter expression="${author}"
     */
    private String author;
    /**
     * @parameter expression="${uiPath}"
     */
    private String uiPath;
    /**
     * @parameter expression="${superControllerClass}"
     */
    private String superControllerClass;
    /**
     * @parameter expression="${superEntityClass}"
     */
    private String superEntityClass;
    /**
     * @parameter expression="${responseResultClass}"
     */
    private String responseResultClass;

    /**
     * @parameter expression="${multiModule}"
     */
    private Boolean multiModule;

    /**
     * @parameter expression="${main.basedir}"
     */
    private String parentBasedir;


    @Override
    public void execute() {
        StrUtil.blankToDefault(author, "");
        String module =null;
        if (multiModule){
            module = scanner("请输入模块名");
        }
        String tables = scanner("输入表名 多个用,分割 ,所有输入 all");
        if (tables.equals("all")) {
            try {
                for (String tableName : getTableNames()) {
                    if (tableName.startsWith(tablePrefix + artifactId)) {
                        init(module,tableName);
                    }
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else {
            for (String s : tables.split(",")) {
                init(module,s);
            }
        }
    }

    public Map<String, List<EnumField>> enumInfo(DataSourceConfig config, String table_name) throws SQLException {
        Connection conn = config.getConn();
        String sql= "SELECT column_name,data_type,column_type FROM information_schema.columns WHERE table_schema='" + schemaName + "' and table_name = '" + table_name + "' and data_type='enum';";
        System.err.println(sql);
        ResultSet res = conn.createStatement().executeQuery(sql);
        Map<String, List<EnumField>> enums = new HashMap<>();
        while (res.next()) {
            String string = res.getString("COLUMN_TYPE");
            System.err.println(string);
            string = string.replace("enum(", "").replace(")", "").replace("'", "");
            List<EnumField> value = new ArrayList<>();
            String[] s = string.split(",");
            for (int i = 1; i <= s.length; i++) {
                try {
                    EnumField field = new EnumField();
                    field.setLabel(s[i - 1]);
                    field.setValue(i);
                    field.setName(Jsoup.connect("https://www.chtml.cn/w?word=" + field.getLabel()).get().select(".list_box .list_table_info .code_name_line>a").get(2).attr("data-val").toUpperCase(Locale.ROOT));
                    value.add(field);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            enums.put(res.getString("COLUMN_NAME"), value);
        }
        res.close();
        conn.close();


        return enums;
    }

    /**
     * 获取数据库下的所有表名
     */
    public List<String> getTableNames() throws SQLException {
        List<String> tableNames = new ArrayList<>();
        Connection conn = mysqlDataSourceConfig().getConn();
        ResultSet rs = null;
        try {
            //获取数据库的元数据
            DatabaseMetaData db = conn.getMetaData();
            //从元数据中获取到所有的表名
            rs = db.getTables(null, null, null, new String[]{"TABLE"});
            while (rs.next()) {
                tableNames.add(rs.getString(3));
            }
        } catch (SQLException ignored) {
        } finally {
            try {
                rs.close();
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return tableNames;
    }

    private static boolean isNeedChange(String content) {
        return content.contains("_");
    }

    public static String underLineToCamel(String content, boolean firstUpperCase) {
        if (content == null || content.length() == 0) {
            return "";
        }

        if (!isNeedChange(content)) {
            return content;
        }

        String result = Stream.of(content.split("_")).map(m -> {
            String text = m;
            text = text.substring(0, 1).toUpperCase() + text.substring(1);
            return text;
        }).collect(Collectors.joining());
        if (firstUpperCase) {
            return result.substring(0, 1).toLowerCase() + result.substring(1);
        } else {
            return result;
        }
    }

    public void init(String modele,String name) {
        System.err.println("开始生成 " + name);
        AutoGenerator mpg = new AutoGenerator();
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        gc.setOutputDir(basedir +(StrUtil.isNotBlank(modele)?"/"+modele:"")+ "/src/main/java");
        gc.setAuthor(author);
        gc.setOpen(false);
        gc.setFileOverride(false);
//        gc.setEnableCache(true);
        mpg.setGlobalConfig(gc);

        // 数据源配置
        mpg.setDataSource(mysqlDataSourceConfig());

        // 包配置
        PackageConfig pc = new PackageConfig();
//        pc.setModuleName(artifactId.replace("-",""));
        if (StrUtil.isNotBlank(modele)) {
            pc.setModuleName(modele);
        }
        pc.setParent(groupId);
        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
                Map<String, Object> map = new HashMap<>();
                map.put("moduleName", artifactId.replace("-", ""));
                map.put("responseResultClass", responseResultClass);
                String[] r = responseResultClass.split("\\.");
                if (r.length > 0) {
                    map.put("responseResult", r[r.length - 1]);
                }

                this.setMap(map);
            }
        };
        List<FileOutConfig> focList = new ArrayList<FileOutConfig>();
        focList.add(new FileOutConfig("/templates/mapper.xml.ftl") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输入文件名称
                return basedir +(StrUtil.isNotBlank(modele)?"/"+modele:"")+ "/src/main/resources/mapper/" + tableInfo.getEntityName() + "Mapper.xml";
            }
        });
        focList.add(new FileOutConfig("/templates/vue.ftl") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return uiPath + "/src/views/" + modele+ "/" + tableInfo.getEntityName().replace(cfg.getMap().get("moduleName").toString(), "") + ".vue";
            }
        });
        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);
        mpg.setTemplate(new TemplateConfig()
                .setEntity("templates/entity.java")
                .setController("templates/controller.java")
                .setXml(null));
        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setRestControllerStyle(true);
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setTablePrefix(tablePrefix);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setEntityLombokModel(true);
        if (StrUtil.isNotBlank(superControllerClass)) {
            strategy.setSuperControllerClass(superControllerClass);
        }
        strategy.setInclude(name);
        if (StrUtil.isNotBlank(superEntityClass)) {
            strategy.setSuperEntityClass(superEntityClass);
        }

        strategy.setControllerMappingHyphenStyle(false);
        mpg.setStrategy(strategy);
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        mpg.execute();
        try {
            Map<String, List<EnumField>> enumInfo = enumInfo(mysqlDataSourceConfig(), name);
            System.err.println("开始生成 enumInfo {}" + enumInfo.entrySet().size());
            Configuration configuration = new Configuration(Configuration.getVersion());
            configuration.setClassForTemplateLoading(InitDoc.class, "/templates/");
            Template template = configuration.getTemplate("enum.ftl");
            name = underLineToCamel(name.replace(tablePrefix, ""), false);
            for (Map.Entry<String, List<EnumField>> stringListEntry : enumInfo.entrySet()) {
                HashMap<String, Object> model = new HashMap<>();
                model.put("name", name + StrUtil.upperFirst(stringListEntry.getKey()));
                model.put("package", pc.getParent() + ".enums");
                model.put("enums", stringListEntry.getValue());
                StringWriter result = new StringWriter(1024);
                template.process(model, result);
                String content = result.toString();
                InputStream inputStream = IOUtils.toInputStream(content, StandardCharsets.UTF_8);
                String d = basedir + (StrUtil.isNotBlank(modele) ? "/" + modele : "") + "/src/main/java/" + pc.getParent().replace(".", "/")+ "/enums/";
                FileUtil.mkdir(d);
                String path = d+ name + StrUtil.upperFirst(stringListEntry.getKey()) + ".java";
                //输出文件
                FileOutputStream fileOutputStream = new FileOutputStream(path);
                int copy = IOUtils.copy(inputStream, fileOutputStream);
            }
        } catch (SQLException | IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>
     * 读取控制台内容
     * </p>
     */
    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输入" + tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotEmpty(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }

    /**
     * MYSQL 数据库配置
     */
    public DataSourceConfig mysqlDataSourceConfig() {
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl("jdbc:mysql://" + host + "/" + schemaName + "?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&useSSL=false&useLocalSessionState=true&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true&tinyInt1isBit=false");
        dsc.setSchemaName(schemaName);
        dsc.setDriverName(StrUtil.blankToDefault(driverName, "com.mysql.cj.jdbc.Driver"));
        dsc.setUsername(username);
        dsc.setPassword(password);
        return dsc;
    }
}
