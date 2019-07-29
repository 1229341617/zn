package nc.jzmobile.refmodel;

import nc.bs.logging.Logger;
import nc.md.MDBaseQueryFacade;
import nc.md.model.IBean;
import nc.md.model.MetaDataException;
import nc.ui.bd.ref.AbstractRefModel;
import nc.ui.bd.ref.IRefDocEdit;
import nc.ui.bd.ref.IRefMaintenanceHandler;
import nc.ui.pub.beans.ValueChangedEvent;
import nc.vo.jzbase.pub.tool.RefModelQueryCondUtils;
import nc.vo.jzvat.jzinv2010.OpenHVO;
import nc.vo.jzvat.pub.consts.IJzinvFunCode;
import nc.vo.pub.BusinessException;
import nc.vo.util.VisibleUtil;

import org.apache.commons.lang.StringUtils;

/**
 * 开票参照
 *
 * @author bailj
 * @date 2016-7-14 上午10:04:07
 */
public class InvOpenRefModel extends AbstractRefModel {
	private String beanID = null;
    private String pk_project = null;
    private String pk_customer = null;
	private String pk_contract = null;
	
	public InvOpenRefModel(){
		
	}
	
	public InvOpenRefModel(String refNodeName) {
		setRefNodeName(refNodeName);
	}

	@Override
	public void setRefNodeName(String refNodeName) {
		m_strRefNodeName = refNodeName;
		setFieldCode(new String[] { OpenHVO.VBILLCODE, OpenHVO.DBILLDATE, OpenHVO.VINVNO, 
				OpenHVO.VINVCODE});
		setFieldName(new String[] {"单据编号", "单据日期","发票号", "发票代码"});
		setHiddenFieldCode(new String[] { OpenHVO.PK_OPEN});
		setTableName("jzinv_open" );
		setPkFieldCode(OpenHVO.PK_OPEN);
		setDefaultFieldCount(4);
		resetFieldName();
		
		this.setRefCodeField(OpenHVO.VINVNO);
		this.setRefNameField(OpenHVO.VINVNO);
		
		setCaseSensive(true);

		// 添加“停用”条件
		setAddEnableStateWherePart(true);

		setRefMaintenanceHandler(new IRefMaintenanceHandler() {

			@Override
			public String[] getFucCodes() {
				return new String[] {IJzinvFunCode.JZINV_OPEN };
			}

			@Override
			public IRefDocEdit getRefDocEdit() {
				return null;
			}
		});

		setFilterRefNodeName(new String[] { "集团" });/*-=notranslate=-*/
	}

	@Override
	protected String getEnvWherePart() {
		try {
			// 获取有效组织ID
			String pk_org = RefModelQueryCondUtils.getAvailableOrgIDByMDMode(getBeanID(), getPk_org(),
					getPk_group());
			return VisibleUtil.getRefVisibleCondition(getPk_group(), pk_org, getBeanID());
		} catch (BusinessException e) {
			Logger.error(e.getMessage());
		}
		return super.getEnvWherePart();
	}
	
	private String getBeanID() throws MetaDataException {
		if (beanID == null) {
			IBean bean = MDBaseQueryFacade.getInstance().getBeanByFullClassName(OpenHVO.class.getName());
			beanID = bean.getID();
		}
		return beanID;
	}

	/**
	 * 参照标题
	 *
	 * @return java.lang.String
	 */
	public String getRefTitle() {
		return "开票参照";
	}

	@Override
	public void filterValueChanged(ValueChangedEvent changedValue) {
		super.filterValueChanged(changedValue);
		String[] pk_orgs = (String[]) changedValue.getNewValue();
		if (pk_orgs != null && pk_orgs.length > 0) {
			setPk_org(pk_orgs[0]);
		}
	}
	@Override
	public String buildBaseSql(String patch, String[] columns,
			String[] hiddenColumns, String tableName, String whereCondition) {
		String sql =  super.buildBaseSql(patch, columns, hiddenColumns, tableName, whereCondition);
		StringBuffer newSql = new StringBuffer();
		if(sql.contains("enablestate")){
			newSql.append(sql.split("where")[0]);
			newSql.append(" where 1=1 ");
			newSql.append(" and isnull(dr,0) = 0");
		}
		if (!StringUtils.isEmpty(pk_contract)) {
			newSql.append(" and pk_open in (select pk_open from jzinv_open_collect where isnull(dr,0)=0 and pk_contract ='" + getPk_contract() + "')");
		}
		if (!StringUtils.isEmpty(pk_project)) {
			newSql.append(" and pk_project='" + pk_project + "'");
		}
		if (!StringUtils.isEmpty(pk_customer)) {
			newSql.append(" and pk_customer = '"+ pk_customer +"'");
		}
		newSql.append(" and bisred = 'N'");
		newSql.append(" and (isnull(nthopenmny,0)-isnull(nhadredmny,0)) > 0 ");
		newSql.append(" and bisabolish = 'N' ");
		newSql.append(" and (bisopen = 'Y' ");
		newSql.append(" or bisgoldopensucess = 'Y') ");
		newSql.append(" and pk_applyorg = '" + getPk_org() + "' ");
		return newSql.toString();
	}

	public String getPk_customer() {
		return pk_customer;
	}

	public void setPk_customer(String pk_customer) {
		this.pk_customer = pk_customer;
	}

	public String getPk_contract() {
		return pk_contract;
	}

	public void setPk_contract(String pk_contract) {
		this.pk_contract = pk_contract;
	}
	
	public String getPk_project() {
		return pk_project;
	}

	public void setPk_project(String pk_project) {
		this.pk_project = pk_project;
	}
}
