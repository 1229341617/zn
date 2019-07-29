package nc.jzmobile.app.impl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.jzmobile.IJZMobileDMO;
import nc.itf.jzmobile.cache.PooledMapCache;
import nc.itf.mobile.app.IMobileBillDetailQuery;
import nc.itf.uap.pf.IMobileTaskService;
import nc.itf.uap.pf.IWorkflowMachine;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.jzmobile.bill.data.access.NCBillAccessBillTemplate;
import nc.jzmobile.bill.util.BillMetaUtil;
import nc.jzmobile.utils.BillTypeModelTrans;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.jzmobile.utils.MobileMessageUtil;
import nc.vo.ali.contract.leasecont.AggContractInVO;
import nc.vo.ali.rent.settle.AggRentInSettleVO;
import nc.vo.ali.settle.AggTempSettleVO;
import nc.vo.am.timerule.LeaseTimeRuleVO;
import nc.vo.approve.ActivityVo;
import nc.vo.bd.cust.CustomerVO;
import nc.vo.bd.supplier.SupplierVO;
import nc.vo.jzcm.jzct.settle.in.AggInCmSettleVO;
import nc.vo.jzcm.jzpr1010.AggPrPayPlanVO;
import nc.vo.jzcm.jzpr1010.PrPayPlanSettleVO;
import nc.vo.jzmobile.app.AssignableUserGroup;
import nc.vo.jzmobile.app.AssignableUserVO;
import nc.vo.jzmobile.app.MobileBillData;
import nc.vo.jzmobile.app.MobileTabContentVO;
import nc.vo.jzmobile.app.MobileTabDataVO;
import nc.vo.jzpm.jzmt1005.AggCmtplanVO;
import nc.vo.jzpm.jzsub10.AggSubContractVO;
import nc.vo.jzpm.jzsub2020.AggSubSettleVO;
import nc.vo.jzpm.jzsub2025.AggSubFinishSettleVO;
import nc.vo.pf.mobileapp.TaskMetaData;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.pub.pf.AssignableInfo;
import nc.vo.pub.pf.AssignedUser;
import nc.vo.pub.pf.AssignedUserList;
import nc.vo.pub.pf.workflow.IPFActionName;
import nc.vo.pub.workflownote.WorkflownoteVO;
import nc.vo.uap.pf.OrganizeUnit;
import nc.vo.wfengine.core.parser.XPDLNames;

import org.apache.commons.lang.StringUtils;
import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Arrays;

/**
 * @version NC5
 * @author wanghui
 * �ƶ���̨��ѯ NC����Ϊ�ƶ�APP ���� �ĵ���ģ�巵�ص�����
 */
public class MobileBillDetailQueryImpl implements IMobileBillDetailQuery {
	/**
	 * ����������ģ�������Ļ����࣬�����Ƕ�Ӧ�ĵ������ͣ�ֵ�Ƕ�Ӧ��ģ������
	 */
	private static final PooledMapCache<String, String> TEMPLATE_ID_CACHE = new PooledMapCache<String, String>();
	/**
	 * ����������DMO��Ļ��棬�����Ƕ�Ӧ�ĵ������ͣ�ֵ�Ƕ�Ӧ��DMO����
	 */
	private static final PooledMapCache<String, IJZMobileDMO> DMO_CACHE = new PooledMapCache<String, IJZMobileDMO>();
	
	public static final String TEMPLATE_PREFIX = "MBL";
	
	

	public List<MobileBillData> getMobileBillDetail(boolean bodyData, String corp, String userid, String[] billidArray,
			String billtype) throws BusinessException {
		List<MobileBillData> dataList = new ArrayList<MobileBillData>();
		try {
			String pk_templet = getTemplateIDBill(billtype);
			if (pk_templet == null || "".equals(pk_templet)) {
				throw new BusinessException("û���ҵ�����:" + billtype + "���ƶ�����ʾģ�棡");
			}

			Logger.info("mobile approve pk_templet:" + pk_templet);
			NCBillAccessBillTemplate ba = new NCBillAccessBillTemplate(pk_templet);
			ba.loadTemplate();
			
			for (String billid : billidArray) {
				AggregatedValueObject aggvo = BillMetaUtil.queryAggVO(billtype, billid);
				ba.setBillVO(aggvo);
				MobileBillData billData = null;
				if (bodyData)
					billData = ba.billVO2MapBill(corp, userid);
				else
					billData = ba.billVO2MapHead(corp, userid);
				billData.setBilltypename(ba.getBillTempletVO().getHeadVO().getBillTempletCaption());
				billData.setId(billid);
				dataList.add(billData);
			}
			return dataList;

		} catch (Exception e) {
			Logger.error(e);
			throw new BusinessException(e);
		}
	}
	
	public Map<String, Object> getMobileBillDetail(String corp, TaskMetaData tmd, String billtype, String statuskey,
			String statuscode, String taskid) throws BusinessException {

		String userid = tmd.getCuserid();
		String billid = tmd.getBillId();
		
		try {
			String pk_templet = getTemplateID(BillTypeModelTrans.getInstance().getModelByBillType(billtype)
					.getBillTypeCode());
			if (pk_templet == null || "".equals(pk_templet)) {
				new BusinessException("û���ҵ�����:" + billtype + "���ƶ�����ʾģ�棡");
			}
			Logger.info("mobile approve pk_templet:" + pk_templet);
			
			NCBillAccessBillTemplate ba = new NCBillAccessBillTemplate(pk_templet);
			ba.loadTemplate();
			Map<String, Object> dataMap = new HashMap<String, Object>();
			
			AggregatedValueObject aggvo = BillMetaUtil.queryAggVO(billtype, billid);
			
			ba.setBillVO(aggvo);
			MobileBillData billData = ba.billVO2Map(corp, userid);
			
			/**�����ͷ�ͱ�������������ģ���������踴�Ƶ������ģ��Ϊ"MBL"+billtype+"-B1",�硰���Ĳ����ܿؼƻ����Ƴ�MBLH5C1-B1"*/
			dealSecondBodyTemptDatas(corp, userid, billtype, aggvo, billData);
			/**������浥���ֶ�ȱʧ�ȴ���*/
			rewriteSqlDatas(billtype, aggvo, billData);
			
			billData.setBilltypename(ba.getBillTempletVO().getHeadVO().getBillTempletCaption());
			billData.setId(billid);
			billData.setTs(aggvo.getParentVO().getAttributeValue("ts").toString());
			billData.setFilecount("" + getBillFileCount(billid, billtype));
			
			//�ж϶�ʧ����---��ͷ����岢����
			/*suppLosePart(billtype,billData,billid);	*/
			
			/**���������ǹ���������������*/
			String isWorkFlow = BillTypeModelTrans.getInstance().getModelByBillType(billtype).getIsWorkFlow();
			/**�����Ƿ����Ӱ��*/
			String isImage = BillTypeModelTrans.getInstance().getModelByBillType(billtype).getIsImage();
			
			if ("unhandled".equals(statuscode) && "ishandled".equals(statuskey)) {
				IWorkflowMachine srv = NCLocator.getInstance().lookup(IWorkflowMachine.class);
				
				//������
                if(!StringUtils.isEmpty(isWorkFlow) && isWorkFlow.equals("Y")){
                	WorkflownoteVO worknoteVO = srv.checkWorkFlow(IPFActionName.SIGNAL, tmd.getBillType(), aggvo, null);
                	dataMap.put("canReject", canReject(worknoteVO));
					dataMap.put("canTransfer", canTransfer(worknoteVO));
					dataMap.put("canAddApprover", canAddApprover(worknoteVO));
					dataMap.put("canAssignableWhenAgree", isExistAssignableInfoWhenPass(worknoteVO));
					dataMap.put("canAssignableWhenDisagree", isExistAssignableInfoWhenNopass(worknoteVO));
					if ((Boolean) dataMap.get("canReject")) {
						List<ActivityVo> activityList = JZMobileAppUtils.findActivateForWork(tmd.getBillId(), tmd.getBillType());
						dataMap.put("activityList", activityList);
					}
				
			    //������
				}else{
					isWorkFlow = "N";
					WorkflownoteVO worknoteVO = srv.checkWorkFlow(IPFActionName.APPROVE, tmd.getBillType(), aggvo, null);
					dataMap.put("canReject", canReject(worknoteVO));
					dataMap.put("canTransfer", canTransfer(worknoteVO));
					dataMap.put("canAddApprover", canAddApprover(worknoteVO));

					dataMap.put("canAssignableWhenAgree", isExistAssignableInfoWhenPass(worknoteVO));
					dataMap.put("canAssignableWhenDisagree", isExistAssignableInfoWhenNopass(worknoteVO));

					if ((Boolean) dataMap.get("canReject")) {
						List<ActivityVo> activityList = JZMobileAppUtils.findActivate(tmd.getBillId(), tmd.getBillType());
						dataMap.put("activityList", activityList);
					}
					/**
					 * ����Ա������̫���Ϊ���������󣬷�ҳ����
					 * ***/
					//					List<UserVO> userList = MobileAppUtils.findSendToUserList(userid);
					//					dataMap.put("userList", userList);//��ǩ  ����   ����  �������userList
					if ((Boolean) dataMap.get("canAssignableWhenAgree")) {
						dataMap.put("agreeAssignableUserList",
								this.getAssignableUserList(worknoteVO, AssignableInfo.CRITERION_PASS));
					} else {
						String name = null;
						if (null != worknoteVO && null != worknoteVO.getPk_wf_task()) {
							name = NCLocator.getInstance().lookup(IMobileTaskService.class)
									.getNextApprover(worknoteVO.getPk_wf_task());
						}
						dataMap.put("nextApprover", name == null ? "��ǰ������Ϊ���������" : "��һ�������ˣ�" + name);
					}
					if ((Boolean) dataMap.get("canAssignableWhenDisagree")) {
						dataMap.put("disagreeAssignableUserList",
								this.getAssignableUserList(worknoteVO, AssignableInfo.CRITERION_NOPASS));
					}
					dataMap.put("canCpySend", true);
				}
                
			}
			/**��������ʱ�Ƿ��ǹ���������*/
            dataMap.put("isWorkFlow", isWorkFlow);
            
            /**�����Ƿ����Ӱ��ģ��*/
            dataMap.put("isImage", isImage);
            
			dataMap.put("billid", billid);
			dataMap.put("billtype", billtype);
			dataMap.put("billtypename", billData.getBilltypename());
			dataMap.put("taskbill", billData);
			dataMap.put("statuskey", statuskey);
			dataMap.put("statuscode", statuscode);
			return dataMap;

		} catch (Exception e) {
			if(e.getMessage().contains("��ǰ������û�д�������")){
				throw new BusinessException("��ǰ��������״̬�Ѹ��£���ˢ�����µĴ�������...");
			}
			Logger.error(e);
			throw new BusinessException(e);
		}
	} 
	
