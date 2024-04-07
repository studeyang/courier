<!DOCTYPE html>
<html>
<head>
    <#import "../common/common.macro.ftl" as netCommon>
    <@netCommon.commonStyle />
    <!-- DataTables -->
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/css/dataTables.bootstrap.min.css">
    <title>任务调度中心</title>
</head>
<body class="hold-transition skin-blue sidebar-mini">
<div class="wrapper">
    <!-- header -->
    <@netCommon.commonHeader />
    <!-- left -->
    <@netCommon.commonLeft "alarm" />

    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <!-- Content Header (Page header) -->
        <section class="content-header">
            <h1>告警管理</h1>
        </section>

        <!-- Main content -->
        <section class="content">

            <div class="row">
                <div class="col-xs-12">
                    <div class="box">
                        <div class="box-header">
                            <h3 class="box-title">告警配置列表</h3>&nbsp;&nbsp;
                            <button class="btn btn-info btn-xs pull-left2 add">新增告警配置</button>
                        </div>
                        <div class="box-body">
                            <table id="alarm_list" class="table table-bordered table-striped display" width="100%">
                                <thead>
                                <tr>
                                    <#--<th name="id" >ID</th>-->
                                    <th name="id">编号</th>
                                    <th name="serviceEn">消费方名称</th>
                                    <th name="serviceCh">中文名称</th>
                                    <th name="owner">负责人</th>
                                    <th name="mobile">（钉钉）手机号</th>
                                    <th name="groupName">所属组</th>
                                    <th name="enabled">禁/启用</th>
                                    <th name="operate">操作</th>
                                </tr>
                                </thead>
                                <tbody>
                                <#if groups?? && groups?size gt 0>
                                    <#list groups as group>
                                        <tr>
                                            <td>${group.id}</td>
                                            <td>${group.serviceEn}</td>
                                            <td>${group.serviceCh}</td>
                                            <td>${group.owner}</td>
                                            <td>
                                                <#if group.mobile??>
                                                    <#list group.mobile?split(",") as item>
                                                        <span class="badge bg-blue">${item}</span>
                                                        <br>
                                                    </#list>
                                                </#if>
                                            </td>
                                            <td><#if group.groupName??>${group.groupName}<#else></#if></td>
                                            <td>
                                                <#if group.enabled>
                                                    <span class="badge bg-green">启用</span>
                                                <#else>
                                                    <span class="badge bg-red">禁用</span>
                                                </#if>
                                            </td>
                                            <td width='190px'>
                                                <button class="btn btn-warning btn-xs edit"
                                                        id="${group.id}"
                                                        serviceEn="${group.serviceEn}"
                                                        serviceCh="${group.serviceCh}"
                                                        owner="${group.owner}"
                                                        mobile="${group.mobile}"
                                                        groupName="<#if group.groupName??>${group.groupName}<#else></#if>">编辑
                                                </button>
                                                <button class="btn btn-danger btn-xs remove" id="${group.id}">删除</button>
                                                <button class="btn btn-primary btn-xs enable" id="${group.id}" enabled="${group.enabled?c}">
                                                    <#if group.enabled>禁用<#else>启用</#if>
                                                </button>
                                                <button class="btn btn-info btn-xs test" id="${group.id}" enabled="${group.enabled?c}">测试</button>
                                            </td>
                                        </tr>
                                    </#list>
                                </#if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </div>

    <!-- 新增.模态框 -->
    <div class="modal fade" id="addModal" tabindex="-1" role="dialog" aria-hidden="true">
        <div class="modal-dialog ">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title">新增告警配置</h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal form" role="form">
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">消费方<font color="red">*</font></label>
                            <div class="col-sm-10"><input type="text" class="form-control" name="serviceEn" placeholder="请输入消费方名称" maxlength="64"></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">中文名称<font color="red">*</font></label>
                            <div class="col-sm-10"><input type="text" class="form-control" name="serviceCh" placeholder="请输入中文名称" maxlength="50"></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">负责人<font color="red">*</font></label>
                            <div class="col-sm-10"><input type="text" class="form-control" name="owner" placeholder="请输入负责人" maxlength="50"></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">手机号<font color="red">*</font></label>
                            <div class="col-sm-10"><input type="text" class="form-control" name="mobile" placeholder="请输入手机号" maxlength="255"></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">所属组<font color="red">*</font></label>
                            <div class="col-sm-10">
                                <select class="form-control" name="groupName">
                                    <option value="会员组">会员组</option>
                                    <option value="订单组">订单组</option>
                                    <option value="组件组">组件组</option>
                                    <option value="数据组">数据组</option>
                                    <option value="备货组">备货组</option>
                                    <option value="询报价组">询报价组</option>
                                    <option value="支付结算组">支付结算组</option>
                                    <option value="风控金融组">风控金融组</option>
                                    <option value="商家服务组">商家服务组</option>
                                    <option value="其他">其他</option>
                                </select>
                            </div>
                        </div>
                        <hr>
                        <div class="form-group">
                            <div class="col-sm-offset-3 col-sm-6">
                                <button type="submit" class="btn btn-primary">保存</button>
                                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <!-- 更新.模态框 -->
    <div class="modal fade" id="updateModal" tabindex="-1" role="dialog" aria-hidden="true">
        <div class="modal-dialog ">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title">编辑执行器</h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal form" role="form">
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">消费方<font color="red">*</font></label>
                            <div class="col-sm-10"><input type="text" class="form-control" name="serviceEn" placeholder="请输入消费方名称" maxlength="64"></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">中文名称<font color="red">*</font></label>
                            <div class="col-sm-10"><input type="text" class="form-control" name="serviceCh" placeholder="请输入中文名称" maxlength="50"></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">负责人<font color="red">*</font></label>
                            <div class="col-sm-10"><input type="text" class="form-control" name="owner" placeholder="请输入负责人" maxlength="50"></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">手机号<font color="red">*</font></label>
                            <div class="col-sm-10"><input type="text" class="form-control" name="mobile" placeholder="请输入手机号" maxlength="255"></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">所属组<font color="red">*</font></label>
                            <div class="col-sm-10">
                                <select class="form-control" name="groupName">
                                    <option value="会员组">会员组</option>
                                    <option value="订单组">订单组</option>
                                    <option value="组件组">组件组</option>
                                    <option value="数据组">数据组</option>
                                    <option value="备货组">备货组</option>
                                    <option value="询报价组">询报价组</option>
                                    <option value="支付结算组">支付结算组</option>
                                    <option value="风控金融组">风控金融组</option>
                                    <option value="商家服务组">商家服务组</option>
                                    <option value="其他">其他</option>
                                </select>
                            </div>
                        </div>
                        <hr>
                        <div class="form-group">
                            <div class="col-sm-offset-3 col-sm-6">
                                <button type="submit" class="btn btn-primary">保存</button>
                                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                                <input type="hidden" name="id">
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

</div>

<@netCommon.commonScript />
<!-- DataTables -->
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net/js/jquery.dataTables.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/js/dataTables.bootstrap.min.js"></script>
<script src="${request.contextPath}/static/js/alarm.js"></script>
</body>
</html>