package ${package.Controller};

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tdeado.core.api.R;
import com.tdeado.core.controller.BaseController;
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
    public R<${entity}> getById(@RequestParam("id") String id){
        return success(${table.entityPath}Service.getById(id));
    }

    /**
     * 保存或更新${table.comment!}
     * @param ${table.entityPath} 保存数据
     * @return 返回结果
     */
    @PostMapping("save")
    public R<Boolean> save(@RequestBody ${entity} ${table.entityPath}){
        return success(${table.entityPath}Service.saveOrUpdate(${table.entityPath}));
    }
    /**
     * 删除${table.comment!}
     * @param id 数据ID
     * @return 返回结果
     */
    @RequestMapping("removeById")
    public R<Boolean> removeById(@RequestParam String id){
        return success(${table.entityPath}Service.removeById(id));
    }

    /**
     * ${table.comment!}列表
     * @param page 页码
     * @param size 大小
     * @param ${table.entityPath} 参数实体
     * @return 返回结果
     */
    @RequestMapping("page")
    public R<IPage<${entity}>> page(
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size,
            ${entity} ${table.entityPath}
    ) {
        IPage<${entity}> pageData = ${table.entityPath}Service.page(new Page<>(page, size), new QueryWrapper<>(${table.entityPath}));
        return success(pageData);
    }

}
</#if>
