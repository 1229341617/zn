package nc.vo.jzinv.jzinv1003;

import nc.vo.pub.IVOMeta;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pubapp.pattern.model.meta.entity.vo.VOMetaFactory;

public class InvReceivecollBVO extends SuperVO {
	private static final long serialVersionUID = 1L;
	/**
	 * �к�
	 */
	public static final String CROWNO = "crowno";
	/**
	 * ҵ������
	 */
	public static final String IBUSITYPE = "ibusitype";
	/**
	 * ˰���Ż�
	 */
	public static final String ITAXFAVOURABLE = "itaxfavourable";
	/**
	 * ��Ʊ��ȫ�ֱ��ң�
	 */
	public static final String NGLOBALINVMNY = "nglobalinvmny";
	/**
	 * ��Ʊ��ȫ�ֱ��ң�����˰��
	 */
	public static final String NGLOBALINVTAXMNY = "nglobalinvtaxmny";
	/**
	 * ���ۣ�ȫ�ֱ��ң�
	 */
	public static final String NGLOBALPRICE = "nglobalprice";
	/**
	 * ˰�ȫ�ֱ��ң�
	 */
	public static final String NGLOBALTAXMNY = "nglobaltaxmny";
	/**
	 * ���ۣ�ȫ�֣�����˰��
	 */
	public static final String NGLOBALTAXPRICE = "nglobaltaxprice";
	/**
	 * ��Ʊ�����ű��ң�
	 */
	public static final String NGROUPINVMNY = "ngroupinvmny";
	/**
	 * ��Ʊ�����ű��ң�����˰��
	 */
	public static final String NGROUPINVTAXMNY = "ngroupinvtaxmny";
	/**
	 * ���ۣ����ű��ң�
	 */
	public static final String NGROUPPRICE = "ngroupprice";
	/**
	 * ˰����ű��ң�
	 */
	public static final String NGROUPTAXMNY = "ngrouptaxmny";
	/**
	 * ���ۣ����ű��ң�����˰��
	 */
	public static final String NGROUPTAXPRICE = "ngrouptaxprice";
	/**
	 * ��Ʊ���
	 */
	public static final String NINVMNY = "ninvmny";
	/**
	 * ��Ʊ����˰��
	 */
	public static final String NINVTAXMNY = "ninvtaxmny";
	/**
	 * ����
	 */
	public static final String NNUM = "nnum";
	/**
	 * ��Ʊ��ԭ�ң�
	 */
	public static final String NORIGINVMNY = "noriginvmny";
	/**
	 * ��Ʊ��ԭ�ң�����˰��
	 */
	public static final String NORIGINVTAXMNY = "noriginvtaxmny";
	/**
	 * ����(ԭ�ң�
	 */
	public static final String NORIGPRICE = "norigprice";
	/**
	 * ˰�ԭ�ң�
	 */
	public static final String NORIGTAXMNY = "norigtaxmny";
	/**
	 * ���ۣ�ԭ�ң�����˰��
	 */
	public static final String NORIGTAXPRICE = "norigtaxprice";
	/**
	 * ����
	 */
	public static final String NPRICE = "nprice";
	/**
	 * ˰��
	 */
	public static final String NTAXMNY = "ntaxmny";
	/**
	 * ���ۣ���˰��
	 */
	public static final String NTAXPRICE = "ntaxprice";
	/**
	 * ˰��(%)
	 */
	public static final String NTAXRATE = "ntaxrate";
	/**
	 * ����ת��ԭ��
	 */
	public static final String PK_INTRANREASON = "pk_intranreason";
	/**
	 * �ϲ㵥������
	 */
	public static final String PK_RECEIVE_COLL = "pk_receive_coll";
	/**
	 * ֽ�ʷ�Ʊ��������
	 */
	public static final String PK_RECEIVE_COLL_B = "pk_receive_coll_b";
	/**
	 * Ӧ˰��Ŀ
	 */
	public static final String PK_TAXDEDUCTLIST = "pk_taxdeductlist";
	/**
	 * ʱ���
	 */
	public static final String TS = "ts";
	/**
	 * �Զ�����1
	 */
	public static final String VDEF1 = "vdef1";
	/**
	 * �Զ�����10
	 */
	public static final String VDEF10 = "vdef10";
	/**
	 * �Զ�����11
	 */
	public static final String VDEF11 = "vdef11";
	/**
	 * �Զ�����12
	 */
	public static final String VDEF12 = "vdef12";
	/**
	 * �Զ�����13
	 */
	public static final String VDEF13 = "vdef13";
	/**
	 * �Զ�����14
	 */
	public static final String VDEF14 = "vdef14";
	/**
	 * �Զ�����15
	 */
	public static final String VDEF15 = "vdef15";
	/**
	 * �Զ�����16
	 */
	public static final String VDEF16 = "vdef16";
	/**
	 * �Զ�����17
	 */
	public static final String VDEF17 = "vdef17";
	/**
	 * �Զ�����18
	 */
	public static final String VDEF18 = "vdef18";
	/**
	 * �Զ�����19
	 */
	public static final String VDEF19 = "vdef19";
	/**
	 * �Զ�����2
	 */
	public static final String VDEF2 = "vdef2";
	/**
	 * �Զ�����20
	 */
	public static final String VDEF20 = "vdef20";
	/**
	 * �Զ�����3
	 */
	public static final String VDEF3 = "vdef3";
	/**
	 * �Զ�����4
	 */
	public static final String VDEF4 = "vdef4";
	/**
	 * �Զ�����5
	 */
	public static final String VDEF5 = "vdef5";
	/**
	 * �Զ�����6
	 */
	public static final String VDEF6 = "vdef6";
	/**
	 * �Զ�����7
	 */
	public static final String VDEF7 = "vdef7";
	/**
	 * �Զ�����8
	 */
	public static final String VDEF8 = "vdef8";
	/**
	 * �Զ�����9
	 */
	public static final String VDEF9 = "vdef9";
	/**
	 * ���
	 */
	public static final String VINVSPEC = "vinvspec";
	/**
	 * �ͺ�
	 */
	public static final String VINVTYPE = "vinvtype";
	/**
	 * ��λ
	 */
	public static final String VINVUNIT = "vinvunit";
	/**
	 * ��ע
	 */
	public static final String VMEMO = "vmemo";
	/**
	 * ��Ʊ����
	 */
	public static final String VOPENCONTENT = "vopencontent";
	/**
	 * Ԥ���ֶ�1
	 */
	public static final String VRESERVE1 = "vreserve1";
	/**
	 * Ԥ���ֶ�10
	 */
	public static final String VRESERVE10 = "vreserve10";
	/**
	 * Ԥ���ֶ�2
	 */
	public static final String VRESERVE2 = "vreserve2";
	/**
	 * Ԥ���ֶ�3
	 */
	public static final String VRESERVE3 = "vreserve3";
	/**
	 * Ԥ���ֶ�4
	 */
	public static final String VRESERVE4 = "vreserve4";
	/**
	 * Ԥ���ֶ�5
	 */
	public static final String VRESERVE5 = "vreserve5";
	/**
	 * Ԥ���ֶ�6
	 */
	public static final String VRESERVE6 = "vreserve6";
	/**
	 * Ԥ���ֶ�7
	 */
	public static final String VRESERVE7 = "vreserve7";
	/**
	 * Ԥ���ֶ�8
	 */
	public static final String VRESERVE8 = "vreserve8";
	/**
	 * Ԥ���ֶ�9
	 */
	public static final String VRESERVE9 = "vreserve9";

