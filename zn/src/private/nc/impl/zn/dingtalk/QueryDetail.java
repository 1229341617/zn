package nc.impl.zn.dingtalk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nc.bs.dao.BaseDAO;
import nc.jdbc.framework.processor.MapListProcessor;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public class QueryDetail {
	
	BaseDAO dao = new BaseDAO();
	
	
	/**
	 * 查询差旅费报销单明细
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryClfbxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();
		
		
		JSONObject jsonh = new JSONObject();//主表JSON对象
		
		//主表拼装
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");
		s.append("	       substr(a.djrq, 0, 10) djrq,	");
		s.append("	       b.name jkbxr,	");
		s.append("	       a.bbje,	");
		s.append("	       c.name pk_org,	");
		s.append("	       d.name deptid,	");
		s.append("	       e.name fydwbm,	");
		s.append("	       a.zyx5	");
		s.append("	  from er_bxzb a	");
		s.append("	  left join bd_psndoc b	");
		s.append("	    on a.jkbxr = b.pk_psndoc	");
		s.append("	  left join org_orgs c	");
		s.append("	    on a.pk_org = c.pk_org	");
		s.append("	  left join org_dept d	");
		s.append("	    on a.deptid = d.pk_dept	");
		s.append("	  left join org_orgs e	");
		s.append("	    on a.fydwbm = e.pk_org	");
		s.append("	 where a.pk_jkbx = '"+pk_bill+"'	");
		
		List list=(List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(list!=null && list.size()>0){
			HashMap map =(HashMap) list.get(0);
			
			jsonh.put("djbh", map.get("djbh")==null?"":map.get("djbh").toString());//编号
			jsonh.put("djrq", map.get("djrq")==null?"":map.get("djrq").toString());//日期
			jsonh.put("jkbxr", map.get("jkbxr")==null?"":map.get("jkbxr").toString());//报销人
			jsonh.put("bbje", map.get("bbje")==null?"":map.get("bbje").toString());//金额
			jsonh.put("pk_org", map.get("pk_org")==null?"":map.get("pk_org").toString());//报销人单位
			jsonh.put("deptid", map.get("deptid")==null?"":map.get("deptid").toString());//报销人部门
			jsonh.put("fydwbm", map.get("fydwbm")==null?"":map.get("fydwbm").toString());//费用承担单位
			jsonh.put("zyx5", map.get("zyx5")==null?"":map.get("zyx5").toString());//报销事由
			jsonObj.put("h", jsonh);
			
		}
		
		//子表的拼装
		StringBuffer b = new StringBuffer();
		b.append("	select b.name       item,	");//(1)收支项目(2)收支项目(3)收支项目
		b.append("	       a.vat_amount,	");//(1)交通费金额(2)住宿费用金额(3)补助金额
		b.append("	       a.defitem1,	");//(1)出发日期(2)入住日期
		b.append("	       a.defitem3,	");//(1)出发地点
		b.append("	       a.defitem2,	");//(1)到达日期(2)离店日期
		b.append("	       a.defitem4,	");//(1)到达地点
		b.append("	       a.defitem5,	");//(1)交通工具
		b.append("	       a.defitem6,	");//(1)增值税税率(2)增值税税率
		b.append("	       a.defitem20,	");//(1)产业类别						
		b.append("	       a.tax_rate,	");//(1)税率(2)税率
		b.append("	       a.tax_amount,	");//(1)税金(2)税金
		b.append("	       a.tni_amount,	");//(1)不含税金额(2)除税金额
		b.append("	       a.fphm,	");//(1)发票号码
		b.append("	       a.defitem11,	");//(3)出差补差标准
		b.append("	       a.defitem9,	");//(2)住宿天数(3)出差天数
		b.append("	       a.tablecode	");//所属页签
		b.append("	  from er_busitem a	");
		b.append("	  left join bd_inoutbusiclass b	");
		b.append("	    on a.szxmid = b.pk_inoutbusiclass	");
	
		b.append("	 where a.pk_jkbx = '"+pk_bill+"' and nvl(a.dr,0)=0	");
		
		List blist=(List) dao.executeQuery(b.toString(), new MapListProcessor());
		
		JSONArray  jtfyjsonArr = new JSONArray();//交通费用JSON数组
		JSONArray  zsfyjsonArr = new JSONArray();//住宿费用JSON数组
		JSONArray  ccbtjsonArr = new JSONArray();//出差补贴JSON数组
		JSONArray  cxmxjsonArr = new JSONArray();//冲销明细JSON数组
		
		ArrayList jtfyList=new ArrayList();//交通费用数组
		ArrayList zsfyList=new ArrayList();//住宿费用数组
		ArrayList ccbtList=new ArrayList();//出差补贴数组
		ArrayList cxmxList=new ArrayList();//冲销明细数组
		if(blist!=null && blist.size()>0){


			for(int i=0;i<blist.size();i++){
				HashMap map = (HashMap) blist.get(i);
				String tablecode=null;
				tablecode=map.get("tablecode")==null?"":map.get("tablecode").toString();
				if("arap_bxbusitem".equals(tablecode)){
					jtfyList.add(map);
				}else if("other".equals(tablecode)){
					zsfyList.add(map);
				}else if("bzitem".equals(tablecode)){
					ccbtList.add(map);						
				}					
			}
			
			if(jtfyList!=null && jtfyList.size()>0){
				//交通费用
				for(int m=0;m<jtfyList.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) jtfyList.get(m);
					json.put("item", map.get("item")==null?"":map.get("item").toString());//收支项目
					json.put("defitem1", map.get("defitem1")==null?"":map.get("defitem1").toString());//出发日期
					json.put("defitem3", map.get("defitem3")==null?"":map.get("defitem3").toString());//出发地点
					json.put("defitem2", map.get("defitem2")==null?"":map.get("defitem2").toString());//到达日期
					json.put("defitem4", map.get("defitem4")==null?"":map.get("defitem4").toString());//到达地点
					json.put("defitem5", map.get("defitem5")==null?"":map.get("defitem5").toString());//交通工具
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//交通费金额
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//增值税税率
					json.put("defitem20", map.get("defitem20")==null?"":map.get("defitem20").toString());//产业类别
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//税率
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//税金
					json.put("tni_amount", map.get("tni_amount")==null?"":map.get("tni_amount").toString());//不含税金额
					
					jtfyjsonArr.put(json);																		
				}					
				jsonObj.put("jtfy", jtfyjsonArr);				
				
			}
			
			if(zsfyList!=null && zsfyList.size()>0){
				//住宿费用
				for(int l=0;l<zsfyList.size();l++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) zsfyList.get(l);
					json.put("defitem1", map.get("defitem1")==null?"":map.get("defitem1").toString());//入住日期
					json.put("defitem2", map.get("defitem2")==null?"":map.get("defitem2").toString());//离店日期
					json.put("defitem9", map.get("defitem9")==null?"":map.get("defitem9").toString());//住宿天数
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//税率
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//税金
					json.put("tni_amount", map.get("tni_amount")==null?"":map.get("tni_amount").toString());//不含税金额
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//住宿费用金额
					json.put("item", map.get("item")==null?"":map.get("item").toString());//收支项目
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//增值税税率
					json.put("fphm", map.get("fphm")==null?"":map.get("fphm").toString());//发票号码						
					
					zsfyjsonArr.put(json);																		
				}
				
				jsonObj.put("zsfy", zsfyjsonArr);
				
			}
							
			if(ccbtList!=null && ccbtList.size()>0){
				
				//出差补贴
				for(int n=0;n<ccbtList.size();n++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) ccbtList.get(n);
					json.put("defitem11", map.get("defitem11")==null?"":map.get("defitem11").toString());//出差补差标准
					json.put("defitem9", map.get("defitem9")==null?"":map.get("defitem9").toString());//出差天数
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//补助金额
					json.put("item", map.get("item")==null?"":map.get("item").toString());//收支项目
									
					ccbtjsonArr.put(json);																		
				}
				
				jsonObj.put("ccbt", ccbtjsonArr);
			}
			



		}
		
		StringBuffer c = new StringBuffer();
		c.append("	  select a.jkdjbh,	");
		c.append("	         b.name    psnname,	");
		c.append("	         c.name    deptname,	");
		c.append("	         a.cjkybje,	");
		c.append("	         a.cxrq,	");
		c.append("	         a.sxrq,	");
		c.append("	         a.sxbz	");
		c.append("	    from er_bxcontrast a	");
		c.append("	    left join bd_psndoc b	");
		c.append("	      on a.jkbxr = b.pk_psndoc	");
		c.append("	    left join org_dept c	");
		c.append("	      on a.deptid = c.pk_dept	");
		c.append("	      where a.pk_bxd = '"+pk_bill+"'	");

						
		cxmxList = (ArrayList) dao.executeQuery(c.toString(), new MapListProcessor());

		if(cxmxList!=null && cxmxList.size()>0){
			//冲销明细
			for(int o=0;o<cxmxList.size();o++){
				JSONObject json = new JSONObject();
				HashMap map = (HashMap) cxmxList.get(o);
				json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//借款单号
				json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//借款人
				json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//借款部门
				json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//冲销原币金额
				json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//冲销日期
				json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//生效日期
				json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//生效标志
				
								
				cxmxjsonArr.put(json);																		
			}
			jsonObj.put("cxmx", cxmxjsonArr);
		}
		
		return jsonObj;
	}
	
	/**
	 * 查询招待费报销单明细
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryzdfbxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//主表JSON对象
		
		//主表拼装
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");
		s.append("	       substr(a.djrq, 0, 10) djrq,	");
		s.append("	       a.total,	");
		s.append("	       b.name jkbxr,	");
		s.append("	       b.mobile,	");
		s.append("	       c.code bankcode,	");
		s.append("	       d.name dwbm,	");
		s.append("	       e.name deptid,	");
		s.append("	       f.name fydwbm,	");
		s.append("	       g.name fydeptid,	");
		s.append("	       h.name jsfs,	");
		s.append("	       i.name fkyhzh,	");
		s.append("	       j.name pk_payorg,	");
		s.append("	       k.project_name,case a.paytarget when 0 then '员工' when 1 then '供应商' when 2 then '客户' end  paytarget,	");
		s.append("	       l.name hbbm,	");
		s.append("	       a.zyx29,	");
		s.append("	       m.code custacccount,	");
		s.append("	       a.zyx19	");
		s.append("		");
		s.append("	  from er_bxzb a	");
		s.append("	  left join bd_psndoc b	");
		s.append("	    on a.jkbxr = b.pk_psndoc	");
		s.append("	  left join bd_bankaccsub c	");
		s.append("	    on a.skyhzh = c.pk_bankaccsub	");
		s.append("	  left join org_orgs d	");
		s.append("	    on a.dwbm = d.pk_org	");
		s.append("	  left join org_dept e	");
		s.append("	    on a.deptid = e.pk_dept	");
		s.append("	  left join org_orgs f	");
		s.append("	    on a.fydwbm = f.pk_org	");
		s.append("	  left join org_dept g	");
		s.append("	    on a.fydeptid = g.pk_dept	");
		s.append("	  left join bd_balatype h	");
		s.append("	    on a.jsfs = h.pk_balatype	");
		s.append("	  left join bd_bankaccsub i	");
		s.append("	    on a.fkyhzh = i.pk_bankaccsub	");
		s.append("	  left join org_financeorg j	");
		s.append("	    on a.pk_payorg = j.pk_financeorg	");
		s.append("	  left join bd_project k	");
		s.append("	    on a.jobid = k.pk_project	");
		s.append("	  left join bd_supplier l	");
		s.append("	    on a.hbbm = l.pk_supplier	");
		s.append("	  left join bd_bankaccsub m	");
		s.append("	    on a.custaccount = m.pk_bankaccsub	");
		s.append("	 where a.pk_jkbx = '"+pk_bill+"'	");

		List list=(List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(list!=null && list.size()>0){
			HashMap map = (HashMap) list.get(0);
	        Iterator it = map.entrySet().iterator();  
	        while(it.hasNext()){  
	            Map.Entry<Object, Object> m = (Entry<Object, Object>) it.next();  
	            jsonh.put(m.getKey().toString(), m.getValue()==null?"":m.getValue().toString()); 
	        } 
	        jsonObj.put("h", jsonh);
		}
		
		//子表的拼装
		StringBuffer b = new StringBuffer();
		b.append("	select b.name       szxmid,	");
		b.append("	       a.defitem1,	");
		b.append("	       a.vat_amount,	");
		b.append("	       c.name       defitem20,	");
		b.append("	       a.defitem10,	");
		b.append("	       a.defitem11,	");
		b.append("	       a.defitem12,	");
		b.append("	       a.tablecode	");
		b.append("	  from er_busitem a	");
		b.append("	  left join bd_inoutbusiclass b	");
		b.append("	    on a.szxmid = b.pk_inoutbusiclass	");
		b.append("	  left join bd_defdoc c	");
		b.append("	    on a.defitem20 = c.pk_defdoc	");
		b.append("	 where 	");
		b.append("	 a.pk_jkbx = '"+pk_bill+"' and nvl(a.dr,0)=0	");	
		
		JSONArray  zdf1jsonArr = new JSONArray();//招待费明细(页签1)JSON数组
		JSONArray  zdf2jsonArr = new JSONArray();//招待费明细(页签2)JSON数组
		JSONArray  cxmxjsonArr = new JSONArray();//冲销明细JSON数组
		
		ArrayList zdf1List=new ArrayList();//招待费明细(页签1)数组
		ArrayList zdf2List=new ArrayList();//招待费明细(页签2)数组
		ArrayList cxmxList=new ArrayList();//冲销明细数组
		
			
		List blist=(List) dao.executeQuery(b.toString(), new MapListProcessor());
		if(blist!=null && blist.size()>0){
			
			for(int i=0;i<blist.size();i++){
				HashMap map = (HashMap) blist.get(i);
				String tablecode=null;
				tablecode=map.get("tablecode")==null?"":map.get("tablecode").toString();
				if("arap_bxbusitem".equals(tablecode)){
					zdf1List.add(map);
				}else if("other".equals(tablecode)){
					zdf2List.add(map);
				}					
			}
			
			if(zdf1List!=null && zdf1List.size()>0){
				//交通费用
				for(int m=0;m<zdf1List.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) zdf1List.get(m);
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//收支项目
					json.put("defitem1", map.get("defitem1")==null?"":map.get("defitem1").toString());//招待日期
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//报销金额
					json.put("defitem20", map.get("defitem20")==null?"":map.get("defitem20").toString());//产业类别
					
					zdf1jsonArr.put(json);																		
				}					
				jsonObj.put("zdf1", zdf1jsonArr);				
				
			}
			
			if(zdf2List!=null && zdf2List.size()>0){
				//交通费用
				for(int m=0;m<zdf2List.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) zdf2List.get(m);
					json.put("defitem10", map.get("defitem10")==null?"":map.get("defitem10").toString());//招待对象
					json.put("defitem11", map.get("defitem11")==null?"":map.get("defitem11").toString());//客人人数
					json.put("defitem12", map.get("defitem12")==null?"":map.get("defitem12").toString());//陪同人数
					
					zdf2jsonArr.put(json);																		
				}					
				jsonObj.put("zdf2", zdf2jsonArr);				
				
			}
			
			StringBuffer c = new StringBuffer();
			c.append("	  select a.jkdjbh,	");
			c.append("	         b.name    psnname,	");
			c.append("	         c.name    deptname,	");
			c.append("	         a.cjkybje,	");
			c.append("	         a.cxrq,	");
			c.append("	         a.sxrq,	");
			c.append("	         a.sxbz	");
			c.append("	    from er_bxcontrast a	");
			c.append("	    left join bd_psndoc b	");
			c.append("	      on a.jkbxr = b.pk_psndoc	");
			c.append("	    left join org_dept c	");
			c.append("	      on a.deptid = c.pk_dept	");
			c.append("	      where a.pk_bxd = '"+pk_bill+"'	");

							
			cxmxList = (ArrayList) dao.executeQuery(c.toString(), new MapListProcessor());

			if(cxmxList!=null && cxmxList.size()>0){
				//冲销明细
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//借款单号
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//借款人
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//借款部门
					json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//冲销原币金额
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//冲销日期
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//生效日期
					json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//生效标志
					
									
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
			
			
			
		}
		
		return jsonObj;
		

		
	}
	
	/**
	 * 查询通用类报销单明细
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object querytylbxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//主表JSON对象
		
		//主表拼装
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");
		s.append("	       substr(a.djrq, 0, 10) djrq,	");
		s.append("	       a.total,	");
		s.append("	       b.name jkbxr,	");
		s.append("	       b.mobile,	");
		s.append("	       c.code bankcode,	");
		s.append("	       d.name dwbm,	");
		s.append("	       e.name deptid,	");
		s.append("	       f.name fydwbm,	");
		s.append("	       g.name fydeptid,	");
		s.append("	       j.name pk_payorg,	");
		s.append("	       h.name jsfs,	");
		s.append("	       n.code pk_cashaccount,	");
		s.append("	       i.name fkyhzh,	");		
		s.append("	       k.project_name, case a.paytarget when 0 then '员工' when 1 then '供应商' when 2 then '客户' end  paytarget,	");
		s.append("	       l.name hbbm,	");
		s.append("	       m.code custacccount,	");
		s.append("	       a.zyx19	");
		s.append("		");
		s.append("	  from er_bxzb a	");
		s.append("	  left join bd_psndoc b	");
		s.append("	    on a.jkbxr = b.pk_psndoc	");
		s.append("	  left join bd_bankaccsub c	");
		s.append("	    on a.skyhzh = c.pk_bankaccsub	");
		s.append("	  left join org_orgs d	");
		s.append("	    on a.dwbm = d.pk_org	");
		s.append("	  left join org_dept e	");
		s.append("	    on a.deptid = e.pk_dept	");
		s.append("	  left join org_orgs f	");
		s.append("	    on a.fydwbm = f.pk_org	");
		s.append("	  left join org_dept g	");
		s.append("	    on a.fydeptid = g.pk_dept	");
		s.append("	  left join bd_balatype h	");
		s.append("	    on a.jsfs = h.pk_balatype	");
		s.append("	  left join bd_bankaccsub i	");
		s.append("	    on a.fkyhzh = i.pk_bankaccsub	");
		s.append("	  left join org_financeorg j	");
		s.append("	    on a.pk_payorg = j.pk_financeorg	");
		s.append("	  left join bd_project k	");
		s.append("	    on a.jobid = k.pk_project	");
		s.append("	  left join bd_supplier l	");
		s.append("	    on a.hbbm = l.pk_supplier	");
		s.append("	  left join bd_bankaccsub m	");
		s.append("	    on a.custaccount = m.pk_bankaccsub	");
		s.append(" left join bd_cashaccount n on a.pk_cashaccount=n.pk_cashaccount ");
		s.append("	 where a.pk_jkbx = '"+pk_bill+"'	");

		List list=(List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(list!=null && list.size()>0){
			HashMap map = (HashMap) list.get(0);
	        Iterator it = map.entrySet().iterator();  
	        while(it.hasNext()){  
	            Map.Entry<Object, Object> m = (Entry<Object, Object>) it.next();  
	            jsonh.put(m.getKey().toString(), m.getValue()==null?"":m.getValue().toString()); 
	        } 
	        jsonObj.put("h", jsonh);				
		}
		
		//子表的拼装
		StringBuffer b = new StringBuffer();
		b.append("	select b.name szxmid,	");
		b.append("	       a.defitem6,	");
		b.append("	       a.fphm,	");
		b.append("	       a.tax_rate,	");
		b.append("	       a.tni_amount,	");
		b.append("	       a.tax_amount,	");
		b.append("	       a.vat_amount,	");
		b.append("	       a.defitem8,	");
		b.append("	       c.name,a.tablecode	");
		b.append("		");
		b.append("	  from er_busitem a	");
		b.append("	  left join bd_inoutbusiclass b	");
		b.append("	    on a.szxmid = b.pk_inoutbusiclass	");
		b.append("	  left join bd_defdoc c	");
		b.append("	    on a.defitem20 = c.pk_defdoc	");
		b.append("	 where 	");
		b.append("	 a.pk_jkbx = '"+pk_bill+"' and nvl(a.dr,0)=0	");	
		
		JSONArray  qfyjsonArr = new JSONArray();//招待费明细(页签1)JSON数组
		JSONArray  cxmxjsonArr = new JSONArray();//冲销明细JSON数组
		
		ArrayList qtfyList=new ArrayList();//招待费明细(页签1)数组
		ArrayList cxmxList=new ArrayList();//冲销明细数组
		
			
		List blist=(List) dao.executeQuery(b.toString(), new MapListProcessor());
		if(blist!=null && blist.size()>0){
			
			for(int i=0;i<blist.size();i++){
				HashMap map = (HashMap) blist.get(i);
				String tablecode=null;
				tablecode=map.get("tablecode")==null?"":map.get("tablecode").toString();
				if("arap_bxbusitem".equals(tablecode)){
					qtfyList.add(map);
				}				
			}
			
			if(qtfyList!=null && qtfyList.size()>0){
				//交通费用
				for(int m=0;m<qtfyList.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) qtfyList.get(m);
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//收支项目
					json.put("defitem1", map.get("defitem1")==null?"":map.get("defitem1").toString());//招待日期
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//报销金额
					json.put("defitem20", map.get("defitem20")==null?"":map.get("defitem20").toString());//产业类别
					
					qfyjsonArr.put(json);																		
				}					
				jsonObj.put("qty", qfyjsonArr);				
				
			}

			
			StringBuffer c = new StringBuffer();
			c.append("	  select a.jkdjbh,	");
			c.append("	         b.name    psnname,	");
			c.append("	         c.name    deptname,	");
			c.append("	         a.cjkybje,	");
			c.append("	         a.cxrq,	");
			c.append("	         a.sxrq,	");
			c.append("	         a.sxbz	");
			c.append("	    from er_bxcontrast a	");
			c.append("	    left join bd_psndoc b	");
			c.append("	      on a.jkbxr = b.pk_psndoc	");
			c.append("	    left join org_dept c	");
			c.append("	      on a.deptid = c.pk_dept	");
			c.append("	      where a.pk_bxd = '"+pk_bill+"'	");

							
			cxmxList = (ArrayList) dao.executeQuery(c.toString(), new MapListProcessor());

			if(cxmxList!=null && cxmxList.size()>0){
				//冲销明细
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//借款单号
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//借款人
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//借款部门
					json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//冲销原币金额
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//冲销日期
					json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//生效标志
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//生效日期
					
														
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
						
			
		}
		
		return jsonObj;

		
	}
	
	
	/**
	 * 查询公司福利报销单明细
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object querygsflbxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//主表JSON对象
		
		//主表拼装
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//单据编号
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//单据日期
		s.append("	       a.total,	");//合计金额
		s.append("	       b.name jkbxr,	");//报销人
		s.append("	       b.mobile,	");//手机
		s.append("	       c.code bankcode,	");//个人银行账户
		s.append("	       d.name dwbm,	");//报销人单位
		s.append("	       e.name deptid,	");//报销人部门
		s.append("	       f.name fydwbm,	");//费用承担单位
		s.append("	       g.name fydeptid,	");//费用承担部门
		s.append("	       j.name pk_payorg,	");//支付单位
		s.append("	       h.name jsfs,	");//结算方式
		s.append("	       i.name fkyhzh,	");//单位银行账户		
		s.append("	       k.project_name, case a.paytarget when 0 then '员工' when 1 then '供应商' when 2 then '客户' end  paytarget,	");//支付对象
		s.append("	       l.name hbbm,	");//供应商
		s.append("	       m.code custacccount,	");//供应商银行账户
		s.append("	       o.project_name jobid,	");//项目
		s.append("	       a.zyx29,	");//备注
		s.append("	       a.zyx19	");//事由
		s.append("		");
		s.append("	  from er_bxzb a	");
		s.append("	  left join bd_psndoc b	");
		s.append("	    on a.jkbxr = b.pk_psndoc	");
		s.append("	  left join bd_bankaccsub c	");
		s.append("	    on a.skyhzh = c.pk_bankaccsub	");
		s.append("	  left join org_orgs d	");
		s.append("	    on a.dwbm = d.pk_org	");
		s.append("	  left join org_dept e	");
		s.append("	    on a.deptid = e.pk_dept	");
		s.append("	  left join org_orgs f	");
		s.append("	    on a.fydwbm = f.pk_org	");
		s.append("	  left join org_dept g	");
		s.append("	    on a.fydeptid = g.pk_dept	");
		s.append("	  left join bd_balatype h	");
		s.append("	    on a.jsfs = h.pk_balatype	");
		s.append("	  left join bd_bankaccsub i	");
		s.append("	    on a.fkyhzh = i.pk_bankaccsub	");
		s.append("	  left join org_financeorg j	");
		s.append("	    on a.pk_payorg = j.pk_financeorg	");
		s.append("	  left join bd_project k	");
		s.append("	    on a.jobid = k.pk_project	");
		s.append("	  left join bd_supplier l	");
		s.append("	    on a.hbbm = l.pk_supplier	");
		s.append("	  left join bd_bankaccsub m	");
		s.append("	    on a.custaccount = m.pk_bankaccsub	");		
		s.append(" left join bd_cashaccount n on a.pk_cashaccount=n.pk_cashaccount ");
		s.append("	  left join bd_project o on a.jobid=o.pk_project   	");
		s.append("	 where a.pk_jkbx = '"+pk_bill+"'	");

		List list=(List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(list!=null && list.size()>0){
			HashMap map = (HashMap) list.get(0);
	        Iterator it = map.entrySet().iterator();  
	        while(it.hasNext()){  
	            Map.Entry<Object, Object> m = (Entry<Object, Object>) it.next();  
	            jsonh.put(m.getKey().toString(), m.getValue()==null?"":m.getValue().toString()); 
	        } 
	        jsonObj.put("h", jsonh);				
		}
		
		//子表的拼装
		StringBuffer b = new StringBuffer();
		b.append("	select b.name szxmid,	");//收支项目		
		b.append("	       a.fphm,	");//发票号码
		b.append("	       a.tax_rate,	");//税率
		b.append("	       a.tax_amount,	");//税金
		b.append("	       a.vat_amount,	");//报销金额
		b.append("	       d.taxrate defitem6,	");//增值税税率
		b.append("	       c.name defitem20,	");//产业类别
		b.append("		   a.tablecode ");
		b.append("	  from er_busitem a	");
		b.append("	  left join bd_inoutbusiclass b	");
		b.append("	    on a.szxmid = b.pk_inoutbusiclass	");
		b.append("	  left join bd_defdoc c	");
		b.append("	    on a.defitem20 = c.pk_defdoc	");
		b.append("	  left join bd_taxrate d	");
		b.append("	    on a.defitem6 = d.pk_taxcode	");
		b.append("	 where 	");
		b.append("	  a.pk_jkbx = '"+pk_bill+"' and nvl(a.dr,0)=0	");	
		
		JSONArray  qfyjsonArr = new JSONArray();//福利费明细(页签1)JSON数组
		JSONArray  cxmxjsonArr = new JSONArray();//冲销明细JSON数组
		
		ArrayList qtfyList=new ArrayList();//福利费明细(页签1)数组
		ArrayList cxmxList=new ArrayList();//冲销明细数组
		
			
		List blist=(List) dao.executeQuery(b.toString(), new MapListProcessor());
		if(blist!=null && blist.size()>0){
			
			for(int i=0;i<blist.size();i++){
				HashMap map = (HashMap) blist.get(i);
				String tablecode=null;
				tablecode=map.get("tablecode")==null?"":map.get("tablecode").toString();
				if("arap_bxbusitem".equals(tablecode)){
					qtfyList.add(map);
				}				
			}
			
			if(qtfyList!=null && qtfyList.size()>0){
				
				for(int m=0;m<qtfyList.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) qtfyList.get(m);
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//收支项目
					json.put("fphm", map.get("fphm")==null?"":map.get("fphm").toString());//发票号码
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//税率
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//税金
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//报销金额
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//增值税税率					
					json.put("defitem20", map.get("defitem20")==null?"":map.get("defitem20").toString());//产业类别
					
					qfyjsonArr.put(json);																		
				}					
				jsonObj.put("qty", qfyjsonArr);				
				
			}

			
			StringBuffer c = new StringBuffer();
			c.append("	  select a.jkdjbh,	");//借款单号
			c.append("	         b.name    psnname,	");//申请人
			c.append("	         c.name    deptname,	");//申请部门
			c.append("	         a.cjkybje,	");//申请金额
			c.append("	         d.name szxmid,	");//收支项目
			c.append("	         a.cxrq,	");//申请日期
			c.append("	         a.sxrq,	");//生效日期
			c.append("	         a.sxbz	");//生效标志
			c.append("	    from er_bxcontrast a	");
			c.append("	    left join bd_psndoc b	");
			c.append("	      on a.jkbxr = b.pk_psndoc	");
			c.append("	    left join org_dept c	");
			c.append("	      on a.deptid = c.pk_dept	");
			c.append("	    left join bd_inoutbusiclass d on a.szxmid=d.pk_inoutbusiclass	");
			c.append("	      where a.pk_bxd = '"+pk_bill+"'	");

							
			cxmxList = (ArrayList) dao.executeQuery(c.toString(), new MapListProcessor());

			if(cxmxList!=null && cxmxList.size()>0){
				//冲销明细
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//借款单号
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//申请人
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//申请部门
					json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//申请金额
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//收支项目
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//申请日期
					json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//生效标志
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//生效日期
					
														
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
						
			
		}
		
		return jsonObj;

		
	}
	
	/**
	 * 查询发票入账单明细
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryfprzd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//主表JSON对象
		
		//主表拼装
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//单据编号
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//单据日期
		s.append("	       a.total,	");//合计金额
		s.append("	       b.name jkbxr,	");//报销人
		s.append("	       b.mobile,	");//手机
		s.append("         a.vat_amount,    ");//报销金额
		s.append("	       c.code bankcode,	");//个人银行账户
		s.append("	       d.name dwbm,	");//报销人单位
		s.append("	       e.name deptid,	");//报销人部门
		s.append("	       f.name fydwbm,	");//费用承担单位
		s.append("	       g.name fydeptid,	");//费用承担部门
		s.append("	       j.name pk_payorg,	");//支付单位
		s.append("	       h.name jsfs,	");//结算方式
		s.append("	       i.name fkyhzh,	");//单位银行账户		
		s.append("	       k.project_name, case a.paytarget when 0 then '员工' when 1 then '供应商' when 2 then '客户' end  paytarget,	");//支付对象
		s.append("	       l.name hbbm,	");//供应商
		s.append("	       m.code custacccount,	");//供应商银行账户
		s.append("	       o.project_name jobid,	");//项目
		s.append("	       a.zyx19	");//事由
		s.append("		");
		s.append("	  from er_bxzb a	");
		s.append("	  left join bd_psndoc b	");
		s.append("	    on a.jkbxr = b.pk_psndoc	");
		s.append("	  left join bd_bankaccsub c	");
		s.append("	    on a.skyhzh = c.pk_bankaccsub	");
		s.append("	  left join org_orgs d	");
		s.append("	    on a.dwbm = d.pk_org	");
		s.append("	  left join org_dept e	");
		s.append("	    on a.deptid = e.pk_dept	");
		s.append("	  left join org_orgs f	");
		s.append("	    on a.fydwbm = f.pk_org	");
		s.append("	  left join org_dept g	");
		s.append("	    on a.fydeptid = g.pk_dept	");
		s.append("	  left join bd_balatype h	");
		s.append("	    on a.jsfs = h.pk_balatype	");
		s.append("	  left join bd_bankaccsub i	");
		s.append("	    on a.fkyhzh = i.pk_bankaccsub	");
		s.append("	  left join org_financeorg j	");
		s.append("	    on a.pk_payorg = j.pk_financeorg	");
		s.append("	  left join bd_project k	");
		s.append("	    on a.jobid = k.pk_project	");
		s.append("	  left join bd_supplier l	");
		s.append("	    on a.hbbm = l.pk_supplier	");
		s.append("	  left join bd_bankaccsub m	");
		s.append("	    on a.custaccount = m.pk_bankaccsub	");		
		s.append(" left join bd_cashaccount n on a.pk_cashaccount=n.pk_cashaccount ");
		s.append("	  left join bd_project o on a.jobid=o.pk_project   	");
		s.append("	 where a.pk_jkbx = '"+pk_bill+"'	");

		List list=(List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(list!=null && list.size()>0){
			HashMap map = (HashMap) list.get(0);
	        Iterator it = map.entrySet().iterator();  
	        while(it.hasNext()){  
	            Map.Entry<Object, Object> m = (Entry<Object, Object>) it.next();  
	            jsonh.put(m.getKey().toString(), m.getValue()==null?"":m.getValue().toString()); 
	        } 
	        jsonObj.put("h", jsonh);				
		}
		
		//子表的拼装
		StringBuffer b = new StringBuffer();
		b.append("	select b.name szxmid,	");//收支项目
		b.append("	       c.taxrate defitem6,	");//增值税税率
		b.append("	       a.tax_rate,	");//税率
		b.append("	       a.tax_amount,	");//税金
		b.append("	       a.tni_amount,	");//不含税金额
		b.append("	       a.fphm,	");//发票号码		
		b.append("	       a.vat_amount,	");//入账金额
		b.append("		   a.tablecode ");
		b.append("	  from er_busitem a	");
		b.append("	  left join bd_inoutbusiclass b	");
		b.append("	    on a.szxmid = b.pk_inoutbusiclass	");
		b.append("	  left join bd_taxrate c	");
		b.append("	    on a.defitem6 = c.pk_taxcode	");
		b.append("	 where 	");
		b.append("	  a.pk_jkbx = '"+pk_bill+"' and nvl(a.dr,0)=0	");	
		
		JSONArray  qfyjsonArr = new JSONArray();//福利费明细(页签1)JSON数组
		
		ArrayList qtfyList=new ArrayList();//福利费明细(页签1)数组

		
			
		List blist=(List) dao.executeQuery(b.toString(), new MapListProcessor());
		if(blist!=null && blist.size()>0){
			
			for(int i=0;i<blist.size();i++){
				HashMap map = (HashMap) blist.get(i);
				String tablecode=null;
				tablecode=map.get("tablecode")==null?"":map.get("tablecode").toString();
				if("arap_bxbusitem".equals(tablecode)){
					qtfyList.add(map);
				}				
			}
			
			if(qtfyList!=null && qtfyList.size()>0){
				for(int m=0;m<qtfyList.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) qtfyList.get(m);
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//收支项目
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//增值税税率	
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//税率
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//税金
					json.put("tni_amount", map.get("tni_amount")==null?"":map.get("tni_amount").toString());//不含税金额										
					json.put("fphm", map.get("fphm")==null?"":map.get("fphm").toString());//发票号码
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//入账金额
					
					qfyjsonArr.put(json);																		
				}						
				jsonObj.put("qty", qfyjsonArr);				
				
			}

			
						
			
		}
		
		return jsonObj;

		
	}
	
	/**
	 * 查询招待费报销单-需申请明细
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryzdfbxd_2(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//主表JSON对象
		
		//主表拼装
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//单据编号
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//单据日期
		s.append("	       a.total,	");//合计金额
		s.append("	       b.name jkbxr,	");//报销人
		s.append("	       b.mobile,	");//手机
		s.append("	       c.code bankcode,	");//个人银行账户
		s.append("	       d.name dwbm,	");//报销人单位
		s.append("	       f.name fydwbm,	");//费用承担单位
		s.append("	       g.name fydeptid,	");//费用承担部门
		s.append("	       h.name jsfs,	");//支付方式
		s.append("	       i.name fkyhzh,	");//单位银行账户
		s.append("	       j.name pk_payorg,	");//支付单位
		s.append("	       k.project_name,	");//项目
		s.append("	       case a.paytarget when 0 then '员工' when 1 then '供应商' when 2 then '客户' end  paytarget,	");// 支付对象
		s.append("	       l.name hbbm,	");//供应商
		s.append("	       a.zyx29,	");//备注
		s.append("	       m.code custacccount,	");//供应商银行账户
		s.append("	       a.zyx19	");//事由
		s.append("		");
		s.append("	  from er_bxzb a	");
		s.append("	  left join bd_psndoc b	");
		s.append("	    on a.jkbxr = b.pk_psndoc	");
		s.append("	  left join bd_bankaccsub c	");
		s.append("	    on a.skyhzh = c.pk_bankaccsub	");
		s.append("	  left join org_orgs d	");
		s.append("	    on a.dwbm = d.pk_org	");
		s.append("	  left join org_dept e	");
		s.append("	    on a.deptid = e.pk_dept	");
		s.append("	  left join org_orgs f	");
		s.append("	    on a.fydwbm = f.pk_org	");
		s.append("	  left join org_dept g	");
		s.append("	    on a.fydeptid = g.pk_dept	");
		s.append("	  left join bd_balatype h	");
		s.append("	    on a.jsfs = h.pk_balatype	");
		s.append("	  left join bd_bankaccsub i	");
		s.append("	    on a.fkyhzh = i.pk_bankaccsub	");
		s.append("	  left join org_financeorg j	");
		s.append("	    on a.pk_payorg = j.pk_financeorg	");
		s.append("	  left join bd_project k	");
		s.append("	    on a.jobid = k.pk_project	");
		s.append("	  left join bd_supplier l	");
		s.append("	    on a.hbbm = l.pk_supplier	");
		s.append("	  left join bd_bankaccsub m	");
		s.append("	    on a.custaccount = m.pk_bankaccsub	");
		s.append("	 where a.pk_jkbx = '"+pk_bill+"'	");

		List list=(List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(list!=null && list.size()>0){
			HashMap map = (HashMap) list.get(0);
	        Iterator it = map.entrySet().iterator();  
	        while(it.hasNext()){  
	            Map.Entry<Object, Object> m = (Entry<Object, Object>) it.next();  
	            jsonh.put(m.getKey().toString(), m.getValue()==null?"":m.getValue().toString()); 
	        } 
	        jsonObj.put("h", jsonh);
		}
		
		//子表的拼装
		StringBuffer b = new StringBuffer();
		b.append("	select b.name       szxmid,	");
		b.append("	       a.defitem1,	");
		b.append("	       a.vat_amount,	");
		b.append("	       c.name       defitem20,	");
		b.append("	       a.defitem10,a.defitem2,	");
		b.append("	       a.defitem11,	");
		b.append("	       a.defitem12,	");
		b.append("	       a.defitem6,	");
		b.append("	       a.defitem7,	");
		b.append("	       a.defitem8,	");
		b.append("	       d.name defitem21,a.tax_rate,a.tax_amount,e.project_name,	");
		b.append("	       a.tablecode	");
		b.append("	  from er_busitem a	");
		b.append("	  left join bd_inoutbusiclass b	");
		b.append("	    on a.szxmid = b.pk_inoutbusiclass	");
		b.append("	  left join bd_defdoc c	");
		b.append("	    on a.defitem20 = c.pk_defdoc	");
		b.append("	  left join bd_defdoc d	");
		b.append("	    on a.defitem21 = d.pk_defdoc	");
		b.append("	  left join bd_project e	");
		b.append("	    on a.jobid = e.pk_project	");
		b.append("	 where 	");
		b.append("	 a.pk_jkbx = '"+pk_bill+"' and nvl(a.dr,0)=0	");	
		
		JSONArray  zdf1jsonArr = new JSONArray();//招待费明细(页签1)JSON数组
		JSONArray  zdf2jsonArr = new JSONArray();//招待费明细(页签2)JSON数组
		JSONArray  cxmxjsonArr = new JSONArray();//冲销明细JSON数组
		
		ArrayList zdf1List=new ArrayList();//招待费明细(页签1)数组
		ArrayList zdf2List=new ArrayList();//招待费明细(页签2)数组
		ArrayList cxmxList=new ArrayList();//冲销明细数组
		
			
		List blist=(List) dao.executeQuery(b.toString(), new MapListProcessor());
		if(blist!=null && blist.size()>0){
			
			for(int i=0;i<blist.size();i++){
				HashMap map = (HashMap) blist.get(i);
				String tablecode=null;
				tablecode=map.get("tablecode")==null?"":map.get("tablecode").toString();
				if("arap_bxbusitem".equals(tablecode)){
					zdf1List.add(map);
				}else if("other".equals(tablecode)){
					zdf2List.add(map);
				}					
			}
			
			if(zdf1List!=null && zdf1List.size()>0){
				
				for(int m=0;m<zdf1List.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) zdf1List.get(m);
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//收支项目
					json.put("defitem1", map.get("defitem1")==null?"":map.get("defitem1").toString());//招待日期
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//报销金额
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//招待对象
					json.put("defitem7", map.get("defitem7")==null?"":map.get("defitem7").toString());//客人人数
					json.put("defitem8", map.get("defitem8")==null?"":map.get("defitem8").toString());//陪同人数
					json.put("defitem20", map.get("defitem20")==null?"":map.get("defitem20").toString());//产业类别
					json.put("defitem21", map.get("defitem21")==null?"":map.get("defitem21").toString());//物业收入类型
					json.put("defitem11", map.get("defitem11")==null?"":map.get("defitem11").toString());//备注
					json.put("project_name", map.get("project_name")==null?"":map.get("project_name").toString());//项目
					
					zdf1jsonArr.put(json);																		
				}					
				jsonObj.put("zdf1", zdf1jsonArr);				
				
			}
			
			if(zdf2List!=null && zdf2List.size()>0){
				
				for(int m=0;m<zdf2List.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) zdf2List.get(m);
					json.put("defitem2", map.get("defitem2")==null?"":map.get("defitem2").toString());//离店日期
					json.put("defitem10", map.get("defitem10")==null?"":map.get("defitem10").toString());//招待对象
					json.put("defitem11", map.get("defitem11")==null?"":map.get("defitem11").toString());//客人人数
					json.put("defitem12", map.get("defitem12")==null?"":map.get("defitem12").toString());//陪同人数
					
					zdf2jsonArr.put(json);																		
				}					
				jsonObj.put("zdf2", zdf2jsonArr);				
				
			}
			
			StringBuffer c = new StringBuffer();
			c.append("	  select a.jkdjbh,	");
			c.append("	         b.name    psnname,	");
			c.append("	         c.name    deptname,d.name szxmid,	");
			c.append("	         a.cjkybje,	");
			c.append("	         a.cxrq,	");
			c.append("	         a.sxrq,	");
			c.append("	         a.sxbz	");
			c.append("	    from er_bxcontrast a	");
			c.append("	    left join bd_psndoc b	");
			c.append("	      on a.jkbxr = b.pk_psndoc	");
			c.append("	    left join org_dept c	");
			c.append("	      on a.deptid = c.pk_dept	");
			c.append("	    left join bd_inoutbusiclass d	");
			c.append("	      on a.szxmid = d.pk_inoutbusiclass	");
			c.append("	      where a.pk_bxd = '"+pk_bill+"'	");

							
			cxmxList = (ArrayList) dao.executeQuery(c.toString(), new MapListProcessor());

			if(cxmxList!=null && cxmxList.size()>0){
				//冲销明细
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//借款单号
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//借款人
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//借款部门
					json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//冲销原币金额
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//冲销日期
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//生效日期
					json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//生效标志
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//收支项目
					
									
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
			
			
			
		}
		
		return jsonObj;
		

		
	}
	
	/**
	 * 查询费用报销单（个人）
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryfybxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//主表JSON对象
		
		//主表拼装
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//单据编号
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//单据日期
		s.append("	       a.total,	");//合计金额
		s.append("	       b.name jkbxr,	");//报销人
		s.append("	       b.mobile,	");//手机
		s.append("	       c.code bankcode,	");//个人银行账户
		s.append("	       d.name dwbm,	");//报销人单位
		s.append("	       e.name deptid,	");//报销人部门
		s.append("	       f.name fydwbm,	");//费用承担单位
		s.append("	       g.name fydeptid,	");//费用承担部门
		s.append("	       h.name jsfs,	");//支付方式
		s.append("	       i.name fkyhzh,	");//单位银行账户
		s.append("	       j.name pk_payorg,	");//支付单位
		s.append("	       k.project_name,	");//项目
		s.append("	       case a.paytarget when 0 then '员工' when 1 then '供应商' when 2 then '客户' end  paytarget,	");// 支付对象
		s.append("	       a.zyx29,	");//备注
		s.append("	       a.zyx19	");//事由
		s.append("	  from er_bxzb a	");
		s.append("	  left join bd_psndoc b	");
		s.append("	    on a.jkbxr = b.pk_psndoc	");
		s.append("	  left join bd_bankaccsub c	");
		s.append("	    on a.skyhzh = c.pk_bankaccsub	");
		s.append("	  left join org_orgs d	");
		s.append("	    on a.dwbm = d.pk_org	");
		s.append("	  left join org_dept e	");
		s.append("	    on a.deptid = e.pk_dept	");
		s.append("	  left join org_orgs f	");
		s.append("	    on a.fydwbm = f.pk_org	");
		s.append("	  left join org_dept g	");
		s.append("	    on a.fydeptid = g.pk_dept	");
		s.append("	  left join bd_balatype h	");
		s.append("	    on a.jsfs = h.pk_balatype	");
		s.append("	  left join bd_bankaccsub i	");
		s.append("	    on a.fkyhzh = i.pk_bankaccsub	");
		s.append("	  left join org_financeorg j	");
		s.append("	    on a.pk_payorg = j.pk_financeorg	");
		s.append("	  left join bd_project k	");
		s.append("	    on a.jobid = k.pk_project	");
		s.append("	 where a.pk_jkbx = '"+pk_bill+"'	");

		List list=(List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(list!=null && list.size()>0){
			HashMap map = (HashMap) list.get(0);
	        Iterator it = map.entrySet().iterator();  
	        while(it.hasNext()){  
	            Map.Entry<Object, Object> m = (Entry<Object, Object>) it.next();  
	            jsonh.put(m.getKey().toString(), m.getValue()==null?"":m.getValue().toString()); 
	        } 
	        jsonObj.put("h", jsonh);
		}
		
		//子表的拼装
		StringBuffer b = new StringBuffer();
		b.append("	select b.name       szxmid,	");
		b.append("	       a.defitem16,	");
		b.append("	       a.defitem17,	");
		b.append("	       a.defitem18,	");
		b.append("	       a.tax_rate,	");
		b.append("	       f.taxrate defitem6,	");
		b.append("	       e.project_name jobid,	");
		b.append("	       g.name defitem21,	");
		b.append("	       a.vat_amount,	");
		b.append("	       a.tax_amount,	");
		b.append("	       a.tni_amount,	");		
		b.append("	       a.tablecode	");
		b.append("	  from er_busitem a	");
		b.append("	  left join bd_inoutbusiclass b	");
		b.append("	    on a.szxmid = b.pk_inoutbusiclass	");
		b.append("	  left join bd_defdoc c	");
		b.append("	    on a.defitem20 = c.pk_defdoc	");
		b.append("	  left join bd_project e	");
		b.append("	    on a.jobid = e.pk_project	");
		b.append("	  left join bd_taxrate f	");
		b.append("	    on a.defitem6 = f.pk_taxcode	");
		b.append("	  left join bd_defdoc g	");
		b.append("	    on a.defitem21 = g.pk_defdoc	");
		b.append("	 where 	");
		b.append("	 a.pk_jkbx = '"+pk_bill+"' and nvl(a.dr,0)=0	");	
		
		JSONArray  zdf1jsonArr = new JSONArray();//招待费明细(页签1)JSON数组
		JSONArray  cxmxjsonArr = new JSONArray();//冲销明细JSON数组
		
		ArrayList zdf1List=new ArrayList();//招待费明细(页签1)数组
		ArrayList cxmxList=new ArrayList();//冲销明细数组
		
			
		List blist=(List) dao.executeQuery(b.toString(), new MapListProcessor());
		if(blist!=null && blist.size()>0){
			
			for(int i=0;i<blist.size();i++){
				HashMap map = (HashMap) blist.get(i);
				String tablecode=null;
				tablecode=map.get("tablecode")==null?"":map.get("tablecode").toString();
				if("arap_bxbusitem".equals(tablecode)){
					zdf1List.add(map);
				}					
			}
			
			if(zdf1List!=null && zdf1List.size()>0){
				
				for(int m=0;m<zdf1List.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) zdf1List.get(m);
					json.put("defitem16", map.get("defitem16")==null?"":map.get("defitem16").toString());//费用总额
					json.put("defitem17", map.get("defitem17")==null?"":map.get("defitem17").toString());//已报销金额
					json.put("defitem18", map.get("defitem18")==null?"":map.get("defitem18").toString());//剩余可报销金额
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//收支项目
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//税率
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//增值税税率
					json.put("jobid", map.get("jobid")==null?"":map.get("jobid").toString());//产业类别
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//报销金额
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//税金
					json.put("tni_amount", map.get("tni_amount")==null?"":map.get("tni_amount").toString());//不含税金额
										
					
					zdf1jsonArr.put(json);																		
				}					
				jsonObj.put("zdf1", zdf1jsonArr);				
				
			}
			

			
			StringBuffer c = new StringBuffer();
			c.append("	  select a.jkdjbh,	");
			c.append("	         b.name    psnname,	");
			c.append("	         c.name    deptname,	");
			c.append("	         a.cxrq,	");
			c.append("	         a.sxrq,a.bbje,	");
			c.append("	         a.sxbz	");
			c.append("	    from er_bxcontrast a	");
			c.append("	    left join bd_psndoc b	");
			c.append("	      on a.jkbxr = b.pk_psndoc	");
			c.append("	    left join org_dept c	");
			c.append("	      on a.deptid = c.pk_dept	");
			c.append("	      where a.pk_bxd = '"+pk_bill+"'	");

							
			cxmxList = (ArrayList) dao.executeQuery(c.toString(), new MapListProcessor());

			if(cxmxList!=null && cxmxList.size()>0){
				//冲销明细
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//借款单号
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//借款人
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//借款部门
					json.put("bbje", map.get("bbje")==null?"":map.get("bbje").toString());//申请金额
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//申请日期
					json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//生效标志
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//生效日期
														
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
			
			
			
		}
		
		return jsonObj;
		

		
	}
	/**
	 * 查询电脑补贴报销单
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object querydnbtbxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//主表JSON对象
		
		//主表拼装
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//单据编号
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//单据日期
		s.append("	       d.name dwbm,	");//报销人单位
		s.append("	       e.name deptid,	");//报销人部门
		s.append("	       b.name jkbxr,	");//报销人
		s.append("	       b.mobile,	");//手机
		s.append("	       c.code bankcode,	");//个人银行账户
		s.append("	       a.total,	");//合计金额						
		s.append("	       f.name fydwbm,	");//费用承担单位
		s.append("	       g.name fydeptid,	");//费用承担部门
		s.append("	       j.name pk_payorg,	");//支付单位
		s.append("	       h.name jsfs,	");//结算方式
		s.append("	       i.name fkyhzh,	");//单位银行账户		
		s.append("	       k.project_name,	");//项目
		s.append("	       a.zyx29,	");//备注
		s.append("	       a.zyx19	");//事由
		s.append("		");
		s.append("	  from er_bxzb a	");
		s.append("	  left join bd_psndoc b	");
		s.append("	    on a.jkbxr = b.pk_psndoc	");
		s.append("	  left join bd_bankaccsub c	");
		s.append("	    on a.skyhzh = c.pk_bankaccsub	");
		s.append("	  left join org_orgs d	");
		s.append("	    on a.dwbm = d.pk_org	");
		s.append("	  left join org_dept e	");
		s.append("	    on a.deptid = e.pk_dept	");
		s.append("	  left join org_orgs f	");
		s.append("	    on a.fydwbm = f.pk_org	");
		s.append("	  left join org_dept g	");
		s.append("	    on a.fydeptid = g.pk_dept	");
		s.append("	  left join bd_balatype h	");
		s.append("	    on a.jsfs = h.pk_balatype	");
		s.append("	  left join bd_bankaccsub i	");
		s.append("	    on a.fkyhzh = i.pk_bankaccsub	");
		s.append("	  left join org_financeorg j	");
		s.append("	    on a.pk_payorg = j.pk_financeorg	");
		s.append("	  left join bd_project k	");
		s.append("	    on a.jobid = k.pk_project	");
		s.append("	 where a.pk_jkbx = '"+pk_bill+"'	");

		List list=(List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(list!=null && list.size()>0){
			HashMap map = (HashMap) list.get(0);
	        Iterator it = map.entrySet().iterator();  
	        while(it.hasNext()){  
	            Map.Entry<Object, Object> m = (Entry<Object, Object>) it.next();  
	            jsonh.put(m.getKey().toString(), m.getValue()==null?"":m.getValue().toString()); 
	        } 
	        jsonObj.put("h", jsonh);
		}
		
		//子表的拼装
		StringBuffer b = new StringBuffer();
		b.append("	select b.name       szxmid,	");
		b.append("	       a.defitem8,	");
		b.append("	       a.fphm,	");
		b.append("	       a.tax_rate,	");
		b.append("	       a.tax_amount,	");
		b.append("	       a.vat_amount,	");
		b.append("	       f.taxrate defitem6,	");
		b.append("	       c.name defitem20,	");	
		b.append("	       a.tablecode	");
		b.append("	  from er_busitem a	");
		b.append("	  left join bd_inoutbusiclass b	");
		b.append("	    on a.szxmid = b.pk_inoutbusiclass	");
		b.append("	  left join bd_defdoc c	");
		b.append("	    on a.defitem20 = c.pk_defdoc	");
		b.append("	  left join bd_taxrate f	");
		b.append("	    on a.defitem6 = f.pk_taxcode	");
		b.append("	 where 	");
		b.append("	 a.pk_jkbx ="
				+ " '"+pk_bill+"' and nvl(a.dr,0)=0	");	
		
		JSONArray  zdf1jsonArr = new JSONArray();//电脑信息JSON数组
		JSONArray  cxmxjsonArr = new JSONArray();//冲销明细JSON数组
		
		ArrayList zdf1List=new ArrayList();//电脑信息数组
		ArrayList cxmxList=new ArrayList();//冲销明细数组
		
			
		List blist=(List) dao.executeQuery(b.toString(), new MapListProcessor());
		if(blist!=null && blist.size()>0){
			
			for(int i=0;i<blist.size();i++){
				HashMap map = (HashMap) blist.get(i);
				String tablecode=null;
				tablecode=map.get("tablecode")==null?"":map.get("tablecode").toString();
				if("arap_bxbusitem".equals(tablecode)){
					zdf1List.add(map);
				}					
			}
			
			if(zdf1List!=null && zdf1List.size()>0){
				
				for(int m=0;m<zdf1List.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) zdf1List.get(m);
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//收支项目
					json.put("defitem8", map.get("defitem8")==null?"":map.get("defitem8").toString());//电脑型号
					json.put("fphm", map.get("fphm")==null?"":map.get("fphm").toString());//发票号码
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//税率
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//税金
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//报销金额					
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//增值税税率
					json.put("jobid", map.get("jobid")==null?"":map.get("jobid").toString());//产业类别
										
					
					zdf1jsonArr.put(json);																		
				}					
				jsonObj.put("zdf1", zdf1jsonArr);				
				
			}
			

			
			StringBuffer c = new StringBuffer();
			c.append("	  select a.jkdjbh,	");
			c.append("	         b.name    psnname,	");
			c.append("	         c.name    deptname,	");
			c.append("	         a.fyybje	");
			c.append("	    from er_bxcontrast a	");
			c.append("	    left join bd_psndoc b	");
			c.append("	      on a.jkbxr = b.pk_psndoc	");
			c.append("	    left join org_dept c	");
			c.append("	      on a.deptid = c.pk_dept	");
			c.append("	      where a.pk_bxd = '"+pk_bill+"'	");

							
			cxmxList = (ArrayList) dao.executeQuery(c.toString(), new MapListProcessor());

			if(cxmxList!=null && cxmxList.size()>0){
				//冲销明细
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//借款单号
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//借款人
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//借款部门
					json.put("fyybje", map.get("fyybje")==null?"":map.get("fyybje").toString());//费用原币金额

														
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
						
			
		}
		
		return jsonObj;
		

		
	}
	
	/**
	 * 查询成本类报销单
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object querycblbxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//主表JSON对象
		
		//主表拼装
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//单据编号
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//单据日期
		s.append("	       d.name dwbm,	");//报销人单位
		s.append("	       e.name deptid,	");//报销人部门
		s.append("	       b.name jkbxr,	");//报销人
		s.append("	       b.mobile,	");//手机
		s.append("	       c.code skyhzh,	");//个人银行账户
		s.append("	       a.total,	");//合计金额						
		s.append("	       f.name fydwbm,	");//费用承担单位
		s.append("	       g.name fydeptid,	");//费用承担部门
		s.append("	       j.name pk_payorg,	");//支付单位
		s.append("	       h.name jsfs,	");//结算方式
		s.append("	       i.name fkyhzh,	");//单位银行账户
		s.append("	       case a.paytarget when 0 then '员工' when 1 then '供应商' when 2 then '客户' end  paytarget,	");// 支付对象
		s.append("	       l.name hbbm,	");//供应商
		s.append("	       m.code custaccount,	");//供应商银行账户
		s.append("	       a.zyx19	");//事由
		s.append("		");
		s.append("	  from er_bxzb a	");
		s.append("	  left join bd_psndoc b	");
		s.append("	    on a.jkbxr = b.pk_psndoc	");
		s.append("	  left join bd_bankaccsub c	");
		s.append("	    on a.skyhzh = c.pk_bankaccsub	");
		s.append("	  left join org_orgs d	");
		s.append("	    on a.dwbm = d.pk_org	");
		s.append("	  left join org_dept e	");
		s.append("	    on a.deptid = e.pk_dept	");
		s.append("	  left join org_orgs f	");
		s.append("	    on a.fydwbm = f.pk_org	");
		s.append("	  left join org_dept g	");
		s.append("	    on a.fydeptid = g.pk_dept	");
		s.append("	  left join bd_balatype h	");
		s.append("	    on a.jsfs = h.pk_balatype	");
		s.append("	  left join bd_bankaccsub i	");
		s.append("	    on a.fkyhzh = i.pk_bankaccsub	");
		s.append("	  left join org_financeorg j	");
		s.append("	    on a.pk_payorg = j.pk_financeorg	");
		s.append("	  left join bd_project k	");
		s.append("	    on a.jobid = k.pk_project	");
		s.append("	  left join bd_supplier l	");
		s.append("	    on a.hbbm = l.pk_supplier	");
		s.append("	  left join bd_bankaccsub m	");
		s.append("	    on a.custaccount = m.pk_bankaccsub	");	
		s.append("	 where a.pk_jkbx = '"+pk_bill+"'	");

		List list=(List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(list!=null && list.size()>0){
			HashMap map = (HashMap) list.get(0);
	        Iterator it = map.entrySet().iterator();  
	        while(it.hasNext()){  
	            Map.Entry<Object, Object> m = (Entry<Object, Object>) it.next();  
	            jsonh.put(m.getKey().toString(), m.getValue()==null?"":m.getValue().toString()); 
	        } 
	        jsonObj.put("h", jsonh);
		}
		
		//子表的拼装
		StringBuffer b = new StringBuffer();
		b.append("	select b.name       szxmid,	");
		b.append("	       a.defitem8,	");
		b.append("	       a.fphm,	");
		b.append("	       a.tax_rate,	");
		b.append("	       a.tax_amount,	");
		b.append("	       a.vat_amount,	");
		b.append("	       f.taxrate defitem6,	");
		b.append("	       c.name defitem20,a.tni_amount,	");	
		b.append("	       a.tablecode	");
		b.append("	  from er_busitem a	");
		b.append("	  left join bd_inoutbusiclass b	");
		b.append("	    on a.szxmid = b.pk_inoutbusiclass	");
		b.append("	  left join bd_defdoc c	");
		b.append("	    on a.defitem20 = c.pk_defdoc	");
		b.append("	  left join bd_taxrate f	");
		b.append("	    on a.defitem6 = f.pk_taxcode	");
		b.append("	 where 	");
		b.append("	 a.pk_jkbx ="
				+ " '"+pk_bill+"' and nvl(a.dr,0)=0	");	
		
		JSONArray  cbqrjsonArr = new JSONArray();//成本确认JSON数组
		JSONArray  cxmxjsonArr = new JSONArray();//冲销明细JSON数组
		
		ArrayList cbqrList=new ArrayList();//成本确认数组
		ArrayList cxmxList=new ArrayList();//冲销明细数组
		
			
		List blist=(List) dao.executeQuery(b.toString(), new MapListProcessor());
		if(blist!=null && blist.size()>0){
			
			for(int i=0;i<blist.size();i++){
				HashMap map = (HashMap) blist.get(i);
				String tablecode=null;
				tablecode=map.get("tablecode")==null?"":map.get("tablecode").toString();
				if("arap_bxbusitem".equals(tablecode)){
					cbqrList.add(map);
				}					
			}
			
			if(cbqrList!=null && cbqrList.size()>0){
				
				for(int m=0;m<cbqrList.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cbqrList.get(m);
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//收支项目
					json.put("fphm", map.get("fphm")==null?"":map.get("fphm").toString());//发票号码
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//增值税税率
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//税金
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//税率
					json.put("tni_amount", map.get("tni_amount")==null?"":map.get("tni_amount").toString());//不含税金额					
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//报销金额										
					json.put("defitem20", map.get("defitem20")==null?"":map.get("defitem20").toString());//产业类别										
					
					cbqrjsonArr.put(json);																		
				}					
				jsonObj.put("cbqr", cbqrjsonArr);				
				
			}
			
			
			StringBuffer c = new StringBuffer();
			c.append("	  select a.jkdjbh,	");
			c.append("	         b.name    psnname,	");
			c.append("	         c.name    deptname,	");
			c.append("	         a.fyybje,a.cjkybje,a.cxrq,a.sxrq,a.sxbz	");
			c.append("	    from er_bxcontrast a	");
			c.append("	    left join bd_psndoc b	");
			c.append("	      on a.jkbxr = b.pk_psndoc	");
			c.append("	    left join org_dept c	");
			c.append("	      on a.deptid = c.pk_dept	");
			c.append("	      where a.pk_bxd = '"+pk_bill+"'	");

							
			cxmxList = (ArrayList) dao.executeQuery(c.toString(), new MapListProcessor());

			if(cxmxList!=null && cxmxList.size()>0){
				//冲销明细
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//借款单号
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//借款人
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//借款部门
					json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//冲销金额
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//冲销日期
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//生效日期
					json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//生效标志

														
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
						
			
		}
		
		return jsonObj;
				
	}
	
	/**
	 * 查询销售宣传费用报销单
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryxsxcfybxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//主表JSON对象
		
		//主表拼装
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//单据编号
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//单据日期
		s.append("	       d.name dwbm,	");//报销人单位
		s.append("	       e.name deptid,	");//报销人部门
		s.append("	       b.name jkbxr,	");//报销人
		s.append("	       b.mobile,	");//手机
		s.append("	       c.code bankcode,	");//个人银行账户
		s.append("	       a.total,	");//合计金额						
		s.append("	       f.name fydwbm,	");//费用承担单位
		s.append("	       g.name fydeptid,	");//费用承担部门
		s.append("	       j.name pk_payorg,	");//支付单位
		s.append("	       h.name jsfs,	");//结算方式
		s.append("	       i.name fkyhzh,	");//单位银行账户
		s.append("	       case a.paytarget when 0 then '员工' when 1 then '供应商' when 2 then '客户' end  paytarget,	");// 支付对象
		s.append("	       l.name hbbm,	");//供应商
		s.append("	       m.code custaccount,	");//供应商银行账户
		s.append("	       k.project_name,	");//项目
		s.append("	       a.zyx29,	");//备注
		s.append("	       a.zyx5	");//事由
		s.append("		");
		s.append("	  from er_bxzb a	");
		s.append("	  left join bd_psndoc b	");
		s.append("	    on a.jkbxr = b.pk_psndoc	");
		s.append("	  left join bd_bankaccsub c	");
		s.append("	    on a.skyhzh = c.pk_bankaccsub	");
		s.append("	  left join org_orgs d	");
		s.append("	    on a.dwbm = d.pk_org	");
		s.append("	  left join org_dept e	");
		s.append("	    on a.deptid = e.pk_dept	");
		s.append("	  left join org_orgs f	");
		s.append("	    on a.fydwbm = f.pk_org	");
		s.append("	  left join org_dept g	");
		s.append("	    on a.fydeptid = g.pk_dept	");
		s.append("	  left join bd_balatype h	");
		s.append("	    on a.jsfs = h.pk_balatype	");
		s.append("	  left join bd_bankaccsub i	");
		s.append("	    on a.fkyhzh = i.pk_bankaccsub	");
		s.append("	  left join org_financeorg j	");
		s.append("	    on a.pk_payorg = j.pk_financeorg	");
		s.append("	  left join bd_project k	");
		s.append("	    on a.jobid = k.pk_project	");
		s.append("	  left join bd_supplier l	");
		s.append("	    on a.hbbm = l.pk_supplier	");
		s.append("	  left join bd_bankaccsub m	");
		s.append("	    on a.custaccount = m.pk_bankaccsub	");	
		s.append("	 where a.pk_jkbx = '"+pk_bill+"'	");

		List list=(List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(list!=null && list.size()>0){
			HashMap map = (HashMap) list.get(0);
	        Iterator it = map.entrySet().iterator();  
	        while(it.hasNext()){  
	            Map.Entry<Object, Object> m = (Entry<Object, Object>) it.next();  
	            jsonh.put(m.getKey().toString(), m.getValue()==null?"":m.getValue().toString()); 
	        } 
	        jsonObj.put("h", jsonh);
		}
		
		//子表的拼装
		StringBuffer b = new StringBuffer();
		b.append("	select b.name       szxmid,	");
		b.append("	       a.defitem8,	");
		b.append("	       a.fpdm,	");
		b.append("	       a.tax_rate,	");
		b.append("	       a.tax_amount,	");
		b.append("	       a.vat_amount,	");
		b.append("	       f.taxrate defitem6,	");
		b.append("	       c.name defitem20,a.tni_amount,	");	
		b.append("	       a.tablecode	");
		b.append("	  from er_busitem a	");
		b.append("	  left join bd_inoutbusiclass b	");
		b.append("	    on a.szxmid = b.pk_inoutbusiclass	");
		b.append("	  left join bd_defdoc c	");
		b.append("	    on a.defitem20 = c.pk_defdoc	");
		b.append("	  left join bd_taxrate f	");
		b.append("	    on a.defitem6 = f.pk_taxcode	");
		b.append("	 where 	");
		b.append("	 a.pk_jkbx ="
				+ " '"+pk_bill+"' and nvl(a.dr,0)=0	");	
		
		JSONArray  xsxcfyjsonArr = new JSONArray();//销售宣传费用JSON数组
		JSONArray  cxmxjsonArr = new JSONArray();//冲销明细JSON数组
		
		ArrayList xsxcfyList=new ArrayList();//销售宣传费用数组
		ArrayList cxmxList=new ArrayList();//冲销明细数组
		
			
		List blist=(List) dao.executeQuery(b.toString(), new MapListProcessor());
		if(blist!=null && blist.size()>0){
			
			for(int i=0;i<blist.size();i++){
				HashMap map = (HashMap) blist.get(i);
				String tablecode=null;
				tablecode=map.get("tablecode")==null?"":map.get("tablecode").toString();
				if("arap_bxbusitem".equals(tablecode)){
					xsxcfyList.add(map);
				}					
			}
			
			if(xsxcfyList!=null && xsxcfyList.size()>0){
				
				for(int m=0;m<xsxcfyList.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) xsxcfyList.get(m);
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//收支项目
					json.put("fpdm", map.get("fpdm")==null?"":map.get("fpdm").toString());//发票号
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//税额
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//税率
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//报销金额
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//增值税税率
					json.put("defitem20", map.get("defitem20")==null?"":map.get("defitem20").toString());//产业类别
									
					xsxcfyjsonArr.put(json);																		
				}					
				jsonObj.put("xsxcfy", xsxcfyjsonArr);				
				
			}
			
			
			StringBuffer c = new StringBuffer();
			c.append("	  select a.jkdjbh,	");
			c.append("	         b.name    psnname,	");
			c.append("	         c.name    deptname,	");
			c.append("	         a.fyybje,a.cjkybje,a.cxrq,a.sxrq,a.sxbz	");
			c.append("	    from er_bxcontrast a	");
			c.append("	    left join bd_psndoc b	");
			c.append("	      on a.jkbxr = b.pk_psndoc	");
			c.append("	    left join org_dept c	");
			c.append("	      on a.deptid = c.pk_dept	");
			c.append("	      where a.pk_bxd = '"+pk_bill+"'	");

							
			cxmxList = (ArrayList) dao.executeQuery(c.toString(), new MapListProcessor());

			if(cxmxList!=null && cxmxList.size()>0){
				//冲销明细
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//借款单号
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//借款人
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//借款部门
					json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//冲销原币金额
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//冲销日期
					json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//生效标志
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//生效日期
																			
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
									
		}
		
		return jsonObj;
				
	}
	
	/**
	 * 查询公车费用报销单
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object querygcfybxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//主表JSON对象
		
		//主表拼装
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//单据编号
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//单据日期
		s.append("	       d.name dwbm,	");//报销人单位
		s.append("	       e.name deptid,	");//报销人部门
		s.append("	       b.name jkbxr,	");//报销人
		s.append("	       b.mobile,	");//手机
		s.append("	       c.code bankcode,	");//个人银行账户
		s.append("	       a.total,	");//合计金额						
		s.append("	       f.name fydwbm,	");//费用承担单位
		s.append("	       g.name fydeptid,	");//费用承担部门
		s.append("	       j.name pk_payorg,	");//支付单位
		s.append("	       h.name jsfs,	");//结算方式
		s.append("	       i.name fkyhzh,	");//单位银行账户
		s.append("	       case a.paytarget when 0 then '员工' when 1 then '供应商' when 2 then '客户' end  paytarget,	");// 支付对象
		s.append("	       l.name hbbm,	");//供应商
		s.append("	       m.code custaccount,	");//客商银行账户
		s.append("	       k.project_name,	");//项目
		s.append("	       a.zyx5	");//事由
		s.append("		");
		s.append("	  from er_bxzb a	");
		s.append("	  left join bd_psndoc b	");
		s.append("	    on a.jkbxr = b.pk_psndoc	");
		s.append("	  left join bd_bankaccsub c	");
		s.append("	    on a.skyhzh = c.pk_bankaccsub	");
		s.append("	  left join org_orgs d	");
		s.append("	    on a.dwbm = d.pk_org	");
		s.append("	  left join org_dept e	");
		s.append("	    on a.deptid = e.pk_dept	");
		s.append("	  left join org_orgs f	");
		s.append("	    on a.fydwbm = f.pk_org	");
		s.append("	  left join org_dept g	");
		s.append("	    on a.fydeptid = g.pk_dept	");
		s.append("	  left join bd_balatype h	");
		s.append("	    on a.jsfs = h.pk_balatype	");
		s.append("	  left join bd_bankaccsub i	");
		s.append("	    on a.fkyhzh = i.pk_bankaccsub	");
		s.append("	  left join org_financeorg j	");
		s.append("	    on a.pk_payorg = j.pk_financeorg	");
		s.append("	  left join bd_project k	");
		s.append("	    on a.jobid = k.pk_project	");
		s.append("	  left join bd_supplier l	");
		s.append("	    on a.hbbm = l.pk_supplier	");
		s.append("	  left join bd_bankaccsub m	");
		s.append("	    on a.custaccount = m.pk_bankaccsub	");	
		s.append("	 where a.pk_jkbx = '"+pk_bill+"'	");

		List list=(List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(list!=null && list.size()>0){
			HashMap map = (HashMap) list.get(0);
	        Iterator it = map.entrySet().iterator();  
	        while(it.hasNext()){  
	            Map.Entry<Object, Object> m = (Entry<Object, Object>) it.next();  
	            jsonh.put(m.getKey().toString(), m.getValue()==null?"":m.getValue().toString()); 
	        } 
	        jsonObj.put("h", jsonh);
		}
		
		//子表的拼装
		StringBuffer b = new StringBuffer();
		b.append("	select d.name defitem10,b.name       szxmid,	");
		b.append("	       a.defitem8,	");
		b.append("	       a.fphm,	");
		b.append("	       a.tax_rate,	");
		b.append("	       a.tax_amount,	");
		b.append("	       a.vat_amount,	");
		b.append("	       f.taxrate defitem6,	");
		b.append("	       c.name defitem20,a.tni_amount,a.defitem1,a.defitem2,a.defitem9,a.defitem11,		");	
		b.append("	       a.tablecode	");
		b.append("	  from er_busitem a	");
		b.append("	  left join bd_inoutbusiclass b	");
		b.append("	    on a.szxmid = b.pk_inoutbusiclass	");
		b.append("	  left join bd_defdoc c	");
		b.append("	    on a.defitem20 = c.pk_defdoc	");
		b.append("	  left join bd_taxrate f	");
		b.append("	    on a.defitem6 = f.pk_taxcode	");
		b.append("	  left join bd_defdoc d	");
		b.append("	    on a.defitem10 = d.pk_defdoc	");
		b.append("	 where 	");
		b.append("	 a.pk_jkbx ="
				+ " '"+pk_bill+"' and nvl(a.dr,0)=0	");	
		
		JSONArray gcfyjsonArr = new JSONArray();//公车费用JSON数组
		JSONArray zsfyjsonArr = new JSONArray();//住宿费用JSON数组
		JSONArray ccbtjsonArr = new JSONArray();//出差补贴JSON数组
		JSONArray cxmxjsonArr = new JSONArray();//冲销明细JSON数组
		
		ArrayList gcfyList=new ArrayList();//公车费用数组
		ArrayList zsfyList=new ArrayList();//住宿费用数组
		ArrayList ccbtList=new ArrayList();//出差补贴数组
		ArrayList cxmxList=new ArrayList();//冲销明细数组
		
		List blist=(List) dao.executeQuery(b.toString(), new MapListProcessor());
		if(blist!=null && blist.size()>0){
			
			for(int i=0;i<blist.size();i++){
				HashMap map = (HashMap) blist.get(i);
				String tablecode=null;
				tablecode=map.get("tablecode")==null?"":map.get("tablecode").toString();
				if("arap_bxbusitem".equals(tablecode)){
					gcfyList.add(map);
				}else if("other".equals(tablecode)){
					zsfyList.add(map);
				}else if("bzitem".equals(tablecode)){
					ccbtList.add(map);
				}
			}
			
			if(gcfyList!=null && gcfyList.size()>0){
				
				for(int m=0;m<gcfyList.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) gcfyList.get(m);
					json.put("defitem10", map.get("defitem10")==null?"":map.get("defitem10").toString());//汽车档案
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//收支项目
					json.put("defitem1", map.get("defitem1")==null?"":map.get("defitem1").toString());//出发日期
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//增值税税率
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//税率
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//税金
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//报销金额
					json.put("tni_amount", map.get("tni_amount")==null?"":map.get("tni_amount").toString());//不含税金额					
					json.put("fphm", map.get("fphm")==null?"":map.get("fphm").toString());//发票号码

									
					gcfyjsonArr.put(json);																		
				}					
				jsonObj.put("gcfy", gcfyjsonArr);				
				
			}
			
			if(zsfyList!=null && zsfyList.size()>0){
				
				for(int m=0;m<zsfyList.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) zsfyList.get(m);
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//收支项目
					json.put("defitem1", map.get("defitem1")==null?"":map.get("defitem1").toString());//入住日期
					json.put("defitem2", map.get("defitem2")==null?"":map.get("defitem2").toString());//离店日期
					json.put("defitem9", map.get("defitem9")==null?"":map.get("defitem9").toString());//住宿天数
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//税率
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//税金
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//报销金额


									
					zsfyjsonArr.put(json);																		
				}					
				jsonObj.put("zsfy", zsfyjsonArr);				
				
			}
			
			if(ccbtList!=null && ccbtList.size()>0){
				
				for(int m=0;m<ccbtList.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) ccbtList.get(m);
					json.put("defitem11", map.get("defitem11")==null?"":map.get("defitem11").toString());//出差补贴天数
					json.put("defitem9", map.get("defitem9")==null?"":map.get("defitem9").toString());//出差天数
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//收支项目
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//补贴金额
									
					ccbtjsonArr.put(json);																		
				}					
				jsonObj.put("ccbt", ccbtjsonArr);				
				
			}
			
			
			StringBuffer c = new StringBuffer();
			c.append("	  select a.jkdjbh,	");
			c.append("	         b.name    psnname,	");
			c.append("	         c.name    deptname,	");
			c.append("	         a.fyybje,a.cjkybje,a.cxrq,a.sxrq,a.sxbz	");
			c.append("	    from er_bxcontrast a	");
			c.append("	    left join bd_psndoc b	");
			c.append("	      on a.jkbxr = b.pk_psndoc	");
			c.append("	    left join org_dept c	");
			c.append("	      on a.deptid = c.pk_dept	");
			c.append("	      where a.pk_bxd = '"+pk_bill+"'	");

							
			cxmxList = (ArrayList) dao.executeQuery(c.toString(), new MapListProcessor());

			if(cxmxList!=null && cxmxList.size()>0){
				//冲销明细
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//借款单号
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//借款人
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//借款部门
					json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//冲销金额
					json.put("fyybje", map.get("fyybje")==null?"":map.get("fyybje").toString());//费用原币金额
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//冲销日期
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//生效日期
																			
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
									
		}
		
		return jsonObj;
				
	}
	
	/**
	 * 查询还款单
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryhkd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//主表JSON对象
		
		//主表拼装
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//单据编号
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//单据日期
		s.append("	       a.hkybje,	");//还款金额
		s.append("	       b.name jkbxr,	");//还款人		
		s.append("	       d.name dwbm,	");//报销人单位
		s.append("	       e.name deptid,	");//报销人部门
		s.append("	       a.cjkbbje,	");//冲借款金额
		s.append("	       f.name fydwbm,	");//费用承担单位
		s.append("	       g.name fydeptid,	");//费用承担部门
		s.append("	       j.name pk_payorg,	");//支付单位
		s.append("	       c.code bankcode,	");//个人银行账户
		s.append("	       h.name jsfs	");//结算方式
		s.append("	  from er_bxzb a	");
		s.append("	  left join bd_psndoc b	");
		s.append("	    on a.jkbxr = b.pk_psndoc	");
		s.append("	  left join bd_bankaccsub c	");
		s.append("	    on a.skyhzh = c.pk_bankaccsub	");
		s.append("	  left join org_orgs d	");
		s.append("	    on a.dwbm = d.pk_org	");
		s.append("	  left join org_dept e	");
		s.append("	    on a.deptid = e.pk_dept	");
		s.append("	  left join org_orgs f	");
		s.append("	    on a.fydwbm = f.pk_org	");
		s.append("	  left join org_dept g	");
		s.append("	    on a.fydeptid = g.pk_dept	");
		s.append("	  left join bd_balatype h	");
		s.append("	    on a.jsfs = h.pk_balatype	");
		s.append("	  left join bd_bankaccsub i	");
		s.append("	    on a.fkyhzh = i.pk_bankaccsub	");
		s.append("	  left join org_financeorg j	");
		s.append("	    on a.pk_payorg = j.pk_financeorg	");
		s.append("	 where a.pk_jkbx = '"+pk_bill+"'	");

		List list=(List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(list!=null && list.size()>0){
			HashMap map = (HashMap) list.get(0);
	        Iterator it = map.entrySet().iterator();  
	        while(it.hasNext()){  
	            Map.Entry<Object, Object> m = (Entry<Object, Object>) it.next();  
	            jsonh.put(m.getKey().toString(), m.getValue()==null?"":m.getValue().toString()); 
	        } 
	        jsonObj.put("h", jsonh);
		}
		

		JSONArray  cxmxjsonArr = new JSONArray();//冲销明细JSON数组
		ArrayList cxmxList=new ArrayList();//冲销明细数组

			
			
			StringBuffer c = new StringBuffer();
			c.append("	  select d.name szxmid, a.jkdjbh,	");
			c.append("	         b.name    psnname,	");
			c.append("	         c.name    deptname,	");
			c.append("	         a.fyybje,a.cjkybje,a.cxrq,a.sxrq,a.sxbz	");
			c.append("	    from er_bxcontrast a	");
			c.append("	    left join bd_psndoc b	");
			c.append("	      on a.jkbxr = b.pk_psndoc	");
			c.append("	    left join org_dept c	");
			c.append("	      on a.deptid = c.pk_dept	");
			c.append("	  left join bd_inoutbusiclass d	");
			c.append("	    on a.szxmid = d.pk_inoutbusiclass	");
			c.append("	      where a.pk_bxd = '"+pk_bill+"'	");

							
			cxmxList = (ArrayList) dao.executeQuery(c.toString(), new MapListProcessor());

			if(cxmxList!=null && cxmxList.size()>0){
				//冲销明细
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//借款单号
//					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//借款人
					json.put("jkbxr", map.get("psnname")==null?"":map.get("psnname").toString());//借款人
//					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//借款部门
					json.put("deptid", map.get("deptname")==null?"":map.get("deptname").toString());//借款部门
//					json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//还款金额
					json.put("hkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//还款金额
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//冲销日期
					json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//生效标志
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//生效日期
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//收支项目
																			
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("hkxx", cxmxjsonArr);
			}
									
		
		
		return jsonObj;
				
	}
	
	/**
	 * 查询油卡充值申请单
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryykczsqd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//主表JSON对象
		
		//主表拼装
		StringBuffer s = new StringBuffer();
		s.append("	select a.billno,	");//单据编号
		s.append("	       substr(a.billdate, 0, 10) billdate,	");//单据日期
		s.append("	       b.name billmaker,	");//申请人
		s.append("	       c.name apply_org,	");//申请单位
		s.append("	       d.name apply_dept,	");//申请部门
//		s.append("	       a.orig_amount,	");//申请金额
		s.append("	       a.orig_amount amount,	");//申请金额
		s.append("	       a.defitem2,	");//手机
		s.append("	       a.defitem3,	");//充值卡号
		s.append("	       a.defitem29,	");//单据录入说明
		s.append("	       a.defitem30	");//事由
		s.append("	  from er_mtapp_bill a	");
		s.append("	  left join bd_psndoc b	");
		s.append("	    on a.billmaker = b.pk_psndoc	");
		s.append("	  left join org_orgs c	");
		s.append("	    on a.apply_org = c.pk_org	");
		s.append("	  left join org_dept d	");
		s.append("	    on a.apply_dept = d.pk_dept	");
		s.append("	 where a.pk_mtapp_bill = '"+pk_bill+"'	");

		List list=(List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(list!=null && list.size()>0){
			HashMap map = (HashMap) list.get(0);
	        Iterator it = map.entrySet().iterator();  
	        while(it.hasNext()){  
	            Map.Entry<Object, Object> m = (Entry<Object, Object>) it.next();  
	            jsonh.put(m.getKey().toString(), m.getValue()==null?"":m.getValue().toString()); 
	        } 
	        jsonObj.put("h", jsonh);
		}
		
		//子表的拼装
		StringBuffer b = new StringBuffer();
		b.append("	select orig_amount,defitem2 from er_mtapp_detail where pk_mtapp_bill='"+pk_bill+"'	");

		
		JSONArray detailjsonArr = new JSONArray();//JSON数组

		
			
		List detailList=(List) dao.executeQuery(b.toString(), new MapListProcessor());
		if(detailList!=null && detailList.size()>0){
			
			for(int m=0;m<detailList.size();m++){
				JSONObject json = new JSONObject();
				HashMap map = (HashMap) detailList.get(m);
//				json.put("orig_amount", map.get("defitorig_amountem10")==null?"":map.get("orig_amount").toString());//充值金额
				json.put("amount", map.get("orig_amount")==null?"":map.get("orig_amount").toString());//充值金额
				json.put("defitem2", map.get("defitem2")==null?"":map.get("defitem2").toString());//备注
								
				detailjsonArr.put(json);																		
			}					
//			jsonObj.put("detail", detailjsonArr);
			jsonObj.put("zdf1", detailjsonArr);
												
		}
		
		return jsonObj;
				
	}
	
	/**
	 * 查询借款单（个人）
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryjkdgr(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//主表JSON对象
		
		//主表拼装
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//单据编号
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//单据日期
		s.append("	       a.total,	");//借款金额
		s.append("	       b.name jkbxr,	");//借款人
		s.append("	       c.name pk_org,	");//借款单位
		s.append("	       d.postname zyx1,	");//借款人岗位
		s.append("	       b.mobile mobile,	");//手机
		s.append("	       e.accnum skyhzh,	");//个人银行账户
		s.append("	       f.name fydwbm,	");//费用承担单位
		s.append("	       g.name fydeptid,	");//费用承担部门
		s.append("	       h.name pk_payorg,	");//支付单位
		s.append("	       i.name jsfs,	");//结算方式
		s.append("	       j.accnum fkyhzh,	");//单位银行账户
		s.append("	       k.name pk_cashaccount,	");//现金账户
		s.append("	       a.zyx5,	");//事由
		s.append("	       l.name deptid,	");//借款人部门
		s.append("	       m.project_name jobid	");//项目
		s.append("	  from er_jkzb a	");
		s.append("	  left join bd_psndoc b	");
		s.append("	    on a.jkbxr = b.pk_psndoc	");
		s.append("	  left join org_orgs c	");
		s.append("	    on a.pk_org = c.pk_org	");
		s.append("	  left join om_post d	");
		s.append("	    on a.zyx1 = d.pk_post	");
		s.append("	  left join bd_bankaccsub e	");
		s.append("	    on a.skyhzh = e.pk_bankaccsub	");
		s.append("	  left join org_orgs f	");
		s.append("	    on a.fydwbm = f.pk_org	");
		s.append("	  left join org_dept g	");
		s.append("	    on a.fydeptid = g.pk_dept	");
		s.append("	  left join org_financeorg h	");
		s.append("	    on a.pk_payorg = h.pk_financeorg	");
		s.append("	  left join bd_balatype i	");
		s.append("	    on a.jsfs = i.pk_balatype	");
		s.append("	  left join bd_bankaccsub j	");
		s.append("	    on a.fkyhzh = j.pk_bankaccsub	");
		s.append("	  left join bd_cashaccount k	");
		s.append("	    on a.pk_cashaccount = k.pk_cashaccount	");
		s.append("	  left join org_dept l	");
		s.append("	    on a.deptid = l.pk_dept	");
		s.append("	  left join bd_project m	");
		s.append("	    on a.jobid = m.pk_project	");
		s.append("	  where a.pk_jkbx='"+pk_bill+"'	");


		List list=(List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(list!=null && list.size()>0){
			HashMap map = (HashMap) list.get(0);
	        Iterator it = map.entrySet().iterator();  
	        while(it.hasNext()){  
	            Map.Entry<Object, Object> m = (Entry<Object, Object>) it.next();  
	            jsonh.put(m.getKey().toString(), m.getValue()==null?"":m.getValue().toString()); 
	        } 
	        jsonObj.put("h", jsonh);
		}
		

		JSONArray  jkjejsonArr = new JSONArray();//JSON数组
		ArrayList jkjeList=new ArrayList();//数组

			
			
			StringBuffer c = new StringBuffer();
			c.append("	  select b.name szxmid,a.amount from er_busitem a left join bd_inoutbusiclass b on a.szxmid = b.pk_inoutbusiclass where a.pk_jkbx='"+pk_bill+"'	");


							
			jkjeList = (ArrayList) dao.executeQuery(c.toString(), new MapListProcessor());

			if(jkjeList!=null && jkjeList.size()>0){
				//冲销明细
				for(int o=0;o<jkjeList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) jkjeList.get(o);
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//收支项目
					json.put("amount", map.get("amount")==null?"":map.get("amount").toString());//借款金额
																			
					jkjejsonArr.put(json);																		
				}
				jsonObj.put("jkje", jkjejsonArr);
			}
		
		return jsonObj;
				
	}
	
	/**
	 * 查询借款单（对公）
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryjkddg(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//主表JSON对象
		
		//主表拼装
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//单据编号
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//单据日期
		s.append("	       a.total,	");//借款金额
		s.append("	       b.name jkbxr,	");//借款人
		s.append("	       c.name pk_org,	");//借款单位		
		s.append("	       d.postname zyx1,	");//借款人岗位
		s.append("	       b.mobile mobile,	");//手机
		s.append("	       e.accnum skyhzh,	");//个人银行账户
		s.append("	       f.name fydwbm,	");//费用承担单位
		s.append("	       g.name fydeptid,	");//费用承担部门
		s.append("	       h.name pk_payorg,	");//支付单位
		s.append("	       i.name jsfs,	");//结算方式
		s.append("	       j.accnum fkyhzh,	");//单位银行账户
		s.append(" case a.paytarget when 0 then '员工' when 1 then '供应商' when 2 then '客户' end  paytarget, ");//收款对象
		s.append("	       k.name pk_cashaccount,	");//现金账户
		s.append("	       n.name hbbm,	");//供应商
		s.append("	       o.code custaccount,	");//客商银行账户
		s.append("	       a.zyx5,	");//事由
		s.append("	       l.name deptid,	");//借款人部门
		s.append("	       m.project_name jobid	");//项目
		s.append("	  from er_jkzb a	");
		s.append("	  left join bd_psndoc b	");
		s.append("	    on a.jkbxr = b.pk_psndoc	");
		s.append("	  left join org_orgs c	");
		s.append("	    on a.pk_org = c.pk_org	");
		s.append("	  left join om_post d	");
		s.append("	    on a.zyx1 = d.pk_post	");
		s.append("	  left join bd_bankaccsub e	");
		s.append("	    on a.skyhzh = e.pk_bankaccsub	");
		s.append("	  left join org_orgs f	");
		s.append("	    on a.fydwbm = f.pk_org	");
		s.append("	  left join org_dept g	");
		s.append("	    on a.fydeptid = g.pk_dept	");
		s.append("	  left join org_financeorg h	");
		s.append("	    on a.pk_payorg = h.pk_financeorg	");
		s.append("	  left join bd_balatype i	");
		s.append("	    on a.jsfs = i.pk_balatype	");
		s.append("	  left join bd_bankaccsub j	");
		s.append("	    on a.fkyhzh = j.pk_bankaccsub	");
		s.append("	  left join bd_cashaccount k	");
		s.append("	    on a.pk_cashaccount = k.pk_cashaccount	");
		s.append("	  left join org_dept l	");
		s.append("	    on a.deptid = l.pk_dept	");
		s.append("	  left join bd_project m	");
		s.append("	    on a.jobid = m.pk_project	");
		s.append("	  left join bd_supplier n	");
		s.append("	    on a.hbbm = n.pk_supplier	");
		s.append("	  left join bd_bankaccsub o	");
		s.append("	    on a.custaccount = o.pk_bankaccsub	");
		s.append("	  where a.pk_jkbx='"+pk_bill+"'	");


		List list=(List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(list!=null && list.size()>0){
			HashMap map = (HashMap) list.get(0);
	        Iterator it = map.entrySet().iterator();  
	        while(it.hasNext()){  
	            Map.Entry<Object, Object> m = (Entry<Object, Object>) it.next();  
	            jsonh.put(m.getKey().toString(), m.getValue()==null?"":m.getValue().toString()); 
	        } 
	        jsonObj.put("h", jsonh);
		}
		

		JSONArray  jkjejsonArr = new JSONArray();//JSON数组
		ArrayList jkjeList=new ArrayList();//数组

			
			
			StringBuffer c = new StringBuffer();
			c.append("	  select b.name szxmid,a.amount from er_busitem a left join bd_inoutbusiclass b on a.szxmid = b.pk_inoutbusiclass where a.pk_jkbx='"+pk_bill+"'	");


							
			jkjeList = (ArrayList) dao.executeQuery(c.toString(), new MapListProcessor());

			if(jkjeList!=null && jkjeList.size()>0){
				//冲销明细
				for(int o=0;o<jkjeList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) jkjeList.get(o);
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//借款类型
					json.put("amount", map.get("amount")==null?"":map.get("amount").toString());//借款金额
																			
					jkjejsonArr.put(json);																		
				}
				jsonObj.put("jkje", jkjejsonArr);
			}
									
		
		
		return jsonObj;
				
	}
	
	/**
	 * 查询业务招待费申请单
	 * （招待费申请单）
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryywzdfsqd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//主表JSON对象
		
		//主表拼装
		StringBuffer s = new StringBuffer();
		s.append("	select a.billno,	");//单据编号
		s.append("	       substr(a.billdate, 0, 10) billdate,	");//单据日期
//		s.append("	       a.orig_amount,	");//申请金额
		s.append("	       a.orig_amount amount,	");//申请金额
		s.append("	       a.defitem2,	");//手机
		s.append("	       b.name billmaker,	");//申请人
		s.append("	       c.name apply_org,	");//申请单位
		s.append("	       d.name apply_dept,	");//申请部门
		s.append("	       a.defitem3,	");//事由
		s.append("	       a.defitem30	");//费用使用说明
		s.append("	  from er_mtapp_bill a	");
		s.append("	  left join bd_psndoc b	");
		s.append("	    on a.billmaker = b.pk_psndoc	");
		s.append("	  left join org_orgs c	");
		s.append("	    on a.apply_org = c.pk_org	");
		s.append("	  left join org_dept d	");
		s.append("	    on a.apply_dept = d.pk_dept	");
		s.append("	 where a.pk_mtapp_bill = '"+pk_bill+"'	");

		List list=(List) dao.executeQuery(s.toString(), new MapListProcessor());
		if(list!=null && list.size()>0){
			HashMap map = (HashMap) list.get(0);
	        Iterator it = map.entrySet().iterator();  
	        while(it.hasNext()){  
	            Map.Entry<Object, Object> m = (Entry<Object, Object>) it.next();  
	            jsonh.put(m.getKey().toString(), m.getValue()==null?"":m.getValue().toString()); 
	        } 
	        jsonObj.put("h", jsonh);
		}
		
		//子表的拼装
		StringBuffer b = new StringBuffer();
		b.append("	select orig_amount amount,max_amount from er_mtapp_detail where pk_mtapp_bill='"+pk_bill+"'	");

		
		JSONArray detailjsonArr = new JSONArray();//JSON数组

		
			
		List detailList=(List) dao.executeQuery(b.toString(), new MapListProcessor());
		if(detailList!=null && detailList.size()>0){
			
			for(int m=0;m<detailList.size();m++){
				JSONObject json = new JSONObject();
				HashMap map = (HashMap) detailList.get(m);
				json.put("amount", map.get("amount")==null?"":map.get("amount").toString());//金额
				json.put("max_amount", map.get("max_amount")==null?"":map.get("max_amount").toString());//允许报销最大金额
								
				detailjsonArr.put(json);															
			}					
//			jsonObj.put("detail", detailjsonArr);	
			jsonObj.put("zdf1", detailjsonArr);	
												
		}
		return jsonObj;
				
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryErpBillDetail(String pk_bill,String pk_billtypecode)throws Exception{
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//主表JSON对象
		
		return jsonObj;
	}
	
	
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public Object getUnApprovalDetails(String billno) throws Exception{
		JSONObject result = new JSONObject();
		JSONArray jsonarray = new JSONArray();
		
		String sql = " select case task.taskstatus when 1 then '已办结' when 0 then '待办' when 4 then '驳回' end as " 
				+ " status, p.name name, note.dealdate dealdate, note.checknote opinion from pub_workflownote note "
				+ " left join pub_wf_task task on note.pk_wf_task = task.pk_wf_task "
				+ " left join sm_user u on note.checkman = u.cuserid "
				+ " left join bd_psndoc p on p.pk_psndoc=u.pk_psndoc "
				+ " where note.billno='"+"ZH201900003280"+"' "
				+ " order by note.senddate ";
		List<Map<String, Object>> list = (List<Map<String, Object>>)dao.executeQuery(sql, new MapListProcessor());
		if(list != null && list.size() > 0){
			for (int i = 0; i < list.size(); i++) {
				JSONObject json = new JSONObject();
				Iterator it = ((HashMap<String, Object>)list.get(i)).entrySet().iterator();
				while(it.hasNext()){  
					Map.Entry<String, Object> m = (Entry<String, Object>)it.next();  
					json.put(m.getKey().toString(), m.getValue() == null ? "" : m.getValue().toString()); 
				} 
				jsonarray.put(json);
			}
		}
		result.put("data", jsonarray);
		
		return result;
	}
}
