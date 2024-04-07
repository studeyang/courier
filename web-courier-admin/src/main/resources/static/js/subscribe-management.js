$(document).ready(function (){

    var jobTable = $('#subscribe-service-list').dataTable( {
        "searching": false,
        "sort":false,
        "autoWidth":false,
        "scrollX": true,
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
        }
    } );
    let search = function () {
        window.location.href= base_url + "/subscribe/management?serviceName=" +$.trim($('#serviceName').val()) ;
    };

    $('#searchBtn').on('click', search);

    $(".input-group input").keypress(function (even) {
        if (even.which == 13) {
            search();
        }
    });

    $('.sava').on('click',function (){
        var reqData = {"service": $("#updateModal .form input[name='service']").val(),
        "consumerNode": $('#ipAndPorts option:selected').val()}
        $.post(base_url + "/subscribe/bind", reqData, function (data, status) {
            if (data.code == "200") {
                $('#addModal').modal('hide');
                layer.open({
                    title: '系统提示',
                    btn: ['确定'],
                    content: '指定节点消费成功',
                    icon: '1',
                    end: function (layero, index) {
                        window.location.reload();
                    }
                });
            } else {
                layer.open({
                    title: '系统提示',
                    btn: ['确定'],
                    content: (data.msg || '指定节点消费失败'),
                    icon: '2'
                });
            }
        });
    });

    $('table').on('click','.bind', function () {
        if($(this).attr("service") == null){
            layer.open({
                title: '系统提示',
                btn: ['确定'],
                content: ('服务名为空'),
                icon: '2',
            });
            return;
        }
        if($(this).attr("mode") == 'PULL'){
            layer.open({
                title: '系统提示',
                btn: ['确定'],
                content: ('PULL订阅模式服务不支持'),
                icon: '2',
            });
            return;
        }
        $("#updateModal .form input[name='service']").val($(this).attr("service"));
        //给下拉列表赋值
        $('#ipAndPorts option').remove();
        var ipportList = $(this).attr("ipAndPort").split(",");
        if(ipportList.length == 0){
            layer.open({
                title: '系统提示',
                btn: ['确定'],
                content: ('没有在线服务节点'),
                icon: '2',
            });
            return;
        }
        for(var index in ipportList ) {
           //最后一个是空行，去掉
            if(index < ipportList.length - 1){
                var ipAndPort = $.trim(ipportList[index]) ;
                var opt = "<option value='" + ipAndPort + "'>" + ipAndPort + "</option>";
                $('#ipAndPorts').append(opt);
            }
        }
        var opt = "<option value='all'>全部</option>";
        $('#ipAndPorts').append(opt);

        $('#updateModal').modal({backdrop: false, keyboard: false}).modal('show');
    });

    $('table').on('click','.unsubscribe', function () {
        if($(this).attr("mode") == 'PULL'){
            layer.open({
                title: '系统提示',
                btn: ['确定'],
                content: ('PULL订阅模式服务不支持'),
                icon: '2',
            });
            return;
        }
        var service = $(this).attr('service');
        layer.confirm(('确定下线'+service+'服务？'), {
            icon: 3,
            title: '系统提示',
            btn: ['确定', '取消']
        }, function (index) {
            layer.close(index);

            $.ajax({
                type: 'POST',
                url: base_url + '/subscribe/unsubscribe',
                data: {"service": service},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        layer.open({
                            title: '系统提示',
                            btn: ['确定'],
                            content: ('下线服务成功'),
                            icon: '1',
                            end: function (layero, index) {
                                window.location.reload();
                            }
                        });
                    } else {
                        layer.open({
                            title: '系统提示',
                            btn: ['确定'],
                            content: (data.msg || '下线服务失败'),
                            icon: '2'
                        });
                    }
                },
            });
        });
    });

});


