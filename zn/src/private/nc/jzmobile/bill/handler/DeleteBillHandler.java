package nc.jzmobile.bill.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.mobile.app.IMobilebillExecute;
import nc.jzmobile.bill.util.BillMetaUtil;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.vo.jzbase.pub.tool.ReflectHelper;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pf.mobileapp.MobileAppUtils;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.IColumnMeta;
import nc.vo.pub.IVOMeta;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pubapp.pattern.model.entity.bill.AbstractBill;
import nc.vo.pubapp.pattern.model.entity.bill.IBill;
import nc.vo.pubapp.pattern.model.meta.entity.bill.IBillMeta;
import nc.vo.pubapp.pattern.model.tool.MetaTool;

/**
 * ɾ������
 * @author mxx
 *
 */
public class DeleteBillHandler implements INCMobileServletHandler{
	
	@Override
	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		Logger.info("---DeleteBillHandler  start---");
		try {
			String userId = map.get("userid");
			if (userId == null) {
				throw new BusinessException("�û���Ϣ����Ϊ�գ�");
			}
			String billType = map.get("billtype");
			if (billType == null) {
				throw new BusinessException("�������Ͳ���Ϊ�գ�");
			}
			
			String billid = map.get("billid");
			InvocationInfoProxy.getInstance().setGroupId(
					MobileAppUtils.getPkGroupByUserId(userId));

			/**����billtype��billid��ȡaggvo*/
			AggregatedValueObject aggVO = BillMetaUtil.queryAggVO(billType, billid);
			
			
			IMobilebillExecute service = NCLocator.getInstance().lookup(IMobilebillExecute.class);
			
			AggregatedValueObject resultVO = service.deleteBill(aggVO, billType);
			
			if(resultVO.getParentVO()==null)
				throw new BusinessException("���صı�ͷ����Ϊ�գ�ɾ��ʧ�ܣ�");
			
			result.success().setData("ɾ���ɹ�!!");
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}

		return result;
	}
}
