package nc.jzmobile.refmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.impl.pub.util.db.InSqlManager;
import nc.ui.bd.ref.AbstractRefGridTreeModel;
import nc.ui.pub.beans.ValueChangedEvent;
import nc.vo.jz.pub.consts.IBillType;
import nc.vo.jzvat.invpub.CmContractRefVO;

import org.apache.commons.lang.StringUtils;

/**
 * ��ͬ����
 * @author yangsyc
 */

public class CmContractRefModel extends AbstractRefGridTreeModel{
	
	
	private String pk_project ;
	
	private List<String> conBillType = new ArrayList<String>();
	
	private List<String> defbilltype = new ArrayList<String>();
	
	private static Map<String, String> billNames = new HashMap<String,String>();
	
	private List<String> pk_creaorgList;
	
	static{
		billNames.put(IBillType.JZCM_INCONTRACT, "���������ͬ");
		billNames.put(IBillType.JZCM_PAYCONTRACT, "����֧����ͬ");
		//billNames.put(IBillType.JZCM_INCONTRACT, "�����ͬ");//������Ŀ
	    //billNames.put(IBillType.JZCM_PAYCONTRACT, "֧����ͬ");//������Ŀ
		billNames.put(IBillType.JZIN_CONTRACT, "ʩ���ܳа���ͬ");
		billNames.put(IBillType.RLM_CON_IN, "��ת���������ͬ");
		billNames.put(IBillType.RLM_CON_OUT, "��ת���������ͬ");
		billNames.put(IBillType.ALI_CON_IN, "�豸�����ͬ");
		billNames.put(IBillType.ALI_CON_OUT, "�豸�����ͬ");
		billNames.put(IBillType.JZSUB_CONTRACT, "�ְ���ͬ");
		billNames.put("Z2", "��Ŀ�ɹ���ͬ");
	}
	public CmContractRefModel(){
		super();
		reset();
		initBilltype();
	}
	/**
	 * ȷ���������ͷ�Χ
	 */
	private void initBilltype(){
		conBillType.addAll(billNames.keySet());
		defbilltype.addAll(billNames.keySet());
	}

	public void reset(){
		//��������
		setRefNodeName("��ͬ");
		// �����ڵ�����
		setRootName("��ͬ");
		//Z2��������"Z2"��������ʱ�ᱨ��
//		setClassFieldCode(new String[]{"pk_billtypecode","billtypename","pk_billtypeid","parentbilltype"});
		setClassFieldCode(new String[]{"pk_billtypecode","billtypename", "parentbilltype"});
		//��������
		setFatherField("parentbilltype");
		//��������
		setChildField("pk_billtypecode");
		
		setClassChildField("pk_billtypecode");
		
		setClassJoinField("pk_billtypecode");
		
		setClassTableName("bd_billtype");
		//�������Ĭ����ʾ�ֶθ���
		setClassDefaultFieldCount(2);
		//�Ƿ�������Ȩ��
		setClassDataPower(false);
		//��ʾ�ֶα���
		setFieldCode(new String[]{CmContractRefVO.VBILLCODE,CmContractRefVO.VNAME,
				"project_name",CmContractRefVO.VCUSTSUPPLIERNAME});
		//��ʾ�ֶ�����
		setFieldName(new String[]{"��ͬ����","��ͬ����","��Ŀ����","��������"});
		//�����ֶ�
		setHiddenFieldCode(new String[]{CmContractRefVO.VTRANTTYPECODE,
				CmContractRefVO.PK_CONTRACT,CmContractRefVO.CBILLTYPECODE,
				CmContractRefVO.CTRANTTYPEID,CmContractRefVO.FSTATUSFLAG,
				CmContractRefVO.ICONTSTATUS,CmContractRefVO.PK_GROUP,
				CmContractRefVO.PK_ORG,CmContractRefVO.PK_ORG_V,
				CmContractRefVO.PK_PROJECT,CmContractRefVO.VTRANTYPENAME,
				CmContractRefVO.PK_OTHER,
				CmContractRefVO.DBEGINDATE,
				CmContractRefVO.DENDDATE,
				CmContractRefVO.NMNY
				});
		//����
		String sql = "SELECT ref.pk_contract,ref.pk_org,ref.pk_org_v,ref.pk_group,ref.pk_project,ref.vbillcode,ref.vname,ref.cbilltypecode,"
					+"ref.fstatusflag,ref.icontstatus,ref.pk_other,ref.vtranttypecode,ref.vtrantypename,ref.ctranttypeid,ref.dbegindate,"
					+"ref.denddate,ref.dbilldate,ref.icustsuppliertype , ref.vcustsuppliername,bp.project_name ,ref.nmny FROM "
					+" jzcm_contractref ref left join bd_project bp "//,ref.nmny
					+" on ref.pk_project = bp.pk_project ";
		
		
		setTableName("("+sql+") jzcm_contractref ");
		
		setPkFieldCode(CmContractRefVO.PK_CONTRACT);
		setRefCodeField(CmContractRefVO.VBILLCODE);
		setRefNameField(CmContractRefVO.VNAME);
		setDefaultFieldCount(4);
		setDocJoinField(CmContractRefVO.CBILLTYPECODE);
		resetFieldName();
		setFilterRefNodeName(new String[] {"ҵ��Ԫ"});
		
		//Ĭ�ϲ�ѯ��Ч��ͬ
		int[] active = {ContractStatus.ACTIVE};
		setIcontstatus(active);
	}
	
