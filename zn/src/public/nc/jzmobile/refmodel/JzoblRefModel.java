package nc.jzmobile.refmodel;

import nc.ui.bd.ref.AbstractRefModel;
import nc.vo.jzvat.jzobl10.OblBillHVO;

/**
 * 外经证参照
 *   
 * @author songlx    
 * @update 2017-3-28
 */
public class JzoblRefModel extends AbstractRefModel {
	
	private String pk_project=null;
	private String pk_location = null;
	private int ioblstatus = 2; //外经证办理
	private int[] ioblstatusArray = null;
	private boolean isFilterOnlyByProject = true;
	
	private String refTitle = "外经证信息";
	private String pkFieldCode =  OblBillHVO.PK_OBLBILL ;
	private String[] hiddenFieldCode = new String[] { OblBillHVO.PK_OBLBILL };
	private String[] fieldCode = new String[] { 
			"obl.voblbillno"
			,"(select org.name from org_orgs org where org.pk_org = OBL.pk_org) as orgname"
			,"(select p.project_name from bd_project p where p.pk_project = OBL.pk_project) as pk_project"
			,"(select ab.vname from (select a.pk_contract,a.vname from jzcm_contract a union all SELECT b.pk_contract,b.vname from jzin_contract b) ab where AB.PK_CONTRACT = obl.pk_contract) as pk_contract"
			,"address.name as pk_location" };
	private String[] fieldName = new String[] { 
			"外经证编号"
			,"业务单元"
			,"项目"
			,"合同"
			, "外出经营地"};
	

	public JzoblRefModel() {
		super();
		resetFieldName();
	}

	@Override
	public String getTableName() {
		return "jzobl_bill obl left join bd_areacl address on obl.pk_location = address.pk_areacl ";
	}

	@Override
	public String getPkFieldCode() {
		return pkFieldCode;
	}

	@Override
	public String[] getHiddenFieldCode() {
		return hiddenFieldCode;
	}

	@Override
	public String[] getFieldCode() {
		return fieldCode;
	}

	@Override
	public String[] getFieldName() {
		return fieldName;
	}

	@Override
	public String getRefTitle() {
		return refTitle;
	}
	
	@Override
	public String getRefCodeField() {
		return "obl.voblbillno";
	}
	
	@Override
	public String getRefNameField() {
		return "obl.voblbillno";
	}
	
	public String getRefSql() {
		return super.getRefSql();
	}
	
	@Override
	public String getWherePart() {
		String wherePart = super.getWherePart();
		String newWherePart = wherePart == null ? "1=1" : wherePart;
		
		newWherePart += " and isnull(obl.dr,0)=0 ";
		// 需求要求，部分单据的外经证参照只根据pk_project过滤，不根据pk_org过滤；且如果pk_project为空，则该参照不显示任何数据
		if (isFilterOnlyByProject) {
			newWherePart += " and obl.pk_project = '" +pk_project+"'";
		}
		if (ioblstatusArray != null && ioblstatusArray.length > 0) {
			newWherePart += " and obl.ioblstatus in ("+getInSQLValue(ioblstatusArray)+")";
		} else {
			newWherePart += " and obl.ioblstatus = "+ioblstatus;
		}
		if (pk_location != null) {
			newWherePart += " and obl.pk_location = '" +pk_location+"'";
		}
		return newWherePart;
	}

	private String getInSQLValue(int[] values){
		StringBuffer inSql = new StringBuffer();
		for (int i = 0; i < values.length; i++) {
			inSql.append(values[i]);
			if (i < values.length - 1) {
				inSql.append(",");
			}
		}
		return inSql.toString();
	}
	
	public String getPk_project() {
		return pk_project;
	}

	public void setPk_project(String pk_project) {
		this.pk_project = pk_project;
	}

	public String getPk_location() {
		return pk_location;
	}

	public void setPk_location(String pk_location) {
		this.pk_location = pk_location;
	}

	public int getIoblstatus() {
		return ioblstatus;
	}

	public void setIoblstatus(int ioblstatus) {
		this.ioblstatus = ioblstatus;
	}

	public int[] getIoblstatusArray() {
		return ioblstatusArray;
	}

	public void setIoblstatusArray(int[] ioblstatusArray) {
		this.ioblstatusArray = ioblstatusArray;
	}

	public boolean isFilterOnlyByProject() {
		return isFilterOnlyByProject;
	}

	public void setFilterOnlyByProject(boolean isFilterOnlyByProject) {
		this.isFilterOnlyByProject = isFilterOnlyByProject;
	}
}
