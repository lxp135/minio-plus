package org.liuxp.minioplus.extension.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

/**
 * 返回值通用结构
 * @author contact@liuxp.me
 * @since  2024/05/22
 */
@Data
@Schema(description = "Response")
@ToString
public class Response<T> {

	/**
	 * 状态码
	 */
	@Schema(description = "状态码")
	protected int code;

	/**
	 * 提示信息
	 */
	@Schema(description = "操作成功")
	protected String message;
	/**
	 * 返回给页面的数据内容，不同接口格式不同
	 */
	@Schema(description = "响应业务参数")
	protected T data;

	public Response() {
		this.setCode(ResponseCodeEnum.SUCCESS.getCode());
		this.setMessage(ResponseCodeEnum.SUCCESS.getMessage());
	}

	public static Response<Void> success() {
		Response<Void> response = new Response<>();
		response.setCode(ResponseCodeEnum.SUCCESS.getCode());
		response.setMessage(ResponseCodeEnum.SUCCESS.getMessage());
		return response;
	}

	public static <T> Response<T> success(T data) {
		Response<T> response = new Response<T>();
		response.setCode(ResponseCodeEnum.SUCCESS.getCode());
		response.setMessage(ResponseCodeEnum.SUCCESS.getMessage());
		response.setData(data);
		return response;
	}

	public static <T> Response<T> error(Integer errorCode, String message) {
		Response<T> response = new Response<>();
		response.setCode(errorCode);
		response.setMessage(message);
		return response;
	}
}
