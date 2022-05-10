<template>
    <section>
        <td-table
                title="${table.comment!}"
                ref="table"
                :data-source="parameter=>${table.entityName?uncap_first}Page(parameter)">
            <template #Search="{formState}">
                <#list table.fields as field>
                    <a-form-item label="${field.comment}" name="${field.propertyName}">
                        <#if field.propertyType=='Long' || field.propertyType=='Integer'>
                            <a-input-number v-model:value="formState.${field.propertyName}"/>
                        <#else>
                            <a-input v-model:value="formState.${field.propertyName}"/>
                        </#if>
                    </a-form-item>
                </#list>
            </template>
            <template #Columns>
                <#list table.fields as field>
                    <a-table-column title="${field.comment}" data-index="${field.propertyName}" align="center" <#if field.propertyType=='Long' || field.propertyType=='Integer'>:sorter="true"</#if> />
                </#list>
            </template>
        </td-table>
    </section>
</template>

<script>
    import TdTable from "@/components/TdTable/TdTable";
    import {${table.entityName?uncap_first}Page} from "@/api/${table.entityName}";

    export default {
        name: "${table.entityName}",
        components: {TdTable},
        data() {
            return {
                ${table.entityName?uncap_first}Page: ${table.entityName?uncap_first}Page
            }
        },
        created() {
        },
        methods: {}
    }
</script>

<style scoped>

</style>