	/**
	 * ��ȡ�к�
	 * 
	 * @return �к�
	 */
	public String getCrowno() {
		return (String) this.getAttributeValue(InvReceivecollBVO.CROWNO);
	}

	/**
	 * �����к�
	 * 
	 * @param crowno
	 *            �к�
	 */
	public void setCrowno(String crowno) {
		this.setAttributeValue(InvReceivecollBVO.CROWNO, crowno);
	}

	/**
	 * ��ȡҵ������
	 * 
	 * @return ҵ������
	 */
	public Integer getIbusitype() {
		return (Integer) this.getAttributeValue(InvReceivecollBVO.IBUSITYPE);
	}

	/**
	 * ����ҵ������
	 * 
	 * @param ibusitype
	 *            ҵ������
	 */
	public void setIbusitype(Integer ibusitype) {
		this.setAttributeValue(InvReceivecollBVO.IBUSITYPE, ibusitype);
	}

	/**
	 * ��ȡ˰���Ż�
	 * 
	 * @return ˰���Ż�
	 */
	public Integer getItaxfavourable() {
		return (Integer) this
				.getAttributeValue(InvReceivecollBVO.ITAXFAVOURABLE);
	}

	/**
	 * ����˰���Ż�
	 * 
	 * @param itaxfavourable
	 *            ˰���Ż�
	 */
	public void setItaxfavourable(Integer itaxfavourable) {
		this.setAttributeValue(InvReceivecollBVO.ITAXFAVOURABLE, itaxfavourable);
	}

