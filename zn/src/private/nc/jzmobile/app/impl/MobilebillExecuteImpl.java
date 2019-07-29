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

	/** 移动收料保存开始========================================= */
	@SuppressWarnings("unchecked")
	@Override
	public PurchaseInVO[] slSaveBill(String userid, String pk_group,
			Map<String, String> head, List<Map<String, String>> bodys)
			throws BusinessException {
		BaseDAO dao = new BaseDAO();
		String pk_order = head.get("pk_order");
		// String[] pk_order_bs = new String[bodys.size()] ;//订单子表主键

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
		/** 填充入库单表头 */
		pih.setApprover(null);// 签字人
		pih.setBillmaker(userid);// 制单人
		pih.setBitinbill(new UFBoolean(false));// 进口入库单
		pih.setBtriatradeflag(poOrderBs.get(0).getBtriatradeflag());// 三角贸易
		pih.setCbizid(head.get("cbizid"));// 采购员
		pih.setCbiztype(poOrders.get(0).getPk_busitype());// 业务流程
		pih.setCcostdomainid(getDataByOrg(poOrderBs.get(0).getPk_org()).get(
				"pk_costregion"));// 结算成本域
		pih.setCcustomerid(poOrders.get(0).getPk_recvcustomer());// 收货客户
		pih.setCdptid(head.get("cdptid"));// 采购部门
		pih.setCdptvid(head.get("cdptvid"));// 采购部门
		pih.setCfanaceorgoid(poOrderBs.get(0).getPk_psfinanceorg());// 结算财务组织
		pih.setCfanaceorgvid(poOrderBs.get(0).getPk_psfinanceorg_v());// 采购订单明细.结算财务组织
		pih.setCorpoid(getDataByOrg(poOrderBs.get(0).getPk_org())
				.get("pk_corp"));// 公司
		pih.setCorpvid(getDataByOrg(poOrderBs.get(0).getPk_org()).get("pk_vid"));// 公司
		pih.setCpayfinorgoid(poOrderBs.get(0).getPk_apfinanceorg());// 应付组织
		pih.setCpayfinorgvid(poOrderBs.get(0).getPk_apfinanceorg_v());// 应付组织
		pih.setCpurorgoid(poOrders.get(0).getPk_org());// 采购组织
		pih.setCpurorgvid(poOrders.get(0).getPk_org_v());// 采购组织
		pih.setCreationtime(new UFDateTime());// 创建时间
		pih.setCreator(userid);// 创建人
		pih.setCrececountryid(poOrderBs.get(0).getCrececountryid());// 收货国家
		pih.setCsendcountryid(poOrderBs.get(0).getCsendcountryid());// 发货国家
		pih.setCsendtypeid(poOrders.get(0).getPk_transporttype());// 运输方式
		pih.setCtaxcountryid(poOrderBs.get(0).getCtaxcountryid());// 报税国家
		pih.setCtradewordid(poOrders.get(0).getCtradewordid());// 贸易术语
		// pih.setCtrantypeid("0001D1100000000026U8");// 出入库类型
		pih.setCtrantypeid(getBillType("45-01").get("pk_billtypeid"));// 出入库类型
		pih.setCvendorid(poOrders.get(0).getPk_supplier());// 供应商
		pih.setCwarehouseid(head.get("cwarehouseid"));// 仓库
		pih.setCwhsmanagerid(head.get("cwhsmanagerid"));// 库管员
		pih.setDbilldate(new UFDate(head.get("dbilldate")));// 单据日期
		pih.setDmakedate(new UFDate());// 制单日期
		pih.setFbillflag(2);// 单据状态
		pih.setFbuysellflag(poOrderBs.get(0).getFbuysellflag());// 购销类型
		pih.setFreplenishflag(poOrders.get(0).getBreturn());// 采购退库
		pih.setIprintcount(0);// 打印次数
		// pih.setModifiedtime(null);//最后修改时间
		// pih.setModifier(null); //最后修改人
		pih.setNtotalnum(new UFDouble(head.get("ntotalnum")));// 总数量
		pih.setNtotalpiece(new UFDouble(0));// 总件数
		pih.setNtotalvolume(new UFDouble(0));// 总体积
		pih.setNtotalweight(new UFDouble(0));// 总重量
		pih.setPk_group(pk_group);// 集团
		// pih.setPk_measware(null);//计量器具
		pih.setPk_org(poOrders.get(0).getPk_org()); // 库存组织最新版本
		pih.setPk_org_v(poOrders.get(0).getPk_org_v()); // 库存组织
		// pih.setTaudittime(null); //签字日期
		// pih.setVbillcode(null); //单据号
		pih.setVdef1(poOrders.get(0).getVdef1());
		pih.setVdef2(poOrders.get(0).getVdef2());
		pih.setVdef3(head.get("vdef3"));
		pih.setVdef4(head.get("vdef4"));
		pih.setVdef5(head.get("vdef5"));
		pih.setVnote(head.get("vnote")); // 备注
		pih.setVreturnreason(null);// 退库理由
		pih.setVtrantypecode("45-01"); // 出入库类型编码 45-01
		pih.setAttributeValue("cpprojectid", poOrderBs.get(0).getCprojectid());

		PurchaseInBodyVO[] pib = new PurchaseInBodyVO[poOrderBs.size()];

		// 表体塞值
		for (int i = 0; i < poOrderBs.size(); i++) {

			PurchaseInBodyVO temp = new PurchaseInBodyVO();
			temp.setBassetcard(new UFBoolean(false)); // 已生成设备卡片
			temp.setBbarcodeclose(new UFBoolean(false)); // 单据行是否条码关闭
			temp.setBfixedasset(new UFBoolean(false)); // 已转固
			temp.setBonroadflag(new UFBoolean(false)); // 是否在途
			temp.setBopptaxflag(new UFBoolean(false)); // 逆向征税标志
			temp.setBorrowinflag(poOrderBs.get(i).getBborrowpur());// 借入转采购
			temp.setBsourcelargess(poOrderBs.get(i).getBlargess()); // 上游赠品行
			// temp.setCarriveorder_bbid (""); //来源到货单质检明细主键
			temp.setCasscustid(poOrderBs.get(i).getCasscustid());// 客户
			temp.setCastunitid(poOrderBs.get(i).getCastunitid());// 单位
			temp.setCbodytranstypecode("45-01"); // 出入库类型
			temp.setCbodywarehouseid(poOrderBs.get(i).getPk_recvstordoc());// 库存仓库
			temp.setCcurrencyid(poOrderBs.get(i).getCcurrencyid());// 本位币
			temp.setCdestiareaid(poOrderBs.get(i).getCdestiareaid());// 目的地区
			temp.setCdesticountryid(poOrderBs.get(i).getCdesticountryid());// 目的国

			temp.setCffileid(poOrderBs.get(i).getCffileid()); // 特征码
			temp.setVfirstbillcode(poOrders.get(0).getVbillcode()); // 源头单据号
			temp.setVfirstrowno(poOrderBs.get(i).getCrowno()); // 源头单据行号
			temp.setCfirstbillbid(poOrderBs.get(i).getPk_order_b()); // 源头单据表体主键
			temp.setCfirstbillhid(poOrders.get(0).getPk_order()); // 源头单据表头主键
			temp.setCfirsttranstype(poOrders.get(0).getCtrantypeid());// 源头单据交易类型
			temp.setCfirsttype("21"); // 源头单据类型
			temp.setCliabilityoid(poOrderBs.get(i).getPk_arrliabcenter());// 利润中心最新版本
			temp.setCliabilityvid(poOrderBs.get(i).getPk_arrliabcenter_v());// 利润中心

			temp.setCliabilityoid(poOrderBs.get(i).getPk_apliabcenter());// 结算利润中心最新版本
			temp.setCliabilityvid(poOrderBs.get(i).getPk_apliabcenter_v());// 结算利润中心

			temp.setCioliabilityoid(poOrderBs.get(i).getPk_arrliabcenter());
			temp.setCioliabilityvid(poOrderBs.get(i).getPk_arrliabcenter_v());

			temp.setCmaterialoid(poOrderBs.get(i).getPk_srcmaterial()); // 物料
			temp.setCmaterialvid(poOrderBs.get(i).getPk_material()); // 物料编码

			temp.setCorder_bb1id(poOrderBs.get(i).getPk_receiveplan()); // 源头采购单据到货计划

			temp.setCorigareaid(poOrderBs.get(i).getCorigareaid()); // 原产地区
			temp.setCorigcountryid(poOrderBs.get(i).getCorigcountryid()); // 原产国
			temp.setCorigcurrencyid(poOrders.get(0).getCorigcurrencyid()); // 币种
			temp.setCorpoid(poOrders.get(0).getPk_org()); // 公司最新版本
			temp.setCorpvid(poOrders.get(0).getPk_org_v()); // 公司

			temp.setCproductorid(poOrderBs.get(i).getCproductorid()); // 生产厂商
			temp.setCprojectid(poOrderBs.get(i).getCprojectid()); // 项目
			temp.setCprojecttaskid(poOrderBs.get(i).getCprojecttaskid()); // 项目任务
			temp.setCqtunitid(poOrderBs.get(i).getCqtunitid()); // 报价单位

			temp.setCreqstoorgoid(poOrderBs.get(0).getPk_reqstoorg());// 需求库存组织最新版本
			temp.setCreqstoorgvid(poOrderBs.get(0).getPk_reqstoorg_v()); // 需求库存组织
			temp.setCrowno(i * 10 + 10 + ""); // 行号

			temp.setCsourcebillbid(poOrderBs.get(i).getPk_order_b());
			temp.setCsourcebillhid(poOrders.get(0).getPk_order());
			temp.setCsourcetranstype(poOrders.get(0).getCtrantypeid());
			temp.setCsourcetype("21");
			temp.setVsourcebillcode(poOrders.get(0).getVbillcode()); // 来源单据号
			temp.setVsourcerowno(poOrderBs.get(i).getCrowno()); // 来源单据行号

			// cgeneralbid.ftaxtypeflag 入库单表体主键.扣税类别 映射 pk_order_b.ftaxtypeflag
			// 采购订单明细.扣税类别
			temp.setCsrcmaterialoid(poOrderBs.get(i).getPk_srcmaterial()); // 来源物料
			temp.setCsrcmaterialvid(poOrderBs.get(i).getPk_material()); // 来源物料编码
			temp.setCtaxcodeid(poOrderBs.get(i).getCtaxcodeid()); // 税码
			temp.setCunitid(poOrderBs.get(i).getCunitid()); // 主单位
			temp.setCvendorid(poOrders.get(0).getPk_supplier());// 供应商
			temp.setDbizdate(new UFDate(bodys.get(i).get("dbizdate")));// 入库日期
			// temp.setFchecked(null); //待捡标志
			temp.setFlargess(poOrderBs.get(i).getBborrowpur()); // 赠品
			temp.setFtaxtypeflag(poOrderBs.get(i).getFtaxtypeflag());// 扣税类别
			temp.setNassistnum(new UFDouble(Double.parseDouble(bodys.get(i)
					.get("nassistnum")))); // 实收数量
			// 无税单价：new UFDouble(poOrderBs.get(i).getNqtorigprice())
			// 数量：含税单价/(1+税率)*数量 poOrderBs.get(i).getNqtorigtaxprice()
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
			temp.setNcalcostmny(new UFDouble(mny1));// 计成本金额
			temp.setNcaltaxmny(new UFDouble(mny1));// 计税金额

			temp.setNchangestdrate(poOrderBs.get(i).getNexchangerate());// 折本汇率
			temp.setNglobalexchgrate(poOrderBs.get(i).getNglobalexchgrate());// 全局本位币汇率
			temp.setNgroupexchgrate(poOrderBs.get(i).getNgroupexchgrate());// 集团本位币汇率
			temp.setNitemdiscountrate(poOrderBs.get(i).getNitemdiscountrate());// 折扣

			// 本币无税单价：new UFDouble(poOrderBs.get(i).getNqtprice())
			// 数量：new
			// UFDouble(Double.parseDouble(bodys.get(i).get("nassistnum")))
			// double mny2 =
			// Double.parseDouble(poOrderBs.get(i).getNqtprice().toString())*
			// Double.parseDouble(bodys.get(i).get("nassistnum"));
			temp.setNmny(new UFDouble(mny1)); // 本币无税金额
			temp.setNnetprice(new UFDouble(poOrderBs.get(i).getNnetprice()));// 主本币无税净价
			if (poOrderBs.get(i).getNnosubtax() != null) {
				temp.setNnosubtax(new UFDouble(poOrderBs.get(i).getNnosubtax()));// 不可抵扣税额
			}
			if (poOrderBs.get(i).getNnosubtaxrate() != null) {
				temp.setNnosubtaxrate(new UFDouble(poOrderBs.get(i)
						.getNnosubtaxrate()));// 不可抵扣税率
			}
			temp.setNshouldnum(new UFDouble(bodys.get(i)
					.get("nshouldassistnum"))); // 应收主数量

			temp.setNorigmny(new UFDouble(mny1));// 无税金额
			temp.setNorignetprice(new UFDouble(poOrderBs.get(i)
					.getNorignetprice()));// 主无税净价
			temp.setNorigprice(new UFDouble(poOrderBs.get(i).getNorigprice())); // 主无税单价

			double d = Double
					.parseDouble(poOrderBs.get(i).getNtax().toString())
					/ Double.parseDouble(poOrderBs.get(i).getNnum().toString())
					* Double.parseDouble(bodys.get(i).get("nassistnum"));
			double ntax = Double.parseDouble(nf.format(d));
			temp.setNtax(new UFDouble(ntax)); // 税额
			// 无税金额+税额
			double mny3 = Double.parseDouble(poOrderBs.get(i)
					.getNqtorigtaxprice().toString())
					* Double.parseDouble(bodys.get(i).get("nassistnum"));
			temp.setNorigtaxmny(new UFDouble(mny3)); // 价税合计
			temp.setNorigtaxnetprice(new UFDouble(poOrderBs.get(i)
					.getNorigtaxnetprice())); // 主含税净价
			temp.setNorigtaxprice(new UFDouble(poOrderBs.get(i)
					.getNorigtaxprice())); // 主含税单价

			temp.setNprice(new UFDouble(poOrderBs.get(i).getNprice()));// 主本币无税净价
			temp.setNqtnetprice(new UFDouble(poOrderBs.get(i).getNqtnetprice())); // 本币无税净价
			temp.setNqtorignetprice(new UFDouble(poOrderBs.get(i)
					.getNqtorignetprice()));// 无税净价
			temp.setNqtorigprice(new UFDouble(poOrderBs.get(i)
					.getNqtorigprice()));// 无税单价
			temp.setNqtorigtaxnetprice(new UFDouble(poOrderBs.get(i)
					.getNqtorigtaxnetprc()));// 含税净价
			temp.setNqtorigtaxprice(new UFDouble(poOrderBs.get(i)
					.getNqtorigtaxprice())); // 含税单价
			temp.setNqtprice(new UFDouble(poOrderBs.get(i).getNqtprice()));// 本币无税单价
			temp.setNqttaxnetprice(new UFDouble(poOrderBs.get(i)
					.getNqttaxnetprice()));// 本币含税净价
			temp.setNqttaxprice(new UFDouble(poOrderBs.get(i).getNqttaxprice())); // 本币含税单价
			temp.setNqtunitnum(new UFDouble(Double.parseDouble(bodys.get(i)
					.get("nassistnum")))); // 报价数量
			temp.setNshouldassistnum(new UFDouble(bodys.get(i).get(
					"nshouldassistnum"))); // 应收数量
			temp.setNshouldnum(new UFDouble(bodys.get(i)
					.get("nshouldassistnum"))); // 应收主数量
			// temp.setNtaxnetprice(new
			// UFDouble(poOrderBs.get(i).getNcaninnum())); //主本币含税净价
			temp.setNtaxrate(new UFDouble(poOrderBs.get(i).getNtaxrate())); // 税率
			temp.setNtaxmny(new UFDouble(mny3)); // 本币价税合计
			temp.setNnum(new UFDouble(Double.parseDouble(bodys.get(i).get(
					"nassistnum"))));
			temp.setNtaxprice(new UFDouble(poOrderBs.get(i).getNtaxprice())); // 主本币税单价
			temp.setNtaxnetprice(new UFDouble(poOrderBs.get(i)
					.getNtaxnetprice())); // 主本币含税净价

			temp.setNvolume(new UFDouble(0.00)); // 体积
			temp.setNweight(new UFDouble(0.00)); // 重量
			temp.setPk_batchcode(poOrderBs.get(i).getPk_batchcode()); // 批次主键
			temp.setPk_group(pk_group); // 集团
			temp.setPk_creqwareid(poOrderBs.get(i).getPk_reqstordoc()); // 需求仓库
			temp.setPk_org(poOrderBs.get(i).getPk_arrvstoorg()); // 库存组织最新版本
			temp.setPk_org_v(poOrderBs.get(i).getPk_arrvstoorg_v()); // 库存组织
			temp.setVchangerate("1.00/1.00"); // 换算率
			temp.setVbatchcode(null); // 批次号
			// temp.setPk_taxrate(poOrderBs.get(i).getPk_t); //库存组织最新版本
			// cgeneralbid.tsourcebodyts 入库单表体主键.来源表体时间戳 映射 pk_order_b.ts
			// 采购订单明细.ts
			temp.setVchangerate(poOrderBs.get(i).getVchangerate()); // 换算率
			// cgeneralbid.vqtunitrate 入库单表体主键.报价换算率 映射 pk_order_b.vqtunitrate
			// 采购订单明细.报价换算率
			// temp.setVnotebody(poOrderBs.get(i).getVbmemo());//备注
			temp.setVqtunitrate(poOrderBs.get(i).getVqtunitrate());// 报价换算率
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
	/** 移动收料保存开始========================================= */

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
	 * 保存实现类
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
	 * 保存实现类
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
	 * 提交实现类
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
	 * 删除单据
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
	 * 收回单据
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
		// TODO 自动生成的方法存根
		return null;
	}

}
