package nc.jzmobile.app.impl;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.itf.ic.m45.self.IPurchaseInMaintain;
import nc.itf.mobile.app.IMobilebillExecute;
import nc.itf.uap.pf.IplatFormEntry;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.vo.ic.m45.entity.PurchaseInBodyVO;
import nc.vo.ic.m45.entity.PurchaseInHeadVO;
import nc.vo.ic.m45.entity.PurchaseInVO;
import nc.vo.pu.m21.entity.OrderHeaderVO;
import nc.vo.pu.m21.entity.OrderItemVO;
import nc.vo.pu.m21.entity.OrderVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;

public class MobilebillExecuteImpl implements IMobilebillExecute {

	/** �ƶ����ϱ��濪ʼ========================================= */
	@SuppressWarnings("unchecked")
	@Override
	public PurchaseInVO[] slSaveBill(String userid, String pk_group,
			Map<String, String> head, List<Map<String, String>> bodys)
			throws BusinessException {
		BaseDAO dao = new BaseDAO();
		String pk_order = head.get("pk_order");
		// String[] pk_order_bs = new String[bodys.size()] ;//�����ӱ�����

		StringBuffer buffer = new StringBuffer(
				"select * from po_order_b where pk_order_b in(");
		for (int i = 0; i < bodys.size(); i++) {
			buffer.append("'" + bodys.get(i).get("pk_order_b") + "',");
		}
		buffer.deleteCharAt(buffer.length() - 1).append(")");

		List<OrderHeaderVO> poOrders = (List<OrderHeaderVO>) dao.executeQuery(
				"select * from po_order where pk_order = '" + pk_order + "'",
				new BeanListProcessor(OrderHeaderVO.class));

		List<OrderItemVO> poOrderBs = (List<OrderItemVO>) dao.executeQuery(
				buffer.toString(), new BeanListProcessor(OrderItemVO.class));

		PurchaseInHeadVO pih = new PurchaseInHeadVO();
		/** �����ⵥ��ͷ */
		pih.setApprover(null);// ǩ����
		pih.setBillmaker(userid);// �Ƶ���
		pih.setBitinbill(new UFBoolean(false));// ������ⵥ
		pih.setBtriatradeflag(poOrderBs.get(0).getBtriatradeflag());// ����ó��
		pih.setCbizid(head.get("cbizid"));// �ɹ�Ա
		pih.setCbiztype(poOrders.get(0).getPk_busitype());// ҵ������
		pih.setCcostdomainid(getDataByOrg(poOrderBs.get(0).getPk_org()).get(
				"pk_costregion"));// ����ɱ���
		pih.setCcustomerid(poOrders.get(0).getPk_recvcustomer());// �ջ��ͻ�
		pih.setCdptid(head.get("cdptid"));// �ɹ�����
		pih.setCdptvid(head.get("cdptvid"));// �ɹ�����
		pih.setCfanaceorgoid(poOrderBs.get(0).getPk_psfinanceorg());// ���������֯
		pih.setCfanaceorgvid(poOrderBs.get(0).getPk_psfinanceorg_v());// �ɹ�������ϸ.���������֯
		pih.setCorpoid(getDataByOrg(poOrderBs.get(0).getPk_org())
				.get("pk_corp"));// ��˾
		pih.setCorpvid(getDataByOrg(poOrderBs.get(0).getPk_org()).get("pk_vid"));// ��˾
		pih.setCpayfinorgoid(poOrderBs.get(0).getPk_apfinanceorg());// Ӧ����֯
		pih.setCpayfinorgvid(poOrderBs.get(0).getPk_apfinanceorg_v());// Ӧ����֯
		pih.setCpurorgoid(poOrders.get(0).getPk_org());// �ɹ���֯
		pih.setCpurorgvid(poOrders.get(0).getPk_org_v());// �ɹ���֯
		pih.setCreationtime(new UFDateTime());// ����ʱ��
		pih.setCreator(userid);// ������
		pih.setCrececountryid(poOrderBs.get(0).getCrececountryid());// �ջ�����
		pih.setCsendcountryid(poOrderBs.get(0).getCsendcountryid());// ��������
		pih.setCsendtypeid(poOrders.get(0).getPk_transporttype());// ���䷽ʽ
		pih.setCtaxcountryid(poOrderBs.get(0).getCtaxcountryid());// ��˰����
		pih.setCtradewordid(poOrders.get(0).getCtradewordid());// ó������
		// pih.setCtrantypeid("0001D1100000000026U8");// ���������
		pih.setCtrantypeid(getBillType("45-01").get("pk_billtypeid"));// ���������
		pih.setCvendorid(poOrders.get(0).getPk_supplier());// ��Ӧ��
		pih.setCwarehouseid(head.get("cwarehouseid"));// �ֿ�
		pih.setCwhsmanagerid(head.get("cwhsmanagerid"));// ���Ա
		pih.setDbilldate(new UFDate(head.get("dbilldate")));// ��������
		pih.setDmakedate(new UFDate());// �Ƶ�����
		pih.setFbillflag(2);// ����״̬
		pih.setFbuysellflag(poOrderBs.get(0).getFbuysellflag());// ��������
		pih.setFreplenishflag(poOrders.get(0).getBreturn());// �ɹ��˿�
		pih.setIprintcount(0);// ��ӡ����
		// pih.setModifiedtime(null);//����޸�ʱ��
		// pih.setModifier(null); //����޸���
		pih.setNtotalnum(new UFDouble(head.get("ntotalnum")));// ������
		pih.setNtotalpiece(new UFDouble(0));// �ܼ���
		pih.setNtotalvolume(new UFDouble(0));// �����
		pih.setNtotalweight(new UFDouble(0));// ������
		pih.setPk_group(pk_group);// ����
		// pih.setPk_measware(null);//��������
		pih.setPk_org(poOrders.get(0).getPk_org()); // �����֯���°汾
		pih.setPk_org_v(poOrders.get(0).getPk_org_v()); // �����֯
		// pih.setTaudittime(null); //ǩ������
		// pih.setVbillcode(null); //���ݺ�
		pih.setVdef1(poOrders.get(0).getVdef1());
		pih.setVdef2(poOrders.get(0).getVdef2());
		pih.setVdef3(head.get("vdef3"));
		pih.setVdef4(head.get("vdef4"));
		pih.setVdef5(head.get("vdef5"));
		pih.setVnote(head.get("vnote")); // ��ע
		pih.setVreturnreason(null);// �˿�����
		pih.setVtrantypecode("45-01"); // ��������ͱ��� 45-01
		pih.setAttributeValue("cpprojectid", poOrderBs.get(0).getCprojectid());

		PurchaseInBodyVO[] pib = new PurchaseInBodyVO[poOrderBs.size()];

		// ������ֵ
		for (int i = 0; i < poOrderBs.size(); i++) {

			PurchaseInBodyVO temp = new PurchaseInBodyVO();
			temp.setBassetcard(new UFBoolean(false)); // �������豸��Ƭ
			temp.setBbarcodeclose(new UFBoolean(false)); // �������Ƿ�����ر�
			temp.setBfixedasset(new UFBoolean(false)); // ��ת��
			temp.setBonroadflag(new UFBoolean(false)); // �Ƿ���;
			temp.setBopptaxflag(new UFBoolean(false)); // ������˰��־
			temp.setBorrowinflag(poOrderBs.get(i).getBborrowpur());// ����ת�ɹ�
			temp.setBsourcelargess(poOrderBs.get(i).getBlargess()); // ������Ʒ��
			// temp.setCarriveorder_bbid (""); //��Դ�������ʼ���ϸ����
			temp.setCasscustid(poOrderBs.get(i).getCasscustid());// �ͻ�
			temp.setCastunitid(poOrderBs.get(i).getCastunitid());// ��λ
			temp.setCbodytranstypecode("45-01"); // ���������
			temp.setCbodywarehouseid(poOrderBs.get(i).getPk_recvstordoc());// ���ֿ�
			temp.setCcurrencyid(poOrderBs.get(i).getCcurrencyid());// ��λ��
			temp.setCdestiareaid(poOrderBs.get(i).getCdestiareaid());// Ŀ�ĵ���
			temp.setCdesticountryid(poOrderBs.get(i).getCdesticountryid());// Ŀ�Ĺ�

			temp.setCffileid(poOrderBs.get(i).getCffileid()); // ������
			temp.setVfirstbillcode(poOrders.get(0).getVbillcode()); // Դͷ���ݺ�
			temp.setVfirstrowno(poOrderBs.get(i).getCrowno()); // Դͷ�����к�
			temp.setCfirstbillbid(poOrderBs.get(i).getPk_order_b()); // Դͷ���ݱ�������
			temp.setCfirstbillhid(poOrders.get(0).getPk_order()); // Դͷ���ݱ�ͷ����
			temp.setCfirsttranstype(poOrders.get(0).getCtrantypeid());// Դͷ���ݽ�������
			temp.setCfirsttype("21"); // Դͷ��������
			temp.setCliabilityoid(poOrderBs.get(i).getPk_arrliabcenter());// �����������°汾
			temp.setCliabilityvid(poOrderBs.get(i).getPk_arrliabcenter_v());// ��������

			temp.setCliabilityoid(poOrderBs.get(i).getPk_apliabcenter());// ���������������°汾
			temp.setCliabilityvid(poOrderBs.get(i).getPk_apliabcenter_v());// ������������

			temp.setCioliabilityoid(poOrderBs.get(i).getPk_arrliabcenter());
			temp.setCioliabilityvid(poOrderBs.get(i).getPk_arrliabcenter_v());

			temp.setCmaterialoid(poOrderBs.get(i).getPk_srcmaterial()); // ����
			temp.setCmaterialvid(poOrderBs.get(i).getPk_material()); // ���ϱ���

			temp.setCorder_bb1id(poOrderBs.get(i).getPk_receiveplan()); // Դͷ�ɹ����ݵ����ƻ�

			temp.setCorigareaid(poOrderBs.get(i).getCorigareaid()); // ԭ������
			temp.setCorigcountryid(poOrderBs.get(i).getCorigcountryid()); // ԭ����
			temp.setCorigcurrencyid(poOrders.get(0).getCorigcurrencyid()); // ����
			temp.setCorpoid(poOrders.get(0).getPk_org()); // ��˾���°汾
			temp.setCorpvid(poOrders.get(0).getPk_org_v()); // ��˾

			temp.setCproductorid(poOrderBs.get(i).getCproductorid()); // ��������
			temp.setCprojectid(poOrderBs.get(i).getCprojectid()); // ��Ŀ
			temp.setCprojecttaskid(poOrderBs.get(i).getCprojecttaskid()); // ��Ŀ����
			temp.setCqtunitid(poOrderBs.get(i).getCqtunitid()); // ���۵�λ

			temp.setCreqstoorgoid(poOrderBs.get(0).getPk_reqstoorg());// ��������֯���°汾
			temp.setCreqstoorgvid(poOrderBs.get(0).getPk_reqstoorg_v()); // ��������֯
			temp.setCrowno(i * 10 + 10 + ""); // �к�

			temp.setCsourcebillbid(poOrderBs.get(i).getPk_order_b());
			temp.setCsourcebillhid(poOrders.get(0).getPk_order());
			temp.setCsourcetranstype(poOrders.get(0).getCtrantypeid());
			temp.setCsourcetype("21");
			temp.setVsourcebillcode(poOrders.get(0).getVbillcode()); // ��Դ���ݺ�
			temp.setVsourcerowno(poOrderBs.get(i).getCrowno()); // ��Դ�����к�

			// cgeneralbid.ftaxtypeflag ��ⵥ��������.��˰��� ӳ�� pk_order_b.ftaxtypeflag
			// �ɹ�������ϸ.��˰���
			temp.setCsrcmaterialoid(poOrderBs.get(i).getPk_srcmaterial()); // ��Դ����
			temp.setCsrcmaterialvid(poOrderBs.get(i).getPk_material()); // ��Դ���ϱ���
			temp.setCtaxcodeid(poOrderBs.get(i).getCtaxcodeid()); // ˰��
			temp.setCunitid(poOrderBs.get(i).getCunitid()); // ����λ
			temp.setCvendorid(poOrders.get(0).getPk_supplier());// ��Ӧ��
			temp.setDbizdate(new UFDate(bodys.get(i).get("dbizdate")));// �������
			// temp.setFchecked(null); //�����־
			temp.setFlargess(poOrderBs.get(i).getBborrowpur()); // ��Ʒ
			temp.setFtaxtypeflag(poOrderBs.get(i).getFtaxtypeflag());// ��˰���
			temp.setNassistnum(new UFDouble(Double.parseDouble(bodys.get(i)
					.get("nassistnum")))); // ʵ������
			// ��˰���ۣ�new UFDouble(poOrderBs.get(i).getNqtorigprice())
			// ��������˰����/(1+˰��)*���� poOrderBs.get(i).getNqtorigtaxprice()
			// poOrderBs.get(i).getNtaxrate()
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(2);
			nf.setRoundingMode(RoundingMode.HALF_UP);
			double hsdj = Double.parseDouble(poOrderBs.get(i)
					.getNqtorigtaxprice().toString());
			double d1 = Double.parseDouble("1");
			d1 = d1
					+ Double.parseDouble(poOrderBs.get(i).getNtaxrate()
							.toString()) / 100;
			double mny1 = Double.parseDouble(nf.format(hsdj / d1
					* Double.parseDouble(bodys.get(i).get("nassistnum"))));
			temp.setNcalcostmny(new UFDouble(mny1));// �Ƴɱ����
			temp.setNcaltaxmny(new UFDouble(mny1));// ��˰���

			temp.setNchangestdrate(poOrderBs.get(i).getNexchangerate());// �۱�����
			temp.setNglobalexchgrate(poOrderBs.get(i).getNglobalexchgrate());// ȫ�ֱ�λ�һ���
			temp.setNgroupexchgrate(poOrderBs.get(i).getNgroupexchgrate());// ���ű�λ�һ���
			temp.setNitemdiscountrate(poOrderBs.get(i).getNitemdiscountrate());// �ۿ�

			// ������˰���ۣ�new UFDouble(poOrderBs.get(i).getNqtprice())
			// ������new
			// UFDouble(Double.parseDouble(bodys.get(i).get("nassistnum")))
			// double mny2 =
			// Double.parseDouble(poOrderBs.get(i).getNqtprice().toString())*
			// Double.parseDouble(bodys.get(i).get("nassistnum"));
			temp.setNmny(new UFDouble(mny1)); // ������˰���
			temp.setNnetprice(new UFDouble(poOrderBs.get(i).getNnetprice()));// ��������˰����
			if (poOrderBs.get(i).getNnosubtax() != null) {
				temp.setNnosubtax(new UFDouble(poOrderBs.get(i).getNnosubtax()));// ���ɵֿ�˰��
			}
			if (poOrderBs.get(i).getNnosubtaxrate() != null) {
				temp.setNnosubtaxrate(new UFDouble(poOrderBs.get(i)
						.getNnosubtaxrate()));// ���ɵֿ�˰��
			}
			temp.setNshouldnum(new UFDouble(bodys.get(i)
					.get("nshouldassistnum"))); // Ӧ��������

			temp.setNorigmny(new UFDouble(mny1));// ��˰���
			temp.setNorignetprice(new UFDouble(poOrderBs.get(i)
					.getNorignetprice()));// ����˰����
			temp.setNorigprice(new UFDouble(poOrderBs.get(i).getNorigprice())); // ����˰����

			double d = Double
					.parseDouble(poOrderBs.get(i).getNtax().toString())
					/ Double.parseDouble(poOrderBs.get(i).getNnum().toString())
					* Double.parseDouble(bodys.get(i).get("nassistnum"));
			double ntax = Double.parseDouble(nf.format(d));
			temp.setNtax(new UFDouble(ntax)); // ˰��
			// ��˰���+˰��
			double mny3 = Double.parseDouble(poOrderBs.get(i)
					.getNqtorigtaxprice().toString())
					* Double.parseDouble(bodys.get(i).get("nassistnum"));
			temp.setNorigtaxmny(new UFDouble(mny3)); // ��˰�ϼ�
			temp.setNorigtaxnetprice(new UFDouble(poOrderBs.get(i)
					.getNorigtaxnetprice())); // ����˰����
			temp.setNorigtaxprice(new UFDouble(poOrderBs.get(i)
					.getNorigtaxprice())); // ����˰����

			temp.setNprice(new UFDouble(poOrderBs.get(i).getNprice()));// ��������˰����
			temp.setNqtnetprice(new UFDouble(poOrderBs.get(i).getNqtnetprice())); // ������˰����
			temp.setNqtorignetprice(new UFDouble(poOrderBs.get(i)
					.getNqtorignetprice()));// ��˰����
			temp.setNqtorigprice(new UFDouble(poOrderBs.get(i)
					.getNqtorigprice()));// ��˰����
			temp.setNqtorigtaxnetprice(new UFDouble(poOrderBs.get(i)
					.getNqtorigtaxnetprc()));// ��˰����
			temp.setNqtorigtaxprice(new UFDouble(poOrderBs.get(i)
					.getNqtorigtaxprice())); // ��˰����
			temp.setNqtprice(new UFDouble(poOrderBs.get(i).getNqtprice()));// ������˰����
			temp.setNqttaxnetprice(new UFDouble(poOrderBs.get(i)
					.getNqttaxnetprice()));// ���Һ�˰����
			temp.setNqttaxprice(new UFDouble(poOrderBs.get(i).getNqttaxprice())); // ���Һ�˰����
			temp.setNqtunitnum(new UFDouble(Double.parseDouble(bodys.get(i)
					.get("nassistnum")))); // ��������
			temp.setNshouldassistnum(new UFDouble(bodys.get(i).get(
					"nshouldassistnum"))); // Ӧ������
			temp.setNshouldnum(new UFDouble(bodys.get(i)
					.get("nshouldassistnum"))); // Ӧ��������
			// temp.setNtaxnetprice(new
			// UFDouble(poOrderBs.get(i).getNcaninnum())); //�����Һ�˰����
			temp.setNtaxrate(new UFDouble(poOrderBs.get(i).getNtaxrate())); // ˰��
			temp.setNtaxmny(new UFDouble(mny3)); // ���Ҽ�˰�ϼ�
			temp.setNnum(new UFDouble(Double.parseDouble(bodys.get(i).get(
					"nassistnum"))));
			temp.setNtaxprice(new UFDouble(poOrderBs.get(i).getNtaxprice())); // ������˰����
			temp.setNtaxnetprice(new UFDouble(poOrderBs.get(i)
					.getNtaxnetprice())); // �����Һ�˰����

			temp.setNvolume(new UFDouble(0.00)); // ���
			temp.setNweight(new UFDouble(0.00)); // ����
			temp.setPk_batchcode(poOrderBs.get(i).getPk_batchcode()); // ��������
			temp.setPk_group(pk_group); // ����
			temp.setPk_creqwareid(poOrderBs.get(i).getPk_reqstordoc()); // ����ֿ�
			temp.setPk_org(poOrderBs.get(i).getPk_arrvstoorg()); // �����֯���°汾
			temp.setPk_org_v(poOrderBs.get(i).getPk_arrvstoorg_v()); // �����֯
			temp.setVchangerate("1.00/1.00"); // ������
			temp.setVbatchcode(null); // ���κ�
			// temp.setPk_taxrate(poOrderBs.get(i).getPk_t); //�����֯���°汾
			// cgeneralbid.tsourcebodyts ��ⵥ��������.��Դ����ʱ��� ӳ�� pk_order_b.ts
			// �ɹ�������ϸ.ts
			temp.setVchangerate(poOrderBs.get(i).getVchangerate()); // ������
			// cgeneralbid.vqtunitrate ��ⵥ��������.���ۻ����� ӳ�� pk_order_b.vqtunitrate
			// �ɹ�������ϸ.���ۻ�����
			// temp.setVnotebody(poOrderBs.get(i).getVbmemo());//��ע
			temp.setVqtunitrate(poOrderBs.get(i).getVqtunitrate());// ���ۻ�����
			temp.setAttributeValue("pk_taxrate", "1001A110000000001U5N");
			pib[i] = temp;
		}

		PurchaseInVO[] inVOs = new PurchaseInVO[1];
		PurchaseInVO inVOs2 = new PurchaseInVO();
		inVOs2.setParent(pih);
		inVOs2.setChildrenVO(pib);

		inVOs2.setHasLoadedLocations(false);
		inVOs2.setTempBillPK(null);
		inVOs[0] = inVOs2;
		PurchaseInVO[] vos = ((IPurchaseInMaintain) NCLocator.getInstance()
				.lookup(IPurchaseInMaintain.class)).insert(inVOs);
		return vos;
	}
	/** �ƶ����ϱ��濪ʼ========================================= */

