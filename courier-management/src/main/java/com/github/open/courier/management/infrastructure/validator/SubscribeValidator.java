package com.github.open.courier.management.infrastructure.validator;

import com.github.open.courier.core.transport.AssignMessagePushEnvRequest;
import com.github.open.courier.core.transport.ClearMessagePushEnvRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubscribeValidator {

	public static void validate(AssignMessagePushEnvRequest request) {
		Assert.hasText(request.getCluster(), "cluster不能为空");
		Assert.hasText(request.getService(), "service不能为空");
		Assert.hasText(request.getType(), "type不能为空");
		Assert.hasText(request.getEnv(), "env不能为空");
	}

	public static void validate(ClearMessagePushEnvRequest request) {
		Assert.hasText(request.getCluster(), "cluster不能为空");
		Assert.hasText(request.getService(), "service不能为空");
		Assert.hasText(request.getType(), "type不能为空");
	}
}
