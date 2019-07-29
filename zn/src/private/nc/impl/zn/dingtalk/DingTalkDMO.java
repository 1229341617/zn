package nc.impl.zn.dingtalk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.er.filemanager.adapter.NCFileManagerAdapter;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.pub.filesystem.IFileSystemService;
import nc.imag.pub.util.ImageServiceUtil;
import nc.itf.uap.IUAPQueryBS;
import nc.itf.uap.pf.IPFConfig;
import nc.itf.uap.pf.IPFMobileAppServiceFacade;
import nc.itf.uap.pf.IPFWorkflowQry;
import nc.itf.uap.pf.IWorkflowMachine;
import nc.itf.uap.pf.IplatFormEntry;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jzmobile.app.impl.MobileBillDetailQueryImpl;
import nc.jzmobile.utils.BillTypeModelTrans;
import nc.jzmobile.utils.MobileMessageUtil;
import nc.uap.lfw.file.vo.LfwFileVO;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.pf.Pfi18nTools;
import nc.vo.pub.pf.workflow.IPFActionName;
import nc.vo.pub.workflownote.WorkflownoteVO;
import nc.vo.wfengine.definition.WorkflowTypeEnum;
import nc.vo.wfengine.pub.WfTaskType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import sun.misc.BASE64Encoder;

public class DingTalkDMO {
	
	BaseDAO dao = new BaseDAO();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
	
	@SuppressWarnings("rawtypes")
	public Object getPersonalAmount(String psncode) throws Exception{
		//查询100104-定额车费、100204-移动电话费、工作餐费
		
		JSONArray  jsonArr = new JSONArray();		//json格式的数组
		String nowtime=String.valueOf(new UFDate()).substring(0, 7);
		
										
		for(int i=0;i<3;i++){
			
			String pk_inoutbusiclass=null;
			if(i==0){
				pk_inoutbusiclass="1001A11000000001H07H";//定额车费
			}else if(i==1){
				pk_inoutbusiclass="1001A11000000001H07S";//移动电话费
			}else{
				pk_inoutbusiclass="1001B2100000001WXQIK";//工作餐费
			}
			
			StringBuffer s = new StringBuffer();
			s.append("	select c.yearmth,b.standard	");
			s.append("	  from zn_expense_h h	");
			s.append("	  left join zn_expense_b b	");
			s.append("	    on h.pk_expense_h = b.pk_expense_h	");
			s.append("	  left join bd_accperiodmonth c	");
			s.append("	    on b.cperiod = c.pk_accperiodmonth	");
			s.append("	 where nvl(h.dr, 0) = 0	");
			s.append("	   and nvl(b.dr, 0) = 0	");
			s.append("	   and nvl(c.dr, 0) = 0	");
			s.append("	   and b.pk_psndoc = (select pk_psndoc from bd_psndoc where code ='"+psncode+"')	");
			s.append("	   and h.pk_billtypeid = (select pk_billtypeid from bd_billtype d where d.pk_billtypecode = '264X-Cxx-003')	");
			s.append("	   and h.pk_inoutbusiclass = '"+pk_inoutbusiclass+"'	");
			s.append("	   and c.yearmth >= '2018-01'	");
			s.append("	   order by c.yearmth	");
			
			UFDouble total = new UFDouble("0.00");//截止目前该人员此收支项目下
			IUAPQueryBS bs = NCLocator.getInstance().lookup(IUAPQueryBS.class);
			List list =(List) bs.executeQuery(s.toString(), new MapListProcessor());
			if(list!=null && list.size()>0){
				for(int j=0;j<list.size();j++){
					HashMap map = (HashMap) list.get(j);
					if (j != list.size() - 1) {// 如果当前不是最后一组数据，则算出当前组的数据中的月份与下一组数据中月份间隔的月份，然后乘以当前一组数据中的标准值
						HashMap map3 = (HashMap) list.get(j + 1);
						int monthnum=0;
						if(this.isbefore(map.get("yearmth").toString(), nowtime)){//如果当前循环的期间大于当前期间，则跳过该循环
							continue;
						}
						if(this.isbefore(map3.get("yearmth").toString(), nowtime)){//如果当前循环的期间的截止期间大于当前期间，则返回的结果应该+1
							monthnum = calMonth(map.get("yearmth").toString(), nowtime)+1;
						}else{
							monthnum = calMonth(map.get("yearmth").toString(), map3
									.get("yearmth").toString());
						}
						
						total = total.add(new UFDouble(monthnum)
								.multiply(new UFDouble(map.get("standard")
										.toString())));

					} else {// 如果循环至最后一组数据，那么要算出最后一组数据至当前月的月份数，然后乘以最后一组数据中的标准值

						if(this.isbefore(map.get("yearmth").toString(), nowtime)){//如果当前循环的期间大于当前期间，则跳过该循环
							continue;
						}
						
						int monthnum = calMonth(map.get("yearmth").toString(),
								nowtime) + 1;
						total = total.add(new UFDouble(monthnum)
								.multiply(new UFDouble(map.get("standard")
										.toString())));
					}
				}
			}
			//查询此人该报销单的此报销项已经报销的金额
			UFDouble bxje=new UFDouble("0.00");
			String sql = " select sum(er_busitem.bbje) bxje from er_bxzb left join er_busitem on er_bxzb.pk_jkbx = er_busitem.pk_jkbx where er_bxzb.jkbxr = ( select pk_psndoc from bd_psndoc where code='"+psncode+"' ) and er_bxzb.djlxbm = '264X-Cxx-003' and er_busitem.szxmid = '"+pk_inoutbusiclass+"' and substr(er_bxzb.creationtime,0,8)>'2018-01-01'   ";
			List bxlist = (List) bs.executeQuery(sql, new MapListProcessor());
			if(bxlist!=null && bxlist.size()>0){
				HashMap map = (HashMap) bxlist.get(0);
				if(map.get("bxje")!=null){
					bxje=new UFDouble(map.get("bxje").toString());
				}
								
			}
			
			//如果当前收支项目为定额车费，还要查询油卡申请单的已申请额度
			UFDouble org_amount=new UFDouble("0.00");
			if("1001A11000000001H07H".equals(pk_inoutbusiclass)){
				String yksql = " select sum(org_amount) org_amount from er_mtapp_bill  where pk_tradetype = '261X-Cxx-002' and billmaker=( select pk_psndoc from bd_psndoc where code='"+psncode+"' ) and nvl(dr,0)=0 and substr(billdate,0,8)>'2018-01-01'  ";
				List yklist = (List) dao.executeQuery(yksql, new MapListProcessor());
				
				if(yklist!=null && yklist.size()>0){
					HashMap map = (HashMap) yklist.get(0);
					org_amount=new UFDouble(map.get("org_amount")==null?"0.00":map.get("org_amount").toString());							
					
				}
			}
			DecimalFormat df2 = new DecimalFormat("0.00");
			UFDouble balance = total.sub(new UFDouble(bxje).add(org_amount));
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("total" ,df2.format(total) );
			jsonObj.put("bxje" ,df2.format(new UFDouble(bxje).add(org_amount)) );
			jsonObj.put("balance" ,df2.format(balance) );
			if(i==0){
				JSONObject json = new JSONObject();
				json.put("decf", jsonObj);
				jsonArr.put(json);
			}else if(i==1){
				JSONObject json = new JSONObject();
				json.put("yddhf", jsonObj);
				jsonArr.put(json);
			}else{
				JSONObject json = new JSONObject();
				json.put("gzcf", jsonObj);
				jsonArr.put(json);
			}
		}

		
		return jsonArr;
	}
	