	/**
	 * ��ȡ��Ʊ��ȫ�ֱ��ң�
	 * 
	 * @return ��Ʊ��ȫ�ֱ��ң�
	 */
	public UFDouble getNglobalinvmny() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGLOBALINVMNY);
	}

	/**
	 * ���÷�Ʊ��ȫ�ֱ��ң�
	 * 
	 * @param nglobalinvmny
	 *            ��Ʊ��ȫ�ֱ��ң�
	 */
	public void setNglobalinvmny(UFDouble nglobalinvmny) {
		this.setAttributeValue(InvReceivecollBVO.NGLOBALINVMNY, nglobalinvmny);
	}

	/**
	 * ��ȡ��Ʊ��ȫ�ֱ��ң�����˰��
	 * 
	 * @return ��Ʊ��ȫ�ֱ��ң�����˰��
	 */
	public UFDouble getNglobalinvtaxmny() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGLOBALINVTAXMNY);
	}

	/**
	 * ���÷�Ʊ��ȫ�ֱ��ң�����˰��
	 * 
	 * @param nglobalinvtaxmny
	 *            ��Ʊ��ȫ�ֱ��ң�����˰��
	 */
	public void setNglobalinvtaxmny(UFDouble nglobalinvtaxmny) {
		this.setAttributeValue(InvReceivecollBVO.NGLOBALINVTAXMNY,
				nglobalinvtaxmny);
	}

	/**
	 * ��ȡ���ۣ�ȫ�ֱ��ң�
	 * 
	 * @return ���ۣ�ȫ�ֱ��ң�
	 */
	public UFDouble getNglobalprice() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGLOBALPRICE);
	}

	/**
	 * ���õ��ۣ�ȫ�ֱ��ң�
	 * 
	 * @param nglobalprice
	 *            ���ۣ�ȫ�ֱ��ң�
	 */
	public void setNglobalprice(UFDouble nglobalprice) {
		this.setAttributeValue(InvReceivecollBVO.NGLOBALPRICE, nglobalprice);
	}

	/**
	 * ��ȡ˰�ȫ�ֱ��ң�
	 * 
	 * @return ˰�ȫ�ֱ��ң�
	 */
	public UFDouble getNglobaltaxmny() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGLOBALTAXMNY);
	}

	/**
	 * ����˰�ȫ�ֱ��ң�
	 * 
	 * @param nglobaltaxmny
	 *            ˰�ȫ�ֱ��ң�
	 */
	public void setNglobaltaxmny(UFDouble nglobaltaxmny) {
		this.setAttributeValue(InvReceivecollBVO.NGLOBALTAXMNY, nglobaltaxmny);
	}

	/**
	 * ��ȡ���ۣ�ȫ�֣�����˰��
	 * 
	 * @return ���ۣ�ȫ�֣�����˰��
	 */
	public UFDouble getNglobaltaxprice() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGLOBALTAXPRICE);
	}

	/**
	 * ���õ��ۣ�ȫ�֣�����˰��
	 * 
	 * @param nglobaltaxprice
	 *            ���ۣ�ȫ�֣�����˰��
	 */
	public void setNglobaltaxprice(UFDouble nglobaltaxprice) {
		this.setAttributeValue(InvReceivecollBVO.NGLOBALTAXPRICE,
				nglobaltaxprice);
	}

	/**
	 * ��ȡ��Ʊ�����ű��ң�
	 * 
	 * @return ��Ʊ�����ű��ң�
	 */
	public UFDouble getNgroupinvmny() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGROUPINVMNY);
	}

	/**
	 * ���÷�Ʊ�����ű��ң�
	 * 
	 * @param ngroupinvmny
	 *            ��Ʊ�����ű��ң�
	 */
	public void setNgroupinvmny(UFDouble ngroupinvmny) {
		this.setAttributeValue(InvReceivecollBVO.NGROUPINVMNY, ngroupinvmny);
	}

	/**
	 * ��ȡ��Ʊ�����ű��ң�����˰��
	 * 
	 * @return ��Ʊ�����ű��ң�����˰��
	 */
	public UFDouble getNgroupinvtaxmny() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGROUPINVTAXMNY);
	}

	/**
	 * ���÷�Ʊ�����ű��ң�����˰��
	 * 
	 * @param ngroupinvtaxmny
	 *            ��Ʊ�����ű��ң�����˰��
	 */
	public void setNgroupinvtaxmny(UFDouble ngroupinvtaxmny) {
		this.setAttributeValue(InvReceivecollBVO.NGROUPINVTAXMNY,
				ngroupinvtaxmny);
	}

	/**
	 * ��ȡ���ۣ����ű��ң�
	 * 
	 * @return ���ۣ����ű��ң�
	 */
	public UFDouble getNgroupprice() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NGROUPPRICE);
	}

	/**
	 * ���õ��ۣ����ű��ң�
	 * 
	 * @param ngroupprice
	 *            ���ۣ����ű��ң�
	 */
	public void setNgroupprice(UFDouble ngroupprice) {
		this.setAttributeValue(InvReceivecollBVO.NGROUPPRICE, ngroupprice);
	}

	/**
	 * ��ȡ˰����ű��ң�
	 * 
	 * @return ˰����ű��ң�
	 */
	public UFDouble getNgrouptaxmny() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGROUPTAXMNY);
	}

	/**
	 * ����˰����ű��ң�
	 * 
	 * @param ngrouptaxmny
	 *            ˰����ű��ң�
	 */
	public void setNgrouptaxmny(UFDouble ngrouptaxmny) {
		this.setAttributeValue(InvReceivecollBVO.NGROUPTAXMNY, ngrouptaxmny);
	}

	/**
	 * ��ȡ���ۣ����ű��ң�����˰��
	 * 
	 * @return ���ۣ����ű��ң�����˰��
	 */
	public UFDouble getNgrouptaxprice() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGROUPTAXPRICE);
	}

	/**
	 * ���õ��ۣ����ű��ң�����˰��
	 * 
	 * @param ngrouptaxprice
	 *            ���ۣ����ű��ң�����˰��
	 */
	public void setNgrouptaxprice(UFDouble ngrouptaxprice) {
		this.setAttributeValue(InvReceivecollBVO.NGROUPTAXPRICE, ngrouptaxprice);
	}

	/**
	 * ��ȡ��Ʊ���
	 * 
	 * @return ��Ʊ���
	 */
	public UFDouble getNinvmny() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NINVMNY);
	}

	/**
	 * ���÷�Ʊ���
	 * 
	 * @param ninvmny
	 *            ��Ʊ���
	 */
	public void setNinvmny(UFDouble ninvmny) {
		this.setAttributeValue(InvReceivecollBVO.NINVMNY, ninvmny);
	}

	/**
	 * ��ȡ��Ʊ����˰��
	 * 
	 * @return ��Ʊ����˰��
	 */
	public UFDouble getNinvtaxmny() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NINVTAXMNY);
	}

	/**
	 * ���÷�Ʊ����˰��
	 * 
	 * @param ninvtaxmny
	 *            ��Ʊ����˰��
	 */
	public void setNinvtaxmny(UFDouble ninvtaxmny) {
		this.setAttributeValue(InvReceivecollBVO.NINVTAXMNY, ninvtaxmny);
	}

	/**
	 * ��ȡ����
	 * 
	 * @return ����
	 */
	public Integer getNnum() {
		return (Integer) this.getAttributeValue(InvReceivecollBVO.NNUM);
	}

	/**
	 * ��������
	 * 
	 * @param nnum
	 *            ����
	 */
	public void setNnum(Integer nnum) {
		this.setAttributeValue(InvReceivecollBVO.NNUM, nnum);
	}

	/**
	 * ��ȡ��Ʊ��ԭ�ң�
	 * 
	 * @return ��Ʊ��ԭ�ң�
	 */
	public UFDouble getNoriginvmny() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NORIGINVMNY);
	}

	/**
	 * ���÷�Ʊ��ԭ�ң�
	 * 
	 * @param noriginvmny
	 *            ��Ʊ��ԭ�ң�
	 */
	public void setNoriginvmny(UFDouble noriginvmny) {
		this.setAttributeValue(InvReceivecollBVO.NORIGINVMNY, noriginvmny);
	}

	/**
	 * ��ȡ��Ʊ��ԭ�ң�����˰��
	 * 
	 * @return ��Ʊ��ԭ�ң�����˰��
	 */
	public UFDouble getNoriginvtaxmny() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NORIGINVTAXMNY);
	}

	/**
	 * ���÷�Ʊ��ԭ�ң�����˰��
	 * 
	 * @param noriginvtaxmny
	 *            ��Ʊ��ԭ�ң�����˰��
	 */
	public void setNoriginvtaxmny(UFDouble noriginvtaxmny) {
		this.setAttributeValue(InvReceivecollBVO.NORIGINVTAXMNY, noriginvtaxmny);
	}

	/**
	 * ��ȡ����(ԭ�ң�
	 * 
	 * @return ����(ԭ�ң�
	 */
	public UFDouble getNorigprice() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NORIGPRICE);
	}

	/**
	 * ���õ���(ԭ�ң�
	 * 
	 * @param norigprice
	 *            ����(ԭ�ң�
	 */
	public void setNorigprice(UFDouble norigprice) {
		this.setAttributeValue(InvReceivecollBVO.NORIGPRICE, norigprice);
	}

	/**
	 * ��ȡ˰�ԭ�ң�
	 * 
	 * @return ˰�ԭ�ң�
	 */
	public UFDouble getNorigtaxmny() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NORIGTAXMNY);
	}

	/**
	 * ����˰�ԭ�ң�
	 * 
	 * @param norigtaxmny
	 *            ˰�ԭ�ң�
	 */
	public void setNorigtaxmny(UFDouble norigtaxmny) {
		this.setAttributeValue(InvReceivecollBVO.NORIGTAXMNY, norigtaxmny);
	}

	/**
	 * ��ȡ���ۣ�ԭ�ң�����˰��
	 * 
	 * @return ���ۣ�ԭ�ң�����˰��
	 */
	public UFDouble getNorigtaxprice() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NORIGTAXPRICE);
	}

	/**
	 * ���õ��ۣ�ԭ�ң�����˰��
	 * 
	 * @param norigtaxprice
	 *            ���ۣ�ԭ�ң�����˰��
	 */
	public void setNorigtaxprice(UFDouble norigtaxprice) {
		this.setAttributeValue(InvReceivecollBVO.NORIGTAXPRICE, norigtaxprice);
	}

	/**
	 * ��ȡ����
	 * 
	 * @return ����
	 */
	public UFDouble getNprice() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NPRICE);
	}

	/**
	 * ���õ���
	 * 
	 * @param nprice
	 *            ����
	 */
	public void setNprice(UFDouble nprice) {
		this.setAttributeValue(InvReceivecollBVO.NPRICE, nprice);
	}

	/**
	 * ��ȡ˰��
	 * 
	 * @return ˰��
	 */
	public UFDouble getNtaxmny() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NTAXMNY);
	}

	/**
	 * ����˰��
	 * 
	 * @param ntaxmny
	 *            ˰��
	 */
	public void setNtaxmny(UFDouble ntaxmny) {
		this.setAttributeValue(InvReceivecollBVO.NTAXMNY, ntaxmny);
	}

	/**
	 * ��ȡ���ۣ���˰��
	 * 
	 * @return ���ۣ���˰��
	 */
	public UFDouble getNtaxprice() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NTAXPRICE);
	}

	/**
	 * ���õ��ۣ���˰��
	 * 
	 * @param ntaxprice
	 *            ���ۣ���˰��
	 */
	public void setNtaxprice(UFDouble ntaxprice) {
		this.setAttributeValue(InvReceivecollBVO.NTAXPRICE, ntaxprice);
	}

	/**
	 * ��ȡ˰��(%)
	 * 
	 * @return ˰��(%)
	 */
	public UFDouble getNtaxrate() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NTAXRATE);
	}

	/**
	 * ����˰��(%)
	 * 
	 * @param ntaxrate
	 *            ˰��(%)
	 */
	public void setNtaxrate(UFDouble ntaxrate) {
		this.setAttributeValue(InvReceivecollBVO.NTAXRATE, ntaxrate);
	}

	/**
	 * ��ȡ����ת��ԭ��
	 * 
	 * @return ����ת��ԭ��
	 */
	public String getPk_intranreason() {
		return (String) this
				.getAttributeValue(InvReceivecollBVO.PK_INTRANREASON);
	}

	/**
	 * ���ý���ת��ԭ��
	 * 
	 * @param pk_intranreason
	 *            ����ת��ԭ��
	 */
	public void setPk_intranreason(String pk_intranreason) {
		this.setAttributeValue(InvReceivecollBVO.PK_INTRANREASON,
				pk_intranreason);
	}

	/**
	 * ��ȡ�ϲ㵥������
	 * 
	 * @return �ϲ㵥������
	 */
	public String getPk_receive_coll() {
		return (String) this
				.getAttributeValue(InvReceivecollBVO.PK_RECEIVE_COLL);
	}

	/**
	 * �����ϲ㵥������
	 * 
	 * @param pk_receive_coll
	 *            �ϲ㵥������
	 */
	public void setPk_receive_coll(String pk_receive_coll) {
		this.setAttributeValue(InvReceivecollBVO.PK_RECEIVE_COLL,
				pk_receive_coll);
	}

	/**
	 * ��ȡֽ�ʷ�Ʊ��������
	 * 
	 * @return ֽ�ʷ�Ʊ��������
	 */
	public String getPk_receive_coll_b() {
		return (String) this
				.getAttributeValue(InvReceivecollBVO.PK_RECEIVE_COLL_B);
	}

	/**
	 * ����ֽ�ʷ�Ʊ��������
	 * 
	 * @param pk_receive_coll_b
	 *            ֽ�ʷ�Ʊ��������
	 */
	public void setPk_receive_coll_b(String pk_receive_coll_b) {
		this.setAttributeValue(InvReceivecollBVO.PK_RECEIVE_COLL_B,
				pk_receive_coll_b);
	}

	/**
	 * ��ȡӦ˰��Ŀ
	 * 
	 * @return Ӧ˰��Ŀ
	 */
	public String getPk_taxdeductlist() {
		return (String) this
				.getAttributeValue(InvReceivecollBVO.PK_TAXDEDUCTLIST);
	}

	/**
	 * ����Ӧ˰��Ŀ
	 * 
	 * @param pk_taxdeductlist
	 *            Ӧ˰��Ŀ
	 */
	public void setPk_taxdeductlist(String pk_taxdeductlist) {
		this.setAttributeValue(InvReceivecollBVO.PK_TAXDEDUCTLIST,
				pk_taxdeductlist);
	}

	/**
	 * ��ȡʱ���
	 * 
	 * @return ʱ���
	 */
	public UFDateTime getTs() {
		return (UFDateTime) this.getAttributeValue(InvReceivecollBVO.TS);
	}

	/**
	 * ����ʱ���
	 * 
	 * @param ts
	 *            ʱ���
	 */
	public void setTs(UFDateTime ts) {
		this.setAttributeValue(InvReceivecollBVO.TS, ts);
	}

	/**
	 * ��ȡ�Զ�����1
	 * 
	 * @return �Զ�����1
	 */
	public String getVdef1() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF1);
	}

	/**
	 * �����Զ�����1
	 * 
	 * @param vdef1
	 *            �Զ�����1
	 */
	public void setVdef1(String vdef1) {
		this.setAttributeValue(InvReceivecollBVO.VDEF1, vdef1);
	}

	/**
	 * ��ȡ�Զ�����10
	 * 
	 * @return �Զ�����10
	 */
	public String getVdef10() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF10);
	}

	/**
	 * �����Զ�����10
	 * 
	 * @param vdef10
	 *            �Զ�����10
	 */
	public void setVdef10(String vdef10) {
		this.setAttributeValue(InvReceivecollBVO.VDEF10, vdef10);
	}

	/**
	 * ��ȡ�Զ�����11
	 * 
	 * @return �Զ�����11
	 */
	public String getVdef11() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF11);
	}

	/**
	 * �����Զ�����11
	 * 
	 * @param vdef11
	 *            �Զ�����11
	 */
	public void setVdef11(String vdef11) {
		this.setAttributeValue(InvReceivecollBVO.VDEF11, vdef11);
	}

	/**
	 * ��ȡ�Զ�����12
	 * 
	 * @return �Զ�����12
	 */
	public String getVdef12() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF12);
	}

	/**
	 * �����Զ�����12
	 * 
	 * @param vdef12
	 *            �Զ�����12
	 */
	public void setVdef12(String vdef12) {
		this.setAttributeValue(InvReceivecollBVO.VDEF12, vdef12);
	}

	/**
	 * ��ȡ�Զ�����13
	 * 
	 * @return �Զ�����13
	 */
	public String getVdef13() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF13);
	}

	/**
	 * �����Զ�����13
	 * 
	 * @param vdef13
	 *            �Զ�����13
	 */
	public void setVdef13(String vdef13) {
		this.setAttributeValue(InvReceivecollBVO.VDEF13, vdef13);
	}

	/**
	 * ��ȡ�Զ�����14
	 * 
	 * @return �Զ�����14
	 */
	public String getVdef14() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF14);
	}

	/**
	 * �����Զ�����14
	 * 
	 * @param vdef14
	 *            �Զ�����14
	 */
	public void setVdef14(String vdef14) {
		this.setAttributeValue(InvReceivecollBVO.VDEF14, vdef14);
	}

	/**
	 * ��ȡ�Զ�����15
	 * 
	 * @return �Զ�����15
	 */
	public String getVdef15() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF15);
	}

	/**
	 * �����Զ�����15
	 * 
	 * @param vdef15
	 *            �Զ�����15
	 */
	public void setVdef15(String vdef15) {
		this.setAttributeValue(InvReceivecollBVO.VDEF15, vdef15);
	}

	/**
	 * ��ȡ�Զ�����16
	 * 
	 * @return �Զ�����16
	 */
	public String getVdef16() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF16);
	}

	/**
	 * �����Զ�����16
	 * 
	 * @param vdef16
	 *            �Զ�����16
	 */
	public void setVdef16(String vdef16) {
		this.setAttributeValue(InvReceivecollBVO.VDEF16, vdef16);
	}

	/**
	 * ��ȡ�Զ�����17
	 * 
	 * @return �Զ�����17
	 */
	public String getVdef17() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF17);
	}

	/**
	 * �����Զ�����17
	 * 
	 * @param vdef17
	 *            �Զ�����17
	 */
	public void setVdef17(String vdef17) {
		this.setAttributeValue(InvReceivecollBVO.VDEF17, vdef17);
	}

	/**
	 * ��ȡ�Զ�����18
	 * 
	 * @return �Զ�����18
	 */
	public String getVdef18() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF18);
	}

	/**
	 * �����Զ�����18
	 * 
	 * @param vdef18
	 *            �Զ�����18
	 */
	public void setVdef18(String vdef18) {
		this.setAttributeValue(InvReceivecollBVO.VDEF18, vdef18);
	}

	/**
	 * ��ȡ�Զ�����19
	 * 
	 * @return �Զ�����19
	 */
	public String getVdef19() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF19);
	}

	/**
	 * �����Զ�����19
	 * 
	 * @param vdef19
	 *            �Զ�����19
	 */
	public void setVdef19(String vdef19) {
		this.setAttributeValue(InvReceivecollBVO.VDEF19, vdef19);
	}

	/**
	 * ��ȡ�Զ�����2
	 * 
	 * @return �Զ�����2
	 */
	public String getVdef2() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF2);
	}

	/**
	 * �����Զ�����2
	 * 
	 * @param vdef2
	 *            �Զ�����2
	 */
	public void setVdef2(String vdef2) {
		this.setAttributeValue(InvReceivecollBVO.VDEF2, vdef2);
	}

	/**
	 * ��ȡ�Զ�����20
	 * 
	 * @return �Զ�����20
	 */
	public String getVdef20() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF20);
	}

	/**
	 * �����Զ�����20
	 * 
	 * @param vdef20
	 *            �Զ�����20
	 */
	public void setVdef20(String vdef20) {
		this.setAttributeValue(InvReceivecollBVO.VDEF20, vdef20);
	}

	/**
	 * ��ȡ�Զ�����3
	 * 
	 * @return �Զ�����3
	 */
	public String getVdef3() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF3);
	}

	/**
	 * �����Զ�����3
	 * 
	 * @param vdef3
	 *            �Զ�����3
	 */
	public void setVdef3(String vdef3) {
		this.setAttributeValue(InvReceivecollBVO.VDEF3, vdef3);
	}

	/**
	 * ��ȡ�Զ�����4
	 * 
	 * @return �Զ�����4
	 */
	public String getVdef4() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF4);
	}

	/**
	 * �����Զ�����4
	 * 
	 * @param vdef4
	 *            �Զ�����4
	 */
	public void setVdef4(String vdef4) {
		this.setAttributeValue(InvReceivecollBVO.VDEF4, vdef4);
	}

	/**
	 * ��ȡ�Զ�����5
	 * 
	 * @return �Զ�����5
	 */
	public String getVdef5() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF5);
	}

	/**
	 * �����Զ�����5
	 * 
	 * @param vdef5
	 *            �Զ�����5
	 */
	public void setVdef5(String vdef5) {
		this.setAttributeValue(InvReceivecollBVO.VDEF5, vdef5);
	}

	/**
	 * ��ȡ�Զ�����6
	 * 
	 * @return �Զ�����6
	 */
	public String getVdef6() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF6);
	}

	/**
	 * �����Զ�����6
	 * 
	 * @param vdef6
	 *            �Զ�����6
	 */
	public void setVdef6(String vdef6) {
		this.setAttributeValue(InvReceivecollBVO.VDEF6, vdef6);
	}

	/**
	 * ��ȡ�Զ�����7
	 * 
	 * @return �Զ�����7
	 */
	public String getVdef7() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF7);
	}

	/**
	 * �����Զ�����7
	 * 
	 * @param vdef7
	 *            �Զ�����7
	 */
	public void setVdef7(String vdef7) {
		this.setAttributeValue(InvReceivecollBVO.VDEF7, vdef7);
	}

	/**
	 * ��ȡ�Զ�����8
	 * 
	 * @return �Զ�����8
	 */
	public String getVdef8() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF8);
	}

	/**
	 * �����Զ�����8
	 * 
	 * @param vdef8
	 *            �Զ�����8
	 */
	public void setVdef8(String vdef8) {
		this.setAttributeValue(InvReceivecollBVO.VDEF8, vdef8);
	}

	/**
	 * ��ȡ�Զ�����9
	 * 
	 * @return �Զ�����9
	 */
	public String getVdef9() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF9);
	}

	/**
	 * �����Զ�����9
	 * 
	 * @param vdef9
	 *            �Զ�����9
	 */
	public void setVdef9(String vdef9) {
		this.setAttributeValue(InvReceivecollBVO.VDEF9, vdef9);
	}

	/**
	 * ��ȡ���
	 * 
	 * @return ���
	 */
	public String getVinvspec() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VINVSPEC);
	}

	/**
	 * ���ù��
	 * 
	 * @param vinvspec
	 *            ���
	 */
	public void setVinvspec(String vinvspec) {
		this.setAttributeValue(InvReceivecollBVO.VINVSPEC, vinvspec);
	}

	/**
	 * ��ȡ�ͺ�
	 * 
	 * @return �ͺ�
	 */
	public String getVinvtype() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VINVTYPE);
	}

	/**
	 * �����ͺ�
	 * 
	 * @param vinvtype
	 *            �ͺ�
	 */
	public void setVinvtype(String vinvtype) {
		this.setAttributeValue(InvReceivecollBVO.VINVTYPE, vinvtype);
	}

	/**
	 * ��ȡ��λ
	 * 
	 * @return ��λ
	 */
	public String getVinvunit() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VINVUNIT);
	}

	/**
	 * ���õ�λ
	 * 
	 * @param vinvunit
	 *            ��λ
	 */
	public void setVinvunit(String vinvunit) {
		this.setAttributeValue(InvReceivecollBVO.VINVUNIT, vinvunit);
	}

	/**
	 * ��ȡ��ע
	 * 
	 * @return ��ע
	 */
	public String getVmemo() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VMEMO);
	}

	/**
	 * ���ñ�ע
	 * 
	 * @param vmemo
	 *            ��ע
	 */
	public void setVmemo(String vmemo) {
		this.setAttributeValue(InvReceivecollBVO.VMEMO, vmemo);
	}

	/**
	 * ��ȡ��Ʊ����
	 * 
	 * @return ��Ʊ����
	 */
	public String getVopencontent() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VOPENCONTENT);
	}

	/**
	 * ���÷�Ʊ����
	 * 
	 * @param vopencontent
	 *            ��Ʊ����
	 */
	public void setVopencontent(String vopencontent) {
		this.setAttributeValue(InvReceivecollBVO.VOPENCONTENT, vopencontent);
	}

	/**
	 * ��ȡԤ���ֶ�1
	 * 
	 * @return Ԥ���ֶ�1
	 */
	public String getVreserve1() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE1);
	}

	/**
	 * ����Ԥ���ֶ�1
	 * 
	 * @param vreserve1
	 *            Ԥ���ֶ�1
	 */
	public void setVreserve1(String vreserve1) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE1, vreserve1);
	}

	/**
	 * ��ȡԤ���ֶ�10
	 * 
	 * @return Ԥ���ֶ�10
	 */
	public String getVreserve10() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE10);
	}

	/**
	 * ����Ԥ���ֶ�10
	 * 
	 * @param vreserve10
	 *            Ԥ���ֶ�10
	 */
	public void setVreserve10(String vreserve10) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE10, vreserve10);
	}

	/**
	 * ��ȡԤ���ֶ�2
	 * 
	 * @return Ԥ���ֶ�2
	 */
	public String getVreserve2() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE2);
	}

	/**
	 * ����Ԥ���ֶ�2
	 * 
	 * @param vreserve2
	 *            Ԥ���ֶ�2
	 */
	public void setVreserve2(String vreserve2) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE2, vreserve2);
	}

	/**
	 * ��ȡԤ���ֶ�3
	 * 
	 * @return Ԥ���ֶ�3
	 */
	public String getVreserve3() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE3);
	}

	/**
	 * ����Ԥ���ֶ�3
	 * 
	 * @param vreserve3
	 *            Ԥ���ֶ�3
	 */
	public void setVreserve3(String vreserve3) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE3, vreserve3);
	}

	/**
	 * ��ȡԤ���ֶ�4
	 * 
	 * @return Ԥ���ֶ�4
	 */
	public String getVreserve4() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE4);
	}

	/**
	 * ����Ԥ���ֶ�4
	 * 
	 * @param vreserve4
	 *            Ԥ���ֶ�4
	 */
	public void setVreserve4(String vreserve4) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE4, vreserve4);
	}

	/**
	 * ��ȡԤ���ֶ�5
	 * 
	 * @return Ԥ���ֶ�5
	 */
	public String getVreserve5() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE5);
	}

	/**
	 * ����Ԥ���ֶ�5
	 * 
	 * @param vreserve5
	 *            Ԥ���ֶ�5
	 */
	public void setVreserve5(String vreserve5) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE5, vreserve5);
	}

	/**
	 * ��ȡԤ���ֶ�6
	 * 
	 * @return Ԥ���ֶ�6
	 */
	public String getVreserve6() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE6);
	}

	/**
	 * ����Ԥ���ֶ�6
	 * 
	 * @param vreserve6
	 *            Ԥ���ֶ�6
	 */
	public void setVreserve6(String vreserve6) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE6, vreserve6);
	}

	/**
	 * ��ȡԤ���ֶ�7
	 * 
	 * @return Ԥ���ֶ�7
	 */
	public String getVreserve7() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE7);
	}

	/**
	 * ����Ԥ���ֶ�7
	 * 
	 * @param vreserve7
	 *            Ԥ���ֶ�7
	 */
	public void setVreserve7(String vreserve7) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE7, vreserve7);
	}

	/**
	 * ��ȡԤ���ֶ�8
	 * 
	 * @return Ԥ���ֶ�8
	 */
	public String getVreserve8() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE8);
	}

	/**
	 * ����Ԥ���ֶ�8
	 * 
	 * @param vreserve8
	 *            Ԥ���ֶ�8
	 */
	public void setVreserve8(String vreserve8) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE8, vreserve8);
	}

	/**
	 * ��ȡԤ���ֶ�9
	 * 
	 * @return Ԥ���ֶ�9
	 */
	public String getVreserve9() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE9);
	}

	/**
	 * ����Ԥ���ֶ�9
	 * 
	 * @param vreserve9
	 *            Ԥ���ֶ�9
	 */
	public void setVreserve9(String vreserve9) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE9, vreserve9);
	}

	@Override
	public IVOMeta getMetaData() {
		return VOMetaFactory.getInstance().getVOMeta("jzinv.InvReceivecollBVO");
	}
}