	private void rewriteSqlDatas(String billtype, AggregatedValueObject aggvo,
			MobileBillData billData) throws BusinessException, DAOException {
		BaseDAO dao = new BaseDAO();
		if("H5A2".equals(billtype)){//1.�ְ�����Э��-�ְ���λ
			String sql = " select s.name from jzsub_change jc "
						+ " left join jzsub_contract sub on sub.pk_subcontract=jc.pk_subcontract "
						+ " left join bd_supplier s on s.pk_supplier = sub.pk_second "
						+ " where jc.pk_change='"+aggvo.getParentVO().getPrimaryKey()+"'";
			Object suborgnameobj = dao.executeQuery(sql, new ColumnProcessor());
			if(suborgnameobj != null) {
				billData.getData().get("head").get(0).getTabdata().get(0).get(2).setColvalue(suborgnameobj.toString());
			}
		}else if("H5A3".equals(billtype)){//2.�ְ�����ǩ֤��-�ְ���λ
			String sql = " select s.name from jzsub_change jc "
					+ " left join jzsub_contract sub on sub.pk_subcontract=jc.pk_subcontract "
					+ " left join bd_supplier s on s.pk_supplier = sub.pk_second "
					+ " where jc.pk_change='"+aggvo.getParentVO().getPrimaryKey()+"'";
			Object suborgnameobj = dao.executeQuery(sql, new ColumnProcessor());
			if(suborgnameobj != null) {
				billData.getData().get("head").get(0).getTabdata().get(0).get(2).setColvalue(suborgnameobj.toString());
			}
		}else if("H5A6".equals(billtype)){//3.�ְ�������ͳ��-�ְ���λ
			String sql = " select s.name from jzsub_projsum jc "
					+ " left join jzsub_contract sub on sub.pk_subcontract=jc.pk_subcontract "
					+ " left join bd_supplier s on s.pk_supplier = sub.pk_second "
					+ " where jc.pk_projsum='"+aggvo.getParentVO().getPrimaryKey()+"'";
			Object suborgnameobj = dao.executeQuery(sql, new ColumnProcessor());
			if(suborgnameobj != null) {
				billData.getData().get("head").get(0).getTabdata().get(0).get(2).setColvalue(suborgnameobj.toString());
			}
			
			StringBuffer listSql1 = new StringBuffer();
			StringBuffer listSql2 = new StringBuffer();
			
			listSql1.append(" select case jpb.bisleaf when 'N' then jc.vname when 'Y' then jf.vname end as vname,unit.name unitname,jf.nnum nnum, ");
			listSql1.append(" jpb.nthisprice nthisprice,jpb.nthistaxprice nthistaxprice,jpb.nfinishnum nfinishnum,jpb.norigfinishmny norigfinishmny, ");
			listSql1.append(" jpb.nthisnum nthisnum,jpb.norigthismny norigthismny,jpb.norigthistaxmny norigthistaxmny,jpb.nsumfinishnum nsumfinishnum, ");
			listSql1.append(" jpb.norigsumfinishtaxmny norigsumfinishtaxmny ");
			listSql1.append(" from jzsub_projsum_b jpb ");
			listSql1.append(" left join jzsub_contract jc on jc.pk_subcontract=jpb.csrcid ");
			listSql1.append(" left join jzsub_contfee jf on jf.pk_contfee=jpb.pk_contfee ");
			listSql1.append(" left join bd_measdoc unit on unit.pk_measdoc=jf.cunitid ");
			listSql1.append(" where jpb.pk_projsum_b= ");
			
			listSql2.append(" select case jpb.bisleaf when 'N' then jc.vname when 'Y' then jf.vname end as vname,unit.name unitname,jf.nnum nnum, ");
			listSql2.append(" jpb.nthisprice nthisprice,jpb.nthistaxprice nthistaxprice,jpb.nfinishnum nfinishnum,jpb.norigfinishmny norigfinishmny, ");
			listSql2.append(" jpb.nthisnum nthisnum,jpb.norigthismny norigthismny,jpb.norigthistaxmny norigthistaxmny,jpb.nsumfinishnum nsumfinishnum, ");
			listSql2.append(" jpb.norigsumfinishtaxmny norigsumfinishtaxmny ");
			listSql2.append(" from jzsub_projsum_b jpb ");
			listSql2.append(" left join jzsub_change jc on jc.pk_change=jpb.csrcid ");
			listSql2.append(" left join jzsub_change_b jf on jf.pk_change_b=jpb.pk_contfee ");
			listSql2.append(" left join bd_measdoc unit on unit.pk_measdoc=jf.cunitid ");
			listSql2.append(" where jpb.pk_projsum_b= ");
			
			String []tabArr = {"pk_projsum_b","�ְ�������ͳ���ӱ�"};
			String [][] bData={{"vname","����"},{"unitname","��λ"},{"nnum","������"},
								  {"nthisprice","����(��˰)"},{"nthistaxprice","����(Ԫ)"},{"nfinishnum","�����-������"},{"norigfinishmny","�����-�ϼ�(Ԫ)"},
								  {"nthisnum","���ι�����"},{"norigthismny","����-�ϼ�(��˰)"},{"norigthistaxmny","����-�ϼ�(Ԫ)"},{"nsumfinishnum","�ۼ����-������"},
								  {"norigsumfinishtaxmny","�ۼ����-�ϼ�(Ԫ)"}
								};
			billData.getData().get("body").get(0).getTabdata().clear();
			CircularlyAccessibleValueObject[] pcbs = aggvo.getChildrenVO();
			if(pcbs != null && pcbs.length > 0) {
				for (int i = 0; i < pcbs.length; i++) {
					List<MobileTabContentVO> bs = null;
					if(pcbs[i].getAttributeValue("vsrccode") != null && "H5A1".equals(pcbs[i].getAttributeValue("vsrccode").toString())) {
						bs = getUniqBodyMobileDatas(new StringBuffer(listSql1 + "'"+pcbs[i].getPrimaryKey()+"'"),bData,tabArr,0);
					} else if(pcbs[i].getAttributeValue("vsrccode") != null && ("H5A2".equals(pcbs[i].getAttributeValue("vsrccode").toString()) || "H5A3".equals(pcbs[i].getAttributeValue("vsrccode").toString()))) {
						bs = getUniqBodyMobileDatas(new StringBuffer(listSql2 + "'"+pcbs[i].getPrimaryKey()+"'"),bData,tabArr,0);
					}
					MobileTabDataVO uniqmny = bs.get(0).getTabdata().get(0).get(3);
					if(uniqmny.getColvalue() != null && uniqmny.getColvalue().toString().startsWith(".")) {
						bs.get(0).getTabdata().get(0).get(3).setColvalue("0" + uniqmny.getColvalue().toString());
					}
					billData.getData().get("body").get(0).getTabdata().add(bs.get(0).getTabdata().get(0));
				}
			}
		}else if("4A3H-01".equals(billtype)){//4.��ʱ������㵥-��Ŀ/����-�ɱ���̯
			String sql = " select p.project_name from ali_temp_settle s "
					+ " left join bd_project p on p.pk_project=s.pk_jobmngfil "
					+ " where s.pk_settle='"+aggvo.getParentVO().getPrimaryKey()+"'";
			Object projectnameobj = dao.executeQuery(sql, new ColumnProcessor());
			if(projectnameobj != null) {
				billData.getData().get("head").get(0).getTabdata().get(0).get(2).setColvalue(projectnameobj.toString());
			}
			String basesql = " select cbs.code cbscode,cbs.name cbsname,fac.factorcode faccode,fac.factorname facname from ali_temp_settle_cost pcb  "
					+ " left join bd_cbsnode cbs on cbs.pk_cbsnode=pcb.pk_cbs "
					+ " left join resa_factorasoa fac on fac.pk_factorasoa=pcb.celementid  "
					+ " where pcb.pk_settle_cost=";
			CircularlyAccessibleValueObject[] pcbs = ((AggTempSettleVO)aggvo).getTableVO("costvos");
			if(pcbs != null && pcbs.length > 0) {
				for (int i = 0; i < pcbs.length; i++) {
					List result = (List)dao.executeQuery(basesql + "'"+pcbs[i].getPrimaryKey()+"'", new MapListProcessor());
					if(result != null) {
						if(((Map)result.get(0)).get("cbscode") != null){
							billData.getData().get("body").get(0).getTabdata().get(i).get(1).setColvalue(((Map)result.get(0)).get("cbscode").toString());
						}
						if(((Map)result.get(0)).get("cbsname") != null){
							billData.getData().get("body").get(0).getTabdata().get(i).get(2).setColvalue(((Map)result.get(0)).get("cbsname").toString());
						}
						if(((Map)result.get(0)).get("faccode") != null){
							billData.getData().get("body").get(0).getTabdata().get(i).get(3).setColvalue(((Map)result.get(0)).get("faccode").toString());
						}
						if(((Map)result.get(0)).get("facname") != null){
							billData.getData().get("body").get(0).getTabdata().get(i).get(4).setColvalue(((Map)result.get(0)).get("facname").toString());
						}
					}
				}
			}
		}else if("4A3B-01".equals(billtype)){//5.���޺�ͬ�����-��ͷ-����
			StringBuffer listSql = new StringBuffer();
			listSql.append(" select al.bill_date bill_date,al.bill_code code,case al.bill_status ");
			listSql.append(" when -1 then '����' when 0 then '����δͨ��' when 1 then  '����ͨ��' ");
			listSql.append(" when 2 then  '����������' when 3 then  '���ύ' end as status,c.bill_code contractcode, ");
			listSql.append(" al.contract_no contractno, c.contract_name contractname,p.project_name projname, ");
			listSql.append(" case al.lease_type when 1 then '�ⲿ����' when 2 then '�ڲ�����-Эͬ' when 3 then  '�ڲ�����-����'  ");
			listSql.append(" end as lease_type, sp.name supplier,al.ncontbeforetaxmny ncontbeforetaxmny,al.ncontaxmny ncontaxmny,al.memo memo ");
			listSql.append(" from ali_cont_alter al ");
			listSql.append(" left join ali_contract c on c.pk_contract=al.pk_contract ");
			listSql.append(" left join bd_project p on p.pk_project=al.pk_jobmngfil ");
			listSql.append(" left join bd_supplier sp on sp.pk_supplier=al.pk_supplier ");
			listSql.append(" where al.pk_contalter= '" + aggvo.getParentVO().getPrimaryKey() + "'");
			
			String []tabArr = {"ContractInAlterHeadVO","������Ϣ"};
			String [][] headData={{"bill_date","�������"},{"code","���������"},{"status","����״̬"},{"contractcode","��ͬ����"},
								  {"contractno","��ͬ��"},{"contractname","��ͬ����"},{"projname","��Ŀ����"},{"lease_type","���޷�ʽ"},
								  {"supplier","���ⷽ"},{"ncontbeforetaxmny","�޶�ǰ��ͬ���"},{"ncontaxmny","��ͬ���(Ԫ)"},{"memo","��ע"}
								};
			billData.getData().put("head", getUniqHeadMobileDatas(listSql,headData,tabArr,0));
		
			StringBuffer listSql2 = new StringBuffer();
			listSql2.append(" select cab.rowno rowno,cab.bill_code billcode,cab.equip_code equipcode,cab.equip_name equipname, ");
			listSql2.append(" cab.spec spec,cab.model model,matv.code matvcode,matv.name matvname, ");
			listSql2.append(" cab.rent_type renttype,case cab.rent_calmode when 1 then '��ͬԼ����׼' when 2 then '�����㷽��' end as rentcalmode, ");
			listSql2.append(" cm.method_name methodname,cab.workload_unit workloadunit,cab.num num,cab.pre_rent pre_rent,tax.ntaxrate ntaxrate, ");
			listSql2.append(" cab.pre_renttax pre_renttax,cab.effect_date effect_date,cab.memo memo ");
			listSql2.append(" from ali_cont_alter_b cab ");
			listSql2.append(" left join bd_material_v matv on matv.pk_material=cab.pk_material_v ");
			listSql2.append(" left join pam_cal_means cm on cm.pk_cal_means=cab.pk_cal_means ");
			listSql2.append(" left join jzbd_taxrate tax on tax.pk_taxrate=cab.pk_taxrate ");
			listSql2.append(" where cab.pk_contalter_b=");
			
			String []tabArr2 = {"bodyvos","��ϸ"};
			String [][] bData2={  {"rowno","�к�"},{"billcode","���뵥��"},{"equipcode","�豸����"},{"equipname","�豸����"},
								  {"spec","���"},{"model","�ͺ�"},{"matvcode","���ϱ���"},{"matvname","��������"},
								  {"renttype","���ⷽʽ"},{"rentcalmode","�����㷽ʽ"},{"methodname","�����㷽��"},{"workloadunit","��������λ"},
								  {"num","����"},{"pre_rent","��𵥼�(��˰)"},{"ntaxrate","˰��"},{"pre_renttax","��𵥼�"},
								  {"effect_date","��Ч����"},{"memo","��ע"}
								};
			billData.getData().get("body").get(0).getTabdata().clear();
			CircularlyAccessibleValueObject[] pcbs2 = aggvo.getChildrenVO();
			if(pcbs2 != null && pcbs2.length > 0) {
				for (int i = 0; i < pcbs2.length; i++) {
					List<MobileTabContentVO> bs = getUniqBodyMobileDatas(new StringBuffer(listSql2 + "'"+pcbs2[i].getPrimaryKey()+"'"),bData2,tabArr2,0);
					Object colvalue = bs.get(0).getTabdata().get(0).get(8).getColvalue();
					if (colvalue != null && StringUtils.isNotBlank(colvalue.toString())){
						bs.get(0).getTabdata().get(0).get(8).setColvalue(LeaseTimeRuleVO.fromXml(colvalue.toString()).toString());
					}
					MobileTabDataVO rentmny = bs.get(0).getTabdata().get(0).get(13);
					if(rentmny.getColvalue() != null && rentmny.getColvalue().toString().startsWith(".")) {
						bs.get(0).getTabdata().get(0).get(13).setColvalue("0" + rentmny.getColvalue().toString());
					}
					MobileTabDataVO rentmnytax = bs.get(0).getTabdata().get(0).get(15);
					if(rentmnytax.getColvalue() != null && rentmnytax.getColvalue().toString().startsWith(".")) {
						bs.get(0).getTabdata().get(0).get(15).setColvalue("0" + rentmnytax.getColvalue().toString());
					}
					billData.getData().get("body").get(0).getTabdata().add(bs.get(0).getTabdata().get(0));
				}
			}
		}else if("H5C2".equals(billtype)){//6.���Ĳ������üƻ�-�ƻ�����
			String sql = " select d.name from jzmt_cmplan p  "
					+ " left join bd_defdoc d on p.vdef5=d.pk_defdoc "
					+ " where p.pk_cmplan='"+aggvo.getParentVO().getPrimaryKey()+"'";
			Object plannameobj = dao.executeQuery(sql, new ColumnProcessor());
			if(plannameobj != null) {
				billData.getData().get("head").get(0).getTabdata().get(0).get(5).setColvalue(plannameobj.toString());
			}else{
				billData.getData().get("head").get(0).getTabdata().get(0).get(5).setColvalue(null);
			}
			
			StringBuffer listSql = new StringBuffer();
			listSql.append(" select cpb.crowno crowno,matv.code matvcode,matv.name matvname, ");
			listSql.append(" matv.materialspec matvspec,cpb.vbdef2 brand, ");
			listSql.append(" unit.name unitname,cpb.nastnum nastnum,cpb.ntotalastnum totnum,cpb.dusedate dusedate, ");
			listSql.append(" cpb.vbdef3 req from jzmt_cmplan_b cpb ");
			listSql.append(" left join bd_material_v matv on matv.pk_material=cpb.pk_material_v ");
			listSql.append(" left join bd_measdoc unit on unit.pk_measdoc=cpb.castunitid ");
			listSql.append(" where cpb.pk_cmplan_b= ");
			
			String []tabArr = {"pk_cmplan_b","���Ĳ������üƻ��ӱ�"};
			String [][] bData={{"crowno","�к�"},{"matvcode","���ϱ���"},{"matvname","��������"},{"matvspec","���/�ͺ�"},
								  {"brand","Ʒ��"},{"unitname","��λ"},{"nastnum","��������"},{"totnum","�ۼ�ʹ������"},
								  {"dusedate","Ԥ�ƽ�������"},{"req","����Ҫ��"}
								};
			
			billData.getData().get("body").get(0).getTabdata().clear();
			CircularlyAccessibleValueObject[] pcbs = aggvo.getChildrenVO();
			if(pcbs != null && pcbs.length > 0) {
				for (int i = 0; i < pcbs.length; i++) {
					List<MobileTabContentVO> bs = getUniqBodyMobileDatas(new StringBuffer(listSql + "'"+pcbs[i].getPrimaryKey()+"'"),bData,tabArr,0);
					billData.getData().get("body").get(0).getTabdata().add(bs.get(0).getTabdata().get(0));
				}
			}
		}else if("H5Q2".equals(billtype)){//7.�ɱ�����-����-�ɱ���̯
			String basesql = " select cbs.name cbsname,fac.factorname facname from jzpc_bill_b pcb  "
					+ " left join bd_cbsnode cbs on cbs.pk_cbsnode=pcb.pk_cbs "
					+ " left join resa_factorasoa fac on fac.pk_factorasoa=pcb.celementid  "
					+ " where pcb.pk_pcbill_b=";
			CircularlyAccessibleValueObject[] pcbs = aggvo.getChildrenVO();
			if(pcbs != null && pcbs.length > 0) {
				for (int i = 0; i < pcbs.length; i++) {
					List result = (List)dao.executeQuery(basesql + "'"+pcbs[i].getPrimaryKey()+"'", new MapListProcessor());
					if(result != null) {
						if(((Map)result.get(0)).get("cbsname") != null){
							billData.getData().get("body").get(0).getTabdata().get(i).get(0).setColvalue(((Map)result.get(0)).get("cbsname").toString());
						}
						if(((Map)result.get(0)).get("facname") != null){
							billData.getData().get("body").get(0).getTabdata().get(i).get(1).setColvalue(((Map)result.get(0)).get("facname").toString());
						}
					}
				}
			}
		}else if("H5AA".equals(billtype)){//8.�ְ��깤����-��ͷ-����-�ɱ���̯
			StringBuffer listSql = new StringBuffer();
			listSql.append(" select  p.project_name projname,jc.vname ctname,bs.name secname,bt.billtypename cttype, ");
			listSql.append(" fs.vdef14 isinner,case fs.itaxway  when 1 then 'һ���˰��ʽ' when 2 then '���׼�˰��ʽ' end as itaxway, ");
			listSql.append(" fs.dfinishsettle dfinishsettle,tax.ntaxrate ntaxrate,fs.norigsettlemny norigsettlemny, ");
			listSql.append(" fs.norigsettletaxmny norigsettletaxmny,fs.norigsumsettlemny norigsumsettlemny,fs.norigsumsettletaxmny norigsumsettletaxmny, ");
			listSql.append(" fs.norigsumdeductmny norigsumdeductmny,fs.norigsumdeducttaxmny norigsumdeducttaxmny,fs.norigsuminvdeductmny norigsuminvdeductmny, ");
			listSql.append(" fs.norigsuminvdeducttaxmny norigsuminvdeducttaxmny,fs.norigpaymny norigpaymny,fs.noriginunpymny noriginunpymny,fs.norigreceivetaxmny norigreceivetaxmny ");
			listSql.append(" from jzsub_finish_settle fs ");
			listSql.append(" left join bd_project p on p.pk_project=fs.pk_project ");
			listSql.append(" left join jzsub_contract jc on jc.pk_subcontract=fs.pk_subcontract ");
			listSql.append(" left join bd_supplier bs on bs.pk_supplier=jc.pk_second ");
			listSql.append(" left join bd_billtype bt on bt.pk_billtypeid=jc.pk_conttype ");
			listSql.append(" left join jzbd_taxrate tax on tax.pk_taxrate=fs.pk_taxrate ");
			listSql.append(" where fs.pk_finishsettle='"  +((AggSubFinishSettleVO)aggvo).getParentVO().getPrimaryKey()+ "'");
			
			String []tabArr = {"SubFinishSettleVO","������Ϣ"};
			String [][] headData={{"projname","��Ŀ"},{"ctname","�ְ���ͬ"},{"secname","��ͬ�ҷ�"},{"cttype","��ͬ����"},
								  {"isinner","�Ƿ��ڲ�����"},{"itaxway","��Ŀ��˰��ʽ"},{"dfinishsettle","�깤��������"},{"ntaxrate","˰��"},
								  {"norigsettlemny","���ν�����(��˰)"},{"norigsettletaxmny","���ν�����"},{"norigsumsettlemny","�ۼ��깤�����ܽ��(��˰)"},{"norigsumsettletaxmny","�ۼ��깤�����ܽ��"},
								  {"norigsumdeductmny","�깤�ۼƿ۷�����(��˰)"},{"norigsumdeducttaxmny","�깤�ۼƿ۷�����"},{"norigsuminvdeductmny","�깤�ۼƲ��Ͻ�����(��˰)"},{"norigsuminvdeducttaxmny","�깤�ۼƲ��Ͻ�����"},
								  {"norigpaymny","�Ѹ�����"},{"noriginunpymny","Ӧ��δ�����"},{"norigreceivetaxmny","����Ʊ���"}
								};
			billData.getData().put("head", getUniqHeadMobileDatas(listSql,headData,tabArr,0));
			
			StringBuffer bsql1 = new StringBuffer();
			bsql1.append(" select sc.vname ctname,fp.norigthissettlemny norigthissettlemny,fp.norigthissettletaxmny norigthissettletaxmny, ");
			bsql1.append(" fp.norigsettlemny norigsettlemny,fp.norigsettletaxmny norigsettletaxmny,fp.norigsumsettlemny norigsumsettlemny, ");
			bsql1.append(" fp.norigsumsettletaxmny norigsumsettletaxmny ");
			bsql1.append(" from jzsub_finishsettle_proj fp ");
			bsql1.append(" left join jzsub_contract sc on sc.pk_subcontract=fp.pk_subcontract ");
			bsql1.append(" where fp.pk_finishsettle_proj=");
			
			String []tabArr1 = {"pk_finishsettle_proj","����������"};
			String [][] bData1={{"ctname","��ͬ����"},{"norigthissettlemny","���ν�����(��˰)"},{"norigthissettletaxmny","���ν�����"},{"norigsettlemny","�ѽ�����(��˰)"},
								  {"norigsettletaxmny","�ѽ�����"},{"norigsumsettlemny","�ۼƽ�����(��˰)"},{"norigsumsettletaxmny","�ۼƽ�����"}
								};
			CircularlyAccessibleValueObject[] pcbs1 = ((AggSubFinishSettleVO)aggvo).getTableVO("pk_finishsettle_proj");
			billData.getData().get("body").get(0).getTabdata().clear();
			if(pcbs1 != null && pcbs1.length > 0) {
				for (int i = 0; i < pcbs1.length; i++) {
					List<MobileTabContentVO> bs = getUniqBodyMobileDatas(new StringBuffer(bsql1 + "'"+pcbs1[i].getPrimaryKey()+"'"),bData1,tabArr1,0);
					billData.getData().get("body").get(0).getTabdata().add(bs.get(0).getTabdata().get(0));
				}
			}
			String basesql = " select cbs.name cbsname,fac.factorname facname from jzsub_finishsettle_cost pcb  "
					+ " left join bd_cbsnode cbs on cbs.pk_cbsnode=pcb.pk_cbs "
					+ " left join resa_factorasoa fac on fac.pk_factorasoa=pcb.celementid  "
					+ " where pcb.pk_settle_cost=";
			CircularlyAccessibleValueObject[] pcbs2 = ((AggSubFinishSettleVO)aggvo).getTableVO("pk_settle_cost");
			if(pcbs2 != null && pcbs2.length > 0) {
				for (int i = 0; i < pcbs2.length; i++) {
					List result = (List)dao.executeQuery(basesql + "'"+pcbs2[i].getPrimaryKey()+"'", new MapListProcessor());
					if(result != null) {
						if(((Map)result.get(0)).get("cbsname") != null){
							billData.getData().get("body").get(3).getTabdata().get(i).get(1).setColvalue(((Map)result.get(0)).get("cbsname").toString());
						}
						if(((Map)result.get(0)).get("facname") != null){
							billData.getData().get("body").get(3).getTabdata().get(i).get(2).setColvalue(((Map)result.get(0)).get("facname").toString());
						}
					}
				}
			}
		}else if("H5A9".equals(billtype)){//9.�ְ����Ƚ���-��ͷ-����-�ɱ���̯
			StringBuffer listSql = new StringBuffer();
			listSql.append(" select p.project_name projname,jc.vname ctname,sp.name secname,bt.billtypename cttype, ");
			listSql.append(" js.dlastsettledate dlastsettledate,js.dsettledate dsettledate,tax.ntaxrate ntaxrate,js.norigsettlemny norigsettlemny, ");
			listSql.append(" js.norigsettletaxmny norigsettletaxmny,js.norigsumsettlemny norigsumsettlemny,js.norigsumsettletaxmny norigsumsettletaxmny, ");
			listSql.append(" js.norigpaymny norigpaymny,js.noriginunpymny noriginunpymny,js.norigreceivetaxmny norigreceivetaxmny,js.norigsumreceivetaxmny norigsumreceivetaxmny ");
			listSql.append(" from jzsub_settle js ");
			listSql.append(" left join bd_project p on p.pk_project=js.pk_project ");
			listSql.append(" left join jzsub_contract jc on jc.pk_subcontract=js.pk_subcontract ");
			listSql.append(" left join bd_supplier sp on sp.pk_supplier=jc.pk_second ");
			listSql.append(" left join bd_billtype bt on bt.pk_billtypeid=jc.pk_conttype ");
			listSql.append(" left join jzbd_taxrate tax on tax.pk_taxrate=js.pk_taxrate   ");
			listSql.append(" where js.pk_settle='"  +aggvo.getParentVO().getPrimaryKey()+ "'");
			
			String []tabArr = {"SubSettleVO","������Ϣ"};
			String [][] headData={{"projname","��Ŀ"},{"ctname","�ְ���ͬ"},{"secname","��ͬ�ҷ�"},{"cttype","��ͬ����"},
								  {"dlastsettledate","�ϴν����ֹ����"},{"dsettledate","���ν����ֹ����"},{"ntaxrate","˰��"},{"norigsettlemny","���ν�����(��˰)"},
								  {"norigsettletaxmny","���ν�����"},{"norigsumsettlemny","�ۼƽ�����(��˰)"},{"norigsumsettletaxmny","�ۼƽ�����"},{"norigpaymny","�Ѹ�����"},
								  {"noriginunpymny","Ӧ��δ�����"},{"norigreceivetaxmny","����Ʊ���"},{"norigsumreceivetaxmny","�ۼ���Ʊ���"}
								};
			billData.getData().put("head", getUniqHeadMobileDatas(listSql,headData,tabArr,0));
			
			StringBuffer listSql1 = new StringBuffer();
			listSql1.append(" select sf.norigfeemny norigfeemny,sf.norigfeetaxmny norigfeetaxmny,ps.dbegindate dbegindate,ps.denddate denddate ");
			listSql1.append(" from jzsub_settle_fee sf ");
			listSql1.append(" left join jzsub_projsum ps on ps.pk_projsum=sf.pk_projsum ");
			listSql1.append(" where sf.pk_settle_fee= ");
			
			String []tabArr1 = {"pk_settle_fee","�ְ�����������"};
			String [][] bData1={{"norigfeemny","����ͳ�Ʒ��ý��(��˰)"},{"norigfeetaxmny","����ͳ�Ʒ��ý��"},{"dbegindate","ͳ�ƿ�ʼ����"},{"denddate","ͳ�ƽ�������"}
								};
			CircularlyAccessibleValueObject[] pcbs1 = ((AggSubSettleVO)aggvo).getTableVO("pk_settle_fee");
			billData.getData().get("body").get(0).getTabdata().clear();
			if(pcbs1 != null && pcbs1.length > 0) {
				for (int i = 0; i < pcbs1.length; i++) {
					List<MobileTabContentVO> bs = getUniqBodyMobileDatas(new StringBuffer(listSql1 + "'"+pcbs1[i].getPrimaryKey()+"'"),bData1,tabArr1,0);
					billData.getData().get("body").get(0).getTabdata().add(bs.get(0).getTabdata().get(0));
				}
			}
			
			String basesql = " select cbs.name cbsname,fac.factorname facname from jzsub_settle_cost pcb  "
					+ " left join bd_cbsnode cbs on cbs.pk_cbsnode=pcb.pk_cbs "
					+ " left join resa_factorasoa fac on fac.pk_factorasoa=pcb.celementid  "
					+ " where pcb.pk_settle_cost=";
			CircularlyAccessibleValueObject[] pcbs = ((AggSubSettleVO)aggvo).getTableVO("pk_settle_cost");
			if(pcbs != null && pcbs.length > 0) {
				for (int i = 0; i < pcbs.length; i++) {
					List result = (List)dao.executeQuery(basesql + "'"+pcbs[i].getPrimaryKey()+"'", new MapListProcessor());
					if(result != null) {
						if(((Map)result.get(0)).get("cbsname") != null){
							billData.getData().get("body").get(4).getTabdata().get(i).get(1).setColvalue(((Map)result.get(0)).get("cbsname").toString());
						}
						if(((Map)result.get(0)).get("facname") != null){
							billData.getData().get("body").get(4).getTabdata().get(i).get(2).setColvalue(((Map)result.get(0)).get("facname").toString());
						}
					}
				}
			}
		}else if("4A3F-01".equals(billtype)){//10.������㵥-��ͷ-����-�ɱ���̯
			String sql = " select p.project_name projname,inoutrent.name rentname,hr.name hrname from ali_rent_settle s "
					+ " left join bd_project p on p.pk_project=s.pk_jobmngfil "
					+ " left join bd_defdoc inoutrent on s.def16=inoutrent.pk_defdoc "
					+ " left join bd_defdoc hr on s.def18=hr.pk_defdoc "
					+ " where s.pk_rentsettle='"+aggvo.getParentVO().getPrimaryKey()+"'";
			List r = (List)dao.executeQuery(sql, new MapListProcessor());
			if(r != null) {
				if(((Map)r.get(0)).get("projname") != null){
					billData.getData().get("head").get(0).getTabdata().get(0).get(4).setColvalue(((Map)r.get(0)).get("projname").toString());
				}
				if(((Map)r.get(0)).get("rentname") != null){
					billData.getData().get("head").get(0).getTabdata().get(0).get(7).setColvalue(((Map)r.get(0)).get("rentname").toString());
				}
				if(((Map)r.get(0)).get("hrname") != null){
					billData.getData().get("head").get(0).getTabdata().get(0).get(8).setColvalue(((Map)r.get(0)).get("hrname").toString());
				}
			}
			
			StringBuffer listSql = new StringBuffer();
			listSql.append(" select rsc.rowno rowno,ac.bill_code billcode,ac.contract_name contractname, ");
			listSql.append(" rsc.settlemoney settlemny,tax.ntaxrate ntaxrate,rsc.settlemoneytax settlemoneytax, ");
			listSql.append(" rsc.invoicemoney invoicemoney,rsc.invoicemoneytax invoicemoneytax,rsc.deductiontax deductiontax, ");
			listSql.append(" rsc.suminvoicemoneytax suminvoicemoneytax,rsc.gathermoney gathermoney ");
			listSql.append(" from ali_rent_settle_c rsc  ");
			listSql.append(" left join ali_contract ac on ac.pk_contract=rsc.pk_contract ");
			listSql.append(" left join jzbd_taxrate tax on tax.pk_taxrate=rsc.pk_taxrate ");
			listSql.append(" where rsc.pk_rentsettle_c=");
			
			String []tabArr = {"sumvos","�������"};
			String [][] bData={{"rowno","�к�"},{"billcode","��ͬ����"},{"contractname","��ͬ����"},{"settlemny","������(��˰)"},
								  {"ntaxrate","˰��"},{"settlemoneytax","������(Ԫ)"},{"invoicemoney","����Ʊ���(��˰)"},{"invoicemoneytax","����Ʊ���(Ԫ)"},
								  {"deductiontax","Ԥ����ۻؽ��(Ԫ)"},{"suminvoicemoneytax","�ۼ���Ʊ���(Ԫ)"},{"gathermoney","������"}
								};
			CircularlyAccessibleValueObject[] pcbs = ((AggRentInSettleVO)aggvo).getTableVO("sumvos");
			billData.getData().get("body").get(1).getTabdata().clear();
			if(pcbs != null && pcbs.length > 0) {
				for (int i = 0; i < pcbs.length; i++) {
					List<MobileTabContentVO> bs = getUniqBodyMobileDatas(new StringBuffer(listSql + "'"+pcbs[i].getPrimaryKey()+"'"),bData,tabArr,1);
					billData.getData().get("body").get(1).getTabdata().add(bs.get(0).getTabdata().get(0));
				}
			}
		}else if("4A3A-01".equals(billtype)){//11.���޺�ͬ-��ͷ����
			String sql = " select p.project_name projname,tax.ntaxrate ntaxrate from ali_contract al "
					+ " left join bd_project p on p.pk_project=al.pk_jobmngfil "
					+ " left join jzbd_taxrate tax on tax.pk_taxrate=al.pk_taxrate "
					+ " where al.pk_contract='"+aggvo.getParentVO().getPrimaryKey()+"'";
			List r = (List)dao.executeQuery(sql, new MapListProcessor());
			if(r != null) {
				if(((Map)r.get(0)).get("projname") != null){
					billData.getData().get("head").get(0).getTabdata().get(0).get(7).setColvalue(((Map)r.get(0)).get("projname").toString());
				}
				if(((Map)r.get(0)).get("ntaxrate") != null){
					billData.getData().get("head").get(0).getTabdata().get(0).get(13).setColvalue(((Map)r.get(0)).get("ntaxrate").toString());
				}
			}
			
			StringBuffer listSql = new StringBuffer();
			listSql.append(" select al.rowno rowno,al.def1 eqcode,al.equip_name eqname,al.spec spec,al.model model, ");
			listSql.append(" mat.code matcode,mat.name matname,catg.category_name catgname, ");
			listSql.append(" al.rent_type renttype,case al.rent_calmode when 1 then '��ͬԼ����׼' when 2 then '�����㷽��' end as rentcalmode, ");
			listSql.append(" al.workload_unit workunit,al.num num,al.pre_rent prerent,c.ntaxrate taxrate, ");
			listSql.append(" al.pre_renttax prerenttax,al.rent_day rentday,al.rent_daytax rentdaytax,al.memo memo ");
			listSql.append(" from ali_contract_b al ");
			listSql.append(" left join bd_material_v mat on al.pk_material_v = mat.pk_material ");
			listSql.append(" left join pam_category catg on catg.pk_category=al.pk_category ");
			listSql.append(" left join jzbd_taxrate c on al.pk_taxrate = c.pk_taxrate  ");
			listSql.append(" where pk_contract_b=");
			
			String []tabArr = {"bodyvos","��ͬ����"};
			String [][] bData={   {"rowno","�к�"},{"eqcode","�豸����"},{"eqname","�豸����"},{"spec","���"},
								  {"model","�ͺ�"},{"matcode","���ϱ���"},{"matname","��������"},{"catgname","�豸���"},
								  {"renttype","���ⷽʽ"},{"rentcalmode","�����㷽ʽ"},{"workunit","��������λ"},{"num","����"},
								  {"prerent","��𵥼�(��˰)"},{"taxrate","˰��"},{"prerenttax","��𵥼�(Ԫ)"},{"rentday","�������������(��˰)"},
								  {"rentdaytax","�������������"},{"memo","��ע"}
								};
			CircularlyAccessibleValueObject[] pcbs = ((AggContractInVO)aggvo).getTableVO("bodyvos");
			billData.getData().get("body").get(0).getTabdata().clear();
			if(pcbs != null && pcbs.length > 0) {
				for (int i = 0; i < pcbs.length; i++) {
					List<MobileTabContentVO> bs = getUniqBodyMobileDatas(new StringBuffer(listSql + "'"+pcbs[i].getPrimaryKey()+"'"),bData,tabArr,0);
					Object colvalue = bs.get(0).getTabdata().get(0).get(8).getColvalue();
					if (colvalue != null && StringUtils.isNotBlank(colvalue.toString())){
						bs.get(0).getTabdata().get(0).get(8).setColvalue(LeaseTimeRuleVO.fromXml(colvalue.toString()).toString());
					}
					MobileTabDataVO rentmny = bs.get(0).getTabdata().get(0).get(12);
					if(rentmny.getColvalue() != null && rentmny.getColvalue().toString().startsWith(".")) {
						bs.get(0).getTabdata().get(0).get(12).setColvalue("0" + rentmny.getColvalue().toString());
					}
					MobileTabDataVO drenttaxmny = bs.get(0).getTabdata().get(0).get(14);
					if(drenttaxmny.getColvalue() != null && drenttaxmny.getColvalue().toString().startsWith(".")) {
						bs.get(0).getTabdata().get(0).get(14).setColvalue("0" + drenttaxmny.getColvalue().toString());
					}
					MobileTabDataVO drentmny = bs.get(0).getTabdata().get(0).get(15);
					if(drentmny.getColvalue() != null && drentmny.getColvalue().toString().startsWith(".")) {
						bs.get(0).getTabdata().get(0).get(15).setColvalue("0" + drentmny.getColvalue().toString());
					}
					for(int k = 0;k<bs.size();k++){
						bs.get(k).getTabdata().get(0).get(13).setColvalue(billData.getData().get("head").get(0).getTabdata().get(0).get(13).getColvalue());
					}
					
					billData.getData().get("body").get(0).getTabdata().add(bs.get(0).getTabdata().get(0));
				}
			}
		}else if(billtype != null && billtype.contains("H541")){//12.ʩ���ܳа���ͬ-��ͷ-˰��
			StringBuffer listSql = new StringBuffer();
			listSql.append(" select p.project_name projname,c.vbillcode vbillcode, c.vname vname, bt.billtypename btname, ");
			listSql.append(" spa.name aname,spb.name bname,c.vreserve21 seeunit,tax.ntaxrate ntaxrate, ");
			listSql.append(" c.norigsigntaxmny norigsigntaxmny,c.dbegindate dbegindate,c.denddate denddate,c.vdef10 vdef10, ");
			listSql.append(" c.vmemo paycond, c.vdef22 keepmny, c.vdef6 vdef6 ");
			listSql.append(" from jzct_contract c ");
			listSql.append(" left join bd_project p on p.pk_project=c.pk_project ");
			listSql.append(" left join bd_billtype bt on bt.pk_billtypeid=c.ctranttypeid ");
			listSql.append(" left join bd_supplier spa on spa.pk_supplier=c.pk_customer ");
			listSql.append(" left join bd_supplier spb on spb.pk_supplier=c.pk_supplier ");
			listSql.append(" left join jzbd_taxrate tax on tax.pk_taxrate=c.pk_taxrate ");
			listSql.append(" where c.pk_contract='"+aggvo.getParentVO().getPrimaryKey()+"'");
			
			String []tabArr = {"pk_contract","������Ϣ"};
			String [][] headData={{"projname","��Ŀ����"},{"vbillcode","��ͬ���"},{"vname","��ͬ����"},{"btname","��ͬ����"},
								  {"aname","��ͬ�׷�"},{"bname","��ͬ�ҷ�"},{"seeunit","����λ"},{"ntaxrate","˰��"},
								  {"norigsigntaxmny","��ͬ���(Ԫ)"},{"dbegindate","�ƻ���������"},{"denddate","��������"},{"vdef10","�ܹ���(��)"},
								  {"paycond","��������"},{"keepmny","��Լ��֤��(Ԫ)"},{"vdef6","��������(��)"}
								};
			billData.getData().put("head", getUniqHeadMobileDatas(listSql,headData,tabArr,0));
		}else if(billtype.contains("H545")){//13.����ȷȨ-��ͷ
			StringBuffer listSql = new StringBuffer();
			listSql.append(" select p.project_name projname,ct.vname ctname,js.dbilldate dbilldate, ");
			listSql.append(" case js.fstatusflag when -1 then '����' when 0 then '����δͨ��' when 1 then  '����ͨ��' ");
			listSql.append(" when 2 then  '����������' when 3 then  '���ύ' end as status, ");
			listSql.append(" tax.ntaxrate ntaxrate,js.norigsettlemny norigsettlemny,js.norigsettletaxmny norigsettletaxmny, ");
			listSql.append(" js.norigsumsettlemny norigsumsettlemny,js.norigsumsettletaxmny norigsumsettletaxmny ");
			listSql.append(" from jzct_settle js  ");
			listSql.append(" left join bd_project p on p.pk_project=js.pk_project ");
			listSql.append(" left join jzct_contract ct on ct.pk_contract=js.pk_contract ");
			listSql.append(" left join jzbd_taxrate tax on tax.pk_taxrate=js.pk_taxrate ");
			listSql.append(" where js.pk_settle='"  +((AggInCmSettleVO)aggvo).getParentVO().getPrimaryKey()+ "'");
			
			String []tabArr = {"CmSettleVO","������Ϣ"};
			String [][] headData={{"projname","��Ŀ"},{"ctname","��ͬ"},{"dbilldate","��������"},{"status","����״̬"},
								  {"ntaxrate","˰��"},{"norigsettlemny","���ν�����(��˰)"},{"norigsettletaxmny","���ν�����"},{"norigsumsettlemny","�ۼƽ�����(��˰)"},
								  {"norigsumsettletaxmny","�ۼƽ�����"}
								};
			List<MobileTabContentVO> headdatas = getUniqHeadMobileDatas(listSql,headData,tabArr,0);
			MobileTabDataVO settlemny = headdatas.get(0).getTabdata().get(0).get(5);
			if(settlemny.getColvalue() != null && settlemny.getColvalue().toString().startsWith(".")) {
				headdatas.get(0).getTabdata().get(0).get(5).setColvalue("0" + settlemny.getColvalue().toString());
			}
			billData.getData().put("head", headdatas);
		}else if(billtype != null && billtype.contains("H5A1")){//13.�ְ���ͬ-��ͷ
			StringBuffer listSql = new StringBuffer();
			listSql.append(" select p.project_name projname,sc.vname vname,sc.vbillcode vbillcode,bt.billtypename btname, ");
			listSql.append(" case sc.bisinnercont when 'N' then '��' when 'Y' then '��' end as isinnercont, ");
			listSql.append(" sp.name spname,sc.vcontfulfill content,tax.ntaxrate ntaxrate,sc.norignsigntaxmny norignsigntaxmny, ");
			listSql.append(" sc.noriginprpymny noriginprpymny,sc.dcontsigndate dcontsigndate,sc.vdef6 ctdate,sc.norigincurrmny norigincurrmny, ");
			listSql.append(" sc.norigincurrtaxmny norigincurrtaxmny,sc.noriginsumstlmny noriginsumstlmny,sc.noriginsumstltaxmny noriginsumstltaxmny ");
			listSql.append(" from jzsub_contract sc  ");
			listSql.append(" left join bd_project p on p.pk_project=sc.pk_project ");
			listSql.append(" left join bd_billtype bt on bt.pk_billtypeid=sc.pk_conttype ");
			listSql.append(" left join bd_supplier sp on sp.pk_supplier=sc.pk_second ");
			listSql.append(" left join jzbd_taxrate tax on tax.pk_taxrate=sc.pk_taxrate ");
			listSql.append(" where sc.pk_subcontract='"  +((AggSubContractVO)aggvo).getParentVO().getPrimaryKey()+ "'");
			
			String []tabArr = {"SubContractVO","������Ϣ"};
			String [][] headData={{"projname","��Ŀ"},{"vname","��ͬ����"},{"vbillcode","��ͬ����"},{"btname","�ְ���ʽ"},
								  {"isinnercont","�Ƿ��ڲ��ְ�"},{"spname","�ְ���λ"},{"content","�ְ�����"},{"ntaxrate","˰��"},
								  {"norignsigntaxmny","��ͬǩ�����(Ԫ)"},{"noriginprpymny","Ԥ������(Ԫ)"},{"dcontsigndate","��ͬǩ������"},{"ctdate","��ͬ���ڣ��죩"},
								  {"norigincurrmny","�ֺ�ͬ���(��˰)"},{"norigincurrtaxmny","�ֺ�ͬ���"},{"noriginsumstlmny","�ۼƽ�����(��˰)"},{"noriginsumstltaxmny","�ۼƽ�����"},
								};
			billData.getData().put("head", getUniqHeadMobileDatas(listSql,headData,tabArr,0));
			
			
			String basesql = " select tax.ntaxrate ntaxrate,unit.name unitname "
							+ " from jzsub_contfee sc "
							+ " left join bd_measdoc unit on unit.pk_measdoc=sc.cunitid "
							+ " left join jzbd_taxrate tax on tax.pk_taxrate=sc.pk_taxrate "
							+ " where sc.pk_contfee=";
			CircularlyAccessibleValueObject[] pcbs = aggvo.getChildrenVO();
			if(pcbs != null && pcbs.length > 0) {
				for (int i = 0; i < pcbs.length; i++) {
					List result = (List)dao.executeQuery(basesql + "'"+pcbs[i].getPrimaryKey()+"'", new MapListProcessor());
					if(result != null) {
						if(((Map)result.get(0)).get("unitname") != null){
							billData.getData().get("body").get(0).getTabdata().get(i).get(3).setColvalue(((Map)result.get(0)).get("unitname").toString());
						}
						if(((Map)result.get(0)).get("ntaxrate") != null){
							billData.getData().get("body").get(0).getTabdata().get(i).get(5).setColvalue(((Map)result.get(0)).get("ntaxrate").toString());
						}
					}
				}
			}
		}else if("H5C3".equals(billtype)){//14.��ת�Ͼ��ܿؼƻ�-����
			StringBuffer listSql = new StringBuffer();
			listSql.append(" select rpb.crowno rowno,mat.code matcode,mat.name matname,mat.materialspec spec, ");
			listSql.append(" mat.materialtype mtype,unit.name unitname,rpb.nastnum nastnum,rpb.nastappendnum nastappendnum, ");
			listSql.append(" rpb.nastsumappendnum nastsumappendnum,rpb.nastsumnum nastsumnum,rpb.nastsumctrlwishnum nastsumctrlwishnum,rpb.dindate dindate, ");
			listSql.append(" rpb.doutdate doutdate,rpb.vmemo vmemo ");
			listSql.append(" from jzmt_rmtplan_b rpb ");
			listSql.append(" left join bd_material_v mat on rpb.pk_material_v = mat.pk_material   ");
			listSql.append(" left join bd_measdoc unit on unit.pk_measdoc=rpb.castunitid ");
			listSql.append(" where rpb.pk_rmtplan_b= ");
			
			String []tabArr = {"pk_rmtplan_b","��ת�����ܿؼƻ��ӱ�"};
			String [][] bData={   {"rowno","�к�"},{"matcode","���ϱ���"},{"matname","��������"},{"spec","���"},
								  {"mtype","�ͺ�"},{"unitname","��λ"},{"nastnum","ԭ�ܿ�����"},{"nastappendnum","����׷������"},
								  {"nastsumappendnum","�ۼ�׷������"},{"nastsumnum","�ܿ������ϼ�"},{"nastsumctrlwishnum","�ܿ����üƻ�����"},{"dindate","��������"},
								  {"doutdate","�˳�����"},{"vmemo","��ע"}
								};
			CircularlyAccessibleValueObject[] pcbs = aggvo.getChildrenVO();
			billData.getData().get("body").get(0).getTabdata().clear();
			if(pcbs != null && pcbs.length > 0) {
				for (int i = 0; i < pcbs.length; i++) {
					List<MobileTabContentVO> bs = getUniqBodyMobileDatas(new StringBuffer(listSql + "'"+pcbs[i].getPrimaryKey()+"'"),bData,tabArr,0);
					billData.getData().get("body").get(0).getTabdata().add(bs.get(0).getTabdata().get(0));
				}
			}
		}else if("H5C4".equals(billtype)){//15.��ת�Ͼ����üƻ�-����
			StringBuffer listSql = new StringBuffer();
			listSql.append(" select rb.crowno rowno,matv.code matvcode,matv.name matvname,matv.materialspec matvspec, ");
			listSql.append(" matv.materialtype matvtype,unit.name unitname,rb.nastnum nastnum,rb.dindate dindate, ");
			listSql.append(" rb.doutdate doutdate,rb.vmemo vmemo ");
			listSql.append(" from jzmt_rmplan_b  rb ");
			listSql.append(" left join bd_material_v matv on matv.pk_material=rb.pk_material_v ");
			listSql.append(" left join bd_measdoc unit on unit.pk_measdoc=rb.castunitid   ");
			listSql.append(" where rb.pk_rmplan_b=");
			
			String []tabArr = {"pk_rmplan_b","��ת�������üƻ��ӱ�"};
			String [][] bData={   {"rowno","�к�"},{"matvcode","���ϱ���"},{"matvname","��������"},{"matvspec","���"},
								  {"matvtype","�ͺ�"},{"unitname","��λ"},{"nastnum","��������"},{"dindate","Ԥ�ƽ�������"},
								  {"doutdate","Ԥ���˳�����"},{"vmemo","��ע"}
								};
			CircularlyAccessibleValueObject[] pcbs = aggvo.getChildrenVO();
			billData.getData().get("body").get(0).getTabdata().clear();
			if(pcbs != null && pcbs.length > 0) {
				for (int i = 0; i < pcbs.length; i++) {
					List<MobileTabContentVO> bs = getUniqBodyMobileDatas(new StringBuffer(listSql + "'"+pcbs[i].getPrimaryKey()+"'"),bData,tabArr,0);
					billData.getData().get("body").get(0).getTabdata().add(bs.get(0).getTabdata().get(0));
				}
			}
		}else if("H5C1".equals(billtype)){//16.���Ĳ����ܿؼƻ�-����
			StringBuffer listSql1 = new StringBuffer();
			listSql1.append(" select matv.code matvcode,matv.name matvname,matv.materialspec matvspec,matv.materialtype matvtype, ");
			listSql1.append(" unit.name unitname,cmb.nastnum nastnum,cmb.nastappendnum nastappendnum,cmb.nastsumappendnum nastsumappendnum, ");
			listSql1.append(" cmb.nastsumnum nastsumnum,cmb.vmemo vmemo ");
			listSql1.append(" from jzmt_cmtplan_b cmb ");
			listSql1.append(" left join bd_material_v matv on matv.pk_material=cmb.pk_material_v ");
			listSql1.append(" left join bd_measdoc unit on unit.pk_measdoc=cmb.castunitid ");
			listSql1.append(" where cmb.pk_cmtplan_b=");
			
			String []tabArr1 = {"pk_cmtplan_b","������ϸ"};
			String [][] bData1={   {"matvcode","���ϱ���"},{"matvname","��������"},{"matvspec","���"},{"matvtype","�ͺ�"},
								  {"unitname","��λ"},{"nastnum","ԭ�ܿ�����"},{"nastappendnum","����׷������"},{"nastsumappendnum","�ۼ�׷������"},
								  {"nastsumnum","�ܿ������ϼ�"},{"vmemo","��ע"}
								};
			CircularlyAccessibleValueObject[] pcbs1 = ((AggCmtplanVO)aggvo).getTableVO("pk_cmtplan_b");
			billData.getData().get("body").get(0).getTabdata().clear();
			if(pcbs1 != null && pcbs1.length > 0) {
				for (int i = 0; i < pcbs1.length; i++) {
					List<MobileTabContentVO> bs = getUniqBodyMobileDatas(new StringBuffer(listSql1 + "'"+pcbs1[i].getPrimaryKey()+"'"),bData1,tabArr1,0);
					billData.getData().get("body").get(0).getTabdata().add(bs.get(0).getTabdata().get(0));
				}
			}
			
			String []tabArr2 = {"pk_cmtplan_bb","���ϻ���"};
			String [][] bData2={   {"matvcode","���ϱ���"},{"matvname","��������"},{"matvspec","���"},{"matvtype","�ͺ�"},
					{"unitname","��λ"},{"nastnum","ԭ�ܿ�����"},{"nastappendnum","����׷������"},{"nastsumappendnum","�ۼ�׷������"},
					{"nastsumnum","�ܿ������ϼ�"},{"vmemo","��ע"}
			};
			billData.getData().get("body").get(1).getTabdata().clear();
			if(pcbs1 != null && pcbs1.length > 0) {
				for (int i = 0; i < pcbs1.length; i++) {
					List<MobileTabContentVO> bs = getUniqBodyMobileDatas(new StringBuffer(listSql1 + "'"+pcbs1[i].getPrimaryKey()+"'"),bData2,tabArr2,1);
					billData.getData().get("body").get(1).getTabdata().add(bs.get(0).getTabdata().get(0));
				}
			}
		}else if("H5Q6".equals(billtype)){//17.���Ĳ��ϳɱ�-����
			StringBuffer listSql = new StringBuffer();
			listSql.append(" select cbs.name cbsname,fac.factorname facname,cc.vbdef1 tmcf,cc.ncostmny ncostmny ");
			listSql.append(" from jzpc_cmt_cost cc ");
			listSql.append(" left join bd_cbsnode cbs on cbs.pk_cbsnode=cc.pk_cbs ");
			listSql.append(" left join resa_factorasoa fac on fac.pk_factorasoa=cc.celementid ");
			listSql.append(" where cc.pk_pcbill_b=");
			
			String []tabArr = {"pk_pcbill_b","�ɱ�����"};
			String [][] bData={   {"cbsname","�����������"},{"facname","����Ҫ������"},
								  {"tmcf","��ľ����Ҫ��"},{"ncostmny","���ܽ��"}
								};
			CircularlyAccessibleValueObject[] pcbs = aggvo.getChildrenVO();
			billData.getData().get("body").get(0).getTabdata().clear();
			if(pcbs != null && pcbs.length > 0) {
				for (int i = 0; i < pcbs.length; i++) {
					List<MobileTabContentVO> bs = getUniqBodyMobileDatas(new StringBuffer(listSql + "'"+pcbs[i].getPrimaryKey()+"'"),bData,tabArr,0);
					billData.getData().get("body").get(0).getTabdata().add(bs.get(0).getTabdata().get(0));
				}
			}
		}else if("H543".equals(billtype)){//18.�Լױ��-��ͷ
			String sql = " select p.project_name projname,jc.vname ctname "
						+ " from jzct_change jc "
						+ " left join bd_project p on p.pk_project=jc.pk_project "
						+ " left join jzct_contract ct on ct.pk_contract=jc.pk_contract "
						+ " where jc.pk_change='"+aggvo.getParentVO().getPrimaryKey()+"'";
			
			List r = (List)dao.executeQuery(sql, new MapListProcessor());
			if(r != null) {
				if(((Map)r.get(0)).get("projname") != null){
					billData.getData().get("head").get(0).getTabdata().get(0).get(0).setColvalue(((Map)r.get(0)).get("projname").toString());
				}
				if(((Map)r.get(0)).get("ctname") != null){
					billData.getData().get("head").get(0).getTabdata().get(0).get(1).setColvalue(((Map)r.get(0)).get("ctname").toString());
				}
			}
		}else if(billtype != null && billtype.contains("4455")){//19.�������뵥-����
			StringBuffer listSql = new StringBuffer();
			listSql.append(" select isb.crowno rowno,matv.code matvcode,matv.name matvname,matv.materialspec materialspec, ");
			listSql.append(" munit.name munitname,unit.name unitname,isb.vchangerate vchangerate,isb.nnum nnum, ");
			listSql.append(" isb.nassistnum nassistnum,isb.dplanrecvdate dplanrecvdate,isb.vnotebody vnotebody ");
			listSql.append(" from ic_sapply_b isb ");
			listSql.append(" left join bd_material_v matv on matv.pk_material=isb.cmaterialvid ");
			listSql.append(" left join bd_measdoc munit on munit.pk_measdoc=isb.cunitid   ");
			listSql.append(" left join bd_measdoc unit on unit.pk_measdoc=isb.castunitid ");
			listSql.append(" where isb.cgeneralbid=");
			
			String []tabArr = {"cgeneralbid","�������뵥��ϸ"};
			String [][] bData={   {"rowno","�к�"},{"matvcode","���ϱ���"},{"matvname","��������"},
								  {"materialspec","���"},{"munitname","����λ"},{"unitname","��λ"},
								  {"vchangerate","������"},{"nnum","����������"},{"nassistnum","��������"},
								  {"dplanrecvdate","�ƻ���������"},{"vnotebody","��ע"}
								};
			CircularlyAccessibleValueObject[] pcbs = aggvo.getChildrenVO();
			billData.getData().get("body").get(0).getTabdata().clear();
			if(pcbs != null && pcbs.length > 0) {
				for (int i = 0; i < pcbs.length; i++) {
					List<MobileTabContentVO> bs = getUniqBodyMobileDatas(new StringBuffer(listSql + "'"+pcbs[i].getPrimaryKey()+"'"),bData,tabArr,0);
					billData.getData().get("body").get(0).getTabdata().add(bs.get(0).getTabdata().get(0));
				}
			}
		}else if("H550".equals(billtype)){//21.��������-��ͷ-����
			PrPayPlanSettleVO[] settles = (PrPayPlanSettleVO[])((AggPrPayPlanVO)aggvo).getTableVO("pk_payplan_settle");
			String sql = " select norigpaiedmny from jzpr_payplan_settle where pk_payplan_settle='";
			if(settles != null) {
				for (int i = 0; i < settles.length; i++) {
					Object norigpaiedmnyobj = dao.executeQuery(sql + settles[i].getPk_payplan_settle() + "'", new ColumnProcessor());
					if(norigpaiedmnyobj != null) {
						billData.getData().get("body").get(0).getTabdata().get(i).get(17).setColvalue(norigpaiedmnyobj);
					}
				}
			}
			billData.getData().get("head").get(0).getTabdata().addAll(billData.getData().get("head").get(1).getTabdata());
		}
	}

