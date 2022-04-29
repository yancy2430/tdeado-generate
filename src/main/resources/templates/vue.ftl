<template>
    <section>
        <a-card size="small">
            <td-table
                    ref="table"
                    :data-source="parameter=>dataSource(parameter)">
                <template #Search="{formState}">
                    <#list table.fields as field>
                    <a-form-item label="${field.comment}" name="${field.propertyName}">
                        <#if field.propertyType=='Long' || field.propertyType=='Integer'>
                        <a-input-number v-model:value="formState.${field.propertyName}" />
                        <#else>
                        <a-input v-model:value="formState.${field.propertyName}" />
                        </#if>
                    </a-form-item>
                    </#list>
                </template>
                <template #Columns>
                    <#list table.fields as field>

                    <a-table-column title="${field.comment}" data-index="${field.propertyName}" <#if field.propertyType=='Long' || field.propertyType=='Integer'>:sorter="true"</#if> />
                    </#list>
                </template>
            </td-table>
        </a-card>
    </section>
</template>

<script>
    import TdTable from "../../components/TdTable/TdTable";
    export default {
        name: "List",
        components: {TdTable},
        data() {
            return {
            }
        },
        created() {
        },
        methods: {
            dataSource(params) {
                return  this.$request({
                    url: "/fresh/shopAppMenu/page",
                    method:"POST",
                    params: params
                })
            },
            handleResizeColumn: (w, col) => {
                col.width = w;
            }
        }
    }
</script>

<style scoped>

</style>