package com.github.open.courier.management.restful;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.TableOperationRequest;
import com.github.open.courier.core.constant.Separator;
import com.github.open.courier.management.application.service.TableOperationAppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import static com.github.open.courier.core.constant.PropertyConstant.THRESHOLD_ALARM_HANDLE_COST;

@Api(tags = "运维服务")
@Slf4j
@RestController
@RequestMapping("/devops")
public class DevopsService {

    @Autowired
    private TableOperationAppService tableOperationAppService;

    @ApiOperation("创建分表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tableOperation", value = "表操作", dataType = "TableOperation", required = true, paramType = "body")
    })
    @PostMapping(URLConstant.MANAGEMENT_TABLE)
    public String createTable(@RequestBody TableOperationRequest request) {

        validateTableOperation(request);

        return tableOperationAppService.createTable(request);
    }

    @ApiOperation("删除分表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tableOperation", value = "表操作", dataType = "TableOperation", required = true, paramType = "body")
    })
    @DeleteMapping(URLConstant.MANAGEMENT_TABLE)
    public String dropTable(@RequestBody TableOperationRequest request) {

        validateTableOperation(request);

        return tableOperationAppService.dropTable(request);
    }

    @ApiOperation("更新配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "property", value = "配置", dataType = "string", paramType = "param",
                    example = "处理时长告警阈值：threshold.alarm.handle-cost"),
            @ApiImplicitParam(name = "value", value = "配置值", dataType = "string", paramType = "param")
    })
    @PutMapping("/property")
    public void update(@RequestParam String property, @RequestParam String value) {

        switch (property) {

            case THRESHOLD_ALARM_HANDLE_COST:
                Assert.isTrue(NumberUtils.isDigits(value), "必须为数值");
                System.setProperty(property, value);
                break;

            default:
                throw new IllegalArgumentException("无此配置：" + property);
        }
    }

    @ApiOperation("查询配置")
    @ApiImplicitParam(name = "property", value = "配置", dataType = "string", paramType = "param")
    @GetMapping("/property")
    public String getByProperty(@RequestParam String property) {

        switch (property) {

            case THRESHOLD_ALARM_HANDLE_COST:
                return System.getProperty(property);

            default:
                throw new IllegalArgumentException("无此配置：" + property);
        }

    }

    private void validateTableOperation(TableOperationRequest request) {

        Assert.notNull(request, "参数不可为空");
        Assert.notNull(request.getTable(), "表名不可为空");
        Assert.notEmpty(request.getSuffixList(), () -> "表分片不可为空");

        boolean checkResult = request.getSuffixList().stream()
                .anyMatch(suffix -> !StringUtils.startsWith(suffix, Separator.UNDERLINE.getSymbol()));

        Assert.isTrue(!checkResult, "存在表名前缀不以_开头");
    }

}
