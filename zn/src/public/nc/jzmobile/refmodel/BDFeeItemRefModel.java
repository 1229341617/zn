package nc.jzmobile.refmodel;

import nc.ui.bd.ref.AbstractRefTreeModel;
import nc.ui.bd.ref.IRefDocEdit;
import nc.ui.bd.ref.IRefMaintenanceHandler;
import nc.vo.jz.jzbd30.BDFeeItemVO;

public class BDFeeItemRefModel extends AbstractRefTreeModel {
	public BDFeeItemRefModel() {
		setRefNodeName("费用项目");

		setRefMaintenanceHandler(new IRefMaintenanceHandler() {
			@Override
			public String[] getFucCodes() {
				// 功能号
				return new String[] { "H5H2030", };
			}

			@Override
			public IRefDocEdit getRefDocEdit() {
				return null;
			}
		});
	}

	@Override
	public void setRefNodeName(String refNodeName) {
		m_strRefNodeName = refNodeName;
		setFieldCode(new String[] { BDFeeItemVO.VCODE, BDFeeItemVO.VNAME });
		setFieldName(new String[] {
				nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("common",
						"UC000-0003279")/* @res "编码" */,
				nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("common",
						"UC000-0001155") /* @res "名称" */});
		setHiddenFieldCode(new String[] { BDFeeItemVO.PK_FEEITEM,
				BDFeeItemVO.PK_PARENT });
		setTableName(BDFeeItemVO.TABLE_NAME);
		setPkFieldCode(BDFeeItemVO.PK_FEEITEM);
		resetFieldName();

		this.setRefCodeField(BDFeeItemVO.VCODE);
		this.setRefNameField(BDFeeItemVO.VNAME);
		this.setFatherField(BDFeeItemVO.PK_PARENT);
		this.setChildField(BDFeeItemVO.PK_FEEITEM);

		// 设置大小写敏感
		setCaseSensive(true);

		// 添加“停用”条件
		setAddEnableStateWherePart(true);
	}

	/**
	 * 管控模式控制
	 */
	@Override
	protected String getEnvWherePart() {
		String wherePart = null;

		return wherePart;
	}

	@Override
	public String getRefTitle() {
		return "费用项目";
	}

	@Override
	public String getWherePart() {
		String wheresql = super.getWherePart();
		if (wheresql != null && wheresql.length() > 0) {
			return wheresql + " and isnull(dr,0)=0 and pk_group='"+getPk_group()+"'";
		} else {
			return " isnull(dr,0)=0 and pk_group='"+getPk_group()+"'";
		}
	}
}