	@SuppressWarnings("unchecked")
	private Map<String, String> getDataByOrg(String org) throws DAOException {

		Map<String, String> map = new HashMap<String, String>();
		StringBuffer buffer = new StringBuffer(
				"SELECT orgs.pk_corp,corp.pk_vid,cost.pk_costregion ");
		buffer.append(" FROM org_orgs orgs");
		buffer.append(" LEFT JOIN org_corp corp ON orgs.pk_corp = corp.pk_corp");
		buffer.append(" LEFT JOIN org_costregion cost ON orgs.pk_org = cost.pk_org");
		buffer.append(" WHERE orgs.pk_org = '" + org + "'");
		List<Map<String, String>> dataList = (List<Map<String, String>>) new BaseDAO()
				.executeQuery(buffer.toString(), new MapListProcessor());

		if (dataList != null) {
			return dataList.get(0);
		}
		return map;

	}

	@SuppressWarnings("unchecked")
	private Map<String, String> getBillType(String billcode)
			throws DAOException {

		Map<String, String> map = new HashMap<String, String>();
		StringBuffer buffer = new StringBuffer(
				"select pk_billtypeid from bd_billtype where pk_billtypecode  = '"
						+ billcode + "' AND billcoderule = '~'");
		List<Map<String, String>> dataList = (List<Map<String, String>>) new BaseDAO()
				.executeQuery(buffer.toString(), new MapListProcessor());

		if (dataList != null) {
			return dataList.get(0);
		}
		return map;

	}

