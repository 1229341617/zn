package nc.jzmobile.handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.uap.pf.IPFMobileAppService;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.vo.jzmobile.app.FilterModel;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;

/**
 * 获取项目组织以及提交人
 * @author mxx
 *
 */
public class GetTasksMakerAndOrgHandler implements INCMobileServletHandler{

	@Override
	public Result handler(Map<String, String> map) throws Exception {
		Logger.info("==========GetTaskListHandler start==========");
		Result result = Result.instance();
		try{
			IPFMobileAppService service = NCLocator.getInstance().lookup(IPFMobileAppService.class);
			String userid = map.get("userid");
			String statuskey = map.get("statuskey");
			String statuscode = map.get("statuscode");
			String condition=map.get("condition");
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String date = format.format(new Date());
			String pkGroup = JZMobileAppUtils.getPkGroupByUserId(userid);
			InvocationInfoProxy.getInstance().setGroupId(pkGroup);
			Map<String,List<FilterModel>> dataMap = service.getOrgAndBillmaker(pkGroup, userid, date, statuskey, statuscode, condition);
			result.setData(dataMap);
		}catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		Logger.info("==========GetTaskListHandler end==========");
		return result;
	}

}
