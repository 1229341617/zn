package nc.jzmobile.handler;

import java.util.Map;

import nc.vo.jzmobile.app.Result;

public interface INCMobileServletHandler {
	public Result handler(Map<String, String> map) throws Exception;
}