	public boolean isbefore(String before,String after){
		Calendar bef = Calendar.getInstance();
		Calendar now = Calendar.getInstance();
		try {
			bef.setTime(sdf.parse(before));
			now.setTime(sdf.parse(sdf.format(new Date())));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(bef.after(now)){
			return true;
		}else{
			return false;
		}
		
	}
	
	//ww add 2018-03-08 月份相差计算
	public static int calMonth(String before,String after){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Calendar bef = Calendar.getInstance();
        Calendar aft = Calendar.getInstance();
        try {
        	 bef.setTime(sdf.parse(before));
             aft.setTime(sdf.parse(after));
             int result = aft.get(Calendar.MONTH) - bef.get(Calendar.MONTH);
             int month = (aft.get(Calendar.YEAR) - bef.get(Calendar.YEAR)) * 12;
             return Math.abs(month + result);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
       
		
		return 0;
	}
	
	@SuppressWarnings("rawtypes")
	public Object getMyApplication(String psncode)throws Exception{
		
		JSONArray  jsonArr = new JSONArray();
		StringBuffer s = new StringBuffer();
		s.append("	select (select bd_billtype.billtypename	");
		s.append("	from bd_billtype	");
		s.append("	where bd_billtype.pk_billtypecode = b.pk_billtype) as pk_billtype,	");
		s.append("	b.billno,b.billid,	");
		s.append("	(select sm_user.user_name from sm_user where cuserid = b.checkman) as checkman	");
		s.append("	 from (select max(pub_workflownote.ts) ts, billno	");
		s.append("	from pub_workflownote	");
		s.append("	 where billno in	");
		s.append("	(select billno	");
		s.append("	 FROM pub_workflownote	");
		s.append("	 where actiontype like 'Z%')	");
		s.append("	   and senderman = (select cuserid from sm_user where pk_base_doc=(select pk_psndoc from bd_psndoc where code = '"+psncode+"') )	");
		s.append("	   and MESSAGENOTE like '%提交单据%'	");
		s.append("	   and ACTIONTYPE = 'Z'	");
		s.append("	   AND APPROVESTATUS <> 4	");
		s.append("	 and billid in (SELECT DISTINCT tance.billid	");
		s.append("	 FROM pub_wf_instance tance	");
		s.append("	 WHERE tance.procstatus = 0)	");
		s.append("	 group by billno ) a left join pub_workflownote b on a.billno = b.billno where a.ts=b.ts	");


		
		List list=null;
		list = (List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(list!=null && list.size()>0){
			for(int i=0;i<list.size();i++){
				JSONObject jsonObj = new JSONObject();
				HashMap map = (HashMap) list.get(i);
				
				jsonObj.put("pk_billtype", map.get("pk_billtype")==null?"":map.get("pk_billtype").toString());//单据类型
				jsonObj.put("billno", map.get("billno")==null?"":map.get("billno").toString());//单据编码
				jsonObj.put("billid", map.get("billid")==null?"":map.get("billid").toString());//单据主键
				jsonObj.put("checkman", map.get("checkman")==null?"":map.get("checkman").toString());//审批人
				jsonArr.put(jsonObj);
			}
			return jsonArr;
			
		}else{
			return "";
		}

	
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getMyToDoList(String psncode,String condition) throws Exception {
		JSONArray  jsonArr = new JSONArray();
		IPFMobileAppServiceFacade pf =NCLocator.getInstance().lookup(IPFMobileAppServiceFacade.class);
		List<Map<String, Object>> alist = pf.getTaskList("0001A110000000000AUG", getCuserid(psncode), null, null, null, "", 1, 100);
		HashMap submap = new HashMap();
		
		List pklist =new ArrayList();
		if(alist!=null && alist.size()>0){
			List list=(List)alist.get(0).get("taskstructlist");
			if(list!=null && list.size()>0){
				for(int i=0;i<list.size();i++){
					HashMap amap =(HashMap) list.get(i);
					pklist.add(amap.get("taskid")==null?"":amap.get("taskid").toString());
					submap.put(amap.get("taskid")==null?"":amap.get("taskid").toString(), amap.get("title")==null?"":amap.get("title").toString());
				}
			}else{
				return "";
			}
		}else{
			return "";
		}
		String insql="";
		if(pklist!=null && pklist.size()>0){
			insql = this.getInStr(pklist, 0, pklist.size());
		}else{
			return "";
		}
		
		StringBuffer s = new StringBuffer();
		s.append(" select * from ( ");
		s.append(" select pub_workflownote.pk_checkflow taskid,  pub_workflownote.pk_billtype, ");
		s.append(" (select distinct billtypename ");
		s.append(" from bd_billtype where pk_billtypecode = pub_workflownote.pk_billtype) billtypename, ");
		s.append(" pub_workflownote.billid, pub_workflownote.billno, ");
		s.append(" (select user_name from sm_user where cuserid = pub_wf_instance.billcommiter) sender_name, ");
		s.append(" pub_wf_instance.billcommiter senderman,pub_workflownote.senddate,  ");
		s.append(" pub_workflownote.checkman,pub_workflownote.workflow_type ");
		s.append(" from pub_workflownote left join pub_wf_task on pub_workflownote.pk_wf_task = pub_wf_task.pk_wf_task ");
		s.append(" left join pub_wf_instance on pub_wf_task.processdefid = pub_wf_instance.processdefid  ");
		s.append(" where pub_wf_instance.billid= pub_workflownote.billid and pk_checkflow  "+insql+" ) ");

		if(condition!=null && !"".equals(condition)){
			s.append(" where ( sender_name like '%"+condition+"%' or billtypename like '%"+condition+"%' ) ");
		}
		List tasklist = (List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(tasklist!=null && tasklist.size()>0){
			for(int l=0;l<tasklist.size();l++){
				JSONObject jsonObj = new JSONObject();
				HashMap map =(HashMap) tasklist.get(l);
				String mobilebilltype="";
				if("2641".equals(map.get("pk_billtype").toString())){//差旅费报销单
					mobilebilltype="type01";
				}else if("264X-Cxx-007".equals(map.get("pk_billtype").toString())){//招待费报销单
					mobilebilltype="type02";
				}else if("264X-Cxx-004".equals(map.get("pk_billtype").toString())){//通用类报销单
					mobilebilltype="type03";
				}else if("264X-Cxx-002".equals(map.get("pk_billtype").toString())){//公司福利报销单
					mobilebilltype="type04";
				}else if("264X-Cxx-014".equals(map.get("pk_billtype").toString())){//发票入账单
					mobilebilltype="type05";
				}else if("264X-Cxx-011".equals(map.get("pk_billtype").toString())){//招待费报销单-需申请
					mobilebilltype="type06";
				}else if("264X-Cxx-003".equals(map.get("pk_billtype").toString())){//费用报销单（个人）
					mobilebilltype="type07";
				}else if("264X-Cxx-009".equals(map.get("pk_billtype").toString())){//电脑补贴报销单
					mobilebilltype="type08";
				}else if("264X-Cxx-006".equals(map.get("pk_billtype").toString())){//成本类报销单
					mobilebilltype="type09";
				}else if("264X-Cxx-010".equals(map.get("pk_billtype").toString())){//销售宣传费用报销单
					mobilebilltype="type10";
				}else if("264X-Cxx-008".equals(map.get("pk_billtype").toString())){//公车费用报销单
					mobilebilltype="type11";
				}else if("2647".equals(map.get("pk_billtype").toString())){//还款单
					mobilebilltype="type12";
				}else if("263X-Cxx-001".equals(map.get("pk_billtype").toString())){//借款单（个人）
					mobilebilltype="type13";
				}else if("263X-Cxx-002".equals(map.get("pk_billtype").toString())){//借款单（对公）
					mobilebilltype="type14";
				}else if("261X-Cxx-001".equals(map.get("pk_billtype").toString())){//业务招待申请单
					mobilebilltype="type15";
				}else if("261X-Cxx-002".equals(map.get("pk_billtype").toString())){//油卡充值申请单
					mobilebilltype="type16";
				}
				
				jsonObj.put("pk_billtype", map.get("pk_billtype")==null?"":map.get("pk_billtype").toString());//单据类型
				jsonObj.put("billtypename", map.get("billtypename")==null?"":map.get("billtypename").toString());//单据名称
				jsonObj.put("mobilebilltype", mobilebilltype);//单据类型
				jsonObj.put("billid", map.get("billid")==null?"":map.get("billid").toString());//单据主键
				jsonObj.put("billno", map.get("billno")==null?"":map.get("billno").toString());//单据号
				jsonObj.put("sender_name", map.get("sender_name")==null?"":map.get("sender_name").toString());//发送人名字
				jsonObj.put("senderman", map.get("senderman")==null?"":map.get("senderman").toString());//发送人
				jsonObj.put("senddate", map.get("senddate")==null?"":map.get("senddate").toString());//发送时间
				jsonObj.put("checkman", map.get("checkman")==null?"":map.get("checkman").toString());//审批人
				jsonObj.put("workflow_type", map.get("workflow_type")==null?"":map.get("workflow_type").toString());//审批人
				jsonObj.put("subject", submap.get(map.get("taskid")==null?"":map.get("taskid").toString()));//主题
				jsonObj.put("taskid", map.get("taskid")==null?"":map.get("taskid").toString());//流程主键
				jsonArr.put(jsonObj);
			}
			return jsonArr;						
		}else{
			return "";
		}
				 				
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getMyErpToDoList(String psncode,String condition) throws Exception {
		JSONArray  jsonArr = new JSONArray();
		IPFMobileAppServiceFacade pf =NCLocator.getInstance().lookup(IPFMobileAppServiceFacade.class);
		List<Map<String, Object>> alist = pf.getTaskList("0001A110000000000AUG", getCuserid(psncode), null, null, null, "", 1, 100);
		HashMap submap = new HashMap();
		
		List pklist =new ArrayList();
		if(alist!=null && alist.size()>0){
			List list=(List)alist.get(0).get("taskstructlist");
			if(list!=null && list.size()>0){
				for(int i=0;i<list.size();i++){
					HashMap amap =(HashMap) list.get(i);
					pklist.add(amap.get("taskid")==null?"":amap.get("taskid").toString());
					submap.put(amap.get("taskid")==null?"":amap.get("taskid").toString(), amap.get("title")==null?"":amap.get("title").toString());
				}
			}else{
				return "";
			}
		}else{
			return "";
		}
		String insql="";
		if(pklist!=null && pklist.size()>0){
			insql = this.getInStr(pklist, 0, pklist.size());
		}else{
			return "";
		}
		
		StringBuffer s = new StringBuffer();
		s.append(" select * from ( ");
		s.append(" select pub_workflownote.pk_checkflow taskid,  pub_workflownote.pk_billtype, ");
		s.append(" (select distinct billtypename ");
		s.append(" from bd_billtype where pk_billtypecode = pub_workflownote.pk_billtype and rownum=1) billtypename, ");
		s.append(" pub_workflownote.billid, pub_workflownote.billno, ");
		s.append(" (select user_name from sm_user where cuserid = pub_wf_instance.billcommiter) sender_name, ");
		s.append(" pub_wf_instance.billcommiter senderman,pub_workflownote.senddate,  ");
		s.append(" pub_workflownote.checkman,pub_workflownote.workflow_type ");
		s.append(" from pub_workflownote left join pub_wf_task on pub_workflownote.pk_wf_task = pub_wf_task.pk_wf_task ");
		s.append(" left join pub_wf_instance on pub_wf_task.processdefid = pub_wf_instance.processdefid  ");
		s.append(" where pub_wf_instance.billid= pub_workflownote.billid and pk_checkflow  "+insql+" order by pub_workflownote.senddate desc) ");
		
		if(condition!=null && !"".equals(condition)){
			s.append(" where ( sender_name like '%"+condition+"%' or billtypename like '%"+condition+"%' ) ");
		}
		List tasklist = (List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(tasklist!=null && tasklist.size()>0){
			for(int l=0;l<tasklist.size();l++){
				JSONObject jsonObj = new JSONObject();
				HashMap map =(HashMap) tasklist.get(l);
				
				String billtypename = "";
				if(map.get("pk_billtype") != null) {
					billtypename = getBillTypeNameByBillType(map.get("pk_billtype").toString());
					
					if("".equals(billtypename)) {
						billtypename = map.get("billtypename")==null?"":map.get("billtypename").toString();
					}
					
				}
				
				jsonObj.put("pk_billtype", map.get("pk_billtype")==null?"":map.get("pk_billtype").toString());//单据类型
				jsonObj.put("billtypename", billtypename);//单据名称
				jsonObj.put("mobilebilltype", map.get("pk_billtype")==null?"":map.get("pk_billtype").toString());//单据类型
				jsonObj.put("billid", map.get("billid")==null?"":map.get("billid").toString());//单据主键
				jsonObj.put("billno", map.get("billno")==null?"":map.get("billno").toString());//单据号
				jsonObj.put("sender_name", map.get("sender_name")==null?"":map.get("sender_name").toString());//发送人名字
				jsonObj.put("senderman", map.get("senderman")==null?"":map.get("senderman").toString());//发送人
				jsonObj.put("senddate", map.get("senddate")==null?"":map.get("senddate").toString());//发送时间
				jsonObj.put("checkman", map.get("checkman")==null?"":map.get("checkman").toString());//审批人
				jsonObj.put("workflow_type", map.get("workflow_type")==null?"":map.get("workflow_type").toString());//审批人
				jsonObj.put("subject", submap.get(map.get("taskid")==null?"":map.get("taskid").toString()));//主题
				jsonObj.put("taskid", map.get("taskid")==null?"":map.get("taskid").toString());//流程主键
				jsonArr.put(jsonObj);
			}
			return jsonArr;						
		}else{
			return "";
		}
		
	}

	public String getBillTypeNameByBillType(String billtype) throws DAOException {
		try {
			billtype = BillTypeModelTrans.getInstance().getModelByBillType(billtype).getBillTypeCode();
			String pk_billtemplet = MobileMessageUtil.getOABillTempletPkByBillType(billtype, MobileBillDetailQueryImpl.TEMPLATE_PREFIX);
			if (pk_billtemplet == null || "".equals(pk_billtemplet)) {
					billtype = BillTypeModelTrans.getInstance().getModelByBillType(billtype).getBillTypeCode();
			}
			Object billtypenameobj = dao.executeQuery("select distinct billtypename from bd_billtype where pk_billtypecode='" + billtype + "'", 
					new ColumnProcessor());
			if(billtypenameobj != null) {
				return billtypenameobj.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	@SuppressWarnings("rawtypes")
	private static String getInStr( List pks, int start,
			int end) {
		int m_start = start;
		int m_end = end;
		start = Math.min(m_start, m_end);
		end = Math.max(m_start, m_end);
		StringBuffer sb = new StringBuffer();
		sb.append(" in (");
		String key = null;
		for (int i = start; i < pks.size(); i++) {
			String pk =(String) pks.get(i);
			if (i > end) {
				break;
			}
			if (pk == null)
				continue;
			key = pk.trim();
			sb.append("'");
			sb.append(key);
			sb.append("',");
		}
		String inStr = sb.substring(0, sb.length() - 1) + ") ";
		return inStr;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getMyToDoNum(String psncode)throws Exception {
		
		IPFMobileAppServiceFacade pf =NCLocator.getInstance().lookup(IPFMobileAppServiceFacade.class);
		List<Map<String, Object>> alist = pf.getTaskList("0001A110000000000AUG", getCuserid(psncode), null, null, null, "", 1, 100);
		JSONObject jsonObj = new JSONObject();
		List pklist =new ArrayList();
		if(alist!=null && alist.size()>0){
			List list=(List)alist.get(0).get("taskstructlist");
			if(list!=null && list.size()>0){
				HashMap submap = new HashMap();				
				
				for(int i=0;i<list.size();i++){
					HashMap amap =(HashMap) list.get(i);
					pklist.add(amap.get("taskid")==null?"":amap.get("taskid").toString());
					submap.put(amap.get("taskid")==null?"":amap.get("taskid").toString(), amap.get("title")==null?"":amap.get("title").toString());
				}
			}else{
				jsonObj.put("num",0);
				return jsonObj;
			}
			
		}else{
			jsonObj.put("num",0);
			return jsonObj;
		}
		String insql="";
		if(pklist!=null && pklist.size()>0){
			insql = this.getInStr(pklist, 0, pklist.size());
		}else{
			jsonObj.put("num",0);
			return jsonObj;
		}
		StringBuffer s = new StringBuffer();
		s.append(" select * from ( ");
		s.append(" select pub_workflownote.pk_checkflow taskid,  pub_workflownote.pk_billtype, ");
		s.append(" (select distinct billtypename ");
		s.append(" from bd_billtype where pk_billtypecode = pub_workflownote.pk_billtype) billtypename, ");
		s.append(" pub_workflownote.billid, pub_workflownote.billno, ");
		s.append(" (select user_name from sm_user where cuserid = pub_wf_instance.billcommiter) sender_name, ");
		s.append(" pub_wf_instance.billcommiter senderman,pub_workflownote.senddate,  ");
		s.append(" pub_workflownote.checkman,pub_workflownote.workflow_type ");
		s.append(" from pub_workflownote left join pub_wf_task on pub_workflownote.pk_wf_task = pub_wf_task.pk_wf_task ");
		s.append(" left join pub_wf_instance on pub_wf_task.processdefid = pub_wf_instance.processdefid  ");
		s.append(" where pub_wf_instance.billid= pub_workflownote.billid and pk_checkflow  "+insql+" ) ");
		List tasklist = (List) dao.executeQuery(s.toString(), new MapListProcessor());
		ArrayList numlist=new ArrayList();
		if(tasklist!=null && tasklist.size()>0){
			for(int l=0;l<tasklist.size();l++){
				HashMap map =(HashMap) tasklist.get(l);
				String mobilebilltype="";
				if("2641".equals(map.get("pk_billtype").toString())){//差旅费报销单
					mobilebilltype="type01";
				}else if("264X-Cxx-007".equals(map.get("pk_billtype").toString())){//招待费报销单
					mobilebilltype="type02";
				}else if("264X-Cxx-004".equals(map.get("pk_billtype").toString())){//通用类报销单
					mobilebilltype="type03";
				}else if("264X-Cxx-002".equals(map.get("pk_billtype").toString())){//公司福利报销单
					mobilebilltype="type04";
				}else if("264X-Cxx-014".equals(map.get("pk_billtype").toString())){//发票入账单
					mobilebilltype="type05";
				}else if("264X-Cxx-011".equals(map.get("pk_billtype").toString())){//招待费报销单-需申请
					mobilebilltype="type06";
				}else if("264X-Cxx-003".equals(map.get("pk_billtype").toString())){//费用报销单（个人）
					mobilebilltype="type07";
				}else if("264X-Cxx-009".equals(map.get("pk_billtype").toString())){//电脑补贴报销单
					mobilebilltype="type08";
				}else if("264X-Cxx-006".equals(map.get("pk_billtype").toString())){//成本类报销单
					mobilebilltype="type09";
				}else if("264X-Cxx-010".equals(map.get("pk_billtype").toString())){//销售宣传费用报销单
					mobilebilltype="type10";
				}else if("264X-Cxx-008".equals(map.get("pk_billtype").toString())){//公车费用报销单
					mobilebilltype="type11";
				}else if("2647".equals(map.get("pk_billtype").toString())){//还款单
					mobilebilltype="type12";
				}else if("263X-Cxx-001".equals(map.get("pk_billtype").toString())){//借款单（个人）
					mobilebilltype="type13";
				}else if("263X-Cxx-002".equals(map.get("pk_billtype").toString())){//借款单（对公）
					mobilebilltype="type14";
				}else if("261X-Cxx-001".equals(map.get("pk_billtype").toString())){//业务招待申请单
					mobilebilltype="type15";
				}else if("261X-Cxx-002".equals(map.get("pk_billtype").toString())){//油卡充值申请单
					mobilebilltype="type16";
				}
				if(!"".equals(mobilebilltype)){
					numlist.add(map);
				}
			}
			jsonObj.put("num",numlist.size());			
		}
		return jsonObj;		 				
	}
	
	public Object getBillDetail(String pk_bill,String pk_billtypecode)throws Exception{

		QueryDetail qd = new QueryDetail();
		if("2641".equals(pk_billtypecode)){//差旅费报销单			
			return qd.queryClfbxd(pk_bill,pk_billtypecode);			
		}else if("264X-Cxx-007".equals(pk_billtypecode)){//招待费报销单
			return qd.queryzdfbxd(pk_bill, pk_billtypecode);
		}else if("264X-Cxx-004".equals(pk_billtypecode)){//通用类报销单
			return qd.querytylbxd(pk_bill, pk_billtypecode);			
		}else if("264X-Cxx-002".equals(pk_billtypecode)){//公司福利报销单
			return qd.querygsflbxd(pk_bill, pk_billtypecode);
		}else if("264X-Cxx-014".equals(pk_billtypecode)){//发票入账单
			return qd.queryfprzd(pk_bill, pk_billtypecode);
		}else if("264X-Cxx-011".equals(pk_billtypecode)){//招待费报销单-需申请
			return qd.queryzdfbxd_2(pk_bill, pk_billtypecode);
		}else if("264X-Cxx-003".equals(pk_billtypecode)){//费用报销单（个人）
			return qd.queryfybxd(pk_bill, pk_billtypecode);
		}else if("264X-Cxx-009".equals(pk_billtypecode)){//电脑补贴报销单
			return qd.querydnbtbxd(pk_bill, pk_billtypecode);
		}else if("264X-Cxx-006".equals(pk_billtypecode)){//成本类报销单
			return qd.querycblbxd(pk_bill, pk_billtypecode);
		}else if("264X-Cxx-010".equals(pk_billtypecode)){//销售宣传费用报销单
			return qd.queryxsxcfybxd(pk_bill, pk_billtypecode);
		}else if("264X-Cxx-008".equals(pk_billtypecode)){//公车费用报销单
			return qd.querygcfybxd(pk_bill, pk_billtypecode);
		}else if("2647".equals(pk_billtypecode)){//还款单
			return qd.queryhkd(pk_bill, pk_billtypecode);
		}else if("261X-Cxx-002".equals(pk_billtypecode)){//油卡充值申请单
			return qd.queryykczsqd(pk_bill,pk_billtypecode);
		}else if("263X-Cxx-001".equals(pk_billtypecode)){//借款单（个人）
			return qd.queryjkdgr(pk_bill,pk_billtypecode);
		}else if("263X-Cxx-002".equals(pk_billtypecode)){//借款单（对公）
			return qd.queryjkddg(pk_bill,pk_billtypecode);
		}else if("261X-Cxx-001".equals(pk_billtypecode)){//业务招待申请单
			return qd.queryywzdfsqd(pk_bill,pk_billtypecode);
		}
			
		//业务招待申请单、油卡充值申请单、借款单（个人）、借款单（对公）
		return null;
	}
	
	public Object getErpBillDetail(String pk_bill,String pk_billtypecode)throws Exception{
		return new QueryDetail().queryErpBillDetail(pk_bill, pk_billtypecode);
	}

	
	@SuppressWarnings("rawtypes")
	public String processApprove(String billtype,String billid,String checkResult,String checkman,String checkNote,String pk_flow,String workflow_type) throws Exception{
		Logger.init("nclog");
		Logger.error("调用办理接口开始");
		//环境赋值
		InvocationInfoProxy.getInstance().setGroupId("0001A110000000000AUG");
		InvocationInfoProxy.getInstance().setUserId(checkman);
		
		// 1.获得单据聚合VO
		IPFConfig bsConfig = (IPFConfig) NCLocator.getInstance().lookup(
				IPFConfig.class.getName());
		AggregatedValueObject billVo = bsConfig.queryBillDataVO(billtype,billid);
				
		Logger.error("调用单据查询结束");		
		String action=null;
		String nodeid="";
		//获取动作类型
		action=this.getApproveActionName(Integer.valueOf(workflow_type));
		
		IWorkflowMachine bsWorkflow = (IWorkflowMachine) NCLocator
				.getInstance().lookup(IWorkflowMachine.class.getName());
		HashMap hmPfExParams = new HashMap();
		Logger.error("调用任务查询接口开始");
		WorkflownoteVO worknoteVO = bsWorkflow.checkWorkFlow(
				action + checkman, billtype, billVo,
				hmPfExParams);		
		
		Logger.error("调用任务查询接口结束");

		if (worknoteVO != null) {			
			// 获取审批结果-通过/不通过/驳回
			if ("Y".equalsIgnoreCase(checkResult)) {
				worknoteVO.setChecknote(checkNote==null?"批准":checkNote);
				worknoteVO.setApproveresult("Y");
			} else if ("N".equalsIgnoreCase(checkResult)) {
				worknoteVO.setApproveresult("N");
			} else if ("R".equalsIgnoreCase(checkResult)) {
				worknoteVO.setChecknote(checkNote);
				worknoteVO.setApproveresult("R");
				worknoteVO.getTaskInfo().getTask().setTaskType(WfTaskType.Backward.getIntValue());
		        worknoteVO.getTaskInfo().getTask().setSubmit2RjectTache(false);
		//获取可驳回的上一位审批人
		try {
			
			StringBuffer s = new StringBuffer();
			s.append("	select pub_wf_task.activitydefid	");
			s.append("	  from pub_wf_task	");
			s.append("	 where pk_wf_task = (select pk_wf_task from (select a.pk_wf_task,rownum	");
			s.append("	                       from pub_workflownote a	");
			s.append("	                      where a.billid = '"+billid+"'	");
			s.append("	                        and a.checkman = '"+worknoteVO.getSenderman()+"'	");
			s.append("	                        and a.actiontype = 'Z'	");
			s.append("	                        and a.workflow_type <> 6 order by ts desc ) where rownum=1)	");
			s.append("	                        	");
			Object taskid = dao.executeQuery(s.toString(), new ColumnProcessor());
			if(taskid!=null && !"".equals(taskid)){
				nodeid=taskid.toString();
			}else{//
//				StringBuffer sub = new StringBuffer();
//				sub.append("	select pub_wf_task.activitydefid	");
//				sub.append("	  from pub_wf_task	");
//				sub.append("	 where pk_wf_task = (select a.pk_wf_task	");
//				sub.append("	                       from pub_workflownote a	");
//				sub.append("	                      where a.billid = '"+billid+"'	");
//				sub.append("	                        and a.messagenote like '%提交单据%'	");
//				sub.append("	                        and a.actiontype = 'Z'	");
//				sub.append("	                        and rownum = 1)	");
//				Object id = dao.executeQuery(sub.toString(), new ColumnProcessor());
//				if(id!=null && !"".equals(id)){
//					nodeid=id.toString();
//				}
				
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		worknoteVO.getTaskInfo().getTask().setBackToFirstActivity(true);
		worknoteVO.getTaskInfo().getTask().setJumpToActivity(null);
		/*if (StringUtil.isEmptyWithTrim(nodeid)) {
			
		} else {
			worknoteVO.getTaskInfo().getTask().setBackToFirstActivity(false);
			worknoteVO.getTaskInfo().getTask().setJumpToActivity(nodeid);
		}*/

			} else
				return NCLangRes4VoTransl.getNCLangRes().getStrByID("busitype",
						"busitypehint-000064")/* 错误：消息格式不对 */;
		}
		
		

		// 3.执行动作
		IplatFormEntry pff = (IplatFormEntry) NCLocator.getInstance().lookup(
				IplatFormEntry.class.getName());
		JSONObject jsonObj = new JSONObject();
		
		try {
			
			Logger.error("调用办理方法开始");
			if("Y".equalsIgnoreCase(checkResult)){
				pff.processAction(action+ checkman, billtype,
						worknoteVO, billVo, null, null);
			}else if("R".equalsIgnoreCase(checkResult)){
//				this.doRejectAction(checkman, pk_flow, "11", nodeid, null);
				
				
				pff.processAction(action+ checkman, billtype,
						worknoteVO, billVo, null, null);
				if("6".equals(workflow_type) && "R".equals(checkResult)){//任务类型为6的驳回需要再处理一下
					//查询提交人
					String psnsql = " select e.senderman from pub_workflownote e where e.messagenote like '%提交单据%' and e.billid ='"+billid+"' ";
					Object psn = dao.executeQuery(psnsql, new ColumnProcessor(1));
					//查询最新的任务
					String sql = " select c.checkman,c.workflow_type from pub_workflownote c where c.actiontype='Z' and c.approveresult is null and c.billid='"+billid+"' ";
					List list = (List) dao.executeQuery(sql, new MapListProcessor());
					String checkman2=null;
					String workflowtype2=null;
					if(list!=null && list.size()>0){
						HashMap map = (HashMap) list.get(0);
						checkman2 = map.get("checkman")==null?"":map.get("checkman").toString();
						workflowtype2 = map.get("workflow_type")==null?"":map.get("workflow_type").toString();
					}
					
					WorkflownoteVO worknote2VO = bsWorkflow.checkWorkFlow(
							this.getApproveActionName(Integer.valueOf(workflowtype2)) + checkman2, billtype, billVo,
							hmPfExParams);
					pff.processAction("UNSAVE"+psn.toString(), billtype,
							worknote2VO, billVo, null, null);
				}
			}

			Logger.error("调用办理方法结束开始");
			
			jsonObj.put("status", "1");
			return jsonObj.toString();
		} catch (Exception e) {
			Logger.error("调用办理接口异常");
			e.printStackTrace();
			jsonObj.put("status", "0");
			jsonObj.put("msg", e.getMessage());
			return jsonObj.toString();
		}

		
	}	

	public String showImage(String pk_bill,String pk_billtypecode,String cuserid) throws Exception{
						
		JSONObject json = new JSONObject();
				
		InvocationInfoProxy.getInstance().setGroupId("0001A110000000000AUG");
		IPFConfig bsConfig = (IPFConfig) NCLocator.getInstance().lookup(
				IPFConfig.class.getName());
		AggregatedValueObject billVo = bsConfig.queryBillDataVO(pk_billtypecode,
				pk_bill);	
		
		String url="";				
		try {
			
			url=ImageServiceUtil.getImageShowURL(billVo, pk_billtypecode, pk_bill, cuserid, "0001A110000000000AUG");
			json.put("status", "1");
			json.put("url", url);
		} catch (Exception e) {
			e.printStackTrace();
			json.put("status", "0");
			json.put("msg", e.getMessage());
		}																		
				
		return json.toString();
	}
	
	public Object doQueryFile(String pk_bill) throws Exception{
//		LfwFileVO[] file = null;
//		try {
////			file = new NCFileManagerAdapter().getFileQryService().getFile(null,pk_bill);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		if(file !=null && file.length>0){
//			for(int i=0;i<file.length;i++){
//				LfwFileVO vo = file[i];
//				String ext1 = vo.getExt1();
//				ext1 = ext1.substring(0, ext1.lastIndexOf("/")) + "|"
//						+ ext1.substring(ext1.lastIndexOf("/") + 1, ext1.length());
//				vo.setExt1(URLEncoder.encode(URLEncoder.encode(ext1, "UTF-8"), "UTF-8"));
//				json.put("displayname", vo.getDisplayname());
//				json.put("filename", vo.getFilename());
//				json.put("filetypo", vo.getFiletypo());
//				json.put("filesize", vo.getFilesize());
//				json.put("filemgr", vo.getFilemgr());	
//				json.put("pk_lfwfile","portal"+File.separator+"pt"+File.separator+"yerfile"+File.separator+"down?id="+ vo.getExt1());
//			}
//		}else{
//			return null;
//		}
		
		JSONArray jsarr = new JSONArray();
		JSONObject filejson = new JSONObject();
		
		String sql = "select filelength,filepath, pk from sm_pub_filesystem  where filepath like '%"
				+ pk_bill + "%' and isfolder ='n'";
		ArrayList list = (ArrayList) dao.executeQuery(sql,
				new ArrayListProcessor());
		for (int i = 0; i < list.size(); i++) {
			com.alibaba.fastjson.JSONObject fileMap = new com.alibaba.fastjson.JSONObject();
			Object[] os = (Object[]) list.get(i);
			String[] paths = ((String) os[1]).split("/");
			JSONObject json = new JSONObject();
			json.put("filename", paths[paths.length - 1]);
			json.put("pk", (String) os[2]);
			
			json.put("displayname", "");
			json.put("filetypo", "");
			json.put("filesize", "");
			json.put("filemgr", "");	
			json.put("pk_lfwfile", "");
			
			jsarr.put(json);
		}
		
		filejson.put("file", jsarr);
		return filejson;
		
	}
	
	public Object getUnApprovalDetails(String billno) throws Exception {
		return new QueryDetail().getUnApprovalDetails(billno);
	}
	
	
	private String getApproveActionName(int workflowtype) {
		if (workflowtype == WorkflowTypeEnum.Workflow.getIntValue()
				|| workflowtype == WorkflowTypeEnum.SubWorkflow.getIntValue()
				|| workflowtype == WorkflowTypeEnum.SubWorkApproveflow
						.getIntValue()) {
			return IPFActionName.SIGNAL;
		} else {
			return IPFActionName.APPROVE;
		}
	}
	

	
	public String getCuserid(String psncode){
		
		String sql = " select cuserid from sm_user left join bd_psndoc on sm_user.pk_base_doc=bd_psndoc.pk_psndoc where bd_psndoc.code='"+psncode+"' ";
		
		try {
			
			Object cuserid = dao.executeQuery(sql, new ColumnProcessor());
			if(cuserid==null || "".equals(cuserid)){
				return null;
			}else{
				return cuserid.toString();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getBillTypeName(String billtype){
		try {
			String sql = "select billtypename from bd_billtype where pk_billtypecode='"+billtype+"'";
			Object billtypename = dao.executeQuery(sql, new ColumnProcessor());
			if(billtypename == null || "".equals(billtypename)){
				return null;
			}else{
				return billtypename.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Object getSender(String pk_user) {
		try {
			String sql = "select USER_NAME from sm_user where CUSERID ='" +pk_user+"'";
			Object senderman = dao.executeQuery(sql, new ColumnProcessor());
			if(senderman == null || "".equals(senderman)){
				return null;
			}else{
				return senderman.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Object getSenderdate(String pk_taskid) {
		try {
			String sql = "select note.senddate from pub_workflownote note "
						+ "left join pub_wf_task task on note.pk_wf_task = task.pk_wf_task "
						+ "where task.pk_wf_task='"+pk_taskid+"'";
			Object senderdate = dao.executeQuery(sql, new ColumnProcessor());
			if(senderdate == null || "".equals(senderdate)){
				return null;
			}else{
				return senderdate.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Object getApproveHistoryInfo(String billid, String billtype) throws Exception {
		com.alibaba.fastjson.JSONArray wfjsonarr = new com.alibaba.fastjson.JSONArray();
		try{
			WorkflownoteVO[] vos = NCLocator.getInstance().lookup(IPFWorkflowQry.class).queryWorkitems(billid, billtype, 0, 0);
			if(vos == null || vos.length == 0) {
				vos = getWorkflowNote(billid);
				if(vos == null || vos.length == 0) {
					return wfjsonarr;
				}
			}
			if(vos != null && vos.length > 0) {
				for(WorkflownoteVO vo : vos) {
					if(vo.getApprovestatus() != null) {
						if(vo.getApprovestatus() != 0){
							vo.setCheckname(Pfi18nTools.getUserName(vo.getCheckman()));
						}
					}
				}
			}
			Arrays.sort(vos, new Comparator<WorkflownoteVO>() {
				@Override
				public int compare(WorkflownoteVO o1, WorkflownoteVO o2) {
					if(o1.getDealdate()==null)
						return 0;
					else if(o2.getDealdate()==null)
						return -1;
					else
						return o1.getDealdate().compareTo(o2.getDealdate());
				}
			});
			if (vos != null && vos.length > 0) {
				for (WorkflownoteVO vo : vos) {
					if(vo.getDealdate() == null) {
						continue;
					}
					com.alibaba.fastjson.JSONObject wfjson = new com.alibaba.fastjson.JSONObject();
					if("S".equals(vo.getApproveresult())) {
						wfjson.put("approveStatus", "提交");
					}else if("Y".equals(vo.getApproveresult())) {
						wfjson.put("approveStatus", "同意");
					}else if("N".equals(vo.getApproveresult())) {
						wfjson.put("approveStatus", "不批准");
					}else if("R".equals(vo.getApproveresult())) {
						wfjson.put("approveStatus", "驳回");
					}else if(vo.getApproveresult() == null || "Q".equals(vo.getApproveresult())) {
						wfjson.put("approveStatus", "未审批");
					}
					
					wfjson.put("checkman", vo.getCheckname());
					wfjson.put("checknote", vo.getChecknote() == null ? "" : vo.getChecknote());
					wfjson.put("dealdate", vo.getDealdate() != null ? vo.getDealdate().toString() : "");
					wfjsonarr.add(wfjson);
				}
			}
		}catch (Exception e) {
			Logger.error(e);
		}
		return wfjsonarr;
	}
	
	public Object getFileList(String billid, String billtype){
		try {
			return getBillFileList(billid,billtype);
		} catch (BusinessException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		}
	}
	
	public Object getFileContent(String pk_attachment){
		try {
			return queryAttachment(pk_attachment);
		} catch (BusinessException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return null;
		}
	}
	
	private Object queryAttachment(String strPk_Attachment)
			throws BusinessException {
		//Map<String, Object> map = new HashMap<String, Object>();
		com.alibaba.fastjson.JSONObject map = new com.alibaba.fastjson.JSONObject();
		String filePath = this.getFilePath(strPk_Attachment);
		if (filePath == null) {
			return null;
		}
		String[] paths = filePath.split("/");
		String filename = paths[paths.length - 1];
		String attachment = this.getFileAttatchment(filePath);
		map.put("attachment", attachment);
		map.put("filename", filename);

		return map;
	}
	
	private String getFileAttatchment(String filePath) {
		if (filePath == null || filePath.equals("")) {
			return null;
		}
		String fileAttatchment = "";
		ByteArrayOutputStream output = null;

		IFileSystemService service = NCLocator.getInstance().lookup(
				IFileSystemService.class);
		output = new ByteArrayOutputStream();
		try {
			service.downLoadFile(filePath, output);
			byte[] fileBts = output.toByteArray();
			fileAttatchment = new BASE64Encoder().encode(fileBts);
		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
		} finally {
			try {
				output.close();
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
			}
		}

		return fileAttatchment;
	}
	
	private String getFilePath(String pkAttatch) {

		String filePath = null;

		String sql = "select filepath from sm_pub_filesystem where pk='"
				+ pkAttatch + "'";
		try {
			ArrayList list = (ArrayList) new BaseDAO().executeQuery(sql,
					new ArrayListProcessor());
			if (list.size() == 0) {
				return null;
			} else {
				Object[] fileAttatchment = (Object[]) list.get(0);
				String filepath = (String) fileAttatchment[0];
				return filepath;
			}
		} catch (DAOException e) {
			Logger.error(e.getMessage(), e);
		}

		return filePath;
	}
	
	@SuppressWarnings({ "rawtypes" })
	private Object getBillFileList(String billid, String billtype)
			throws BusinessException {
		BaseDAO dao = new BaseDAO();
		String sql = "";
		//List<Map<String, String>> listMapInfo = new ArrayList<Map<String, String>>();
		com.alibaba.fastjson.JSONArray listMapInfo = new com.alibaba.fastjson.JSONArray();
		sql = "select filelength,filepath, pk from sm_pub_filesystem  where filepath like '%"
				+ billid + "%' and isfolder ='n'";
		ArrayList list = (ArrayList) dao.executeQuery(sql,
				new ArrayListProcessor());
		for (int i = 0; i < list.size(); i++) {
			com.alibaba.fastjson.JSONObject fileMap = new com.alibaba.fastjson.JSONObject();
			//Map<String, String> fileMap = new HashMap<String, String>();
			Object[] os = (Object[]) list.get(i);
			String[] paths = ((String) os[1]).split("/");
			String filename = paths[paths.length - 1];
			String size = getFileSize(Integer.parseInt(String.valueOf(os[0])));
			fileMap.put("size", size);
			fileMap.put("filepath", (String) os[1]);
			fileMap.put("name", filename);
			fileMap.put("pk", (String) os[2]);
			listMapInfo.add(fileMap);
		}
		// }
		return listMapInfo;
	}
	
	@SuppressWarnings({ "rawtypes" })
	private WorkflownoteVO[] getWorkflowNote(String billid)
			throws BusinessException {
		String sql = " select c.approveResult result,su.user_name username,c.checknote note,c.dealdate ddate "
					+ " from pub_workflownote c "
					+ " left join sm_user su on su.cuserid=c.checkman "
					+ " where c.billid='"+billid+"' ";
		
		List result = (List)dao.executeQuery(sql, new MapListProcessor());
		if(result != null && result.size() > 0) {
			WorkflownoteVO[] wfarr = new WorkflownoteVO[result.size()];
			for (int i = 0; i < result.size(); i++) {
				wfarr[i] = new WorkflownoteVO();
				
				Map wfmap = ((Map)result.get(i));
				if(wfmap.get("result") != null) {
					wfarr[i].setApproveresult(wfmap.get("result").toString());
				}
				if(wfmap.get("username") != null) {
					wfarr[i].setCheckname(wfmap.get("username").toString());
				}
				if(wfmap.get("note") != null) {
					wfarr[i].setChecknote(wfmap.get("note").toString());
				}
				if(wfmap.get("ddate") != null) {
					try {
						Date senddate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(wfmap.get("ddate").toString());
						wfarr[i].setDealdate(new UFDateTime(senddate));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
			return wfarr;
		}
		return new WorkflownoteVO[]{};
	}
	
	private String getFileSize(int size) // 附件长度转化为**K 或者**M
	{
		int GB = 1024 * 1024 * 1024;// 定义GB的计算常量
		int MB = 1024 * 1024;// 定义MB的计算常量
		int KB = 1024;// 定义KB的计算常量
		if (size / GB >= 1)// 如果当前Byte的值大于等于1GB
		{
			return String.valueOf(Math.round(size / (float) GB)) + "G";// 将其转换成GB
		} else if (size / MB >= 1)// 如果当前Byte的值大于等于1MB
		{
			return String.valueOf(Math.round(size / (float) MB)) + "M";// 将其转换成MB
		} else if (size / KB >= 1)// 如果当前Byte的值大于等于1KB
		{
			return String.valueOf(Math.round(size / (float) KB)) + "K";// 将其转换成KGB
		} else {
			return String.valueOf(size) + "B";// 显示Byte值
		}
	}
	
	
	private String returnJson(Object msg,String status){
		JSONObject jsonObj = new JSONObject();	
		try {
			jsonObj.put("status", status);
			jsonObj.put("msg", msg);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jsonObj.toString();
	}
}
