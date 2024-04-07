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
        startDate: rangesConf['今日'][0],
        endDate: rangesConf['今日'][1]
    });

    var logTable = $("#consume-fail-list").dataTable({
        "deferRender": true,
        "processing": true,
        "serverSide": true,
        "ajax": {
            url: base_url + "/pageList/consume/fail",
            type: "post",
            data: function (d) {
                var request = {};
                request.consumeId = $.trim($('#consumeId').val())
                request.messageId = $.trim($('#messageId').val());
                request.toService = $.trim($('#toService').val());
                request.startTime = $.trim($('#filterTime').val().split(" ")[0])+" 00:00:00";
                request.endTime = $.trim($('#filterTime').val().split(" ")[0])+" 23:59:59";
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
                    return '<a href="/courier-admin/send?messageId=' + data + '&consumeTime=' + new Date(row.createdAt).getTime() + '">'
                        + data
                        + '</a>';
                },
                "width": '10%'
            },
            {
                "data": 'service',
                "width": '10%'
            },
            {
                "data": 'topic',
                "width": '10%'
            },
            {
                "data": 'type',
                "render": function (data, type, row) {
                    return data.split(".")[data.split(".").length - 1];
                },
                "width": '12%'
            },
            {
                "data": 'groupId',
                "width": '20%'
            },
            {
                "data": 'reason',
                "render": function (data, type, row) {
                    return data ? '<a class="logTips" href="javascript:;" >查看<span style="display:none;">'
                        + data
                            .replace(/\tat /g, "&emsp;&emsp;at&ensp;")
                            .replace(/\t... /g, "&emsp;&emsp;...&ensp;")
                        + '</span></a>' : '无';
                },
                "width": '5%'
            },
            {
                "data": 'createdAt',
                "render": function (data, type, row) {
                    return data ? moment(new Date(data)).format("YYYY-MM-DD HH:mm:ss") : "";
                },
                "width": '12%'
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
                        'messageIds':[consumeId],
                        'startTime':$.trim($('#filterTime').val().split(" ")[0])+" 00:00:00",
                        'endTime':$.trim($('#filterTime').val().split(" ")[0])+" 23:59:59"
                    }
                    submit(param);
                }, function () {
                    layer.closeAll();
                });

            });
        }
    });

    $('#consume-fail-list').on('click', '.logTips', function () {
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
            '				<div class="alert alert-tail">' +
            '				</div>' +
            '			</div>' +
            '				<div class="modal-footer">' +
            '				<div class="text-center" >' +
            '					<button type="button" class="btn btn-info ok" data-dismiss="modal" >确定</button>' +
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