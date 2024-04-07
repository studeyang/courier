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
    <@netCommon.commonLeft "send-fail" />

    <div class="content-wrapper">
        <section class="content-header">
            <h1>消息发送失败</h1>
        </section>

        <section class="content">
            <div class="row">
                <div class="col-xs-2">
                    <div class="input-group">
                        <input type="text" class="form-control" id="messageId" autocomplete="on" placeholder="消息ID"/>
                    </div>
                </div>
                <div class="col-xs-2">
                    <div class="input-group">
                        <input type="text" class="form-control" id="event" autocomplete="on" placeholder="消息类型">
                    </div>
                </div>
                <div class="col-xs-2">
                    <div class="input-group">
                        <input type="text" class="form-control" id="content" autocomplete="on" placeholder="消息内容(模糊)">
                    </div>
                </div>
                <div class="col-xs-2">
                    <div class="input-group">
                        <input type="text" class="form-control" id="fromService" autocomplete="on" placeholder="发送服务">
                    </div>
                </div>
                <div class="col-xs-3">
                    <div class="input-group">
                        <span class="input-group-addon">发送时间</span>
                        <input type="text" class="form-control" id="filterTime" readonly>
                    </div>
                </div>
                <div class="col-xs-1">
                    <button class="btn btn-block btn-info" id="searchBtn">搜索</button>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <div class="box">
                        <div class="box-body">
                            <table id="send-fail-list" class="table table-bordered table-striped" width="100%">
                                <thead>
                                <tr>
                                    <th name="messageId">消息ID</th>
                                    <th name="type">消息类型</th>
                                    <th name="content">消息内容</th>
                                    <th name="service">发送服务</th>
                                    <th name="topic">Topic</th>
                                    <th name="createdAt">发送时间</th>
                                    <th name="reason">失败原因</th>
                                    <th name="operation">操作</th>
                                </tr>
                                </thead>
                                <tbody></tbody>
                                <tfoot></tfoot>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </div>
</div>

<@netCommon.commonScript />
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net/js/jquery.dataTables.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/js/dataTables.bootstrap.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/moment/moment.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/bootstrap-daterangepicker/daterangepicker.js"></script>
<script src="${request.contextPath}/static/js/send-fail.js"></script>
</body>
</html>
