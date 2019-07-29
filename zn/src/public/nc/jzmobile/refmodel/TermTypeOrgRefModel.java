package nc.jzmobile.refmodel;

import nc.ui.bd.ref.AbstractRefModel;
import nc.ui.bd.ref.IRefDocEdit;
import nc.ui.bd.ref.IRefMaintenanceHandler;
import nc.vo.pubapp.pattern.pub.SqlBuilder;

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
public class TermTypeOrgRefModel extends AbstractRefModel {

  String[] m_sFieldCodes = {
    "vtermcode", "vtermname", "vtermcontent", "pk_ct_termset"
  };

  String[] m_sFieldNames =
      new String[] {
        nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140z0_2",
            "210140Z002-0001")/* @res "��ͬ�������" */,
        nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140z0_2",
            "210140Z002-0002")/* @res "��ͬ��������" */,
        nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140z0_2",
            "210140Z002-0003")/* @res "��ͬ��������" */,
        nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140z0_2",
            "210140Z002-0004")
      /* @res "��ͬ�������� */
      };

  public TermTypeOrgRefModel() {
    super();
    this.initWherePart();
    // ά��
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
   * ��ȡĬ������
   */
  @Override
  public int getDefaultFieldCount() {
    return 3;
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

  @Override
  public String getPk_org() {
    return super.getPk_org();
  }

  /**
   * �����ֶ���
   */
  @Override
  public String getPkFieldCode() {
    return "pk_ct_termset";
  }

  /**
   * ���ձ���
   */
  @Override
  public String getRefTitle() {
    return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("10140z0_2",
        "210140Z002-0005")/* @res "��ͬ�����" */;
  }

  /**
   * �������ݿ�������ͼ��
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
