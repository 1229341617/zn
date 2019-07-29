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
	 * ��ѯ���÷ѱ�������ϸ
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryClfbxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();
		
		
		JSONObject jsonh = new JSONObject();//����JSON����
		
		//����ƴװ
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
			
			jsonh.put("djbh", map.get("djbh")==null?"":map.get("djbh").toString());//���
			jsonh.put("djrq", map.get("djrq")==null?"":map.get("djrq").toString());//����
			jsonh.put("jkbxr", map.get("jkbxr")==null?"":map.get("jkbxr").toString());//������
			jsonh.put("bbje", map.get("bbje")==null?"":map.get("bbje").toString());//���
			jsonh.put("pk_org", map.get("pk_org")==null?"":map.get("pk_org").toString());//�����˵�λ
			jsonh.put("deptid", map.get("deptid")==null?"":map.get("deptid").toString());//�����˲���
			jsonh.put("fydwbm", map.get("fydwbm")==null?"":map.get("fydwbm").toString());//���óе���λ
			jsonh.put("zyx5", map.get("zyx5")==null?"":map.get("zyx5").toString());//��������
			jsonObj.put("h", jsonh);
			
		}
		
		//�ӱ��ƴװ
		StringBuffer b = new StringBuffer();
		b.append("	select b.name       item,	");//(1)��֧��Ŀ(2)��֧��Ŀ(3)��֧��Ŀ
		b.append("	       a.vat_amount,	");//(1)��ͨ�ѽ��(2)ס�޷��ý��(3)�������
		b.append("	       a.defitem1,	");//(1)��������(2)��ס����
		b.append("	       a.defitem3,	");//(1)�����ص�
		b.append("	       a.defitem2,	");//(1)��������(2)�������
		b.append("	       a.defitem4,	");//(1)����ص�
		b.append("	       a.defitem5,	");//(1)��ͨ����
		b.append("	       a.defitem6,	");//(1)��ֵ˰˰��(2)��ֵ˰˰��
		b.append("	       a.defitem20,	");//(1)��ҵ���						
		b.append("	       a.tax_rate,	");//(1)˰��(2)˰��
		b.append("	       a.tax_amount,	");//(1)˰��(2)˰��
		b.append("	       a.tni_amount,	");//(1)����˰���(2)��˰���
		b.append("	       a.fphm,	");//(1)��Ʊ����
		b.append("	       a.defitem11,	");//(3)������׼
		b.append("	       a.defitem9,	");//(2)ס������(3)��������
		b.append("	       a.tablecode	");//����ҳǩ
		b.append("	  from er_busitem a	");
		b.append("	  left join bd_inoutbusiclass b	");
		b.append("	    on a.szxmid = b.pk_inoutbusiclass	");
	
		b.append("	 where a.pk_jkbx = '"+pk_bill+"' and nvl(a.dr,0)=0	");
		
		List blist=(List) dao.executeQuery(b.toString(), new MapListProcessor());
		
		JSONArray  jtfyjsonArr = new JSONArray();//��ͨ����JSON����
		JSONArray  zsfyjsonArr = new JSONArray();//ס�޷���JSON����
		JSONArray  ccbtjsonArr = new JSONArray();//�����JSON����
		JSONArray  cxmxjsonArr = new JSONArray();//������ϸJSON����
		
		ArrayList jtfyList=new ArrayList();//��ͨ��������
		ArrayList zsfyList=new ArrayList();//ס�޷�������
		ArrayList ccbtList=new ArrayList();//���������
		ArrayList cxmxList=new ArrayList();//������ϸ����
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
				//��ͨ����
				for(int m=0;m<jtfyList.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) jtfyList.get(m);
					json.put("item", map.get("item")==null?"":map.get("item").toString());//��֧��Ŀ
					json.put("defitem1", map.get("defitem1")==null?"":map.get("defitem1").toString());//��������
					json.put("defitem3", map.get("defitem3")==null?"":map.get("defitem3").toString());//�����ص�
					json.put("defitem2", map.get("defitem2")==null?"":map.get("defitem2").toString());//��������
					json.put("defitem4", map.get("defitem4")==null?"":map.get("defitem4").toString());//����ص�
					json.put("defitem5", map.get("defitem5")==null?"":map.get("defitem5").toString());//��ͨ����
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//��ͨ�ѽ��
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//��ֵ˰˰��
					json.put("defitem20", map.get("defitem20")==null?"":map.get("defitem20").toString());//��ҵ���
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//˰��
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//˰��
					json.put("tni_amount", map.get("tni_amount")==null?"":map.get("tni_amount").toString());//����˰���
					
					jtfyjsonArr.put(json);																		
				}					
				jsonObj.put("jtfy", jtfyjsonArr);				
				
			}
			
			if(zsfyList!=null && zsfyList.size()>0){
				//ס�޷���
				for(int l=0;l<zsfyList.size();l++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) zsfyList.get(l);
					json.put("defitem1", map.get("defitem1")==null?"":map.get("defitem1").toString());//��ס����
					json.put("defitem2", map.get("defitem2")==null?"":map.get("defitem2").toString());//�������
					json.put("defitem9", map.get("defitem9")==null?"":map.get("defitem9").toString());//ס������
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//˰��
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//˰��
					json.put("tni_amount", map.get("tni_amount")==null?"":map.get("tni_amount").toString());//����˰���
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//ס�޷��ý��
					json.put("item", map.get("item")==null?"":map.get("item").toString());//��֧��Ŀ
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//��ֵ˰˰��
					json.put("fphm", map.get("fphm")==null?"":map.get("fphm").toString());//��Ʊ����						
					
					zsfyjsonArr.put(json);																		
				}
				
				jsonObj.put("zsfy", zsfyjsonArr);
				
			}
							
			if(ccbtList!=null && ccbtList.size()>0){
				
				//�����
				for(int n=0;n<ccbtList.size();n++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) ccbtList.get(n);
					json.put("defitem11", map.get("defitem11")==null?"":map.get("defitem11").toString());//������׼
					json.put("defitem9", map.get("defitem9")==null?"":map.get("defitem9").toString());//��������
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//�������
					json.put("item", map.get("item")==null?"":map.get("item").toString());//��֧��Ŀ
									
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
			//������ϸ
			for(int o=0;o<cxmxList.size();o++){
				JSONObject json = new JSONObject();
				HashMap map = (HashMap) cxmxList.get(o);
				json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//����
				json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//�����
				json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//����
				json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//����ԭ�ҽ��
				json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//��������
				json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//��Ч����
				json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//��Ч��־
				
								
				cxmxjsonArr.put(json);																		
			}
			jsonObj.put("cxmx", cxmxjsonArr);
		}
		
		return jsonObj;
	}
	
	/**
	 * ��ѯ�д��ѱ�������ϸ
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryzdfbxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//����JSON����
		
		//����ƴװ
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
		s.append("	       k.project_name,case a.paytarget when 0 then 'Ա��' when 1 then '��Ӧ��' when 2 then '�ͻ�' end  paytarget,	");
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
		
		//�ӱ��ƴװ
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
		
		JSONArray  zdf1jsonArr = new JSONArray();//�д�����ϸ(ҳǩ1)JSON����
		JSONArray  zdf2jsonArr = new JSONArray();//�д�����ϸ(ҳǩ2)JSON����
		JSONArray  cxmxjsonArr = new JSONArray();//������ϸJSON����
		
		ArrayList zdf1List=new ArrayList();//�д�����ϸ(ҳǩ1)����
		ArrayList zdf2List=new ArrayList();//�д�����ϸ(ҳǩ2)����
		ArrayList cxmxList=new ArrayList();//������ϸ����
		
			
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
				//��ͨ����
				for(int m=0;m<zdf1List.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) zdf1List.get(m);
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//��֧��Ŀ
					json.put("defitem1", map.get("defitem1")==null?"":map.get("defitem1").toString());//�д�����
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//�������
					json.put("defitem20", map.get("defitem20")==null?"":map.get("defitem20").toString());//��ҵ���
					
					zdf1jsonArr.put(json);																		
				}					
				jsonObj.put("zdf1", zdf1jsonArr);				
				
			}
			
			if(zdf2List!=null && zdf2List.size()>0){
				//��ͨ����
				for(int m=0;m<zdf2List.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) zdf2List.get(m);
					json.put("defitem10", map.get("defitem10")==null?"":map.get("defitem10").toString());//�д�����
					json.put("defitem11", map.get("defitem11")==null?"":map.get("defitem11").toString());//��������
					json.put("defitem12", map.get("defitem12")==null?"":map.get("defitem12").toString());//��ͬ����
					
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
				//������ϸ
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//����
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//�����
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//����
					json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//����ԭ�ҽ��
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//��������
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//��Ч����
					json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//��Ч��־
					
									
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
			
			
			
		}
		
		return jsonObj;
		

		
	}
	
	/**
	 * ��ѯͨ���౨������ϸ
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object querytylbxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//����JSON����
		
		//����ƴװ
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
		s.append("	       k.project_name, case a.paytarget when 0 then 'Ա��' when 1 then '��Ӧ��' when 2 then '�ͻ�' end  paytarget,	");
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
		
		//�ӱ��ƴװ
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
		
		JSONArray  qfyjsonArr = new JSONArray();//�д�����ϸ(ҳǩ1)JSON����
		JSONArray  cxmxjsonArr = new JSONArray();//������ϸJSON����
		
		ArrayList qtfyList=new ArrayList();//�д�����ϸ(ҳǩ1)����
		ArrayList cxmxList=new ArrayList();//������ϸ����
		
			
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
				//��ͨ����
				for(int m=0;m<qtfyList.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) qtfyList.get(m);
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//��֧��Ŀ
					json.put("defitem1", map.get("defitem1")==null?"":map.get("defitem1").toString());//�д�����
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//�������
					json.put("defitem20", map.get("defitem20")==null?"":map.get("defitem20").toString());//��ҵ���
					
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
				//������ϸ
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//����
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//�����
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//����
					json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//����ԭ�ҽ��
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//��������
					json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//��Ч��־
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//��Ч����
					
														
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
						
			
		}
		
		return jsonObj;

		
	}
	
	
	/**
	 * ��ѯ��˾������������ϸ
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object querygsflbxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//����JSON����
		
		//����ƴװ
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//���ݱ��
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//��������
		s.append("	       a.total,	");//�ϼƽ��
		s.append("	       b.name jkbxr,	");//������
		s.append("	       b.mobile,	");//�ֻ�
		s.append("	       c.code bankcode,	");//���������˻�
		s.append("	       d.name dwbm,	");//�����˵�λ
		s.append("	       e.name deptid,	");//�����˲���
		s.append("	       f.name fydwbm,	");//���óе���λ
		s.append("	       g.name fydeptid,	");//���óе�����
		s.append("	       j.name pk_payorg,	");//֧����λ
		s.append("	       h.name jsfs,	");//���㷽ʽ
		s.append("	       i.name fkyhzh,	");//��λ�����˻�		
		s.append("	       k.project_name, case a.paytarget when 0 then 'Ա��' when 1 then '��Ӧ��' when 2 then '�ͻ�' end  paytarget,	");//֧������
		s.append("	       l.name hbbm,	");//��Ӧ��
		s.append("	       m.code custacccount,	");//��Ӧ�������˻�
		s.append("	       o.project_name jobid,	");//��Ŀ
		s.append("	       a.zyx29,	");//��ע
		s.append("	       a.zyx19	");//����
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
		
		//�ӱ��ƴװ
		StringBuffer b = new StringBuffer();
		b.append("	select b.name szxmid,	");//��֧��Ŀ		
		b.append("	       a.fphm,	");//��Ʊ����
		b.append("	       a.tax_rate,	");//˰��
		b.append("	       a.tax_amount,	");//˰��
		b.append("	       a.vat_amount,	");//�������
		b.append("	       d.taxrate defitem6,	");//��ֵ˰˰��
		b.append("	       c.name defitem20,	");//��ҵ���
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
		
		JSONArray  qfyjsonArr = new JSONArray();//��������ϸ(ҳǩ1)JSON����
		JSONArray  cxmxjsonArr = new JSONArray();//������ϸJSON����
		
		ArrayList qtfyList=new ArrayList();//��������ϸ(ҳǩ1)����
		ArrayList cxmxList=new ArrayList();//������ϸ����
		
			
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
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//��֧��Ŀ
					json.put("fphm", map.get("fphm")==null?"":map.get("fphm").toString());//��Ʊ����
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//˰��
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//˰��
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//�������
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//��ֵ˰˰��					
					json.put("defitem20", map.get("defitem20")==null?"":map.get("defitem20").toString());//��ҵ���
					
					qfyjsonArr.put(json);																		
				}					
				jsonObj.put("qty", qfyjsonArr);				
				
			}

			
			StringBuffer c = new StringBuffer();
			c.append("	  select a.jkdjbh,	");//����
			c.append("	         b.name    psnname,	");//������
			c.append("	         c.name    deptname,	");//���벿��
			c.append("	         a.cjkybje,	");//������
			c.append("	         d.name szxmid,	");//��֧��Ŀ
			c.append("	         a.cxrq,	");//��������
			c.append("	         a.sxrq,	");//��Ч����
			c.append("	         a.sxbz	");//��Ч��־
			c.append("	    from er_bxcontrast a	");
			c.append("	    left join bd_psndoc b	");
			c.append("	      on a.jkbxr = b.pk_psndoc	");
			c.append("	    left join org_dept c	");
			c.append("	      on a.deptid = c.pk_dept	");
			c.append("	    left join bd_inoutbusiclass d on a.szxmid=d.pk_inoutbusiclass	");
			c.append("	      where a.pk_bxd = '"+pk_bill+"'	");

							
			cxmxList = (ArrayList) dao.executeQuery(c.toString(), new MapListProcessor());

			if(cxmxList!=null && cxmxList.size()>0){
				//������ϸ
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//����
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//������
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//���벿��
					json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//������
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//��֧��Ŀ
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//��������
					json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//��Ч��־
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//��Ч����
					
														
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
						
			
		}
		
		return jsonObj;

		
	}
	
	/**
	 * ��ѯ��Ʊ���˵���ϸ
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryfprzd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//����JSON����
		
		//����ƴװ
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//���ݱ��
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//��������
		s.append("	       a.total,	");//�ϼƽ��
		s.append("	       b.name jkbxr,	");//������
		s.append("	       b.mobile,	");//�ֻ�
		s.append("         a.vat_amount,    ");//�������
		s.append("	       c.code bankcode,	");//���������˻�
		s.append("	       d.name dwbm,	");//�����˵�λ
		s.append("	       e.name deptid,	");//�����˲���
		s.append("	       f.name fydwbm,	");//���óе���λ
		s.append("	       g.name fydeptid,	");//���óе�����
		s.append("	       j.name pk_payorg,	");//֧����λ
		s.append("	       h.name jsfs,	");//���㷽ʽ
		s.append("	       i.name fkyhzh,	");//��λ�����˻�		
		s.append("	       k.project_name, case a.paytarget when 0 then 'Ա��' when 1 then '��Ӧ��' when 2 then '�ͻ�' end  paytarget,	");//֧������
		s.append("	       l.name hbbm,	");//��Ӧ��
		s.append("	       m.code custacccount,	");//��Ӧ�������˻�
		s.append("	       o.project_name jobid,	");//��Ŀ
		s.append("	       a.zyx19	");//����
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
		
		//�ӱ��ƴװ
		StringBuffer b = new StringBuffer();
		b.append("	select b.name szxmid,	");//��֧��Ŀ
		b.append("	       c.taxrate defitem6,	");//��ֵ˰˰��
		b.append("	       a.tax_rate,	");//˰��
		b.append("	       a.tax_amount,	");//˰��
		b.append("	       a.tni_amount,	");//����˰���
		b.append("	       a.fphm,	");//��Ʊ����		
		b.append("	       a.vat_amount,	");//���˽��
		b.append("		   a.tablecode ");
		b.append("	  from er_busitem a	");
		b.append("	  left join bd_inoutbusiclass b	");
		b.append("	    on a.szxmid = b.pk_inoutbusiclass	");
		b.append("	  left join bd_taxrate c	");
		b.append("	    on a.defitem6 = c.pk_taxcode	");
		b.append("	 where 	");
		b.append("	  a.pk_jkbx = '"+pk_bill+"' and nvl(a.dr,0)=0	");	
		
		JSONArray  qfyjsonArr = new JSONArray();//��������ϸ(ҳǩ1)JSON����
		
		ArrayList qtfyList=new ArrayList();//��������ϸ(ҳǩ1)����

		
			
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
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//��֧��Ŀ
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//��ֵ˰˰��	
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//˰��
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//˰��
					json.put("tni_amount", map.get("tni_amount")==null?"":map.get("tni_amount").toString());//����˰���										
					json.put("fphm", map.get("fphm")==null?"":map.get("fphm").toString());//��Ʊ����
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//���˽��
					
					qfyjsonArr.put(json);																		
				}						
				jsonObj.put("qty", qfyjsonArr);				
				
			}

			
						
			
		}
		
		return jsonObj;

		
	}
	
	/**
	 * ��ѯ�д��ѱ�����-��������ϸ
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryzdfbxd_2(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//����JSON����
		
		//����ƴװ
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//���ݱ��
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//��������
		s.append("	       a.total,	");//�ϼƽ��
		s.append("	       b.name jkbxr,	");//������
		s.append("	       b.mobile,	");//�ֻ�
		s.append("	       c.code bankcode,	");//���������˻�
		s.append("	       d.name dwbm,	");//�����˵�λ
		s.append("	       f.name fydwbm,	");//���óе���λ
		s.append("	       g.name fydeptid,	");//���óе�����
		s.append("	       h.name jsfs,	");//֧����ʽ
		s.append("	       i.name fkyhzh,	");//��λ�����˻�
		s.append("	       j.name pk_payorg,	");//֧����λ
		s.append("	       k.project_name,	");//��Ŀ
		s.append("	       case a.paytarget when 0 then 'Ա��' when 1 then '��Ӧ��' when 2 then '�ͻ�' end  paytarget,	");// ֧������
		s.append("	       l.name hbbm,	");//��Ӧ��
		s.append("	       a.zyx29,	");//��ע
		s.append("	       m.code custacccount,	");//��Ӧ�������˻�
		s.append("	       a.zyx19	");//����
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
		
		//�ӱ��ƴװ
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
		
		JSONArray  zdf1jsonArr = new JSONArray();//�д�����ϸ(ҳǩ1)JSON����
		JSONArray  zdf2jsonArr = new JSONArray();//�д�����ϸ(ҳǩ2)JSON����
		JSONArray  cxmxjsonArr = new JSONArray();//������ϸJSON����
		
		ArrayList zdf1List=new ArrayList();//�д�����ϸ(ҳǩ1)����
		ArrayList zdf2List=new ArrayList();//�д�����ϸ(ҳǩ2)����
		ArrayList cxmxList=new ArrayList();//������ϸ����
		
			
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
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//��֧��Ŀ
					json.put("defitem1", map.get("defitem1")==null?"":map.get("defitem1").toString());//�д�����
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//�������
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//�д�����
					json.put("defitem7", map.get("defitem7")==null?"":map.get("defitem7").toString());//��������
					json.put("defitem8", map.get("defitem8")==null?"":map.get("defitem8").toString());//��ͬ����
					json.put("defitem20", map.get("defitem20")==null?"":map.get("defitem20").toString());//��ҵ���
					json.put("defitem21", map.get("defitem21")==null?"":map.get("defitem21").toString());//��ҵ��������
					json.put("defitem11", map.get("defitem11")==null?"":map.get("defitem11").toString());//��ע
					json.put("project_name", map.get("project_name")==null?"":map.get("project_name").toString());//��Ŀ
					
					zdf1jsonArr.put(json);																		
				}					
				jsonObj.put("zdf1", zdf1jsonArr);				
				
			}
			
			if(zdf2List!=null && zdf2List.size()>0){
				
				for(int m=0;m<zdf2List.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) zdf2List.get(m);
					json.put("defitem2", map.get("defitem2")==null?"":map.get("defitem2").toString());//�������
					json.put("defitem10", map.get("defitem10")==null?"":map.get("defitem10").toString());//�д�����
					json.put("defitem11", map.get("defitem11")==null?"":map.get("defitem11").toString());//��������
					json.put("defitem12", map.get("defitem12")==null?"":map.get("defitem12").toString());//��ͬ����
					
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
				//������ϸ
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//����
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//�����
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//����
					json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//����ԭ�ҽ��
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//��������
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//��Ч����
					json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//��Ч��־
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//��֧��Ŀ
					
									
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
			
			
			
		}
		
		return jsonObj;
		

		
	}
	
	/**
	 * ��ѯ���ñ����������ˣ�
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryfybxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//����JSON����
		
		//����ƴװ
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//���ݱ��
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//��������
		s.append("	       a.total,	");//�ϼƽ��
		s.append("	       b.name jkbxr,	");//������
		s.append("	       b.mobile,	");//�ֻ�
		s.append("	       c.code bankcode,	");//���������˻�
		s.append("	       d.name dwbm,	");//�����˵�λ
		s.append("	       e.name deptid,	");//�����˲���
		s.append("	       f.name fydwbm,	");//���óе���λ
		s.append("	       g.name fydeptid,	");//���óе�����
		s.append("	       h.name jsfs,	");//֧����ʽ
		s.append("	       i.name fkyhzh,	");//��λ�����˻�
		s.append("	       j.name pk_payorg,	");//֧����λ
		s.append("	       k.project_name,	");//��Ŀ
		s.append("	       case a.paytarget when 0 then 'Ա��' when 1 then '��Ӧ��' when 2 then '�ͻ�' end  paytarget,	");// ֧������
		s.append("	       a.zyx29,	");//��ע
		s.append("	       a.zyx19	");//����
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
		
		//�ӱ��ƴװ
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
		
		JSONArray  zdf1jsonArr = new JSONArray();//�д�����ϸ(ҳǩ1)JSON����
		JSONArray  cxmxjsonArr = new JSONArray();//������ϸJSON����
		
		ArrayList zdf1List=new ArrayList();//�д�����ϸ(ҳǩ1)����
		ArrayList cxmxList=new ArrayList();//������ϸ����
		
			
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
					json.put("defitem16", map.get("defitem16")==null?"":map.get("defitem16").toString());//�����ܶ�
					json.put("defitem17", map.get("defitem17")==null?"":map.get("defitem17").toString());//�ѱ������
					json.put("defitem18", map.get("defitem18")==null?"":map.get("defitem18").toString());//ʣ��ɱ������
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//��֧��Ŀ
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//˰��
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//��ֵ˰˰��
					json.put("jobid", map.get("jobid")==null?"":map.get("jobid").toString());//��ҵ���
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//�������
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//˰��
					json.put("tni_amount", map.get("tni_amount")==null?"":map.get("tni_amount").toString());//����˰���
										
					
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
				//������ϸ
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//����
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//�����
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//����
					json.put("bbje", map.get("bbje")==null?"":map.get("bbje").toString());//������
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//��������
					json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//��Ч��־
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//��Ч����
														
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
			
			
			
		}
		
		return jsonObj;
		

		
	}
	/**
	 * ��ѯ���Բ���������
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object querydnbtbxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//����JSON����
		
		//����ƴװ
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//���ݱ��
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//��������
		s.append("	       d.name dwbm,	");//�����˵�λ
		s.append("	       e.name deptid,	");//�����˲���
		s.append("	       b.name jkbxr,	");//������
		s.append("	       b.mobile,	");//�ֻ�
		s.append("	       c.code bankcode,	");//���������˻�
		s.append("	       a.total,	");//�ϼƽ��						
		s.append("	       f.name fydwbm,	");//���óе���λ
		s.append("	       g.name fydeptid,	");//���óе�����
		s.append("	       j.name pk_payorg,	");//֧����λ
		s.append("	       h.name jsfs,	");//���㷽ʽ
		s.append("	       i.name fkyhzh,	");//��λ�����˻�		
		s.append("	       k.project_name,	");//��Ŀ
		s.append("	       a.zyx29,	");//��ע
		s.append("	       a.zyx19	");//����
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
		
		//�ӱ��ƴװ
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
		
		JSONArray  zdf1jsonArr = new JSONArray();//������ϢJSON����
		JSONArray  cxmxjsonArr = new JSONArray();//������ϸJSON����
		
		ArrayList zdf1List=new ArrayList();//������Ϣ����
		ArrayList cxmxList=new ArrayList();//������ϸ����
		
			
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
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//��֧��Ŀ
					json.put("defitem8", map.get("defitem8")==null?"":map.get("defitem8").toString());//�����ͺ�
					json.put("fphm", map.get("fphm")==null?"":map.get("fphm").toString());//��Ʊ����
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//˰��
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//˰��
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//�������					
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//��ֵ˰˰��
					json.put("jobid", map.get("jobid")==null?"":map.get("jobid").toString());//��ҵ���
										
					
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
				//������ϸ
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//����
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//�����
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//����
					json.put("fyybje", map.get("fyybje")==null?"":map.get("fyybje").toString());//����ԭ�ҽ��

														
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
						
			
		}
		
		return jsonObj;
		

		
	}
	
	/**
	 * ��ѯ�ɱ��౨����
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object querycblbxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//����JSON����
		
		//����ƴװ
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//���ݱ��
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//��������
		s.append("	       d.name dwbm,	");//�����˵�λ
		s.append("	       e.name deptid,	");//�����˲���
		s.append("	       b.name jkbxr,	");//������
		s.append("	       b.mobile,	");//�ֻ�
		s.append("	       c.code skyhzh,	");//���������˻�
		s.append("	       a.total,	");//�ϼƽ��						
		s.append("	       f.name fydwbm,	");//���óе���λ
		s.append("	       g.name fydeptid,	");//���óе�����
		s.append("	       j.name pk_payorg,	");//֧����λ
		s.append("	       h.name jsfs,	");//���㷽ʽ
		s.append("	       i.name fkyhzh,	");//��λ�����˻�
		s.append("	       case a.paytarget when 0 then 'Ա��' when 1 then '��Ӧ��' when 2 then '�ͻ�' end  paytarget,	");// ֧������
		s.append("	       l.name hbbm,	");//��Ӧ��
		s.append("	       m.code custaccount,	");//��Ӧ�������˻�
		s.append("	       a.zyx19	");//����
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
		
		//�ӱ��ƴװ
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
		
		JSONArray  cbqrjsonArr = new JSONArray();//�ɱ�ȷ��JSON����
		JSONArray  cxmxjsonArr = new JSONArray();//������ϸJSON����
		
		ArrayList cbqrList=new ArrayList();//�ɱ�ȷ������
		ArrayList cxmxList=new ArrayList();//������ϸ����
		
			
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
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//��֧��Ŀ
					json.put("fphm", map.get("fphm")==null?"":map.get("fphm").toString());//��Ʊ����
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//��ֵ˰˰��
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//˰��
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//˰��
					json.put("tni_amount", map.get("tni_amount")==null?"":map.get("tni_amount").toString());//����˰���					
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//�������										
					json.put("defitem20", map.get("defitem20")==null?"":map.get("defitem20").toString());//��ҵ���										
					
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
				//������ϸ
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//����
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//�����
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//����
					json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//�������
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//��������
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//��Ч����
					json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//��Ч��־

														
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
						
			
		}
		
		return jsonObj;
				
	}
	
	/**
	 * ��ѯ�����������ñ�����
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryxsxcfybxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//����JSON����
		
		//����ƴװ
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//���ݱ��
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//��������
		s.append("	       d.name dwbm,	");//�����˵�λ
		s.append("	       e.name deptid,	");//�����˲���
		s.append("	       b.name jkbxr,	");//������
		s.append("	       b.mobile,	");//�ֻ�
		s.append("	       c.code bankcode,	");//���������˻�
		s.append("	       a.total,	");//�ϼƽ��						
		s.append("	       f.name fydwbm,	");//���óе���λ
		s.append("	       g.name fydeptid,	");//���óе�����
		s.append("	       j.name pk_payorg,	");//֧����λ
		s.append("	       h.name jsfs,	");//���㷽ʽ
		s.append("	       i.name fkyhzh,	");//��λ�����˻�
		s.append("	       case a.paytarget when 0 then 'Ա��' when 1 then '��Ӧ��' when 2 then '�ͻ�' end  paytarget,	");// ֧������
		s.append("	       l.name hbbm,	");//��Ӧ��
		s.append("	       m.code custaccount,	");//��Ӧ�������˻�
		s.append("	       k.project_name,	");//��Ŀ
		s.append("	       a.zyx29,	");//��ע
		s.append("	       a.zyx5	");//����
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
		
		//�ӱ��ƴװ
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
		
		JSONArray  xsxcfyjsonArr = new JSONArray();//������������JSON����
		JSONArray  cxmxjsonArr = new JSONArray();//������ϸJSON����
		
		ArrayList xsxcfyList=new ArrayList();//����������������
		ArrayList cxmxList=new ArrayList();//������ϸ����
		
			
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
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//��֧��Ŀ
					json.put("fpdm", map.get("fpdm")==null?"":map.get("fpdm").toString());//��Ʊ��
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//˰��
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//˰��
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//�������
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//��ֵ˰˰��
					json.put("defitem20", map.get("defitem20")==null?"":map.get("defitem20").toString());//��ҵ���
									
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
				//������ϸ
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//����
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//�����
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//����
					json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//����ԭ�ҽ��
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//��������
					json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//��Ч��־
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//��Ч����
																			
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
									
		}
		
		return jsonObj;
				
	}
	
	/**
	 * ��ѯ�������ñ�����
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object querygcfybxd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//����JSON����
		
		//����ƴװ
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//���ݱ��
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//��������
		s.append("	       d.name dwbm,	");//�����˵�λ
		s.append("	       e.name deptid,	");//�����˲���
		s.append("	       b.name jkbxr,	");//������
		s.append("	       b.mobile,	");//�ֻ�
		s.append("	       c.code bankcode,	");//���������˻�
		s.append("	       a.total,	");//�ϼƽ��						
		s.append("	       f.name fydwbm,	");//���óе���λ
		s.append("	       g.name fydeptid,	");//���óе�����
		s.append("	       j.name pk_payorg,	");//֧����λ
		s.append("	       h.name jsfs,	");//���㷽ʽ
		s.append("	       i.name fkyhzh,	");//��λ�����˻�
		s.append("	       case a.paytarget when 0 then 'Ա��' when 1 then '��Ӧ��' when 2 then '�ͻ�' end  paytarget,	");// ֧������
		s.append("	       l.name hbbm,	");//��Ӧ��
		s.append("	       m.code custaccount,	");//���������˻�
		s.append("	       k.project_name,	");//��Ŀ
		s.append("	       a.zyx5	");//����
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
		
		//�ӱ��ƴװ
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
		
		JSONArray gcfyjsonArr = new JSONArray();//��������JSON����
		JSONArray zsfyjsonArr = new JSONArray();//ס�޷���JSON����
		JSONArray ccbtjsonArr = new JSONArray();//�����JSON����
		JSONArray cxmxjsonArr = new JSONArray();//������ϸJSON����
		
		ArrayList gcfyList=new ArrayList();//������������
		ArrayList zsfyList=new ArrayList();//ס�޷�������
		ArrayList ccbtList=new ArrayList();//���������
		ArrayList cxmxList=new ArrayList();//������ϸ����
		
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
					json.put("defitem10", map.get("defitem10")==null?"":map.get("defitem10").toString());//��������
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//��֧��Ŀ
					json.put("defitem1", map.get("defitem1")==null?"":map.get("defitem1").toString());//��������
					json.put("defitem6", map.get("defitem6")==null?"":map.get("defitem6").toString());//��ֵ˰˰��
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//˰��
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//˰��
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//�������
					json.put("tni_amount", map.get("tni_amount")==null?"":map.get("tni_amount").toString());//����˰���					
					json.put("fphm", map.get("fphm")==null?"":map.get("fphm").toString());//��Ʊ����

									
					gcfyjsonArr.put(json);																		
				}					
				jsonObj.put("gcfy", gcfyjsonArr);				
				
			}
			
			if(zsfyList!=null && zsfyList.size()>0){
				
				for(int m=0;m<zsfyList.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) zsfyList.get(m);
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//��֧��Ŀ
					json.put("defitem1", map.get("defitem1")==null?"":map.get("defitem1").toString());//��ס����
					json.put("defitem2", map.get("defitem2")==null?"":map.get("defitem2").toString());//�������
					json.put("defitem9", map.get("defitem9")==null?"":map.get("defitem9").toString());//ס������
					json.put("tax_rate", map.get("tax_rate")==null?"":map.get("tax_rate").toString());//˰��
					json.put("tax_amount", map.get("tax_amount")==null?"":map.get("tax_amount").toString());//˰��
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//�������


									
					zsfyjsonArr.put(json);																		
				}					
				jsonObj.put("zsfy", zsfyjsonArr);				
				
			}
			
			if(ccbtList!=null && ccbtList.size()>0){
				
				for(int m=0;m<ccbtList.size();m++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) ccbtList.get(m);
					json.put("defitem11", map.get("defitem11")==null?"":map.get("defitem11").toString());//���������
					json.put("defitem9", map.get("defitem9")==null?"":map.get("defitem9").toString());//��������
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//��֧��Ŀ
					json.put("vat_amount", map.get("vat_amount")==null?"":map.get("vat_amount").toString());//�������
									
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
				//������ϸ
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//����
					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//�����
					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//����
					json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//�������
					json.put("fyybje", map.get("fyybje")==null?"":map.get("fyybje").toString());//����ԭ�ҽ��
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//��������
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//��Ч����
																			
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("cxmx", cxmxjsonArr);
			}
									
		}
		
		return jsonObj;
				
	}
	
	/**
	 * ��ѯ���
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryhkd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//����JSON����
		
		//����ƴװ
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//���ݱ��
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//��������
		s.append("	       a.hkybje,	");//������
		s.append("	       b.name jkbxr,	");//������		
		s.append("	       d.name dwbm,	");//�����˵�λ
		s.append("	       e.name deptid,	");//�����˲���
		s.append("	       a.cjkbbje,	");//������
		s.append("	       f.name fydwbm,	");//���óе���λ
		s.append("	       g.name fydeptid,	");//���óе�����
		s.append("	       j.name pk_payorg,	");//֧����λ
		s.append("	       c.code bankcode,	");//���������˻�
		s.append("	       h.name jsfs	");//���㷽ʽ
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
		

		JSONArray  cxmxjsonArr = new JSONArray();//������ϸJSON����
		ArrayList cxmxList=new ArrayList();//������ϸ����

			
			
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
				//������ϸ
				for(int o=0;o<cxmxList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) cxmxList.get(o);
					json.put("jkdjbh", map.get("jkdjbh")==null?"":map.get("jkdjbh").toString());//����
//					json.put("psnname", map.get("psnname")==null?"":map.get("psnname").toString());//�����
					json.put("jkbxr", map.get("psnname")==null?"":map.get("psnname").toString());//�����
//					json.put("deptname", map.get("deptname")==null?"":map.get("deptname").toString());//����
					json.put("deptid", map.get("deptname")==null?"":map.get("deptname").toString());//����
//					json.put("cjkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//������
					json.put("hkybje", map.get("cjkybje")==null?"":map.get("cjkybje").toString());//������
					json.put("cxrq", map.get("cxrq")==null?"":map.get("cxrq").toString());//��������
					json.put("sxbz", map.get("sxbz")==null?"":map.get("sxbz").toString());//��Ч��־
					json.put("sxrq", map.get("sxrq")==null?"":map.get("sxrq").toString());//��Ч����
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//��֧��Ŀ
																			
					cxmxjsonArr.put(json);																		
				}
				jsonObj.put("hkxx", cxmxjsonArr);
			}
									
		
		
		return jsonObj;
				
	}
	
	/**
	 * ��ѯ�Ϳ���ֵ���뵥
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryykczsqd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//����JSON����
		
		//����ƴװ
		StringBuffer s = new StringBuffer();
		s.append("	select a.billno,	");//���ݱ��
		s.append("	       substr(a.billdate, 0, 10) billdate,	");//��������
		s.append("	       b.name billmaker,	");//������
		s.append("	       c.name apply_org,	");//���뵥λ
		s.append("	       d.name apply_dept,	");//���벿��
//		s.append("	       a.orig_amount,	");//������
		s.append("	       a.orig_amount amount,	");//������
		s.append("	       a.defitem2,	");//�ֻ�
		s.append("	       a.defitem3,	");//��ֵ����
		s.append("	       a.defitem29,	");//����¼��˵��
		s.append("	       a.defitem30	");//����
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
		
		//�ӱ��ƴװ
		StringBuffer b = new StringBuffer();
		b.append("	select orig_amount,defitem2 from er_mtapp_detail where pk_mtapp_bill='"+pk_bill+"'	");

		
		JSONArray detailjsonArr = new JSONArray();//JSON����

		
			
		List detailList=(List) dao.executeQuery(b.toString(), new MapListProcessor());
		if(detailList!=null && detailList.size()>0){
			
			for(int m=0;m<detailList.size();m++){
				JSONObject json = new JSONObject();
				HashMap map = (HashMap) detailList.get(m);
//				json.put("orig_amount", map.get("defitorig_amountem10")==null?"":map.get("orig_amount").toString());//��ֵ���
				json.put("amount", map.get("orig_amount")==null?"":map.get("orig_amount").toString());//��ֵ���
				json.put("defitem2", map.get("defitem2")==null?"":map.get("defitem2").toString());//��ע
								
				detailjsonArr.put(json);																		
			}					
//			jsonObj.put("detail", detailjsonArr);
			jsonObj.put("zdf1", detailjsonArr);
												
		}
		
		return jsonObj;
				
	}
	
	/**
	 * ��ѯ�������ˣ�
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryjkdgr(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//����JSON����
		
		//����ƴװ
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//���ݱ��
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//��������
		s.append("	       a.total,	");//�����
		s.append("	       b.name jkbxr,	");//�����
		s.append("	       c.name pk_org,	");//��λ
		s.append("	       d.postname zyx1,	");//����˸�λ
		s.append("	       b.mobile mobile,	");//�ֻ�
		s.append("	       e.accnum skyhzh,	");//���������˻�
		s.append("	       f.name fydwbm,	");//���óе���λ
		s.append("	       g.name fydeptid,	");//���óе�����
		s.append("	       h.name pk_payorg,	");//֧����λ
		s.append("	       i.name jsfs,	");//���㷽ʽ
		s.append("	       j.accnum fkyhzh,	");//��λ�����˻�
		s.append("	       k.name pk_cashaccount,	");//�ֽ��˻�
		s.append("	       a.zyx5,	");//����
		s.append("	       l.name deptid,	");//����˲���
		s.append("	       m.project_name jobid	");//��Ŀ
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
		

		JSONArray  jkjejsonArr = new JSONArray();//JSON����
		ArrayList jkjeList=new ArrayList();//����

			
			
			StringBuffer c = new StringBuffer();
			c.append("	  select b.name szxmid,a.amount from er_busitem a left join bd_inoutbusiclass b on a.szxmid = b.pk_inoutbusiclass where a.pk_jkbx='"+pk_bill+"'	");


							
			jkjeList = (ArrayList) dao.executeQuery(c.toString(), new MapListProcessor());

			if(jkjeList!=null && jkjeList.size()>0){
				//������ϸ
				for(int o=0;o<jkjeList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) jkjeList.get(o);
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//��֧��Ŀ
					json.put("amount", map.get("amount")==null?"":map.get("amount").toString());//�����
																			
					jkjejsonArr.put(json);																		
				}
				jsonObj.put("jkje", jkjejsonArr);
			}
		
		return jsonObj;
				
	}
	
	/**
	 * ��ѯ�����Թ���
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryjkddg(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//����JSON����
		
		//����ƴװ
		StringBuffer s = new StringBuffer();
		s.append("	select a.djbh,	");//���ݱ��
		s.append("	       substr(a.djrq, 0, 10) djrq,	");//��������
		s.append("	       a.total,	");//�����
		s.append("	       b.name jkbxr,	");//�����
		s.append("	       c.name pk_org,	");//��λ		
		s.append("	       d.postname zyx1,	");//����˸�λ
		s.append("	       b.mobile mobile,	");//�ֻ�
		s.append("	       e.accnum skyhzh,	");//���������˻�
		s.append("	       f.name fydwbm,	");//���óе���λ
		s.append("	       g.name fydeptid,	");//���óе�����
		s.append("	       h.name pk_payorg,	");//֧����λ
		s.append("	       i.name jsfs,	");//���㷽ʽ
		s.append("	       j.accnum fkyhzh,	");//��λ�����˻�
		s.append(" case a.paytarget when 0 then 'Ա��' when 1 then '��Ӧ��' when 2 then '�ͻ�' end  paytarget, ");//�տ����
		s.append("	       k.name pk_cashaccount,	");//�ֽ��˻�
		s.append("	       n.name hbbm,	");//��Ӧ��
		s.append("	       o.code custaccount,	");//���������˻�
		s.append("	       a.zyx5,	");//����
		s.append("	       l.name deptid,	");//����˲���
		s.append("	       m.project_name jobid	");//��Ŀ
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
		

		JSONArray  jkjejsonArr = new JSONArray();//JSON����
		ArrayList jkjeList=new ArrayList();//����

			
			
			StringBuffer c = new StringBuffer();
			c.append("	  select b.name szxmid,a.amount from er_busitem a left join bd_inoutbusiclass b on a.szxmid = b.pk_inoutbusiclass where a.pk_jkbx='"+pk_bill+"'	");


							
			jkjeList = (ArrayList) dao.executeQuery(c.toString(), new MapListProcessor());

			if(jkjeList!=null && jkjeList.size()>0){
				//������ϸ
				for(int o=0;o<jkjeList.size();o++){
					JSONObject json = new JSONObject();
					HashMap map = (HashMap) jkjeList.get(o);
					json.put("szxmid", map.get("szxmid")==null?"":map.get("szxmid").toString());//�������
					json.put("amount", map.get("amount")==null?"":map.get("amount").toString());//�����
																			
					jkjejsonArr.put(json);																		
				}
				jsonObj.put("jkje", jkjejsonArr);
			}
									
		
		
		return jsonObj;
				
	}
	
	/**
	 * ��ѯҵ���д������뵥
	 * ���д������뵥��
	 * @param pk_bill
	 * @param pk_billtypecode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object queryywzdfsqd(String pk_bill,String pk_billtypecode)throws Exception{
		
		JSONObject jsonObj = new JSONObject();		
		JSONObject jsonh = new JSONObject();//����JSON����
		
		//����ƴװ
		StringBuffer s = new StringBuffer();
		s.append("	select a.billno,	");//���ݱ��
		s.append("	       substr(a.billdate, 0, 10) billdate,	");//��������
//		s.append("	       a.orig_amount,	");//������
		s.append("	       a.orig_amount amount,	");//������
		s.append("	       a.defitem2,	");//�ֻ�
		s.append("	       b.name billmaker,	");//������
		s.append("	       c.name apply_org,	");//���뵥λ
		s.append("	       d.name apply_dept,	");//���벿��
		s.append("	       a.defitem3,	");//����
		s.append("	       a.defitem30	");//����ʹ��˵��
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
		
		//�ӱ��ƴװ
		StringBuffer b = new StringBuffer();
		b.append("	select orig_amount amount,max_amount from er_mtapp_detail where pk_mtapp_bill='"+pk_bill+"'	");

		
		JSONArray detailjsonArr = new JSONArray();//JSON����

		
			
		List detailList=(List) dao.executeQuery(b.toString(), new MapListProcessor());
		if(detailList!=null && detailList.size()>0){
			
			for(int m=0;m<detailList.size();m++){
				JSONObject json = new JSONObject();
				HashMap map = (HashMap) detailList.get(m);
				json.put("amount", map.get("amount")==null?"":map.get("amount").toString());//���
				json.put("max_amount", map.get("max_amount")==null?"":map.get("max_amount").toString());//�����������
								
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
		JSONObject jsonh = new JSONObject();//����JSON����
		
		return jsonObj;
	}
	
	
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public Object getUnApprovalDetails(String billno) throws Exception{
		JSONObject result = new JSONObject();
		JSONArray jsonarray = new JSONArray();
		
		String sql = " select case task.taskstatus when 1 then '�Ѱ��' when 0 then '����' when 4 then '����' end as " 
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
