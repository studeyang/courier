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
    <@netCommon.commonLeft "operation" />

    <div class="content-wrapper" style="position: relative;">
        <section class="content-header">
            <h1>
                <div class="col-xs-3">
                    <div class="input-group">
                        <span class="input-group-addon">发送时间</span>
                        <input type="text" class="form-control" id="filterTime" readonly>
                    </div>
                </div>
                消息重试
            </h1>
        </section>

        <section class="ot-content">
            <div id="msg-input" class="message-area" contenteditable="true" placeholder="输入消息Id，用英文逗号分隔"></div>
            <div class="btn-area">
                <div class="btn-title">消息重新发送</div>
                <a id="send-btn">发送</a>
                <a id="send-reset">重置</a>
            </div>
            <div class="result-area" id="msg-result">
                <div class="success-area"></div>
                <div class="fail-area"></div>
            </div>
        </section>
        <section class="ot-content second">
            <div id="consume-input" class="message-area" contenteditable="true" placeholder="输入消费Id，用英文逗号分隔"></div>
            <div class="btn-area">
                <div class="btn-title">消息重新消费</div>
                <a id="consume-btn">消费</a>
                <a id="consume-reset">重置</a>
            </div>
            <div class="result-area" id="consume-result">
                <div class="success-area"></div>
                <div class="fail-area"></div>
            </div>
        </section>
    </div>
</div>

<@netCommon.commonScript />
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net/js/jquery.dataTables.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/js/dataTables.bootstrap.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/moment/moment.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/bootstrap-daterangepicker/daterangepicker.js"></script>
<script src="${request.contextPath}/static/adminlte/message/js/operation.js"></script>
</body>
</html>
