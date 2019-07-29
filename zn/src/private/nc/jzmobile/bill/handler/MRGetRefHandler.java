package nc.jzmobile.bill.handler;

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
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.jzbase.IJZPubQueryService;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.jzmobile.bill.data.access.NCBillTemplate;
import nc.jzmobile.bill.data.access.PubBillTempletModel;
import nc.jzmobile.bill.util.BillMetaUtil;
import nc.jzmobile.bill.util.BillTempletUtil;
import nc.jzmobile.bill.util.MRRefModelUtil;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.jzmobile.refmodel.BaseTreeGrid;
import nc.jzmobile.refmodel.CmContractRefModel;
import nc.jzmobile.refmodel.JzoblRefModel;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.ui.bd.ref.AbstractRefModel;
import nc.vo.am.common.util.StringUtils;
import nc.vo.jz.pub.consts.IBillType;
import nc.vo.jzmobile.app.Result;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 移动端参照处理类
 * 
 * @author wangruin on 2017/8/14
 * 
 */
public class MRGetRefHandler implements INCMobileServletHandler {

	@SuppressWarnings({ "unchecked", "serial" })
	@Override
	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		Logger.info("---GetRefHandler  start---");
		try {
			String userId = map.get("userid");
			if (userId == null) {
				throw new BusinessException("用户信息不能为空！");
			}

			String billtype = map.get("billtype");
			if (billtype == null) {
				throw new BusinessException("单据编码不能为空！");
			}

			String tabCode = map.get("tabcode");
			if (tabCode == null) {
				throw new BusinessException("页签编码不能为空！");
			}
			String itemKey = map.get("itemkey");
			if (itemKey == null) {
				throw new BusinessException("参照字段名不能为空！");
			}
			String pk_org = map.get("pk_org");

			if (!itemKey.equals("pk_org") && pk_org == null)
				throw new BusinessException("业务单元不能为空！");

			String pk_project = map.get("pk_project");
			// 获取过滤条件
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

			if ("cwarehouseid".equals(itemKey)
					&& "PurchaseInHeadVO".equals(tabCode)) {
				ArrayList<String> pk_orgs = (ArrayList<String>) new BaseDAO()
						.executeQuery(
								"select pk_itemorg from ORG_ITEMORG where PK_VID ='"
										+ pk_org + "'",
								new ResultSetProcessor() {
									@Override
									public Object handleResultSet(ResultSet rs)
											throws SQLException {
										ArrayList<String> pk_orgs = new ArrayList<String>();
										while (rs.next()) {
											pk_orgs.add(rs
													.getString("pk_itemorg"));
										}
										return pk_orgs;
									}
								});
				pk_org = pk_orgs.get(0);
				filterMap.put("pk_org", pk_org);
			}

			InvocationInfoProxy.getInstance().setGroupId(
					JZMobileAppUtils.getPkGroupByUserId(userId));

			PubBillTempletModel templet = BillTempletUtil
					.getTemplateID(billtype);
			if (templet == null || "".equals(templet.getBillTempletId())) {
				throw new BusinessException("没有找到单据:" + billtype + "的模版！");
			}

			String aggvoClassName = BillMetaUtil
					.getAggVOFullClassName(billtype);

			AggregatedValueObject aggvo = (AggregatedValueObject) Class
					.forName(aggvoClassName).newInstance();
			NCBillTemplate ba = new NCBillTemplate(templet.getBillTempletId(),
					aggvo);
			String refModelClassName = null;
			try {
				if (tabCode.equals("main") && itemKey.equals("pk_material")) {
					refModelClassName = "nc.ui.bd.ref.model.MaterialDefaultRefModel";
				}else if(tabCode.equals("material") && itemKey.equals("pk_taxrate")){
					refModelClassName = "nc.jzmobile.JZAddTaxrateRefmodel";
					
					
				}else {
					refModelClassName = ba
							.getItemRefClassName(tabCode, itemKey);
				}

				// refModelClassName为空时抛出异常以进入catch中
				if (refModelClassName == null)
					throw new BusinessException();
			} catch (Exception e) {
				throw new BusinessException("页签名：" + tabCode + "或参照字段名："
						+ itemKey + "和NC不一致！");
			}
			AbstractRefModel refModel = null;

			try {
				if ("nc.ui.bd.ref.model.AddrDocRefModel"
						.equals(refModelClassName))
					throw new ClassNotFoundException();

				Object obj = Class.forName(refModelClassName).newInstance();

				// 通过反射为有pk_project属性的参照类进行pk_project属性赋值
				setProjectByReflect(obj, refModelClassName, pk_project);

				refModel = (AbstractRefModel) obj;
			} catch (ClassNotFoundException e) {
				String refModelClassChangeName = "nc.jzmobile.refmodel"
						+ refModelClassName.substring(refModelClassName
								.lastIndexOf("."));
				try {
					Object obj = Class.forName(refModelClassChangeName)
							.newInstance();
					// 通过反射为有pk_project属性的参照类进行pk_project属性赋值
					setProjectByReflect(obj, refModelClassChangeName,
							pk_project);

					refModel = (AbstractRefModel) obj;
				} catch (ClassNotFoundException ex) {
					throw new ClassNotFoundException(
							"类【"
									+ refModelClassName
									+ "】找不到，请将【"
									+ refModelClassName
									+ "】对应的java文件拷贝到jz63_mobile_pub模块下的nc.jzmobile.refmodel包中！");
				}

			} catch (NoClassDefFoundError e) {
				String refModelClassChangeName = "nc.jzmobile.refmodel"
						+ refModelClassName.substring(refModelClassName
								.lastIndexOf("."));
				try {
					Object obj = Class.forName(refModelClassChangeName)
							.newInstance();
					// 通过反射为有pk_project属性的参照类进行pk_project属性赋值
					setProjectByReflect(obj, refModelClassChangeName,
							pk_project);

					refModel = (AbstractRefModel) obj;
				} catch (ClassNotFoundException ex) {
					throw new ClassNotFoundException(
							"类【"
									+ refModelClassName
									+ "】找不到，请将【"
									+ refModelClassName
									+ "】对应的java文件拷贝到jz63_mobile_pub模块下的nc.jzmobile.refmodel包中！");
				}
			}
			// 将过滤条件拼接在sql where语句上
			// if (filterMap != null) {
			// Set<String> set = filterMap.keySet();
			// StringBuffer buffer = new StringBuffer();
			//
			// for (String s : set) {
			// if (!s.equals("pk_project")) {
			// buffer.append(" and " + s + "='" + filterMap.get(s)
			// + "' ");
			// refModel.addWherePart(buffer.toString());
			// }
			// }
			// }
			// 各个参照自定义的特殊处理请写在该方法中
			specialHandle(refModel, billtype, tabCode, itemKey, pk_project,
					pk_org);

			// 设置组织信息
			if (StringUtils.isNotEmpty(pk_org)
					&& refModelClassName
							.equals("nc.ui.bd.ref.model.PsndocDefaultNCRefModel"))
				refModel.setPk_org(pk_org);
			// 根据得到的参照示例获得返回的Json数据
			if (StringUtils.isNotEmpty(pk_org)
					&& refModelClassName
							.equals("nc.ui.bd.ref.model.StorDocDefaulteRefModel"))
				refModel.setPk_org(pk_org);
			String pageIndex = map.get("index");
			String pageSize = map.get("page");
			List<BaseTreeGrid> list = MRRefModelUtil.getJsonData(refModel,
					pageIndex, pageSize, filterMap);

			result.success().setData(list);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e);
			result.fail().setErrorMessage(e.getMessage());
		}
		return result;
	}

	/**
	 * 通过反射为有pk_project属性的参照类进行pk_project属性赋值
	 */
	private void setProjectByReflect(Object obj, String refModelClassName,
			String pk_project) throws IllegalArgumentException {
		try {
			Method m = Class.forName(refModelClassName).getDeclaredMethod(
					"setPk_project", String.class);

			if (pk_project == null)
				throw new IllegalArgumentException(refModelClassName
						+ "需要的参数【项目(pk_project字段)】值不能为空！");
			m.invoke(obj, pk_project);
		} catch (NoSuchMethodException e) {
			return;
		} catch (ClassNotFoundException e) {
			return;
		} catch (IllegalAccessException e) {
			return;
		} catch (InvocationTargetException e) {
			return;
		} catch (SecurityException e) {
			return;
		}
	}

	/**
	 * 各个参照自定义的特殊处理写在方法里面
	 */
	private void specialHandle(AbstractRefModel refModel, String billtype,
			String tabCode, String itemKey, String pk_project, String pk_org)
			throws BusinessException {
		// 【外经证开具申请】合同参照特殊处理
		if (billtype.equals("H5Y1") && tabCode.equals("pk_apply")
				&& itemKey.equals("pk_contract")) {
			List<String> contractTypeList = new ArrayList<String>();
			contractTypeList.add(IBillType.JZIN_CONTRACT);// 施工总承包合同
			contractTypeList.add(IBillType.JZCM_INCONTRACT);// 其它收入合同
			((CmContractRefModel) refModel).setConBillType(contractTypeList);
			((CmContractRefModel) refModel).setIcontstatus(new int[] { 1 });
		}
		// 【收票】合同参照特殊处理
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
			IJZPubQueryService queryService = NCLocator.getInstance().lookup(
					IJZPubQueryService.class);
			List<String> pk_creaorgList = queryService
					.getMainOrgList(pk_orgList);
			if (null != pk_creaorgList && pk_creaorgList.size() > 0) {
				((CmContractRefModel) refModel)
						.setPk_creaorgList(pk_creaorgList);
			}
		}
		// 【开票申请】外经证编号参照特殊处理
		if (billtype.equals("H5W0") && tabCode.equals("pk_invapp")
				&& itemKey.equals("pk_oblbill")) {
			int[] ioblstatusArray = new int[] { 2, 3 };
			((JzoblRefModel) refModel).setIoblstatusArray(ioblstatusArray);
		}
		// 外经证开具申请外出经营地参照特殊处理
		if (billtype.equals("H5Y1") && tabCode.equals("pk_apply")
				&& itemKey.equals("pk_location")) {
			if (pk_project == null)
				throw new BusinessException(
						"【外经证开具申请】外出经营地参照过滤条件项目(pk_project字段)值不能为空！");
			refModel.addWherePart(" and pk_addressdoc in (select pk_address from bd_project_addrrela where pk_addr_rela = '"
					+ pk_project + "')");

		}
	}
}
