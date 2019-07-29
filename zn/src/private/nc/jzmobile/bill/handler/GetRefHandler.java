package nc.jzmobile.bill.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.jzbase.IJZPubQueryService;
import nc.jzmobile.bill.data.access.NCBillTemplate;
import nc.jzmobile.bill.data.access.PubBillTempletModel;
import nc.jzmobile.bill.util.BillMetaUtil;
import nc.jzmobile.bill.util.BillTempletUtil;
import nc.jzmobile.bill.util.RefModelUtil;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.jzmobile.refmodel.BaseTreeGrid;
import nc.jzmobile.refmodel.CmContractRefModel;
import nc.jzmobile.refmodel.JzoblRefModel;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.ui.bd.ref.AbstractRefModel;
import nc.ui.bd.ref.RefPubUtil;
import nc.vo.am.common.util.StringUtils;
import nc.vo.jz.pub.consts.IBillType;
import nc.vo.jzmobile.app.Result;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * �ƶ��˲��մ�����
 * 
 * @author wangruin on 2017/8/14
 * 
 */
public class GetRefHandler implements INCMobileServletHandler {

	@Override
	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		Logger.info("---GetRefHandler  start---");
		try {
			String userId = map.get("userid");
			if (userId == null) {
				throw new BusinessException("�û���Ϣ����Ϊ�գ�");
			}

			String billtype = map.get("billtype");
			if (billtype == null) {
				throw new BusinessException("���ݱ��벻��Ϊ�գ�");
			}

			String tabCode = map.get("tabcode");
			if (tabCode == null) {
				throw new BusinessException("ҳǩ���벻��Ϊ�գ�");
			}
			String itemKey = map.get("itemkey");
			if (itemKey == null) {
				throw new BusinessException("�����ֶ�������Ϊ�գ�");
			}
			String pk_org = map.get("pk_org");
			if (!itemKey.equals("pk_org") && pk_org == null)
				throw new BusinessException("ҵ��Ԫ����Ϊ�գ�");

			String pk_project = map.get("pk_project");
			// ��ȡ��������
			String filter = map.get("filter");
			Map<String, Object> filterMap = null;
			if (filter != null) {
				JSONObject json = (JSONObject) JSON.parseObject(filter).get(
						"filter");
				if (json != null) {
					Set<String> set = json.keySet();
					if (set.size() != 0) {
						filterMap = new HashMap<String, Object>();
						for (String s : set) {
							if (json.get(s) != null)
								filterMap.put(s, json.get(s));
						}
					}
				}
			}

			InvocationInfoProxy.getInstance().setGroupId(
					JZMobileAppUtils.getPkGroupByUserId(userId));

			PubBillTempletModel templet = BillTempletUtil
					.getTemplateID(billtype);
			if (templet == null || "".equals(templet.getBillTempletId())) {
				throw new BusinessException("û���ҵ�����:" + billtype + "��ģ�棡");
			}

			String aggvoClassName = BillMetaUtil
					.getAggVOFullClassName(billtype);

			AggregatedValueObject aggvo = (AggregatedValueObject) Class
					.forName(aggvoClassName).newInstance();
			NCBillTemplate ba = new NCBillTemplate(templet.getBillTempletId(),
					aggvo);
			String refModelClassName = null;
			try {
				refModelClassName = ba.getItemRefClassName(tabCode, itemKey);
				
				if("H5UH".equals(billtype) && "pk_rptype".equals(itemKey) && "RpbillVO".equals(tabCode)){
					refModelClassName = RefPubUtil.getRefModelClassName("����������������");
				}
				if("H5VL".equals(billtype) && "pk_rptype".equals(itemKey) && "SecRpbillVO".equals(tabCode)){
					refModelClassName = RefPubUtil.getRefModelClassName("��ȫ������������");
				}
				
				//refModelClassNameΪ��ʱ�׳��쳣�Խ���catch��
				if(refModelClassName==null)
					throw new BusinessException();
			} catch (Exception e) {
				throw new BusinessException("ҳǩ����" + tabCode + "������ֶ�����"
						+ itemKey + "��NC��һ�£�");
			}
			AbstractRefModel refModel = null;

			try {
				if("nc.ui.bd.ref.model.AddrDocRefModel".equals(refModelClassName))
					throw new ClassNotFoundException();
				
//				if("H5U6".equals(billtype) && "pk_qcrecord".equals(tabCode) && "pk_supplier".equals(itemKey)){
//					refModelClassName = "nc.ui.bd.ref.model.SupplierClassDefaultRefModel";
//				}
				Object obj = Class.forName(refModelClassName).newInstance();

				// ͨ������Ϊ��pk_project���ԵĲ��������pk_project���Ը�ֵ
				setProjectByReflect(obj, refModelClassName, pk_project);

				refModel = (AbstractRefModel) obj;
			} catch (ClassNotFoundException e) {
				String refModelClassChangeName = "nc.jzmobile.refmodel"
						+ refModelClassName.substring(refModelClassName
								.lastIndexOf("."));
				try {
					Object obj = Class.forName(refModelClassChangeName)
							.newInstance();
					// ͨ������Ϊ��pk_project���ԵĲ��������pk_project���Ը�ֵ
					setProjectByReflect(obj, refModelClassChangeName,
							pk_project);

					refModel = (AbstractRefModel) obj;
				} catch (ClassNotFoundException ex) {
					throw new ClassNotFoundException(
							"�ࡾ"
									+ refModelClassName
									+ "���Ҳ������뽫��"
									+ refModelClassName
									+ "����Ӧ��java�ļ�������jz65_mobile_pubģ���µ�nc.jzmobile.refmodel���У�");
				}

			}
			catch(NoClassDefFoundError e){
				String refModelClassChangeName = "nc.jzmobile.refmodel"
						+ refModelClassName.substring(refModelClassName
								.lastIndexOf("."));
				try {
					Object obj = Class.forName(refModelClassChangeName)
							.newInstance();
					// ͨ������Ϊ��pk_project���ԵĲ��������pk_project���Ը�ֵ
					setProjectByReflect(obj, refModelClassChangeName,
							pk_project);

					refModel = (AbstractRefModel) obj;
				} catch (ClassNotFoundException ex) {
					throw new ClassNotFoundException(
							"�ࡾ"
									+ refModelClassName
									+ "���Ҳ������뽫��"
									+ refModelClassName
									+ "����Ӧ��java�ļ�������jz65_mobile_pubģ���µ�nc.jzmobile.refmodel���У�");
				}
			}
			// ����������ƴ����sql where�����
			//String string  = refModel.getRefSql();
			if (filterMap != null) {
				Set<String> set = filterMap.keySet();
				StringBuffer buffer = new StringBuffer();
				
				for (String s : set) {
					if (!s.equals("pk_project")) {
						buffer.append(" and " + s + " like '%" + filterMap.get(s)
								+ "%' ");
						refModel.addWherePart(buffer.toString());
					}
				}
			}
			
			/**���˽�������*/
			if("H5U6".equals(billtype) && "ctrantypeid".equals(itemKey) && "pk_qcrecord".equals(tabCode)){
				refModel.addWherePart("and pk_billtypecode like '"+billtype+"%'");
			}
			if("H5V7".equals(billtype) && "ctrantypeid".equals(itemKey) && "SirecordVO".equals(tabCode)){
				refModel.addWherePart("and pk_billtypecode like '"+billtype+"%'");
			}
			// ���������Զ�������⴦����д�ڸ÷�����
			specialHandle(refModel, billtype, tabCode, itemKey, pk_project,pk_org);

			// ������֯��Ϣ
			if (StringUtils.isNotEmpty(pk_org))
				refModel.setPk_org(pk_org);
			// ���ݵõ��Ĳ���ʾ����÷��ص�Json����
			/*List<BaseTreeGrid> list = RefModelUtil.getJsonData(refModel);*/
			
			String pageIndex = map.get("index");
			String pageSize = map.get("page");
			
			//������ȫ�Ƶ�
			Map<String,List<BaseTreeGrid>> resultMap = RefModelUtil.getJsonData(refModel,pageIndex,pageSize,filterMap);
			result.success().setData(resultMap);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e);
			result.fail().setErrorMessage(e.getMessage());
		}
		return result;
	}

	/**
	 * ͨ������Ϊ��pk_project���ԵĲ��������pk_project���Ը�ֵ
	 */
	private void setProjectByReflect(Object obj, String refModelClassName,
			String pk_project) throws IllegalArgumentException {
		try {
			Method m = Class.forName(refModelClassName).getDeclaredMethod(
					"setPk_project", String.class);
			
			if (pk_project == null)
				throw new IllegalArgumentException(refModelClassName
						+ "��Ҫ�Ĳ�������Ŀ(pk_project�ֶ�)��ֵ����Ϊ�գ�");
			m.invoke(obj, pk_project);
		} catch (NoSuchMethodException e) {
			return;
		}catch (ClassNotFoundException e) {
			return;
		}catch (IllegalAccessException e) {
			return;
		}catch (InvocationTargetException e) {
			return;
		}catch (SecurityException e){
			return;
		}
	}

	/**
	 * ���������Զ�������⴦��д�ڷ�������
	 */
	private void specialHandle(AbstractRefModel refModel, String billtype,
			String tabCode, String itemKey, String pk_project,String pk_org)
			throws BusinessException {
		// ���⾭֤�������롿��ͬ�������⴦��
		if (billtype.equals("H5Y1") && tabCode.equals("pk_apply")
				&& itemKey.equals("pk_contract")) {
			List<String> contractTypeList = new ArrayList<String>();
			contractTypeList.add(IBillType.JZIN_CONTRACT);// ʩ���ܳа���ͬ
			contractTypeList.add(IBillType.JZCM_INCONTRACT);// ���������ͬ
			((CmContractRefModel) refModel).setConBillType(contractTypeList);
			((CmContractRefModel) refModel).setIcontstatus(new int[] { 1 });
		}
		//����Ʊ����ͬ�������⴦��
		if (billtype.equals("H5W2") && tabCode.equals("pk_receive")
				&& itemKey.equals("pk_contract")) {
			List<String> conBillTypes = new ArrayList<String>();
			conBillTypes.add(IBillType.RLM_CON_IN);
			conBillTypes.add(IBillType.ALI_CON_IN);
			conBillTypes.add(IBillType.JZCM_PAYCONTRACT);
			conBillTypes.add(IBillType.JZSUB_CONTRACT);
			conBillTypes.add(IBillType.CT_HNT);
			conBillTypes.add("Z2");
			((CmContractRefModel) refModel).setConBillType(conBillTypes);
			((CmContractRefModel) refModel).setIcontstatus(new int[] { 1 });
			List<String> pk_orgList = new ArrayList<String>();
			pk_orgList.add(pk_org);
			IJZPubQueryService queryService = NCLocator.getInstance().lookup(IJZPubQueryService.class);
			List<String> pk_creaorgList = queryService.getMainOrgList(pk_orgList);
			if(null != pk_creaorgList && pk_creaorgList.size() > 0){
				((CmContractRefModel) refModel).setPk_creaorgList(pk_creaorgList);
			}
		}
		// ����Ʊ���롿�⾭֤��Ų������⴦��
		if (billtype.equals("H5W0") && tabCode.equals("pk_invapp")
				&& itemKey.equals("pk_oblbill")) {
			int[] ioblstatusArray = new int[] { 2, 3 };
			((JzoblRefModel) refModel).setIoblstatusArray(ioblstatusArray);
		}
		// �⾭֤�������������Ӫ�ز������⴦��
		if (billtype.equals("H5Y1") && tabCode.equals("pk_apply")
				&& itemKey.equals("pk_location")) {
			if (pk_project == null)
				throw new BusinessException(
						"���⾭֤�������롿�����Ӫ�ز��չ���������Ŀ(pk_project�ֶ�)ֵ����Ϊ�գ�");
			refModel.addWherePart(" and pk_addressdoc in (select pk_address from bd_project_addrrela where pk_addr_rela = '"
					+ pk_project + "')");
		
		}
	}
}