	/**
	 * ����ʵ����
	 */
	@Override
	public AggregatedValueObject saveBill(AggregatedValueObject aggVO,
			String billType) throws BusinessException {

		IplatFormEntry iIplatFormEntry = (IplatFormEntry) NCLocator
				.getInstance().lookup(IplatFormEntry.class.getName());

		AggregatedValueObject[] retObj = (AggregatedValueObject[]) iIplatFormEntry
				.processAction("SAVEBASE", billType, null, aggVO, null, null);
		return retObj[0];
	}

	/**
	 * ����ʵ����
	 */
	@Override
	public AggregatedValueObject[] saveBatch(AggregatedValueObject[] aggVOs,
			String billType) throws BusinessException {

		IplatFormEntry iIplatFormEntry = (IplatFormEntry) NCLocator
				.getInstance().lookup(IplatFormEntry.class.getName());

		AggregatedValueObject[] retObjs = new AggregatedValueObject[aggVOs.length];
		for (int i = 0; i < aggVOs.length; i++) {
			AggregatedValueObject[] retObj = (AggregatedValueObject[]) iIplatFormEntry
					.processAction("SAVEBASE", billType, null, aggVOs[i], null,
							null);
			retObjs[i] = retObj[0];
		}
		return retObjs;
	}

	/**
	 * �ύʵ����
	 */
	@Override
	public AggregatedValueObject submitBill(AggregatedValueObject aggVO,
			String billType) throws BusinessException {
		IplatFormEntry iIplatFormEntry = (IplatFormEntry) NCLocator
				.getInstance().lookup(IplatFormEntry.class.getName());

		AggregatedValueObject[] retObj = (AggregatedValueObject[]) iIplatFormEntry
				.processAction("SAVE", billType, null, aggVO, null, null);

		return retObj[0];
	}

