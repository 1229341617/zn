package nc.jzmobile.bill.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.IColumnMeta;
import nc.vo.pub.ISuperVO;
import nc.vo.pub.IVOMeta;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pubapp.pattern.model.entity.bill.IBill;
import nc.vo.pubapp.pattern.model.meta.entity.bill.IBillMeta;
import nc.vo.pubapp.pattern.model.tool.MetaTool;
import nc.vo.uap.pf.PfProcessBatchRetObject;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * �������浥��
 * 
 */
public class BatchSaveBillHandler implements INCMobileServletHandler {

	@Override
	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		Logger.info("---BatchSaveBillHandler  start---");
		try {
			String userId = map.get("userid");
			/*
			 * if (userId == null) { throw new BusinessException("�û���Ϣ����Ϊ�գ�"); }
			 */
			String billType = map.get("billtype");
			if (billType == null) {
				throw new BusinessException("�������Ͳ���Ϊ�գ�");
			}
			InvocationInfoProxy.getInstance().setGroupId(
					JZMobileAppUtils.getPkGroupByUserId(userId));

			IMobilebillExecute service = NCLocator.getInstance().lookup(
					IMobilebillExecute.class);
			// ����map��װaggvo
			List<AggregatedValueObject> aggVOList = changeMap2AggVOList(map,
					billType, userId);

			Object retObj = service.batchSaveBill(
					aggVOList.toArray(new AggregatedValueObject[0]), billType);
			if (retObj instanceof PfProcessBatchRetObject) {
				String errMsg = ((PfProcessBatchRetObject) retObj)
						.getExceptionMsg();
				result.fail().setErrorMessage(errMsg);
				retObj = ((PfProcessBatchRetObject) retObj).getRetObj();
			}
		
			if (retObj != null && ((Object[]) retObj).length > 0) {
				// ������ʱ����һ���ɹ��ľ���Ϊ�ɹ�
				result.success().setErrorMessage(
						"�ɹ�ͬ�����ݣ�" + ((Object[]) retObj).length + "����");
			}

		} catch (Exception e) {
			e.printStackTrace();
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}

