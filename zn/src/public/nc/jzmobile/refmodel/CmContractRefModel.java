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
 * 合同参照
 * @author yangsyc
 */

public class CmContractRefModel extends AbstractRefGridTreeModel{
	
	
	private String pk_project ;
	
	private List<String> conBillType = new ArrayList<String>();
	
	private List<String> defbilltype = new ArrayList<String>();
	
	private static Map<String, String> billNames = new HashMap<String,String>();
	
	private List<String> pk_creaorgList;
	
	static{
		billNames.put(IBillType.JZCM_INCONTRACT, "其它收入合同");
		billNames.put(IBillType.JZCM_PAYCONTRACT, "其它支出合同");
		//billNames.put(IBillType.JZCM_INCONTRACT, "收入合同");//环宇项目
	    //billNames.put(IBillType.JZCM_PAYCONTRACT, "支出合同");//环宇项目
		billNames.put(IBillType.JZIN_CONTRACT, "施工总承包合同");
		billNames.put(IBillType.RLM_CON_IN, "周转材料租入合同");
		billNames.put(IBillType.RLM_CON_OUT, "周转材料租出合同");
		billNames.put(IBillType.ALI_CON_IN, "设备租入合同");
		billNames.put(IBillType.ALI_CON_OUT, "设备租出合同");
		billNames.put(IBillType.JZSUB_CONTRACT, "分包合同");
		billNames.put("Z2", "项目采购合同");
	}
	public CmContractRefModel(){
		super();
		reset();
		initBilltype();
	}
	/**
	 * 确定单据类型范围
	 */
	private void initBilltype(){
		conBillType.addAll(billNames.keySet());
		defbilltype.addAll(billNames.keySet());
	}

	public void reset(){
		//参照名称
		setRefNodeName("合同");
		// 树根节点名称
		setRootName("合同");
		//Z2的主键是"Z2"，构造树时会报错
//		setClassFieldCode(new String[]{"pk_billtypecode","billtypename","pk_billtypeid","parentbilltype"});
		setClassFieldCode(new String[]{"pk_billtypecode","billtypename", "parentbilltype"});
		//父类主键
		setFatherField("parentbilltype");
		//子类主键
		setChildField("pk_billtypecode");
		
		setClassChildField("pk_billtypecode");
		
		setClassJoinField("pk_billtypecode");
		
		setClassTableName("bd_billtype");
		//设置左侧默认显示字段个数
		setClassDefaultFieldCount(2);
		//是否走数据权限
		setClassDataPower(false);
		//显示字段编码
		setFieldCode(new String[]{CmContractRefVO.VBILLCODE,CmContractRefVO.VNAME,
				"project_name",CmContractRefVO.VCUSTSUPPLIERNAME});
		//显示字段名称
		setFieldName(new String[]{"合同编码","合同名称","项目名称","客商名称"});
		//隐藏字段
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
		//表名
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
		setFilterRefNodeName(new String[] {"业务单元"});
		
		//默认查询生效合同
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
		
		if(StringUtils.isNotEmpty(getPk_project())){ //项目
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
		//建筑合同查询条件
		where.append("((");
		where.append(CmContractRefVO.ICONTSTATUS+ " in("+ jz_con +") and ");  //合同状态
		where.append(CmContractRefVO.FSTATUSFLAG +" in ("+ jz_bill +") and ");  //单据状态
		where.append(CmContractRefVO.CBILLTYPECODE +" in ('H541','H542' ,'H5A1') ");
		
		where.append(") or (");
		
		where.append(CmContractRefVO.ICONTSTATUS+ " in("+ jz_con +") and ");  //合同状态
		where.append(CmContractRefVO.FSTATUSFLAG +" in ("+ jz_bill +") and ");  //单据状态
		/*where.append(" pk_contract in (select pk_contract from jzin_task where dr = 0 and fstatusflag = ");
		where.append( IBillStatus.CHECKPASS).append( " and  pk_org = '");
		where.append( getPk_org() ).append( "')  and " );*/
		where.append(CmContractRefVO.CBILLTYPECODE + "='H583'");
		where.append(") or (");
		
		
		//供应链查询条件
		where.append(CmContractRefVO.ICONTSTATUS+ " in ("+gyl +") and ");
		where.append(CmContractRefVO.CBILLTYPECODE+ " ='Z2' ");
		
		where.append(") or (");
		//资产查询条件
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
	 * 修改节点名称
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
	 * 设置需要查询的单据状态
	 * 1|生效
	 * 2|审批通过
	 * 3|未生效，
	 * 4|冻结，
	 * 5|关闭
	 * 6|完成
	 * 0|自由(包括 自由，提交,进行中,审批未通过，)
	 * 以后必须废除
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
