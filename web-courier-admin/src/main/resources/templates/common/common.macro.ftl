<#macro commonStyle>
    <link rel="icon" href="${request.contextPath}/static/favicon.ico"/>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/font-awesome/css/font-awesome.min.css">
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/Ionicons/css/ionicons.min.css">
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/dist/css/AdminLTE.min.css">
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/dist/css/skins/_all-skins.min.css">
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/PACE/themes/blue/pace-theme-flash.css">
    <script src="${request.contextPath}/static/adminlte/other/html5shiv.min.js"></script>
    <script src="${request.contextPath}/static/adminlte/other/respond.min.js"></script>
</#macro>

<#macro commonScript>
    <script src="${request.contextPath}/static/adminlte/bower_components/jquery/jquery.min.js"></script>
    <script src="${request.contextPath}/static/adminlte/bower_components/bootstrap/js/bootstrap.min.js"></script>
    <script src="${request.contextPath}/static/adminlte/bower_components/fastclick/fastclick.js"></script>
    <script src="${request.contextPath}/static/adminlte/dist/js/adminlte.min.js"></script>
    <script src="${request.contextPath}/static/adminlte/bower_components/jquery-slimscroll/jquery.slimscroll.min.js"></script>
    <script src="${request.contextPath}/static/adminlte/bower_components/PACE/pace.min.js"></script>
    <script src="${request.contextPath}/static/plugins/jquery/jquery.cookie.js"></script>
    <script src="${request.contextPath}/static/plugins/jquery/jquery.validate.min.js"></script>
    <script src="${request.contextPath}/static/plugins/layer/layer.js"></script>
    <script src="${request.contextPath}/static/js/common.js"></script>
    <script>
        var base_url = '${request.contextPath}';
    </script>
</#macro>

<#macro commonHeader>
    <header class="main-header">
        <a href="${request.contextPath}/" class="logo">
            <span class="logo-mini"><b>Courier</b></span>
            <span class="logo-lg"><b>消息管理中心</b></span>
        </a>
        <nav class="navbar navbar-static-top" role="navigation">
            <a href="#" class="sidebar-toggle" data-toggle="push-menu" role="button">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </a>
        </nav>
    </header>
</#macro>

<#macro commonLeft pageName >
    <aside class="main-sidebar">
        <section class="sidebar">
            <ul class="sidebar-menu">
                <li class="header">导航</li>
                <li class="nav-click <#if pageName == "index">active</#if>">
                    <a href="${request.contextPath}/">
                        <i class="fa fa-circle-o text-aqua"></i>
                        <span>消息报表</span>
                    </a>
                </li>
                <li class="nav-click <#if pageName == "send-success">active</#if>">
                    <a href="${request.contextPath}/send">
                        <i class="fa fa-circle-o text-yellow"></i>
                        <span>发送成功</span>
                    </a>
                </li>
                <li class="nav-click <#if pageName == "send-fail">active</#if>">
                    <a href="${request.contextPath}/send/fail">
                        <i class="fa fa-circle-o text-gray"></i>
                        <span>发送失败</span>
                    </a>
                </li>
                <li class="nav-click <#if pageName == "consume-success">active</#if>">
                    <a href="${request.contextPath}/consume">
                        <i class="fa fa-circle-o text-green"></i>
                        <span>消费成功</span>
                    </a>
                </li>
                <li class="nav-click <#if pageName == "consume-fail">active</#if>">
                    <a href="${request.contextPath}/consume/fail">
                        <i class="fa fa-circle-o text-purple"></i>
                        <span>消费失败</span>
                    </a>
                </li>
                <li class="nav-click <#if pageName == "operation">active</#if>">
                    <a href="${request.contextPath}/operations">
                        <i class="fa fa-circle-o text-blue"></i>
                        <span>消息重试</span>
                    </a>
                </li>
                <li class="nav-click <#if pageName == "service-management">active</#if>">
                    <a href="${request.contextPath}/subscribe/management">
                        <i class="fa fa-circle-o text-yellow"></i>
                        <span>订阅管理</span>
                    </a>
                </li>
            </ul>
        </section>
    </aside>
</#macro>