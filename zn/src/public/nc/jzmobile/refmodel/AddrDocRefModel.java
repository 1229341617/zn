package nc.jzmobile.refmodel;

/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/

import nc.bs.logging.Logger;
import nc.bs.uif2.BusinessExceptionAdapter;
import nc.md.model.MetaDataException;
import nc.ui.bd.ref.AbstractRefGridTreeModel;
import nc.ui.bd.ref.IRefDocEdit;
import nc.ui.bd.ref.IRefMaintenanceHandler;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.util.SqlWhereUtil;
import nc.vo.util.VisibleUtil;

public class AddrDocRefModel extends AbstractRefGridTreeModel {
	public void reset() {
		super.reset();
		this.setRootName(NCLangRes4VoTransl.getNCLangRes().getStrByID("common",
				"UC000-0001235"));
		this.setClassFieldCode(new String[] { "code", "name",
				"bd_areacl.pk_areacl", "pk_fatherarea","pk_areacl" });
		this.setFatherField("pk_fatherarea");
		this.setChildField("pk_areacl");
		this.setClassJoinField("bd_areacl.pk_areacl");
		this.setClassTableName("bd_areacl");
		this.setClassDefaultFieldCount(2);
		this.setFieldCode(new String[] { "code", "name" });
		this.setFieldName(new String[] {
				NCLangRes4VoTransl.getNCLangRes().getStrByID("common",
						"UC000-0003279"),
				NCLangRes4VoTransl.getNCLangRes().getStrByID("common",
						"UC000-0001155") });
		this.setHiddenFieldCode(new String[] { "pk_addressdoc",
				"bd_addressdoc.pk_areacl" });
		this.setPkFieldCode("pk_addressdoc");
		this.setTableName("bd_addressdoc");
		this.setDefaultFieldCount(2);
		this.setDocJoinField("bd_addressdoc.pk_areacl");
		this.setOrderPart("bd_addressdoc.code");
		this.setResourceID("8eeaabfe-7644-4bd0-a954-f8971a14f079");
		this.setRefMaintenanceHandler(new IRefMaintenanceHandler() {
			public String[] getFucCodes() {
				return new String[] { "10140ADRB", "10140ADRG" };
			}

			public IRefDocEdit getRefDocEdit() {
				return null;
			}
		});
		this.setAddEnableStateWherePart(true);
		this.resetFieldName();
	}

	public String getClassWherePart() {
		SqlWhereUtil wherePart = new SqlWhereUtil(super.getClassWherePart());

		try {
			wherePart.and(VisibleUtil.getRefVisibleCondition(
					this.getPk_group(), this.getPk_org(),
					"7f91af95-154e-43f9-995e-da76a192be15"));
			if (this.isDisabledDataShow()) {
				wherePart.and("enablestate in (2,3) ");
			} else {
				wherePart.and("enablestate = 2");
			}
		} catch (MetaDataException arg2) {
			Logger.error(arg2.getMessage());
			throw new BusinessExceptionAdapter(arg2);
		} catch (BusinessException arg3) {
			Logger.error(arg3.getMessage());
			throw new BusinessExceptionAdapter(arg3);
		}

		return wherePart.getSQLWhere();
	}

	protected String getEnvWherePart() {
		String wherePart = null;

		try {
			wherePart = VisibleUtil.getRefVisibleCondition(this.getPk_group(),
					this.getPk_org(), "8eeaabfe-7644-4bd0-a954-f8971a14f079");
		} catch (MetaDataException arg2) {
			Logger.error(arg2.getMessage());
		} catch (BusinessException arg3) {
			Logger.error(arg3.getMessage());
		}

		return wherePart;
	}
}