	@Override
	public boolean isCacheEnabled() {
		return false;
	}
	@Override
	protected String getSql(String strPatch, String[] strFieldCode,
			String[] hiddenFields, String strTableName, String strWherePart,
			String strGroupPart, String strOrderField) {
		// TODO Auto-generated method stub
		String sql =  super.getSql(strPatch, strFieldCode, hiddenFields, strTableName,
				strWherePart, strGroupPart, strOrderField);
		return sql;
	}
	
	@Override
	public String getWherePart() {
		StringBuffer where = new StringBuffer();
		
		if(StringUtils.isNotEmpty(getPk_project())){ //��Ŀ
			where.append(CmContractRefVO.PK_PROJECT+ " = '"+getPk_project()+"' and fstatusflag IN (1,3,7,9) AND icontstatus IN (1,3,7,9) and  ");
		}
		
		if(StringUtils.isNotEmpty(getPk_org())){
			where.append("("+CmContractRefVO.PK_ORG+ " = '" +getPk_org()+ "'");
			if(null != pk_creaorgList && pk_creaorgList.size() > 0){
				where.append(" or "+ CmContractRefVO.PK_ORG + " in " + InSqlManager.getInSQLValue(pk_creaorgList));
			}
			where.append(" or pk_contract in (");
			where.append(" select pk_contract from jzin_task where dr = 0 and fstatusflag = 1");
			if(null != pk_creaorgList && pk_creaorgList.size() > 0){
				where.append(" and (pk_org = '"+getPk_org()+"') ");
				where.append(" or pk_org in " + InSqlManager.getInSQLValue(pk_creaorgList));
			}
			else{
				where.append(" and (pk_org = '"+getPk_org()+"' ");
			}
			
			where.append(")) and ");
		}
		//������ͬ��ѯ����
		where.append("((");
		where.append(CmContractRefVO.ICONTSTATUS+ " in("+ jz_con +") and ");  //��ͬ״̬
		where.append(CmContractRefVO.FSTATUSFLAG +" in ("+ jz_bill +") and ");  //����״̬
		where.append(CmContractRefVO.CBILLTYPECODE +" in ('H541','H542' ,'H5A1') ");
		
		where.append(") or (");
		
		where.append(CmContractRefVO.ICONTSTATUS+ " in("+ jz_con +") and ");  //��ͬ״̬
		where.append(CmContractRefVO.FSTATUSFLAG +" in ("+ jz_bill +") and ");  //����״̬
		/*where.append(" pk_contract in (select pk_contract from jzin_task where dr = 0 and fstatusflag = ");
		where.append( IBillStatus.CHECKPASS).append( " and  pk_org = '");
		where.append( getPk_org() ).append( "')  and " );*/
		where.append(CmContractRefVO.CBILLTYPECODE + "='H583'");
		where.append(") or (");
		
		
		//��Ӧ����ѯ����
		where.append(CmContractRefVO.ICONTSTATUS+ " in ("+gyl +") and ");
		where.append(CmContractRefVO.CBILLTYPECODE+ " ='Z2' ");
		
		where.append(") or (");
		//�ʲ���ѯ����
		where.append(CmContractRefVO.ICONTSTATUS+ " in (" +zj+") and ");
		where.append(CmContractRefVO.CBILLTYPECODE+ " in ('4A80','4A3A')");
		where.append(") or cbilltypecode in ('4A2A-01','4A70-01','4A70-02','4A70-03'))");
		if(pk_creaorgList==null){
		where.append(")");
		}
		return where.toString();
	}

	@Override
	public void filterValueChanged(ValueChangedEvent changedValue) {
		super.filterValueChanged(changedValue);
		String[] pk_orgs = (String[])changedValue.getNewValue();
		if(pk_orgs != null && pk_orgs.length>0){
			setPk_org(pk_orgs[0]);
		}
	}
 