		return result;
	}

	public List<AggregatedValueObject> changeMap2AggVOList(
			Map<String, String> map, String billType, String userId)
			throws Exception {
		List<AggregatedValueObject> ll = new ArrayList<>();
		String aggVOFullClassName = BillMetaUtil
				.getAggVOFullClassName(billType);

		IBillMeta billMeta = MetaTool
				.getBillMeta((Class<? extends IBill>) Class
						.forName(BillMetaUtil.getAggVOFullClassName(billType)));
		IVOMeta[] metas = billMeta.getChildren();

		List<JSONObject> list = JSON.parseArray(map.get("data"),
				JSONObject.class);
		Class<? extends ISuperVO> voClass = billMeta.getVOClass(billMeta
				.getParent());
		IColumnMeta[] columns = billMeta.getParent().getStatisticInfo()
				.getTables()[0].getColumns();
		for (JSONObject obj : list) {
			AggregatedValueObject billVO = (AggregatedValueObject) ((AggregatedValueObject) Class
					.forName(aggVOFullClassName).newInstance());
			billVO.setParentVO(getJson2ParentVO(obj, billMeta, userId, voClass,
					columns));
			for (IVOMeta meta : metas) {
				billVO.setChildrenVO(getJson2ChildrenVOs(obj, billMeta, meta,
						billType));
			}
			ll.add(billVO);
		}
		return ll;
	}

	/**
	 * ��װ��ͷ
	 * 
	 * @param parentStr
	 * @return
	 * @throws Exception
	 */
	private SuperVO getJson2ParentVO(JSONObject obj, IBillMeta billMeta,
			String userId, Class<? extends ISuperVO> voClass,
			IColumnMeta[] columns) throws Exception {
		SuperVO parentVO = (SuperVO) ReflectHelper.newInstance(voClass);
		JSONObject jsonObject = JSON.parseObject((String) obj.get("headdata"));
		if (jsonObject == null)
			throw new BusinessException("������keyΪheaddata��valueֵ����Ϊ��");
		Set<String> sets = jsonObject.keySet();
		checkColumnName(columns, sets);
		for (String str : sets) {
			parentVO.setAttributeValue(str,
					"".equals(jsonObject.get(str)) ? null : jsonObject.get(str));
		}
		parentVO.setAttributeValue("ts", new UFDateTime());
		parentVO.setAttributeValue("billmaker", userId);
		parentVO.setAttributeValue("dmakedate", new UFDateTime());
		parentVO.setAttributeValue("creator", userId);
		parentVO.setAttributeValue("creationtime", new UFDateTime());
		Object orgNameObj = jsonObject.get("pk_org");
		if (null == orgNameObj) {
			throw new BusinessException("������keyΪheaddata�������pk_org(��������)����Ϊ�գ�");
		}
		//��������jsonObject.get("pk_org")�ǻ������ֵ�����£���������ȫƥ�����pk_org��pk_group
		Map<String, String> orgInfo = JZMobileAppUtils
				.getOrgInfoByOrgName((String) jsonObject.get("pk_org"));
		parentVO.setAttributeValue("pk_group", orgInfo.get("pk_group"));
		parentVO.setAttributeValue("pk_org", orgInfo.get("pk_org"));
		// ����״̬ ����̬
		parentVO.setAttributeValue("fstatusflag", -1);
		// �汾 1
		parentVO.setAttributeValue("iversion", 1);
		return parentVO;
	}

	/**
	 * ��װ����
	 * 
	 * @param obj
	 * @param billMeta
	 * @return
	 * @throws BusinessException
	 */
	private SuperVO[] getJson2ChildrenVOs(JSONObject obj, IBillMeta billMeta,
			IVOMeta meta, String billType) throws BusinessException {
		JSONObject jsonObject = JSONObject.parseObject((String) obj
				.get("bodydata"));
		if (jsonObject == null || jsonObject.equals(""))
			return null;
		List<SuperVO> list = new ArrayList<SuperVO>();
		Set<String> sets = jsonObject.keySet();

		IColumnMeta[] columns = meta.getStatisticInfo().getTables()[0]
				.getColumns();
		// У��ҳǩ���Ƿ��NC��һ��H54300505
		checkTabCode(BillMetaUtil.findBillPosByBillType(billType, 1), sets);
		String tabCode = BillMetaUtil.getTabCodeByBillType(meta, billType);
		JSONArray array = (JSONArray) jsonObject.get(tabCode);
		if (array == null)
			return null;
		for (int j = 0; j < array.size(); j++) {
			SuperVO vo = (SuperVO) ReflectHelper.newInstance(billMeta
					.getVOClass(meta));
			JSONObject json = (JSONObject) array.get(j);
			Set<String> set = json.keySet();
			// У���ֶ��Ƿ�һ��
			checkColumnName(columns, set);
			for (String s : set) {
				vo.setAttributeValue(s,
						"".equals(json.get(s)) ? null : json.get(s));// �����""����null
			}
			list.add(vo);
		}

		if (list.size() == 0)
			return null;
		SuperVO[] vos = new SuperVO[list.size()];
		for (int i = 0; i < list.size(); i++) {
			vos[i] = list.get(i);
		}
		return vos;
	}

	/*
	 * У��ҳǩ���Ƿ��NC��һ��
	 */
	private void checkTabCode(List<String> list, Set<String> sets)
			throws BusinessException {
		for (String s : sets) {
			if (!list.contains(s))
				throw new BusinessException("ҳǩ��Ϊ" + s + "�����ݿ����ҳǩ����һ�£�");
		}
	}

	/**
	 * У���ֶ��Ƿ�һ��,���в�һ���ֶ������set���Ƴ�
	 * 
	 * @param columns
	 * @param sets
	 * @throws BusinessException
	 */
	private void checkColumnName(IColumnMeta[] columns, Set<String> sets)
			throws BusinessException {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < columns.length; i++) {
			list.add(columns[i].getName());
		}
		Iterator<String> it = sets.iterator();
		while (it.hasNext())
			if (!list.contains(it.next())) {
				it.remove();
			}
	}
}
