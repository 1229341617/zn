package nc.jzmobile.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.jzbase.IJZPubQueryService;
import nc.itf.jzmobile.cache.PooledMapCache;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.jzmobile.bill.data.access.NCBillTemplate;
import nc.jzmobile.bill.data.access.PubBillTempletModel;
import nc.jzmobile.bill.util.BillMetaUtil;
import nc.jzmobile.bill.util.BillTempletUtil;
import nc.jzmobile.bill.util.RefModelUtil;
import nc.jzmobile.refmodel.BaseTreeGrid;
import nc.jzmobile.refmodel.CmContractRefModel;
import nc.jzmobile.refmodel.JzoblRefModel;
import nc.jzmobile.utils.BillTypeModelTrans;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.ui.bd.ref.AbstractRefGridTreeModel;
import nc.ui.bd.ref.AbstractRefModel;
import nc.ui.bd.ref.AbstractRefTreeModel;
import nc.vo.jz.pub.consts.IBillType;
import nc.vo.jzmobile.app.Result;
import nc.vo.pf.mobileapp.ITaskType;
import nc.vo.pf.mobileapp.MobileAppUtils;
import nc.vo.pf.mobileapp.TaskMetaData;
import nc.vo.pf.mobileapp.query.TaskQuery;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class GetRefDataHandler implements INCMobileServletHandler{
	
	private static final PooledMapCache<String, String> TEMPLATE_ID_CACHE = new PooledMapCache<String, String>();
	
	@Override
	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		Logger.info("---GetRefHandler  start---");
		try {
			String userId = map.get("userid");
			if (userId == null) {
				throw new BusinessException("�û���Ϣ����Ϊ�գ�");
			}

			String taskid = map.get("taskid");
			if (taskid == null) {
				throw new BusinessException("���ݲ�������ˢ��");
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
			
			
			ITaskType taskType = MobileAppUtils.getTaskType("ishandled", "unhandled");
			TaskQuery query = taskType.createNewTaskQuery();
			TaskMetaData tmd = query.queryTaskMetaData(taskid);
			String pk_billtype = tmd.getBillType();
			
			
			
			
			/**��ȡ��ӦXML�ļ��е�����**/
			String pk_templet = getTemplateID(BillTypeModelTrans.getInstance().getModelByBillType(pk_billtype)
					.getBillTypeCode());
			if (pk_templet == null || "".equals(pk_templet)) {
				new BusinessException("û���ҵ�����:" + pk_billtype + "���ƶ�����ʾģ�棡");
			}
			
			PubBillTempletModel templet = getTemplateModel(pk_templet);
			
			if (templet == null || "".equals(templet.getBillTempletId())) {
				throw new BusinessException("û���ҵ�����:" + pk_billtype + "��ģ�棡");
			}

			String aggvoClassName = BillMetaUtil
					.getAggVOFullClassName(pk_billtype);

			AggregatedValueObject aggvo = (AggregatedValueObject) Class
					.forName(aggvoClassName).newInstance();
			NCBillTemplate ba = new NCBillTemplate(templet.getBillTempletId(),
					aggvo);
			String refModelClassName = null;
			try {
				refModelClassName = ba.getItemRefClassName(tabCode, itemKey);
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
									+ "����Ӧ��java�ļ�������jz63_mobile_pubģ���µ�nc.jzmobile.refmodel���У�");
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
									+ "����Ӧ��java�ļ�������jz63_mobile_pubģ���µ�nc.jzmobile.refmodel���У�");
				}
			}
			// ����������ƴ����sql where�����
			if (filterMap != null) {
				Set<String> set = filterMap.keySet();
				StringBuffer buffer = new StringBuffer();
				
				for (String s : set) {
					if (!s.equals("pk_project")) {
						buffer.append(" and " + s + "='" + filterMap.get(s)
								+ "' ");
						refModel.addWherePart(buffer.toString());
					}
				}
			}
			// ���������Զ�������⴦����д�ڸ÷�����
			specialHandle(refModel, pk_billtype, tabCode, itemKey, pk_project,pk_org);

			// ������֯��Ϣ
			if (pk_org != null)
				refModel.setPk_org(pk_org);
			// ���ݵõ��Ĳ���ʾ����÷��ص�Json����
			Map<String,Object> resltMap = new HashMap<String,Object>();
			List<BaseTreeGrid> list = RefModelUtil.getJsonData(refModel);
			
			resltMap.put("data", list);

			/**�жϲ�������*/
			if(refModel instanceof AbstractRefTreeModel){
				
				if((AbstractRefTreeModel)refModel instanceof AbstractRefGridTreeModel){
					resltMap.put("type", "treetable");
					result.success().setData(resltMap);
					return result;
				}
				resltMap.put("type", "tree");
				result.success().setData(resltMap);
				return result;
			}
			resltMap.put("type", "table");
			result.success().setData(resltMap);
			
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e);
			result.fail().setErrorMessage(e.getMessage());
		}
		return result;
	}
	
	public  PubBillTempletModel getTemplateModel(String pk_billtemplet)
			throws BusinessException {

		// ��ѯ���ݿ�
		PubBillTempletModel templet = null;
		try {
			BaseDAO dao = new BaseDAO();
			templet = (PubBillTempletModel) dao
					.executeQuery(
							"select pk_billtemplet,bill_templetcaption,bill_templetname,pk_billtypecode from pub_billtemplet where isnull(dr,0)=0 and pk_billtemplet='"
									+ pk_billtemplet + "' ",
							new ResultSetProcessor() {

								private static final long serialVersionUID = 1L;

								public Object handleResultSet(ResultSet rs)
										throws SQLException {
									while (rs.next()) {
										PubBillTempletModel templet = new PubBillTempletModel();
										templet.setBillTempletCaption(rs
												.getString("bill_templetcaption"));
										templet.setBillTempletName(rs
												.getString("bill_templetname"));
										templet.setBillTypeCode(rs
												.getString("pk_billtypecode"));
										templet.setBillTempletId(rs
												.getString("pk_billtemplet"));
										return templet;
									}
									return null;
								}
							});
		} catch (DAOException e) {
			Logger.error(e);
			throw new BusinessException(e);
		}

		return templet;
	}
	
	
	public String getTemplateID(String billtype) throws BusinessException {
		if (TEMPLATE_ID_CACHE.containsKey(billtype)) {
			return TEMPLATE_ID_CACHE.get(billtype);
		}
		//��ѯ���ݿ�
		String pk_billtemplet = null;
		try {
			BaseDAO dao = new BaseDAO();
			pk_billtemplet = (String) dao.executeQuery(
					"select pk_billtemplet from pub_billtemplet where isnull(dr,0)=0 and bill_templetname = 'SYSTEM' and pk_billtypecode='"
							+ "MBL" + billtype + "' ", new ResultSetProcessor() {
						public Object handleResultSet(ResultSet rs) throws SQLException {
							while (rs.next()) {
								return rs.getString("pk_billtemplet");
							}
							return null;
						}
					});
		} catch (DAOException e) {
			Logger.error(e);
			throw new BusinessException(e);
		}

		if (pk_billtemplet != null) {
			TEMPLATE_ID_CACHE.put(billtype, pk_billtemplet);
		}
		return pk_billtemplet;
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
