package nc.jzmobile.bill.handler;

import java.util.Map;

import nc.bs.logging.Logger;
import nc.jzmobile.bill.data.access.BillTempletModel;
import nc.jzmobile.bill.data.access.NCBillAccessBillTemplate;
import nc.jzmobile.bill.data.access.NCBillTemplate;
import nc.jzmobile.bill.data.access.PubBillTempletModel;
import nc.jzmobile.bill.util.BillMetaUtil;
import nc.jzmobile.bill.util.BillTempletUtil;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;

public class GetBillTempletHandler implements INCMobileServletHandler {
	
	public Result handler(Map<String, String> map) throws Exception {
		Logger.error("==========GetBillTempletHandler start==========");
		Result result = Result.instance();
		try{
			
			
			String userid = map.get("userid");
			String billtype = map.get("billtype");
			
			PubBillTempletModel templet = BillTempletUtil.getTemplateID(billtype);
			if (templet == null || "".equals(templet.getBillTempletId())) {
				throw new BusinessException("没有找到单据:" + billtype + "的模版！");
			}
			String aggvoClassName = BillMetaUtil.getAggVOFullClassName(billtype);
			AggregatedValueObject aggvo = (AggregatedValueObject) Class.forName(aggvoClassName).newInstance();
			NCBillTemplate ba = new NCBillTemplate(templet.getBillTempletId(),aggvo);
			BillTempletModel model = ba.billVO2MapT("", userid);

			result.setData(model);
		}catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		Logger.error("==========GetBillTempletHandler end==========");
		return result;
	}

}
