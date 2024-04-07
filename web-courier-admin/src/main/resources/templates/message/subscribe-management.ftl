<!DOCTYPE html>
<html>
<head>
    <#import "../common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/css/dataTables.bootstrap.min.css">
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/bootstrap-daterangepicker/daterangepicker.css">
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/message/css/messageCommon.css">
    <title>消息管理中心</title>
</head>
<body class="hold-transition skin-blue sidebar-mini">
<div class="wrapper">
    <@netCommon.commonHeader />
    <@netCommon.commonLeft "service-management" />

    <div class="content-wrapper">
        <section class="content-header">
            <h1>订阅管理</h1>
        </section>

        <section class="content">
            <div class="row">

                <div class="col-xs-2">
                    <div class="input-group">
                        <input type="text" class="form-control" id="serviceName" autocomplete="on"
                               placeholder= "服务名"  <#if serviceName??>value="${serviceName}"></#if>


                    </div>
                </div>

                <div class="col-xs-1">
                    <button class="btn btn-block btn-info" id="searchBtn" >搜索</button>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <div class="box">
                        <div class="box-body">
                            <table id="subscribe-service-list" class="table table-bordered table-striped"  >
                                <thead>
                                <tr>
                                    <th name="service" >订阅服务</th>
                                    <th name="mode" style="overflow: hidden;text-overflow: ellipsis;white-space: nowrap">订阅类型</th>
                                    <th name="topic" >Topic</th>
                                    <th name="groupId" >消费组</th>
                                    <th name="ipAndPort" style="overflow: hidden;text-overflow: ellipsis;white-space: nowrap">在线机器地址</th>
<#--                                    <th name="consumerNode" style="overflow: hidden;text-overflow: ellipsis;white-space: nowrap">指定消费地址</th>-->
<#--                                    <th name="operate" >操作</th>-->
                                    <th name="type" >消息类型</th>
                                    <th name="url" >推送url</th>
                                </tr>
                                </thead>
                                <tbody>
                                <#if subscribes?? && subscribes?size gt 0>
                                    <#list subscribes as subscribe>
                                        <tr>
                                            <td > <span class="badge bg-teal" title="${subscribe.service}">${subscribe.service}</span></td>
                                            <td ><span class="badge bg-purple" title="${subscribe.mode}">${subscribe.mode}</span></td>
                                            <td ><#if subscribe.topics?? && subscribe.topics?size gt 0>
                                                    <#list subscribe.topics as topic>
                                                        <span class="badge bg-aqua" title="${topic}">
                                                            ${topic}
                                                        </span>
                                                        <br>
                                                    </#list>
                                                </#if>
                                            </td>
                                            <td ><#if subscribe.groupIds?? && subscribe.groupIds?size gt 0>
                                                    <#list subscribe.groupIds as groupId>
                                                        <span class="badge bg-blue" title="${groupId}">
                                                            ${groupId}
                                                        </span>
                                                        <br>
                                                    </#list>
                                                </#if>
                                            </td>
                                            <td ><#if subscribe.ipAndPort?? && subscribe.ipAndPort?size gt 0>
                                                    <#list subscribe.ipAndPort as ipport>
                                                        <span class="badge bg-green" title="${ipport}">
                                                            ${ipport}
                                                        </span>
                                                        <br>
                                                    </#list>
                                                </#if>
                                            </td>
<#--                                            <td ><#if subscribe.consumerNode?? && subscribe.consumerNode != 'all'>-->
<#--                                                    <span class="badge bg-orange" title="${subscribe.consumerNode}">-->
<#--                                                         ${subscribe.consumerNode}-->
<#--                                                    </span>-->
<#--                                                    <br>-->
<#--                                                </#if>-->
<#--                                            </td>-->
<#--                                            <td style="overflow: hidden;text-overflow: ellipsis;white-space: nowrap">-->
<#--                                                <button class="btn btn-warning btn-xs bind"-->
<#--                                                        service="${subscribe.service}"-->
<#--                                                        mode="${subscribe.mode.name()}"-->
<#--                                                        ipAndPort="<#if subscribe.ipAndPort?? && subscribe.ipAndPort?size gt 0>-->
<#--                                                                <#list subscribe.ipAndPort as ip>-->
<#--                                                                ${ip + ","}-->
<#--                                                            </#list>-->
<#--                                                </#if>">指定节点消费-->
<#--                                                </button>-->
<#--                                                <button class="btn btn-danger btn-xs unsubscribe" id="unsubscribe"-->
<#--                                                        service="${subscribe.service}"-->
<#--                                                        mode="${subscribe.mode.name()}"-->
<#--                                                >服务下线</button>-->
<#--                                            </td>-->
                                            <td ><#if subscribe.types?? && subscribe.types?size gt 0>
                                                    <#list subscribe.types as type>
                                                        <span  title="${type}">
                                                            ${type}
                                                        </span>
                                                        <br>
                                                    </#list>
                                                </#if>
                                            </td>
                                            <td >${subscribe.url}</td>
                                        </tr>
                                    </#list>
                                </#if>

                                </tbody>
                                <tfoot></tfoot>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </div>

    <!-- 更新.模态框 -->
    <div class="modal fade" id="updateModal" tabindex="-1" role="dialog" aria-hidden="true">
        <div class="modal-dialog ">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title">指定消费节点</h4>
                </div>
                <div class="modal-body">

                    <form class="form-horizontal form" role="form">
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">服务名</label>
                            <div class="col-sm-10"><input type="text" class="form-control" name="service"  ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">机器地址</label>
                            <div class="col-sm-10">
                                <select id="ipAndPorts" class="form-control" name="consumerNode">

                                </select>
                            </div>
                        </div>
                        <hr>
                        <div class="form-group">
                            <div class="col-sm-offset-3 col-sm-6">
                                <button type="button" class="btn btn-primary sava">保存</button>
                                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<@netCommon.commonScript />
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net/js/jquery.dataTables.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/js/dataTables.bootstrap.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/moment/moment.min.js"></script>
<script src="${request.contextPath}/static/js/subscribe-management.js"></script>
</body>
</html>
