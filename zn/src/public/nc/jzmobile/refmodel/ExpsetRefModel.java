package nc.jzmobile.refmodel;

import nc.ui.bd.ref.IRefDocEdit;
import nc.ui.bd.ref.IRefMaintenanceHandler;

/**
 * <p>
 * <b>������Ҫ������¹��ܣ�</b> ��ͬ���ò���Model
 * <ul>
 * <li>
 * </ul>
 * <p>
 * <p>
 * 
 * @version 6.0
 * @since 6.0
 * @author lizhengb
 * @time 2010-4-26 ����10:26:08
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
            "210140Z003-0003")/* @res "��������" */,
        nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140z0_2",
            "210140Z003-0000")/* @res "����������" */,
        nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140z0_2",
            "210140Z003-0004")/* @res "��ע" */,
        nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140z0_2",
            "210140Z003-0002")
      /* @res "���ö�������" */

      };

  public ExpsetRefModel() {
    super();

    // ά��
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
   * ��ȡĬ������
   */
  @Override
  public int getDefaultFieldCount() {
    return 2;
  }

  /**
   * ��ʾ�ֶ��б�
   */
  @Override
  public java.lang.String[] getFieldCode() {
    return this.m_sFieldCodes;
  }

  /**
   * ��ʾ�ֶ�������
   */
  @Override
  public java.lang.String[] getFieldName() {
    return this.m_sFieldNames;
  }

  /**
   * �����ֶ���
   */
  @Override
  public String getPkFieldCode() {
    return "pk_ct_expset";
  }

  /**
   * ���ձ���
   */
  @Override
  public String getRefTitle() {
    return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140Z0_2",
        "210140Z003-0005")
    /* @res "��ͬ����" */;
  }

  /**
   * �������ݿ�������ͼ��
   */
  @Override
  public String getTableName() {
    return "ct_expset";
  }

  /**
   * �˴����뷽��˵��
   */
  @Override
  public String getWherePart() {
    return " pk_org='" + super.getPk_group() + "' and dr = 0 ";
  }
}
