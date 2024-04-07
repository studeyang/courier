package com.github.open.courier.core.transport;

import java.util.Date;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SubscribeGroupId {
	
	private int id;
	
	private int state;
	
	private String holder;
	
	private Date holdedAt;
}
