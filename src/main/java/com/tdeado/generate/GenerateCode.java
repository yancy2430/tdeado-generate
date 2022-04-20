package com.tdeado.generate;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 生成DAO代码
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
     * @parameter expression="${jdbcUrl}"
     */
    private String jdbcUrl;
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
     * @parameter expression="${vuePath}"
     */
    private String vuePath;
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



    @Override
    public void execute() {
            String tables = scanner("输入表名 多个用,分割 ,所有输入 all");
            if (tables.equals("all")) {
                try {
                    for (String tableName : getTableNames()) {
                        if (tableName.startsWith(tablePrefix+artifactId)){
                            init(tableName);
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }else {
                for (String s : tables.split(",")) {
                    init(s);
                }
            }
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
        } catch (SQLException e) {
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

    public void init(String name) {
        System.err.println("开始生成 "+name);
        AutoGenerator mpg = new AutoGenerator();
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        gc.setOutputDir(basedir + "/src/main/java");
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
        pc.setParent(groupId+"."+artifactId);
        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
                Map<String, Object> map = new HashMap<>();
                map.put("moduleName", artifactId.replace("-",""));
                map.put("responseResultClass",responseResultClass);
                String[] r = responseResultClass.split("\\.");
                if (r.length>0) {
                    map.put("responseResult",r[r.length-1]);
                }

                this.setMap(map);
            }
        };
        List<FileOutConfig> focList = new ArrayList<FileOutConfig>();
        focList.add(new FileOutConfig("/templates/mapper.xml.ftl") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输入文件名称
                return basedir + "/src/main/resources/mapper/" + tableInfo.getEntityName() + "Mapper.xml";
            }
        });
        focList.add(new FileOutConfig("/templates/vue.ftl") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return vuePath+artifactId.toLowerCase()+"/"+tableInfo.getEntityPath().replace(cfg.getMap().get("moduleName").toString(), "")+".vue";
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
    }
    public static void initVue(){

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
        dsc.setUrl(jdbcUrl+"?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&useSSL=false&useLocalSessionState=true&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true&tinyInt1isBit=false");
        // dsc.setSchemaName("public");
        dsc.setDriverName(driverName);
        dsc.setUsername(username);
        dsc.setPassword(password);
        return dsc;
    }
}