	@Override
	public Object batchSaveBill(AggregatedValueObject[] aggVO, String billType)
			throws BusinessException {
		IplatFormEntry iIplatFormEntry = (IplatFormEntry) NCLocator
				.getInstance().lookup(IplatFormEntry.class.getName());

		Object retObj = iIplatFormEntry.processBatch("SAVEBASE", billType,
				null, aggVO, null, null);
		/*
		 * if (retObj instanceof PfProcessBatchRetObject) { retObj =
		 * ((PfProcessBatchRetObject) retObj).getRetObj(); }
		 * AggregatedValueObject[] a = (AggregatedValueObject[]) retObj; Object
		 * b = iIplatFormEntry.processBatch("SAVE", billType, null, a, null,
		 * null); if (b instanceof PfProcessBatchRetObject) { retObj =
		 * ((PfProcessBatchRetObject) b).getRetObj(); }
		 * iIplatFormEntry.processBatch("APPROVE", billType, null,
		 * (AggregatedValueObject[]) retObj, null, null);
		 */
		return retObj;
	}

	/**
	 * 
	 * ɾ������
	 */
	@Override
	public AggregatedValueObject deleteBill(AggregatedValueObject aggVO,
			String billType) throws BusinessException {

		IplatFormEntry iIplatFormEntry = (IplatFormEntry) NCLocator
				.getInstance().lookup(IplatFormEntry.class.getName());

		AggregatedValueObject[] retObj = (AggregatedValueObject[]) iIplatFormEntry
				.processAction("DELETE", billType, null, aggVO, null, null);
		return retObj[0];
	}

	/**
	 * �ջص���
	 */
	@Override
	public AggregatedValueObject recallBill(AggregatedValueObject aggVO,
			String billType) throws BusinessException {
		IplatFormEntry iIplatFormEntry = (IplatFormEntry) NCLocator
				.getInstance().lookup(IplatFormEntry.class.getName());

		AggregatedValueObject[] retObj = (AggregatedValueObject[]) iIplatFormEntry
				.processAction("UNSAVEBILL", billType, null, aggVO, null, null);
		return retObj[0];
	}
	@Override
	public OrderVO[] xlSaveBill(String userid, String pk_group,
			Map<String, String> head, List<Map<String, String>> bodys)
			throws BusinessException {
		// TODO �Զ����ɵķ������
		return null;
	}

}
