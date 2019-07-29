package nc.jzmobile.refmodel;

import nc.ui.bd.ref.AbstractRefModel;
import nc.ui.bd.ref.IRefDocEdit;
import nc.ui.bd.ref.IRefMaintenanceHandler;
import nc.vo.pubapp.pattern.pub.SqlBuilder;

/**
 * <p>
 * <b>本类主要完成以下功能：</b> 合同费用参照Model
 * <ul>
 * <li>
 * </ul>
 * <p>
 * <p>
 * 
 * @version 6.0
 * @since 6.0
 * @author lizhengb
 * @time 2010-4-26 上午10:26:08
 */
public class TermTypeOrgRefModel extends AbstractRefModel {

  String[] m_sFieldCodes = {
    "vtermcode", "vtermname", "vtermcontent", "pk_ct_termset"
  };

  String[] m_sFieldNames =
      new String[] {
        nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140z0_2",
            "210140Z002-0001")/* @res "合同条款编码" */,
        nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140z0_2",
            "210140Z002-0002")/* @res "合同条款名称" */,
        nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140z0_2",
            "210140Z002-0003")/* @res "合同条款内容" */,
        nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140z0_2",
            "210140Z002-0004")
      /* @res "合同条款主键 */
      };

  public TermTypeOrgRefModel() {
    super();
    this.initWherePart();
    // 维护
    this.setRefMaintenanceHandler(new IRefMaintenanceHandler() {

      @Override
      public String[] getFucCodes() {
        return new String[] {
          "10140Z02", "10140Z00"
        };
      }

      @Override
      public IRefDocEdit getRefDocEdit() {
        return null;
      }
    });
  }

  /**
   * 获取默认列数
   */
  @Override
  public int getDefaultFieldCount() {
    return 3;
  }

  /**
   * 显示字段列表
   */
  @Override
  public java.lang.String[] getFieldCode() {
    return this.m_sFieldCodes;
  }

  /**
   * 显示字段中文名
   */
  @Override
  public java.lang.String[] getFieldName() {
    return this.m_sFieldNames;
  }

  @Override
  public String getPk_org() {
    return super.getPk_org();
  }

  /**
   * 主键字段名
   */
  @Override
  public String getPkFieldCode() {
    return "pk_ct_termset";
  }

  /**
   * 参照标题
   */
  @Override
  public String getRefTitle() {
    return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140z0_2",
        "210140Z002-0005")/* @res "合同条款档案" */;
  }

  /**
   * 参照数据库表或者视图名
   */
  @Override
  public String getTableName() {
    return "ct_termset";
  }

  @Override
  public void setPk_org(String pk_org) {
    super.setPk_org(pk_org);
    this.initWherePart();
  }

  private void initWherePart() {
    SqlBuilder defaultwhere = new SqlBuilder();
    defaultwhere.startParentheses();
    defaultwhere.append("ct_termset.pk_org", this.getPk_org());
    defaultwhere.append(" or ");
    defaultwhere.append("ct_termset.pk_org", this.getPk_group());
    defaultwhere.endParentheses();
    defaultwhere.append(" and ct_termset.dr = 0 ");
    this.setWherePart(defaultwhere.toString());
  }

}
