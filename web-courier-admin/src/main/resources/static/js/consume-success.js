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
            $("#createAt").val() > 0
                ? moment(new Date(parseInt($("#createAt").val()))).startOf('day')
                : rangesConf['今日'][0],
        endDate:
            $("#createAt").val() > 0
                ? moment(new Date(parseInt($("#createAt").val()))).endOf('day')
                : rangesConf['今日'][1]
    });

    var logTable = $("#consume-success-list").dataTable({
        "deferRender": true,
        "processing": true,
        "serverSide": true,
        "ajax": {
            url: base_url + "/pageList/consume/success",
            type: "post",
            data: function (d) {
                var request = {};
                request.consumeId = $.trim($('#consumeId').val());
                request.messageId = $.trim($('#messageId').val());
                request.fromService = $.trim($('#fromService').val());
                request.toService = $.trim($('#toService').val());
                request.startTime = $('#filterTime').val().split(" ")[0]+" 00:00:00";
                request.endTime = $('#filterTime').val().split(" ")[0]+" 23:59:59";
                request.start = d.start;
                request.length = d.length;
                return request;
            }
        },
        "searching": false,
        "ordering": false,
        "columns": [
            {
                "data": 'id',
                "width": '10%'
            },
            {
                "data": 'messageId',
                "render": function (data, type, row) {
                    return '<a href="/courier-admin/send?messageId=' + data + '&consumeTime=' + new Date(row.pollTime).getTime() + '">'
                        + data
                        + '</a>';
                },
                "width": '10%'
            },
            {
                "data": 'fromService',
                "width": '10%'
            },
            {
                "data": 'toService',
                "width": '10%'
            },
            {
                "data": 'topic',
                "width": '12%'
            },
            {
                "data": 'groupId',
                "width": '12%'
            },
            {
                "data": 'state',
                "width": '5%',
                "render": function (data, type, row) {
                    if (data === 'HANDLED') {
                        return '<span style="color: #32d24e;">已消费</span>'
                    }
                    if (data === 'COMMITED') {
                        return '<span style="color: #f59c4b;; font-weight: bold;">消费中</span>';
                    }
                    if (data === 'PUSH_FAIL') {
                        return '<span style="color: red;">推送失败</span>'
                    }
                    if (data === 'HANDLE_FAIL') {
                        return '<span style="color: red;">消费失败</span>'
                    }
                    return '<span style="color: darkgray;">未知</span>'
                }
            },
            {
                "data": 'clientReceiveTime',
                "width": '12%',
                "render": function (data, type, row) {

                    var detail = '';
                    detail += 'kafka 拉取时间&emsp;&emsp;&thinsp;&thinsp;：' + formatTime(row.pollTime);
                    detail += '<br>开始推送时间&emsp;&emsp;&emsp;：' + formatTime(row.beforePushTime);
                    detail += '<br>结束推送时间&emsp;&emsp;&emsp;：' + formatTime(row.endPushTime);
                    detail += '<br>客户端开始消费时间：' + formatTime(row.clientReceiveTime);
                    detail += '<br>客户端结束消费时间：' + formatTime(row.clientEndTime);
                    detail += '<br>消费耗时&emsp;&emsp;&emsp;&emsp;&emsp;：' + row.clientHandledCost + '&ensp;ms';

                    return '<a class="logTips" href="javascript:;">'
                        + formatTime(row.clientReceiveTime)
                        + '<span style="display:none;">' + detail + '</span></a>';
                }
            },
            {
                "data": 'id',
                "render": function (data, type, row) {
                    return data ? '<a class="msg-operation-btn msg-reconsume-btn" consumeid = "' + data + '">再消费</a>' : "";
                },
                "width": '5%'
            }
        ],
        "language": {
            "sProcessing": '处理中...',
            "sLengthMenu": '每页 _MENU_ 条记录',
            "sZeroRecords": '没有匹配结果',
            "sInfo": '第 _PAGE_ 页 ( 总共 _PAGES_ 页，_TOTAL_ 条记录 )',
            "sInfoEmpty": '无记录',
            "sInfoFiltered": '(由 _MAX_ 项结果过滤)',
            "sInfoPostFix": "",
            "sSearch": '搜索',
            "sUrl": "",
            "sEmptyTable": '表中数据为空',
            "sLoadingRecords": '载入中...',
            "sInfoThousands": ",",
            "oPaginate": {
                "sFirst": '首页',
                "sPrevious": '上页',
                "sNext": '下页',
                "sLast": '末页'
            },
            "oAria": {
                "sSortAscending": ': 以升序排列此列',
                "sSortDescending": '以降序排列此列'
            }
        },
        "fnDrawCallback": function () {
            $(".msg-reconsume-btn").on('click', function () {

                var consumeId = $(this).attr("consumeid");

                layer.confirm('是否确认消费这条消息', {
                    icon: 7,
                    title: "提示",
                    btn: ['确认', '取消']
                }, function () {
                    var param = {
                        'ids':[consumeId],
                        'startTime':$.trim($('#filterTime').val().split(" ")[0])+" 00:00:00",
                        'endTime':$.trim($('#filterTime').val().split(" ")[0])+" 23:59:59"
                    };
                    submit(param);
                }, function () {
                    layer.closeAll();
                });

            });
        }
    });

    $('#consume-success-list').on('click', '.logTips', function () {
        var msg = $(this).find('span').html();
        ComAlertTec.show(msg);
    });

    $('#searchBtn').on('click', function () {
        logTable.fnDraw();
    });

    $(".input-group input").keypress(function (even) {
        if (even.which == 13) {
            logTable.fnDraw();
        }
    });
});

var ComAlertTec = {
    html: function () {
        var html =
            '<div class="modal fade" id="ComAlertTec" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">' +
            '	<div class="modal-dialog modal-lg-">' +
            '		<div class="modal-content-tec">' +
            '			<div class="modal-body">' +
            '				<div class="alert alert-tail" style="padding-left: 180px;">' +
            '				</div>' +
            '			</div>' +
            '				<div class="modal-footer">' +
            '				<div class="text-center" >' +
            '					<button type="button" class="btn btn-info ok" data-dismiss="modal">确定</button>' +
            '				</div>' +
            '			</div>' +
            '		</div>' +
            '	</div>' +
            '</div>';
        return html;
    },
    show: function (msg, callback) {
        if ($('#ComAlertTec').length == 0) {
            $('body').append(ComAlertTec.html());
        }
        $('#ComAlertTec .alert').html(msg);
        $('#ComAlertTec').modal('show');

        $('#ComAlertTec .ok').click(function () {
            $('#ComAlertTec').modal('hide');
            if (typeof callback == 'function') {
                callback();
            }
        });
    }
};

function submit(param) {

    layer.load();

    $.ajax({
        type: 'POST',
        url: base_url + "/reconsume",
        data: JSON.stringify(param),
        dataType: "json",
        contentType: "application/json",
        success: function (data) {
            layer.closeAll();
            if (data.code == 200 && data.data != null) {

                var result = data.data[0];

                if (!result.success) {
                    layer.msg("消息消费失败,原因：" + result.reason, {time: 2000});
                } else {
                    layer.msg("消息消费成功", {time: 1000}, function () {
                        window.location.reload();
                    });
                }
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

function formatTime(time) {
    return time ? moment(new Date(time)).format("YYYY-MM-DD HH:mm:ss.SSS") : '无';
}

function queryMessage(a) {
    let messageId = $(a).html();
    let time = $(a).parents("tr").children('td:eq(7)').children('a').data('time');
    window.location = "/courier-admin/send?messageId=" + messageId + "&consumeTime=" + moment(new Date(time));
}