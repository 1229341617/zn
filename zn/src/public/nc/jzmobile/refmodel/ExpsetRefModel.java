package nc.jzmobile.refmodel;

import nc.ui.bd.ref.IRefDocEdit;
import nc.ui.bd.ref.IRefMaintenanceHandler;

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
public class ExpsetRefModel extends nc.ui.bd.ref.AbstractRefModel {

  /**
   * 
   */
  private static final long serialVersionUID = -4564062033976587230L;

  String[] m_sFieldCodes = {
    "expitemcode", "expitemname", "memo", "pk_ct_expset"
  };

  String[] m_sFieldNames =
      new String[] {
        nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140z0_2",
            "210140Z003-0003")/* @res "费用项编号" */,
        nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140z0_2",
            "210140Z003-0000")/* @res "费用项名称" */,
        nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140z0_2",
            "210140Z003-0004")/* @res "备注" */,
        nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140z0_2",
            "210140Z003-0002")
      /* @res "费用定义主键" */

      };

  public ExpsetRefModel() {
    super();

    // 维护
    this.setRefMaintenanceHandler(new IRefMaintenanceHandler() {

      @Override
      public String[] getFucCodes() {
        return new String[] {
          "10140Z04"
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
    return 2;
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

  /**
   * 主键字段名
   */
  @Override
  public String getPkFieldCode() {
    return "pk_ct_expset";
  }

  /**
   * 参照标题
   */
  @Override
  public String getRefTitle() {
    return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140Z0_2",
        "210140Z003-0005")
    /* @res "合同费用" */;
  }

  /**
   * 参照数据库表或者视图名
   */
  @Override
  public String getTableName() {
    return "ct_expset";
  }

  /**
   * 此处插入方法说明
   */
  @Override
  public String getWherePart() {
    return " pk_org='" + super.getPk_group() + "' and dr = 0 ";
  }
}
