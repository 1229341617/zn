package nc.jzmobile.bill.handler;

import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.logging.Logger;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pub.BusinessException;

/**
 * * 内网
 * 请假单请假类型
 */
public class JZMNGHolidayTypeListHandler implements INCMobileServletHandler {
	@Override
	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		Logger.info("---JZMNGHolidayTypeListHandler  start---");
		try{
			String userId = map.get("userid");
			if(userId==null){
				throw new BusinessException("用户信息不能为空！");
			}	
			
			InvocationInfoProxy.getInstance().setGroupId(JZMobileAppUtils.getPkGroupByUserId(userId));
			String sql = "select pk_defdoc,name from bd_defdoc where pk_defdoclist = '1001Y9100000000001LO' order by ts asc";
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> list = (List<Map<String, Object>>) new BaseDAO()
					.executeQuery(sql, new MapListProcessor());
			
			result.success().setData(list);
			
		}catch(Exception e){
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		return result;
	}

}
