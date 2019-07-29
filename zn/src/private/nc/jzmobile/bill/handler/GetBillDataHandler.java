package nc.jzmobile.bill.handler;

import java.util.List;
import java.util.Map;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.itf.mobile.app.IMobileBillDetailQuery;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.vo.jzmobile.app.MobileBillData;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;

public class GetBillDataHandler implements INCMobileServletHandler{

	@Override
	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		try {
			String userid = map.get("userid");
			String billtype = map.get("billtype");
			String taskid = map.get("taskid");
			
			InvocationInfoProxy.getInstance().setGroupId(JZMobileAppUtils.getPkGroupByUserId(userid));
			
			IMobileBillDetailQuery service = NCLocator.getInstance().lookup(IMobileBillDetailQuery.class);
			// 查询单据明细
//			Object obj = query.queryTaskBill(taskid);
			//boolean bodyData,String corp,String userid, String[] billidArray,String billtype
			List<MobileBillData> billDataList = service.getMobileBillDetail(true,"", userid, new String[]{taskid}, billtype);
			
			result.setData(billDataList);
		} catch (Exception e) {
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}

		return result;
	}

}