	@Override
	public String getClassWherePart() {
		if(getConBillType().size()>0){
			return getLeftSql(getConBillType());
		}else{
			return getLeftSql(defbilltype);
		}
	}
	
	
	/**
	 * �޸Ľڵ�����
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Vector getClassData() {
		Vector classDatas = super.getClassData();
		for(Vector classData:(Vector<Vector>)classDatas){
			if(billNames.containsKey(classData.get(0))){
				classData.add(1, billNames.get(classData.get(0)));	
			}
		}
		return classDatas;
	}
	
	
	
 
	private String getLeftSql(List<String> conTypeList){
		 StringBuffer sql = new StringBuffer();
		 sql.append(" (parentbilltype in (");
			for(String billtype:conTypeList){
				sql.append("'"+billtype+"',");
			}
			sql.deleteCharAt(sql.length()-1);
			sql.append(") or");
		sql.append(" pk_billtypecode in (");
		for(String billtype:conTypeList){
			sql.append("'"+billtype+"',");
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append(")) and (pk_group ='~' or pk_group='");
		sql.append(InvocationInfoProxy.getInstance().getGroupId()+"')");
		return sql.toString(); 
	}
	@Override
	protected void addJoinCondition(StringBuffer sqlBuffer) {
		sqlBuffer.append(" and (cbilltypecode ='");
		sqlBuffer.append(getClassJoinValue()+"'");
		sqlBuffer.append(" or vtranttypecode ='");
		sqlBuffer.append(getClassJoinValue()+"')");
	}
	
	
	
	public List<String> getConBillType() {
		return conBillType;
	}

	public void setConBillType(List<String> conBillTypes) {
		conBillType.clear();
		for(String billtype:conBillTypes){
			if(StringUtils.isNotBlank(billtype)&&defbilltype.contains(billtype)){
				this.conBillType.add(billtype);
			}
		}
	}

	public String getPk_project() {
		return pk_project;
	}

	public void setPk_project(String pk_project) {
		this.pk_project = pk_project;
	}

	/**
	 * ������Ҫ��ѯ�ĵ���״̬
	 * 1|��Ч
	 * 2|����ͨ��
	 * 3|δ��Ч��
	 * 4|���ᣬ
	 * 5|�ر�
	 * 6|���
	 * 0|����(���� ���ɣ��ύ,������,����δͨ����)
	 * �Ժ����ϳ�
	 * @param icontstatus
	 */
	public void setIcontstatus(int icontstatus[]) {
		if(icontstatus==null||icontstatus.length == 0){
			return ;
		}
		Set<String> jz_status = new HashSet<String>();
		Set<String> jz_contract = new HashSet<String>();
		Set<String> gyl_status = new HashSet<String>();
		Set<String> zj_status = new HashSet<String>();
		for(int i=0;i<icontstatus.length;i++){
			 switch(icontstatus[i]){
			 case ContractStatus.FREE:
				 addValue(jz_status,"-1,2,3,0,");
				 addValue(jz_contract,"0");
				 addValue(gyl_status,"0,2,4,");
				 addValue(zj_status,"0,1,2,4,");
				 break;
			 case ContractStatus.ACTIVE:
				 jz_status.add("1");
				 jz_contract.add("1");
				 gyl_status.add("1");
				 zj_status.add("9");
				 break;
			 case ContractStatus.CHECKPASS:
				 jz_status.add("1");
				 jz_contract.add("0");
				 gyl_status.add("3");
				 zj_status.add("3");
				 break;
			 case ContractStatus.CLOSE:
				 jz_status.add("1");
				 jz_contract.add("3");
				 zj_status.add("12");
				 break;
			 case ContractStatus.FINISH:	 
				 jz_status.add("1");
				 jz_contract.add("4");
				 zj_status.add("11");
				 gyl_status.add("6");
				 break;
			 case ContractStatus.FREEZE:
				 jz_status.add("1");
				 jz_contract.add("2");
				 gyl_status.add("5");
				 zj_status.add("10");
				 break;
			 case ContractStatus.UNACTIVE:
				 jz_status.add("1");
				 jz_contract.add("0");
				 gyl_status.add("3");
				 zj_status.add("3");
				 break;
			 }
		}
		 
		jz_bill = filtValue(jz_status);
		jz_con = filtValue(jz_contract);
		gyl  = filtValue(gyl_status);
		zj = filtValue(zj_status);
		
	}
	
	private void addValue(Set<String> set,String str){
		String[] ss = str.split(",");
		for(String s:ss){
			set.add(s);
		}
	}
	
	private String filtValue(Set<String> set){
		StringBuffer buffer = new StringBuffer();
		for(String str:set){
			buffer.append(str+",");
		}
		buffer.deleteCharAt(buffer.length()-1);
		return buffer.toString();
	}
	
	private String jz_bill ;
	private String jz_con;
	private String gyl  ;
	private String zj ;
	
	public List<String> getPk_creaorgList() {
		return pk_creaorgList;
	}
	public void setPk_creaorgList(List<String> pk_creaorgList) {
		this.pk_creaorgList = pk_creaorgList;
	}
}
