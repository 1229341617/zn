package nc.jzmobile.handler;

import java.util.List;
import java.util.Map;

import nc.jzmobile.utils.BillTypeModelTrans;
import nc.vo.jzmobile.app.Result;

public class GetApproveBillNumHandler implements INCMobileServletHandler{

	@Override
	public Result handler(Map<String, String> map) throws Exception {
		// TODO Auto-generated method stub
		Result result = Result.instance();
		List<String> billTypeList=BillTypeModelTrans.getInstance().getMobileApproveBillTypeList();
		result.setData(billTypeList==null?0:billTypeList.size());
		return result;
	}

}