	private void dealSecondBodyTemptDatas(String corp, String userid, String billtype,
			AggregatedValueObject aggvo, MobileBillData billData)
			throws DAOException {
		try {
			if(billtype != null && !"".equals(billtype)) {
				billtype = BillTypeModelTrans.getInstance().getModelByBillType(billtype).getBillTypeCode();
				String pk_templet_b = MobileMessageUtil.getOABillTempletPkByBillType(billtype + "-B1", TEMPLATE_PREFIX);
				if (pk_templet_b != null && !"".equals(pk_templet_b)) {
					NCBillAccessBillTemplate bab = new NCBillAccessBillTemplate(pk_templet_b);
					bab.loadTemplate();
					bab.setBillVO(aggvo);
					MobileBillData bodyBillData = bab.billVO2Map(corp, userid);
					if("10GY".equals(billtype)) {
						fillSupplierPfInfo(aggvo, billData, bodyBillData);
					} else if("10KH".equals(billtype)) {
						fillCustPfInfo(aggvo, billData, bodyBillData);
					} else {
						billData.getData().put("body", bodyBillData.getData().get("body"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void fillCustPfInfo(AggregatedValueObject aggvo,
			MobileBillData billData, MobileBillData bodyBillData)
			throws DAOException {
		List<MobileTabContentVO> list = bodyBillData.getData().get("head");
		
		CustomerVO customervo = (CustomerVO)aggvo.getParentVO().getAttributeValue("customerinfo");
		if(customervo.getPk_org() != null) {
			list.get(0).getTabdata().get(0).get(0).setColvalue(getOrgName(customervo.getPk_org()));
		}
		list.get(0).getTabdata().get(0).get(1).setColvalue(customervo.getCode());
		list.get(0).getTabdata().get(0).get(2).setColvalue(customervo.getName());
		list.get(0).getTabdata().get(0).get(3).setColvalue(customervo.getEname());
		if(customervo.getPk_custclass() != null) {
			list.get(0).getTabdata().get(0).get(4).setColvalue(getCustclassName(customervo.getPk_custclass()));
		}
		list.get(0).getTabdata().get(0).get(5).setColvalue(customervo.getTaxpayerid());
		if(customervo.getPk_customer_main() != null) {
			list.get(0).getTabdata().get(0).get(6).setColvalue(getCustomerName(customervo.getPk_customer_main()));
		}
		if(customervo.getPk_country() != null) {
			list.get(0).getTabdata().get(0).get(7).setColvalue(getCountryName(customervo.getPk_country()));
		}
		list.get(0).getTabdata().get(0).get(8).setColvalue(customervo.getTel1());
//		list.get(0).getTabdata().get(0).get(9).setColvalue(customervo.getMemo());
		billData.getData().put("body", list);
		
		fillBankInfo(billData, "custbanks", "�ͻ������˺�", customervo.getExbeanname_tabvo_map().get("cust_bank"));
	}

	private void fillSupplierPfInfo(AggregatedValueObject aggvo,
			MobileBillData billData, MobileBillData bodyBillData)
			throws DAOException {
		List<MobileTabContentVO> list = bodyBillData.getData().get("head");
		
		SupplierVO supervo = (SupplierVO)aggvo.getParentVO().getAttributeValue("bsupbaseinfo");
		list.get(0).getTabdata().get(0).get(0).setColvalue(supervo.getCode());
		list.get(0).getTabdata().get(0).get(1).setColvalue(supervo.getName());
		if(supervo.getPk_supplierclass() != null) {
			list.get(0).getTabdata().get(0).get(2).setColvalue(getSupperclassnameByPk(supervo.getPk_supplierclass()));
		}
		list.get(0).getTabdata().get(0).get(3).setColvalue(supervo.getTaxpayerid());
		if(supervo.getPk_country() != null) {
			list.get(0).getTabdata().get(0).get(4).setColvalue(getCountryName(supervo.getPk_country()));
		}
		list.get(0).getTabdata().get(0).get(5).setColvalue(supervo.getTel1());
//		list.get(0).getTabdata().get(0).get(6).setColvalue(supervo.getMemo());
		billData.getData().put("body", list);
		
		fillBankInfo(billData, "supbankacc", "��Ӧ�������˺�", supervo.getExbeanname_tabvo_map().get("sup_bank"));
	}

	private void fillBankInfo(MobileBillData billData, String tablecode,
			String tabname, List<SuperVO> banks) throws DAOException {
		String []tabArr = { tablecode,tabname };
		String [][] bData={ {"accnum","�˺�"},{"accname","����"},{"docname","��������"},{"typename","�������" },
							{"bcname","����"},{"psn","��ϵ��"},{"tel","��ϵ�绰" }};
		
		if(banks != null && banks.size() > 0) {
			for (int i = 0; i < banks.size(); i++) {
				List<MobileTabContentVO> bs = getBankMobileDatas(banks.get(i),bData,tabArr,1);
				if(billData.getData().get("body").size() == 1) {
					billData.getData().get("body").add(bs.get(0));
				} else {
					billData.getData().get("body").get(1).getTabdata().add(bs.get(0).getTabdata().get(0));
				}
			}
		}
	}
	
	private List<MobileTabContentVO> getUniqHeadMobileDatas(StringBuffer listSql,String[][] headData,String []tabArr,Integer pos) throws DAOException{
		MobileTabContentVO dataMobile = new MobileTabContentVO();
		List<Map<String,String>> sqlResult = this.getSqlResult(listSql);
		
		List<List<MobileTabDataVO>> mobileRealData = this.getMobileData(sqlResult, headData);
		dataMobile.setCode(tabArr[0]);
		dataMobile.setTabTitle(tabArr[1]);
		dataMobile.setPos(pos);
		dataMobile.setDatacount(mobileRealData.size()+"");
		dataMobile.setTabdata(mobileRealData);
		
		return Arrays.asList(new Object[]{dataMobile});
	}
	
	private List<MobileTabContentVO> getUniqBodyMobileDatas(StringBuffer listSql,String[][] bodyData,String []tabArr,Integer pos) throws DAOException{
		MobileTabContentVO dataMobile = new MobileTabContentVO();
		List<Map<String,String>> sqlResult = this.getSqlResult(listSql);
		
		List<List<MobileTabDataVO>> mobileRealData = this.getMobileData(sqlResult, bodyData);
		dataMobile.setCode(tabArr[0]);
		dataMobile.setTabTitle(tabArr[1]);
		dataMobile.setPos(pos);
		dataMobile.setDatacount(mobileRealData.size()+"");
		dataMobile.setTabdata(mobileRealData);
		
		return Arrays.asList(new Object[]{dataMobile});
		
	}
	
	private List<MobileTabContentVO> getBankMobileDatas(SuperVO supervo,String[][] bodyData,String []tabArr,Integer pos) throws DAOException{
		MobileTabContentVO dataMobile = new MobileTabContentVO();
		Map<String, String> bankmap = new HashMap<>();
		bankmap.put("ACCNUM", supervo.getAttributeValue("accnum") != null ? supervo.getAttributeValue("accnum").toString() : "");
		bankmap.put("ACCNAME", supervo.getAttributeValue("accname") != null ? supervo.getAttributeValue("accname").toString() : "");
		bankmap.put("DOCNAME", "");
		bankmap.put("TYPENAME", "");
		bankmap.put("BCNAME", "�����");
		bankmap.put("PSN", supervo.getAttributeValue("contactpsn") != null ? supervo.getAttributeValue("contactpsn").toString() : "");
		bankmap.put("TEL", supervo.getAttributeValue("tel") != null ? supervo.getAttributeValue("tel").toString() : "");
		
		BaseDAO dao = new BaseDAO();
		if(supervo.getAttributeValue("pk_bankdoc") != null) {
			String sql = " select name from bd_bankdoc where pk_bankdoc='" + supervo.getAttributeValue("pk_bankdoc").toString()+"'";
			Object docnameobj = dao.executeQuery(sql, new ColumnProcessor());
			if(docnameobj != null) {
				bankmap.put("DOCNAME", docnameobj.toString());
			}
		}
		if(supervo.getAttributeValue("pk_banktype") != null) {
			String sql = " select name from bd_banktype where pk_banktype='" + supervo.getAttributeValue("pk_banktype").toString()+"'";
			Object banktypeobj = dao.executeQuery(sql, new ColumnProcessor());
			if(banktypeobj != null) {
				bankmap.put("TYPENAME", banktypeobj.toString());
			}
		}
		
		List<Map<String,String>> bankmaplist = new ArrayList<Map<String,String>>();
		bankmaplist.add(bankmap);
		
		List<List<MobileTabDataVO>> mobileRealData = this.getMobileData(bankmaplist, bodyData);
		dataMobile.setCode(tabArr[0]);
		dataMobile.setTabTitle(tabArr[1]);
		dataMobile.setPos(pos);
		dataMobile.setDatacount(mobileRealData.size()+"");
		dataMobile.setTabdata(mobileRealData);
		
		return Arrays.asList(new Object[]{dataMobile});
	}
	
	
	

	

	
	/**
	 * �ж϶�ʧ�Ƿ�Ϊ��ͷ
	 */
	private void suppLosePart(String billtype,MobileBillData billData,String billid)throws BusinessException{
		try {
			List<Map<String,String>> xmlFileData=BillTypeModelTrans.getInstance().getLoseData(billtype).getDatalist();
			if(xmlFileData.size()>0){
			for(int i = 0 ; i < xmlFileData.size(); i++) {
				String listSql =xmlFileData.get(i).get("listSql"); 
				String[][] bodyData=toTwoArray(xmlFileData.get(i).get("dataArr"));
				int pos=Integer.parseInt(xmlFileData.get(i).get("posMark"));
				String[] title= xmlFileData.get(i).get("title").split(",");
				String dataMark=xmlFileData.get(i).get("mark");	
				listSql=dealSql(listSql,billid);
				if("body".equals(dataMark)){
					List<MobileTabContentVO> bodydata = getMobileDatas(billData.getData().get("body"),listSql,bodyData,title,pos);
					billData.getData().put("body", bodydata);
				}else{
					List<MobileTabContentVO> bodydata = getMobileDatas(billData.getData().get("head"),listSql,bodyData,title,pos);
					billData.getData().put("head", bodydata);
				}
				 
			}
		}			
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("XML��ͷ�Զ���������");
		
		}
	}
	
	/**
	 * ��xml�е�sql���д����滻��ʶ��
	 * @return 
	 */
	private String dealSql(String listsql,String billid) throws Exception{
		String[] splitArr=listsql.split("#billid#");
		listsql="";
		for(int i=0;i<splitArr.length;i++){
			if(i < splitArr.length-1){
				listsql=listsql+splitArr[i]+billid;
			}else{
				listsql=listsql+splitArr[i];
			}
		}
		return listsql;	
	}

	
	/**
	 * �ַ���ת��Ϊ��ά����
	 */
	private static String[][] toTwoArray(String arr) throws Exception{
		String ontArr[]=arr.split("#");
	
		String[][] bodyStr=new String[ontArr.length][]; 
		 for(int i=0;i <ontArr.length;i++){ 
			 bodyStr [i] = ontArr[i].split(","); 
		 }   
		return bodyStr;
	}
	
	/**
	 * �����ͷ������������ģ������
	 */
	private void dealHeadAndBodySep(String corp, String billtype,
			String userid, AggregatedValueObject aggvo, MobileBillData billData)
			throws BusinessException {
		//�����ͷ�ͱ�������������ģ���������踴�Ƶ������ģ��Ϊ"MBL"+billtype+"-B1",�硰���Ĳ����ܿؼƻ����Ƴ�MBLH5C1-B1"
		String pk_templet1 = getTemplateID(billtype+"-B1");
		if (pk_templet1 != null && !"".equals(pk_templet1)) {
			NCBillAccessBillTemplate ba1 = new NCBillAccessBillTemplate(pk_templet1);
			ba1.loadTemplate();
			ba1.setBillVO(aggvo);
			MobileBillData billData2 = ba1.billVO2Map(corp, userid);
			Map<String, List<MobileTabContentVO>> map = billData.getData();
			Map<String, List<MobileTabContentVO>> map2 = billData2.getData();
			if (map.get("head") == null || map.get("head").size() == 0) {
				map.put("head", map2.get("head"));
			}
			if (map.get("body") == null || map.get("body").size() == 0) {
				map.put("body", map2.get("body"));
			}
			billData.setData(map);
		}
	}

	
	/**
	 * ӳ��ncģ�涪ʧ��ͷ��Ϣ��sql��ȡ
	 * �Զ����������������
	 * @param heads 
	 * @param listSql
	 * @param headData
	 * @param tabArr
	 * @return
	 * @throws DAOException
	 */
	private List<MobileTabContentVO> getMobileDatas(List<MobileTabContentVO> heads,
			String listSql,String[][] headData,String []tabArr,Integer pos) throws DAOException{
		MobileTabContentVO dataMobile = new MobileTabContentVO();
		//ִ��sql
		List<Map<String,String>> sqlResult = this.getSqlResult(listSql,heads);
		
		//�������
		List<List<MobileTabDataVO>> mobileRealData = this.getMobileData(sqlResult,headData);
		
		dataMobile.setCode(tabArr[0]);
		dataMobile.setTabTitle(tabArr[1]);
		dataMobile.setPos(pos);
		dataMobile.setDatacount(mobileRealData.size()+"");
		
		dataMobile.setTabdata(mobileRealData);
		
		heads.add(dataMobile);
		
		return heads;

	}
	
	//��ȡsql���ݼ�
	@SuppressWarnings("unchecked")
	private List<Map<String, String>> getSqlResult(String listsql,List<MobileTabContentVO> heads) throws DAOException{
		//�滻sql�Ĳ���
		BaseDAO dao = new BaseDAO();
		List<Map<String,String>> sqlResult = new ArrayList<Map<String,String>>();
		sqlResult = (List<Map<String,String>>) dao.executeQuery(listsql.toString(), new ResultSetProcessor() {
			public Object handleResultSet(ResultSet rs) throws SQLException {
				List<Map<String,String>> temp_trs = new ArrayList<Map<String,String>>();
				ResultSetMetaData rsmd = rs.getMetaData();
				int count=rsmd.getColumnCount();
				String[] names=new String[count];
				for(int i=0;i<count;i++){
					names[i]=rsmd.getColumnName(i+1);
				}
				while (rs.next()) {
					Map<String,String> map = new HashMap<String,String>();
					for(int i=0;i<count;i++){
						map.put(names[i], rs.getString(names[i]));
					}
					temp_trs.add(map);
				}
				
				return temp_trs;
			}
		});
		return sqlResult;
	}
	
	//��ȡsql���ݼ�
		@SuppressWarnings("unchecked")
		private List<Map<String, String>> getSqlResult(StringBuffer listsql) throws DAOException{
			BaseDAO dao = new BaseDAO();
			List<Map<String,String>> sqlResult = new ArrayList<Map<String,String>>();
			sqlResult = (List<Map<String,String>>) dao.executeQuery(listsql.toString(), new ResultSetProcessor() {
				public Object handleResultSet(ResultSet rs) throws SQLException {
					List<Map<String,String>> temp_trs = new ArrayList<Map<String,String>>();
					ResultSetMetaData rsmd = rs.getMetaData();
					int count=rsmd.getColumnCount();
					String[] names=new String[count];
					for(int i=0;i<count;i++){
						names[i]=rsmd.getColumnName(i+1);
					}
					while (rs.next()) {
						Map<String,String> map = new HashMap<String,String>();
						for(int i=0;i<count;i++){
							map.put(names[i], rs.getString(names[i]));
						}
						temp_trs.add(map);
					}
					
					return temp_trs;
				}
			});
			return sqlResult;
		}
	
	
	//����sql���ݼ��������
	private List<List<MobileTabDataVO>> getMobileData(List<Map<String,String>> sqlResult,String[][] headData){
		List<List<MobileTabDataVO>> mobileData=new ArrayList<List<MobileTabDataVO>>();

		   //������Ϣ
		   for(Map<String,String> result:sqlResult){
			   List<MobileTabDataVO> dataresult = new ArrayList<MobileTabDataVO>();
			   for(int i=0;i<headData.length;i++){		
				   MobileTabDataVO dataArr = new MobileTabDataVO();
				   dataArr.setColkey(headData[i][0]);
				   dataArr.setColname(headData[i][1]);
				   dataArr.setColvalue(result.get(headData[i][0].toUpperCase()));
				   dataresult.add(dataArr);
			   }
			   mobileData.add(dataresult);
		   }

		return mobileData;
	}
	
	/**
	 * �Ƿ���Ը���
	 * **/
	private boolean canTransfer(WorkflownoteVO worknoteVO) {
		Object value = worknoteVO.getRelaProperties().get(XPDLNames.CAN_TRANSFER);
		if (value != null && "true".equalsIgnoreCase(value.toString())) {
			if (worknoteVO.actiontype.endsWith(WorkflownoteVO.WORKITEM_ADDAPPROVER_SUFFIX))
				return false;
			else
				return true;
		} else
			return false;
	}

	/**
	 * �Ƿ���Լ�ǩ
	 * **/
	private boolean canAddApprover(WorkflownoteVO worknoteVO) {// �Ƿ���Լ�ǩ
		Object value = worknoteVO.getRelaProperties().get(XPDLNames.CAN_ADDAPPROVER);
		if (value != null && "true".equalsIgnoreCase(value.toString())) {
			if (worknoteVO.actiontype.endsWith(WorkflownoteVO.WORKITEM_ADDAPPROVER_SUFFIX))
				return false;
			else
				return true;
		} else
			return false;
	}

	/**
	 * �Ƿ���Բ���
	 * **/
	private boolean canReject(WorkflownoteVO worknoteVO) {
		// ��ǩ���û���������
		return !worknoteVO.getActiontype().endsWith(WorkflownoteVO.WORKITEM_ADDAPPROVER_SUFFIX);
	}

	/**
	 * ����׼ʱ���Ƿ���ڿ�ָ�ɵĺ�̻
	 *
	 * @return
	 */
	private boolean isExistAssignableInfoWhenNopass(WorkflownoteVO worknoteVO) {
		if (worknoteVO.getActiontype().endsWith(WorkflownoteVO.WORKITEM_ADDAPPROVER_SUFFIX))
			return false;

		Vector<AssignableInfo> assignInfos = worknoteVO.getTaskInfo().getAssignableInfos();
		if (assignInfos != null && assignInfos.size() > 0) {
			String strCriterion = null;
			for (AssignableInfo ai : assignInfos) {
				strCriterion = ai.getCheckResultCriterion();
				if (AssignableInfo.CRITERION_NOTGIVEN.equals(strCriterion)
						|| AssignableInfo.CRITERION_NOPASS.equals(strCriterion))
					return true;
			}
		}
		return false;
	}

	/**
	 * ��׼ʱ���Ƿ���ڿ�ָ�ɵĺ�̻
	 *
	 * @return
	 */
	private boolean isExistAssignableInfoWhenPass(WorkflownoteVO worknoteVO) {
		if (worknoteVO.getActiontype().endsWith(WorkflownoteVO.WORKITEM_ADDAPPROVER_SUFFIX))
			return false;

		Vector<AssignableInfo> assignInfos = worknoteVO.getTaskInfo().getAssignableInfos();
		if (assignInfos != null && assignInfos.size() > 0) {
			String strCriterion = null;
			for (AssignableInfo ai : assignInfos) {
				strCriterion = ai.getCheckResultCriterion();
				if (AssignableInfo.CRITERION_NOTGIVEN.equals(strCriterion)
						|| AssignableInfo.CRITERION_PASS.equals(strCriterion))
					return true;
			}
		}
		return false;
	}

	/**
	 * �ƶ�����templateid
	 * ***/
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
							+ TEMPLATE_PREFIX + billtype + "' ", new ResultSetProcessor() {
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
	 * �ƶ� �Ƶ� templateid
	 * ***/
	private String getTemplateIDBill(String billtype) throws BusinessException {
		String pk_billtemplet = null;
		try {
			BaseDAO dao = new BaseDAO();
			pk_billtemplet = (String) dao.executeQuery(
					"select pk_billtemplet from pub_billtemplet where isnull(dr,0)=0 and bill_templetcaption='"
							+ "MOBILE" + billtype + "'", new ResultSetProcessor() {
						private static final long serialVersionUID = 1L;

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

		return pk_billtemplet;
	}

	//	public IJZMobileDMO getDMO(String billtype) throws BusinessException{
	//		if( DMO_CACHE.containsKey(billtype) ){
	//			return DMO_CACHE.get(billtype);
	//		}
	//		BilltypeVO vo = PfDataCache.getBillType(billtype);
	//		if( vo == null ){
	//			throw new BusinessException("��������δע�ᣡ");
	//		}
	//		
	//		String refclass =  vo.getReferclassname();
	//		if( StringUtil.isEmpty(refclass) ){
	//			throw new BusinessException("δע����ղ�ѯ��Ӧ��DMO�࣡");
	//		}
	//		
	//		try {
	//			Object instance = Class.forName(refclass).newInstance();
	//			if( instance instanceof IJZMobileDMO ){
	//				DMO_CACHE.put(billtype, (IJZMobileDMO)instance);
	//				return (IJZMobileDMO)instance;
	//			}
	//			throw new BusinessException("��" +refclass +"����̳�AbstractJZMobileDMO��");
	//		} catch (ClassNotFoundException e) {
	//			Logger.error(e);
	//			throw new BusinessException("δ�ҵ���"+refclass+"��",e);
	//		} catch (InstantiationException e) {
	//			Logger.error(e);
	//			throw new BusinessException("���ʼ������",e);
	//		} catch (IllegalAccessException e) {
	//			Logger.error(e);
	//			throw new BusinessException(e);
	//		}
	//	}
	
	
	public String getSupperclassnameByPk(String pk_supplierclass) throws DAOException {
		String supplierclassname = null;
		BaseDAO dao = new BaseDAO();
		supplierclassname = (String) dao.executeQuery(
			"select name from bd_supplierclass where pk_supplierclass='"
					+ pk_supplierclass + "'", new ResultSetProcessor() {
				public Object handleResultSet(ResultSet rs) throws SQLException {
					while (rs.next()) {
						return rs.getString("name");
					}
					return null;
				}
			});
		return supplierclassname;
	}
	
	public String getCountryName(String pk_country) throws DAOException {
		String countryname = null;
		BaseDAO dao = new BaseDAO();
		countryname = (String) dao.executeQuery(
				"select name from bd_countryzone where pk_country='"
						+ pk_country + "'", new ResultSetProcessor() {
							public Object handleResultSet(ResultSet rs) throws SQLException {
								while (rs.next()) {
									return rs.getString("name");
								}
								return null;
							}
						});
		return countryname;
	}
	
	public String getCustclassName(String pk_custclass) throws DAOException {
		String custclassname = null;
		BaseDAO dao = new BaseDAO();
		custclassname = (String) dao.executeQuery(
				"select name from bd_custclass where pk_custclass='"
						+ pk_custclass + "'", new ResultSetProcessor() {
							public Object handleResultSet(ResultSet rs) throws SQLException {
								while (rs.next()) {
									return rs.getString("name");
								}
								return null;
							}
						});
		return custclassname;
	}
	
	public String getOrgName(String pk_org) throws DAOException {
		String orgname = null;
		BaseDAO dao = new BaseDAO();
		orgname = (String) dao.executeQuery(
				"select name from org_orgs where pk_org='"
						+ pk_org + "'", new ResultSetProcessor() {
							public Object handleResultSet(ResultSet rs) throws SQLException {
								while (rs.next()) {
									return rs.getString("name");
								}
								return null;
							}
						});
		return orgname;
	}
	
	public String getCustomerName(String pk_customer) throws DAOException {
		String customername = null;
		BaseDAO dao = new BaseDAO();
		customername = (String) dao.executeQuery(
				"select name from bd_customer where pk_customer='"
						+ pk_customer + "'", new ResultSetProcessor() {
							public Object handleResultSet(ResultSet rs) throws SQLException {
								while (rs.next()) {
									return rs.getString("name");
								}
								return null;
							}
						});
		return customername;
	}
	
	
	private int getBillFileCount(String billid, String billtype) throws BusinessException {
		BaseDAO dao = new BaseDAO();
		String sql = "select count(1) from sm_pub_filesystem  where filepath like '%" + billid + "%' and isfolder ='n'";

		Object obj = dao.executeQuery(sql, new ColumnProcessor());
		int num = obj == null ? 0 : (Integer) obj;
		return num;
	}

	private List<AssignableUserGroup> getAssignableUserList(WorkflownoteVO note, String strCriterion) {
		Vector<AssignableInfo> _assignableInfos = note.getTaskInfo().getAssignableInfos();
		List<AssignableUserGroup> userGrouplist = new ArrayList<AssignableUserGroup>();
		if (_assignableInfos != null) {
			for (Iterator iter = _assignableInfos.iterator(); iter.hasNext();) {
				AssignableInfo ainfo = (AssignableInfo) iter.next();
				String s = ainfo.getCheckResultCriterion();
				if (AssignableInfo.CRITERION_NOTGIVEN.equals(strCriterion)
						|| AssignableInfo.CRITERION_NOTGIVEN.equals(s) || s.equals(strCriterion)) {
					List<AssignableUserVO> assignableUserList = new ArrayList<AssignableUserVO>();
					getAssignMan(ainfo, assignableUserList);
					AssignableUserGroup group = new AssignableUserGroup(ainfo.getDesc(), assignableUserList);
					userGrouplist.add(group);
				}
			}
		}
		return userGrouplist;
	}

	private void getAssignMan(AssignableInfo ainfo, List<AssignableUserVO> userlist) {
		Vector<String> assigned = ainfo.getAssignedOperatorPKs();
		// ���ɵ�ǰ��¼�û���ָ�ɵ�OrganizeUnit��vector
		Vector<OrganizeUnit> ouAssignedUsers = filterByOperator(ainfo.getOuUsers(), assigned);

		int numRightUsers = ouAssignedUsers.size();

		/****************
		 * 2.���������������б�
		 ****************/
		Iterator uiter = ainfo.getOuUsers().iterator();
		while (uiter.hasNext()) {
			OrganizeUnit element = (OrganizeUnit) uiter.next();
			boolean hasAssigned = false; //�ж���ߴ�ָ���û��Ƿ����ұߴ���
			for (int i = 0; i < numRightUsers; i++) {
				if (ouAssignedUsers.get(i).getPk().equals(element.getPk())) {
					hasAssigned = true;
					break;
				}
			}
			if (!hasAssigned) {
				AssignableUserVO userVO = new AssignableUserVO();
				String id = element.getPk() + "#" + ainfo.getActivityDefId();
				userVO.setUser_code(element.getCode());
				userVO.setCuserid(id);
				userVO.setUser_name(element.getName());
				userlist.add(userVO);
			}
		}
	}

	private Vector<OrganizeUnit> filterByOperator(Vector<OrganizeUnit> all, Vector<String> assigned) {
		Map<String, OrganizeUnit> allMap = new HashMap<String, OrganizeUnit>();

		for (OrganizeUnit ou : all) {
			allMap.put(ou.getPk(), ou);
		}

		AssignedUserList list = new AssignedUserList();
		for (String ou : assigned) {
			list.addAssignedUser(ou);
		}

		AssignedUserList filtered = list.getAssignedUserOf(InvocationInfoProxy.getInstance().getUserId());

		Vector<OrganizeUnit> result = new Vector<OrganizeUnit>();
		for (AssignedUser ou : filtered) {
			result.add(allMap.get(ou.getAssignedUser()));
		}

		return result;
	}
}
