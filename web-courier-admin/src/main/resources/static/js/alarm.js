$(function () {

    // enable
    $('.enable').on('click', function () {
        var id = $(this).attr('id');
        var enabled = $(this).attr('enabled');

        var operation;
        var url;
        if (enabled === 'false') {
            operation = '启用';
            url = base_url + '/alarm/' + id + '?enabled=true';
        } else {
            operation = '禁用';
            url = base_url + '/alarm/' + id + '?enabled=false';
        }

        layer.confirm('确定' + operation + '任务 ？', {
            icon: 3,
            title: '系统提示',
            // content: ('若上次有暂停任务则恢复上次所有，否则启动全部！'),
            btn: ['确定', '取消']
        }, function (index) {
            layer.close(index);

            $.ajax({
                type: 'PUT',
                url: url,
                data: {"id": id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        layer.open({
                            title: '系统提示',
                            btn: ['确定'],
                            content: (operation + '任务成功！'),
                            icon: '1',
                            end: function (layero, index) {
                                window.location.reload();
                            }
                        });
                    } else {
                        layer.open({
                            title: '系统提示',
                            btn: ['确定'],
                            content: (operation + '任务失败！'),
                            icon: '2'
                        });
                    }
                },
            });
        });

    });

    // test
    $('.test').on('click', function () {
        var id = $(this).attr('id');
        var enabled = $(this).attr('enabled');

        if (enabled === 'false') {
            layer.confirm('配置已禁用，如需测试，请先启用！', {
                icon: 5,
                title: '配置未启用提示',
                btn: ['确定']
            });
            return;
        }

        layer.confirm(('确定' + '发送测试消息' + '？'), {
            icon: 3,
            title: '发送测试消息',
            btn: ['确定', '取消']
        }, function (index) {
            layer.close(index);

            $.ajax({
                type: 'POST',
                url: base_url + '/alarm/' + id + '/test',
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        layer.open({
                            title: '系统提示',
                            btn: ['确定'],
                            content: (('发送测试消息成功！')),
                            icon: '1'
                        });
                    } else {
                        layer.open({
                            title: '系统提示',
                            btn: ['确定'],
                            content: ('发送测试消息失败！'),
                            icon: '2'
                        });
                    }
                },
            });
        });

    });

    // remove
    $('.remove').on('click', function () {
        var id = $(this).attr('id');

        layer.confirm(('确定' + '删除' + '？'), {
            icon: 3,
            title: '系统提示',
            btn: ['确定', '取消']
        }, function (index) {
            layer.close(index);

            $.ajax({
                type: 'DELETE',
                url: base_url + '/alarm/' + id,
                success: function (data) {
                    if (data.code == 200) {
                        layer.open({
                            title: '系统提示',
                            btn: ['确定'],
                            content: ('删除成功'),
                            icon: '1',
                            end: function (layero, index) {
                                window.location.reload();
                            }
                        });
                    } else {
                        layer.open({
                            title: '系统提示',
                            btn: ['确定'],
                            content: (data.msg || '删除失败'),
                            icon: '2'
                        });
                    }
                },
            });
        });

    });

    // jquery.validate mobile
    jQuery.validator.addMethod("mobileValidation", function (value, element) {
        var valid = /^1[3-9]\d{9}(,1[3-9]\d{9})*$/;
        return this.optional(element) || valid.test(value);
    }, '请输入正确的手机号，多手机号以 , 隔开');

    // add
    $('.add').on('click', function () {
        $('#addModal').modal({backdrop: false, keyboard: false}).modal('show');
    });
    var addModalValidate = $("#addModal .form").validate({
        errorElement: 'span',
        errorClass: 'help-block',
        focusInvalid: true,
        rules: {
            serviceEn: {
                required: true,
                rangelength: [1, 32]
            },
            serviceCh: {
                required: true,
                rangelength: [1, 50]
            },
            owner: {
                required: true,
                rangelength: [1, 50]
            },
            mobile: {
                required: true,
                rangelength: [1, 50],
                mobileValidation: true
            }
        },
        messages: {
            serviceEn: {
                required: '请输入消费方名称',
                rangelength: '长度限制为1~32'
            },
            serviceCh: {
                required: '请输入中文名称',
                rangelength: '长度限制为1~50'
            },
            owner: {
                required: '请输入负责人',
                rangelength: '长度限制为1~50'
            },
            mobile: {
                required: '请输入（钉钉）手机号',
                rangelength: '长度限制为1~50'
            }
        },
        highlight: function (element) {
            $(element).closest('.form-group').addClass('has-error');
        },
        success: function (label) {
            label.closest('.form-group').removeClass('has-error');
            label.remove();
        },
        errorPlacement: function (error, element) {
            element.parent('div').append(error);
        },
        submitHandler: function (form) {
            $.post(base_url + "/alarm", $("#addModal .form").serialize(), function (data, status) {
                if (data.code == 200) {
                    $('#addModal').modal('hide');
                    layer.open({
                        title: '系统提示',
                        btn: ['确定'],
                        content: '新增成功',
                        icon: '1',
                        end: function (layero, index) {
                            window.location.reload();
                        }
                    });
                } else {
                    layer.open({
                        title: '系统提示',
                        btn: ['确定'],
                        content: (data.msg || '新增失败'),
                        icon: '2'
                    });
                }
            });
        }
    });
    $("#addModal").on('hide.bs.modal', function () {
        $("#addModal .form")[0].reset();
        addModalValidate.resetForm();
        $("#addModal .form .form-group").removeClass("has-error");
    });

    // edit
    $('.edit').on('click', function () {
        $("#updateModal .form input[name='id']").val($(this).attr("id"));
        $("#updateModal .form input[name='serviceEn']").val($(this).attr("serviceEn"));
        $("#updateModal .form input[name='serviceCh']").val($(this).attr("serviceCh"));
        $("#updateModal .form input[name='owner']").val($(this).attr("owner"));
        $("#updateModal .form input[name='mobile']").val($(this).attr("mobile"));
        $("#updateModal .form select[name='groupName']").val($(this).attr("groupName"));

        $('#updateModal').modal({backdrop: false, keyboard: false}).modal('show');
    });
    var updateModalValidate = $("#updateModal .form").validate({
        errorElement: 'span',
        errorClass: 'help-block',
        focusInvalid: true,
        rules: {
            serviceEn: {
                required: true,
                rangelength: [1, 32]
            },
            serviceCh: {
                required: true,
                rangelength: [1, 50]
            },
            owner: {
                required: true,
                rangelength: [1, 50]
            },
            mobile: {
                required: true,
                rangelength: [1, 50],
                mobileValidation: true
            }
        },
        messages: {
            serviceEn: {
                required: '请输入消费方名称',
                rangelength: '长度限制为1~32'
            },
            serviceCh: {
                required: '请输入中文名称',
                rangelength: '长度限制为1~50'
            },
            owner: {
                required: '请输入负责人',
                rangelength: '长度限制为1~50'
            },
            mobile: {
                required: '请输入（钉钉）手机号',
                rangelength: '长度限制为1~50'
            }
        },
        highlight: function (element) {
            $(element).closest('.form-group').addClass('has-error');
        },
        success: function (label) {
            label.closest('.form-group').removeClass('has-error');
            label.remove();
        },
        errorPlacement: function (error, element) {
            element.parent('div').append(error);
        },
        submitHandler: function (form) {
            var formData = $("#updateModal .form").serialize();
            $.ajax({
                url: base_url + "/alarm",
                data: formData,
                type: 'PUT',
                success: function (data) {
                    if (data.code == 200) {
                        $('#addModal').modal('hide');

                        layer.open({
                            title: '系统提示',
                            btn: ['确定'],
                            content: '更新成功',
                            icon: '1',
                            end: function (layero, index) {
                                window.location.reload();
                            }
                        });
                    } else {
                        layer.open({
                            title: '系统提示',
                            btn: ['确定'],
                            content: (data.msg || '更新失败'),
                            icon: '2'
                        });
                    }
                }
            });
        }
    });
    $("#updateModal").on('hide.bs.modal', function () {
        $("#updateModal .form")[0].reset();
        addModalValidate.resetForm();
        $("#updateModal .form .form-group").removeClass("has-error");
    });

});
