package com.github.open.courier.core.transport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PageModel<T>{

	/**
	 * 总条数
	 */
	private long totalNum;

	/**
	 * 总页数
	 */
	private long totalPage;

	/**
	 * 当前页
	 */
	private long page;

	/**
	 * 每页显示条数
	 */
	private int size;

	/**
	 * 分页数据列表
	 */
	private List<T> records;
}
