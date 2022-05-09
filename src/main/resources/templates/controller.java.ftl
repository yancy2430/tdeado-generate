package ${package.Controller};

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ${cfg.responseResultClass};
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import ${package.Controller?replace(".controller","")}.entity.${entity};
import ${package.Controller?replace(".controller","")}.service.I${entity}Service;
<#if restControllerStyle>
import org.springframework.web.bind.annotation.RestController;
<#else>
import org.springframework.stereotype.Controller;
</#if>
<#if superControllerClassPackage??>
import ${superControllerClassPackage};
</#if>

import java.util.List;

/**
 *
 * ${table.comment!}
 *
 * @author ${author}
 * @since ${date}
 */
<#if restControllerStyle>
@RestController
<#else>
@Controller
</#if>
@RequestMapping("<#if cfg.moduleName??>/${cfg.moduleName}/</#if><#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath?replace(cfg.moduleName,"")}</#if>")
<#if kotlin>
class ${table.controllerName}<#if superControllerClass??> : ${superControllerClass}()</#if>
<#else>
<#if superControllerClass??>
public class ${table.controllerName} extends ${superControllerClass} {
<#else>
public class ${table.controllerName} {
</#if>
    @Autowired
    I${entity}Service ${table.entityPath}Service;

    /**
    * 获取${table.comment!}
    * @param id 数据ID
    * @return 返回结果
    */
    @RequestMapping("getById")
    public ${cfg.responseResult}<${entity}> getById(@RequestParam("id") String id){
        return ${cfg.responseResult}.ok(${table.entityPath}Service.getById(id));
    }

    /**
     * 保存或更新${table.comment!}
     * @param ${table.entityPath} 保存数据
     * @return 返回结果
     */
    @PostMapping("save")
    public ${cfg.responseResult}<Boolean> save(@RequestBody ${entity} ${table.entityPath}){
        return ${cfg.responseResult}.ok(${table.entityPath}Service.saveOrUpdate(${table.entityPath}));
    }
    /**
     * 删除${table.comment!}
     * @param id 数据ID
     * @return 返回结果
     */
    @RequestMapping("removeById")
    public ${cfg.responseResult}<Boolean> removeById(@RequestParam String id){
        return ${cfg.responseResult}.ok(${table.entityPath}Service.removeById(id));
    }

    /**
     * ${table.comment!}列表
     * @param page 页码
     * @param size 大小
     * @param ${table.entityPath} 参数实体
     * @return 返回结果
     */
    @RequestMapping("page")
    public ${cfg.responseResult}<IPage<${entity}>> page(
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size,
            @RequestParam(value = "sorter",required = false) List<String> sorter,
            ${entity} ${table.entityPath}
    ) {
        QueryWrapper<${entity}> queryWrapper = new QueryWrapper<${entity}>(${table.entityPath});
        if (null!=sorter && sorter.size()==2){
            queryWrapper.orderBy(true,sorter.get(1).equals("asc"),sorter.get(0));
        }
        IPage<${entity}> pageData = ${table.entityPath}Service.page(new Page<${entity}>(page, size), queryWrapper);
        return ${cfg.responseResult}.ok(pageData);
    }

}
</#if>
