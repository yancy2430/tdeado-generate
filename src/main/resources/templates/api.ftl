import request from '@/utils/request'
import {serviceUrl} from '@/utils/request'

<#list api.getList() as apiMethodDoc>

/**
 * ${apiMethodDoc.detail}
<#if (apiMethodDoc.queryParams?size>0)>
 * @param queryParams 参数对象
</#if>
<#if (apiMethodDoc.requestParams?size>0)>
 * @param data body对象
</#if>
<#if (apiMethodDoc.queryParams?size>0)>
 * @param options ajax其他参数
</#if>
*/
export function ${apiMethodDoc.getName()} (<#if (apiMethodDoc.queryParams?size>0)>queryParams,</#if><#if (apiMethodDoc.requestParams?size>0)>data,</#if>options) {
    <#if (apiMethodDoc.contentType=="multipart/form-data")>
        const formData = new FormData()
        <#list apiMethodDoc.queryParams as param>
            formData.append('${param.field}',queryParams.${param.field});
        </#list>
    </#if>
    return request(Object.assign({
    url: serviceUrl+'${apiMethodDoc.path}',
    method: 'POST',
    <#if ((apiMethodDoc.contentType=="multipart/form-data"))>
        data: formData,
    </#if>
    <#if (apiMethodDoc.requestParams?size>0) && (apiMethodDoc.contentType!="multipart/form-data")>
        data: JSON.stringify(data),
    </#if>
    <#if (apiMethodDoc.queryParams?size>0) && (apiMethodDoc.contentType!="multipart/form-data")>
        params:queryParams,
    </#if>
    headers: {
    'Content-Type': '${apiMethodDoc.contentType}'
    }
    },options))
}
</#list>
