package nc.jzmobile.handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.uap.pf.IPFMobileAppService;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;

public class GetTaskListCountHandler implements INCMobileServletHandler {
	/**
	 * 加载pk时默认加载系数。加载的总行数=起始行+请求行数*此系数
	 */
	public Result handler(Map<String, String> map) throws Exception {
		Logger.info("==========GetTaskListHandler start==========");
		Result result = Result.instance();
		try{
			IPFMobileAppService service = NCLocator.getInstance().lookup(IPFMobileAppService.class);
			String[] pkOrg=null;
			String[] billmaker=null;
			String[] moudles=null;
			String datetype = map.get("datetype");
			if(StringUtils.isNotEmpty(map.get("pkOrg"))){
				pkOrg=map.get("pkOrg").split(",");
			}
			if(StringUtils.isNotEmpty(map.get("billmaker"))){
				billmaker=map.get("billmaker").split(",");
			}
			if(StringUtils.isNotEmpty(map.get("moudle"))){
				moudles=map.get("moudle").split(",");
			}
			String userid = map.get("userid");
			String statuskey = map.get("statuskey");
			String statuscode = map.get("statuscode");
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String condition = map.get("condition");
			String date = format.format(new Date());
			String pkGroup = JZMobileAppUtils.getPkGroupByUserId(userid);
			InvocationInfoProxy.getInstance().setGroupId(pkGroup);
			//Integer count = service.getTaskListCount(pkGroup, userid, date, statuskey,statuscode, condition);
			Integer count = service.getTaskListCount(pkGroup, userid, date, statuskey,statuscode, condition,pkOrg,billmaker,moudles,datetype);
			result.setData(count);
		}catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		Logger.info("==========GetTaskListHandler end==========");
		return result;
	}

}
