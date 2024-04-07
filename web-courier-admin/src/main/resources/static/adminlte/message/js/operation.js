$(function () {

    var rangesConf = {};
    rangesConf['今日'] = [moment().startOf('day'), moment().endOf('day')];
    rangesConf['昨日'] = [moment().subtract(1, 'days').startOf('day'), moment().subtract(1, 'days').endOf('day')];

    $('#filterTime').daterangepicker({
        autoApply: false,
        singleDatePicker: true,
        showDropdowns: false,        // 是否显示年月选择条件
        timePicker: false, 			// 是否显示小时和分钟选择条件
        timePickerIncrement: 10, 	// 时间的增量，单位为分钟
        timePicker24Hour: false,
        opens: 'left', //日期选择框的弹出位置
        ranges: rangesConf,
        locale: {
            format: 'YYYY-MM-DD',
            separator: ' - ',
            customRangeLabel: '自定义',
            applyLabel: '确定',
            cancelLabel: '取消',
            fromLabel: '起始时间',
            toLabel: '结束时间',
            daysOfWeek: '日,一,二,三,四,五,六'.split(','),        // '日', '一', '二', '三', '四', '五', '六'
            monthNames: '一月,二月,三月,四月,五月,六月,七月,八月,九月,十月,十一月,十二月'.split(','),        // '一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'
            firstDay: 1
        },
        startDate:
            $("#consumeTime").val() > 0
                ? moment(new Date(parseInt($("#consumeTime").val()))).startOf('day')
                : rangesConf['今日'][0],
        endDate:
            $("#consumeTime").val() > 0
                ? moment(new Date(parseInt($("#consumeTime").val()))).endOf('day')
                : rangesConf['今日'][1]
    });

    $("#send-btn").on('click', function () {
        layer.confirm('是否确认发送这些消息', {
            icon: 7,
            title: "提示",
            btn: ['确认', '取消']
        }, function () {
            sendSubmit();
        }, function () {
            layer.closeAll();
        });
    });

    $("#consume-btn").on('click', function () {
        layer.confirm('是否确认消费这些消息', {
            icon: 7,
            title: "提示",
            btn: ['确认', '取消']
        }, function () {
            consumeSubmit();
        }, function () {
            layer.closeAll();
        });
    });

    $("#send-reset").on('click', function () {
        $("#msg-input").text("");
        $("#msg-result>.success-area,#msg-result>.fail-area").text("");
    });

    $("#consume-reset").on('click', function () {
        $("#consume-input").text("");
        $("#consume-result>.success-area,#consume-result>.fail-area").text("");
    });
});

function sendSubmit() {

    layer.closeAll();

    $("#msg-result>.success-area,#msg-result>.fail-area").text("");

    var messages = $("#msg-input").text();

    if (messages == "" || null == messages) {
        layerAlertTip("消息Id不能为空");
        return false;
    }

    var params = messages.split(",");
    var date = $('#filterTime').val().split(" ")[0];
    var request = {
        'ids':params,
        'startTime':date+" 00:00:00",
        'endTime':date+" 23:59:59"
    }
    layer.load();

    $.ajax({
        type: 'POST',
        url: base_url + "/resend",
        data:JSON.stringify(request),
        dataType: "json",
        contentType: "application/json",
        success: function (data) {
            layer.closeAll();
            if (data.code == 200 && data.data != null) {

                var results = data.data;

                var failHtml = "";

                var successCount = 0;

                var failCount = 0;

                for (var i = 0; i < results.length; i++) {

                    var result = results[i];

                    if (result.success) {

                        successCount++;
                    } else {
                        failCount++;

                        failHtml += "<p><span class='result-id' '>id:" + result.messageId + "; </span><span class='reason'>原因：</p>" +
                            "<p><span>" + result.reason + "</span></p>\n";
                    }
                }

                $("#msg-result>.success-area").append('<p>消息发送成功条数：<span style="color: #e22e33">' + successCount + '</span></p>');

                if (failCount != 0) {
                    var pHtml = '<div><p>消息发送失败条数：<span style="color: #e22e33">' + failCount + '</span>；具体信息如下：</p></div>';
                    $("#msg-result>.fail-area").append(pHtml);
                    $("#msg-result>.fail-area>div").append(failHtml);
                }

                layer.msg("消息发送完成", {time: 1000});

            } else {
                layer.msg(data.errorMessage, {time: 1000});
            }
        },
        error: function () {
            layer.closeAll();
            layer.msg("消息发送异常", {time: 1000});
        }
    });
}

function consumeSubmit() {

    layer.closeAll();

    $("#consume-result>.success-area,#consume-result>.fail-area").text("");

    var consumes = $("#consume-input").text();

    if (consumes == "" || null == consumes) {
        layerAlertTip("消费Id不能为空");
        return false;
    }

    var params = consumes.split(",");
    var date = $('#filterTime').val().split(" ")[0];
    var request = {
        'ids':params,
        'startTime':date+" 00:00:00",
        'endTime':date+" 23:59:59"
    }
    layer.load();

    $.ajax({
        type: 'POST',
        url: base_url + "/reconsume",
        data: JSON.stringify(request),
        dataType: "json",
        contentType: "application/json",
        success: function (data) {
            layer.closeAll();
            if (data.code == 200 && data.data != null) {

                var results = data.data;

                var failHtml = "";

                var successCount = 0;

                var failCount = 0;

                for (var i = 0; i < results.length; i++) {

                    var result = results[i];

                    if (result.success) {

                        successCount++;
                    } else {
                        failCount++;

                        failHtml += "<p><span class='result-id' '>id:" + result.id + "; </span><span class='reason'>原因：</p>" +
                            "<p><span>" + result.reason + "</span></p>\n";
                    }
                }

                $("#consume-result>.success-area").append('<p>消息消费成功条数：<span style="color: #e22e33">' + successCount + '</span></p>');

                if (failCount != 0) {
                    var pHtml = '<div><p>消息消费失败条数：<span style="color: #e22e33">' + failCount + '</span>；具体信息如下：</p></div>';
                    $("#consume-result>.fail-area").append(pHtml);
                    $("#consume-result>.fail-area>div").append(failHtml);
                }

                layer.msg("消息消费完成", {time: 1000});

            } else {
                layer.msg(data.errorMessage, {time: 1000});
            }
        },
        error: function () {
            layer.closeAll();
            layer.msg("消息消费异常", {time: 1000});
        }
    });
}

function layerAlertTip(tip) {
    layer.msg(tip, {time: 1000});
}