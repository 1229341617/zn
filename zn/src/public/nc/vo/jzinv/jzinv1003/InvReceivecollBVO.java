package nc.vo.jzinv.jzinv1003;

import nc.vo.pub.IVOMeta;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pubapp.pattern.model.meta.entity.vo.VOMetaFactory;

public class InvReceivecollBVO extends SuperVO {
	private static final long serialVersionUID = 1L;
	/**
	 * 行号
	 */
	public static final String CROWNO = "crowno";
	/**
	 * 业务类型
	 */
	public static final String IBUSITYPE = "ibusitype";
	/**
	 * 税收优惠
	 */
	public static final String ITAXFAVOURABLE = "itaxfavourable";
	/**
	 * 发票金额（全局本币）
	 */
	public static final String NGLOBALINVMNY = "nglobalinvmny";
	/**
	 * 发票金额（全局本币）（含税）
	 */
	public static final String NGLOBALINVTAXMNY = "nglobalinvtaxmny";
	/**
	 * 单价（全局本币）
	 */
	public static final String NGLOBALPRICE = "nglobalprice";
	/**
	 * 税额（全局本币）
	 */
	public static final String NGLOBALTAXMNY = "nglobaltaxmny";
	/**
	 * 单价（全局）（含税）
	 */
	public static final String NGLOBALTAXPRICE = "nglobaltaxprice";
	/**
	 * 发票金额（集团本币）
	 */
	public static final String NGROUPINVMNY = "ngroupinvmny";
	/**
	 * 发票金额（集团本币）（含税）
	 */
	public static final String NGROUPINVTAXMNY = "ngroupinvtaxmny";
	/**
	 * 单价（集团本币）
	 */
	public static final String NGROUPPRICE = "ngroupprice";
	/**
	 * 税额（集团本币）
	 */
	public static final String NGROUPTAXMNY = "ngrouptaxmny";
	/**
	 * 单价（集团本币）（含税）
	 */
	public static final String NGROUPTAXPRICE = "ngrouptaxprice";
	/**
	 * 发票金额
	 */
	public static final String NINVMNY = "ninvmny";
	/**
	 * 发票金额（含税）
	 */
	public static final String NINVTAXMNY = "ninvtaxmny";
	/**
	 * 数量
	 */
	public static final String NNUM = "nnum";
	/**
	 * 发票金额（原币）
	 */
	public static final String NORIGINVMNY = "noriginvmny";
	/**
	 * 发票金额（原币）（含税）
	 */
	public static final String NORIGINVTAXMNY = "noriginvtaxmny";
	/**
	 * 单价(原币）
	 */
	public static final String NORIGPRICE = "norigprice";
	/**
	 * 税额（原币）
	 */
	public static final String NORIGTAXMNY = "norigtaxmny";
	/**
	 * 单价（原币）（含税）
	 */
	public static final String NORIGTAXPRICE = "norigtaxprice";
	/**
	 * 单价
	 */
	public static final String NPRICE = "nprice";
	/**
	 * 税额
	 */
	public static final String NTAXMNY = "ntaxmny";
	/**
	 * 单价（含税）
	 */
	public static final String NTAXPRICE = "ntaxprice";
	/**
	 * 税率(%)
	 */
	public static final String NTAXRATE = "ntaxrate";
	/**
	 * 进项转出原因
	 */
	public static final String PK_INTRANREASON = "pk_intranreason";
	/**
	 * 上层单据主键
	 */
	public static final String PK_RECEIVE_COLL = "pk_receive_coll";
	/**
	 * 纸质发票内容主表
	 */
	public static final String PK_RECEIVE_COLL_B = "pk_receive_coll_b";
	/**
	 * 应税项目
	 */
	public static final String PK_TAXDEDUCTLIST = "pk_taxdeductlist";
	/**
	 * 时间戳
	 */
	public static final String TS = "ts";
	/**
	 * 自定义项1
	 */
	public static final String VDEF1 = "vdef1";
	/**
	 * 自定义项10
	 */
	public static final String VDEF10 = "vdef10";
	/**
	 * 自定义项11
	 */
	public static final String VDEF11 = "vdef11";
	/**
	 * 自定义项12
	 */
	public static final String VDEF12 = "vdef12";
	/**
	 * 自定义项13
	 */
	public static final String VDEF13 = "vdef13";
	/**
	 * 自定义项14
	 */
	public static final String VDEF14 = "vdef14";
	/**
	 * 自定义项15
	 */
	public static final String VDEF15 = "vdef15";
	/**
	 * 自定义项16
	 */
	public static final String VDEF16 = "vdef16";
	/**
	 * 自定义项17
	 */
	public static final String VDEF17 = "vdef17";
	/**
	 * 自定义项18
	 */
	public static final String VDEF18 = "vdef18";
	/**
	 * 自定义项19
	 */
	public static final String VDEF19 = "vdef19";
	/**
	 * 自定义项2
	 */
	public static final String VDEF2 = "vdef2";
	/**
	 * 自定义项20
	 */
	public static final String VDEF20 = "vdef20";
	/**
	 * 自定义项3
	 */
	public static final String VDEF3 = "vdef3";
	/**
	 * 自定义项4
	 */
	public static final String VDEF4 = "vdef4";
	/**
	 * 自定义项5
	 */
	public static final String VDEF5 = "vdef5";
	/**
	 * 自定义项6
	 */
	public static final String VDEF6 = "vdef6";
	/**
	 * 自定义项7
	 */
	public static final String VDEF7 = "vdef7";
	/**
	 * 自定义项8
	 */
	public static final String VDEF8 = "vdef8";
	/**
	 * 自定义项9
	 */
	public static final String VDEF9 = "vdef9";
	/**
	 * 规格
	 */
	public static final String VINVSPEC = "vinvspec";
	/**
	 * 型号
	 */
	public static final String VINVTYPE = "vinvtype";
	/**
	 * 单位
	 */
	public static final String VINVUNIT = "vinvunit";
	/**
	 * 备注
	 */
	public static final String VMEMO = "vmemo";
	/**
	 * 发票内容
	 */
	public static final String VOPENCONTENT = "vopencontent";
	/**
	 * 预留字段1
	 */
	public static final String VRESERVE1 = "vreserve1";
	/**
	 * 预留字段10
	 */
	public static final String VRESERVE10 = "vreserve10";
	/**
	 * 预留字段2
	 */
	public static final String VRESERVE2 = "vreserve2";
	/**
	 * 预留字段3
	 */
	public static final String VRESERVE3 = "vreserve3";
	/**
	 * 预留字段4
	 */
	public static final String VRESERVE4 = "vreserve4";
	/**
	 * 预留字段5
	 */
	public static final String VRESERVE5 = "vreserve5";
	/**
	 * 预留字段6
	 */
	public static final String VRESERVE6 = "vreserve6";
	/**
	 * 预留字段7
	 */
	public static final String VRESERVE7 = "vreserve7";
	/**
	 * 预留字段8
	 */
	public static final String VRESERVE8 = "vreserve8";
	/**
	 * 预留字段9
	 */
	public static final String VRESERVE9 = "vreserve9";

	/**
	 * 获取行号
	 * 
	 * @return 行号
	 */
	public String getCrowno() {
		return (String) this.getAttributeValue(InvReceivecollBVO.CROWNO);
	}

	/**
	 * 设置行号
	 * 
	 * @param crowno
	 *            行号
	 */
	public void setCrowno(String crowno) {
		this.setAttributeValue(InvReceivecollBVO.CROWNO, crowno);
	}

	/**
	 * 获取业务类型
	 * 
	 * @return 业务类型
	 */
	public Integer getIbusitype() {
		return (Integer) this.getAttributeValue(InvReceivecollBVO.IBUSITYPE);
	}

	/**
	 * 设置业务类型
	 * 
	 * @param ibusitype
	 *            业务类型
	 */
	public void setIbusitype(Integer ibusitype) {
		this.setAttributeValue(InvReceivecollBVO.IBUSITYPE, ibusitype);
	}

	/**
	 * 获取税收优惠
	 * 
	 * @return 税收优惠
	 */
	public Integer getItaxfavourable() {
		return (Integer) this
				.getAttributeValue(InvReceivecollBVO.ITAXFAVOURABLE);
	}

	/**
	 * 设置税收优惠
	 * 
	 * @param itaxfavourable
	 *            税收优惠
	 */
	public void setItaxfavourable(Integer itaxfavourable) {
		this.setAttributeValue(InvReceivecollBVO.ITAXFAVOURABLE, itaxfavourable);
	}

	/**
	 * 获取发票金额（全局本币）
	 * 
	 * @return 发票金额（全局本币）
	 */
	public UFDouble getNglobalinvmny() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGLOBALINVMNY);
	}

	/**
	 * 设置发票金额（全局本币）
	 * 
	 * @param nglobalinvmny
	 *            发票金额（全局本币）
	 */
	public void setNglobalinvmny(UFDouble nglobalinvmny) {
		this.setAttributeValue(InvReceivecollBVO.NGLOBALINVMNY, nglobalinvmny);
	}

	/**
	 * 获取发票金额（全局本币）（含税）
	 * 
	 * @return 发票金额（全局本币）（含税）
	 */
	public UFDouble getNglobalinvtaxmny() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGLOBALINVTAXMNY);
	}

	/**
	 * 设置发票金额（全局本币）（含税）
	 * 
	 * @param nglobalinvtaxmny
	 *            发票金额（全局本币）（含税）
	 */
	public void setNglobalinvtaxmny(UFDouble nglobalinvtaxmny) {
		this.setAttributeValue(InvReceivecollBVO.NGLOBALINVTAXMNY,
				nglobalinvtaxmny);
	}

	/**
	 * 获取单价（全局本币）
	 * 
	 * @return 单价（全局本币）
	 */
	public UFDouble getNglobalprice() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGLOBALPRICE);
	}

	/**
	 * 设置单价（全局本币）
	 * 
	 * @param nglobalprice
	 *            单价（全局本币）
	 */
	public void setNglobalprice(UFDouble nglobalprice) {
		this.setAttributeValue(InvReceivecollBVO.NGLOBALPRICE, nglobalprice);
	}

	/**
	 * 获取税额（全局本币）
	 * 
	 * @return 税额（全局本币）
	 */
	public UFDouble getNglobaltaxmny() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGLOBALTAXMNY);
	}

	/**
	 * 设置税额（全局本币）
	 * 
	 * @param nglobaltaxmny
	 *            税额（全局本币）
	 */
	public void setNglobaltaxmny(UFDouble nglobaltaxmny) {
		this.setAttributeValue(InvReceivecollBVO.NGLOBALTAXMNY, nglobaltaxmny);
	}

	/**
	 * 获取单价（全局）（含税）
	 * 
	 * @return 单价（全局）（含税）
	 */
	public UFDouble getNglobaltaxprice() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGLOBALTAXPRICE);
	}

	/**
	 * 设置单价（全局）（含税）
	 * 
	 * @param nglobaltaxprice
	 *            单价（全局）（含税）
	 */
	public void setNglobaltaxprice(UFDouble nglobaltaxprice) {
		this.setAttributeValue(InvReceivecollBVO.NGLOBALTAXPRICE,
				nglobaltaxprice);
	}

	/**
	 * 获取发票金额（集团本币）
	 * 
	 * @return 发票金额（集团本币）
	 */
	public UFDouble getNgroupinvmny() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGROUPINVMNY);
	}

	/**
	 * 设置发票金额（集团本币）
	 * 
	 * @param ngroupinvmny
	 *            发票金额（集团本币）
	 */
	public void setNgroupinvmny(UFDouble ngroupinvmny) {
		this.setAttributeValue(InvReceivecollBVO.NGROUPINVMNY, ngroupinvmny);
	}

	/**
	 * 获取发票金额（集团本币）（含税）
	 * 
	 * @return 发票金额（集团本币）（含税）
	 */
	public UFDouble getNgroupinvtaxmny() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGROUPINVTAXMNY);
	}

	/**
	 * 设置发票金额（集团本币）（含税）
	 * 
	 * @param ngroupinvtaxmny
	 *            发票金额（集团本币）（含税）
	 */
	public void setNgroupinvtaxmny(UFDouble ngroupinvtaxmny) {
		this.setAttributeValue(InvReceivecollBVO.NGROUPINVTAXMNY,
				ngroupinvtaxmny);
	}

	/**
	 * 获取单价（集团本币）
	 * 
	 * @return 单价（集团本币）
	 */
	public UFDouble getNgroupprice() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NGROUPPRICE);
	}

	/**
	 * 设置单价（集团本币）
	 * 
	 * @param ngroupprice
	 *            单价（集团本币）
	 */
	public void setNgroupprice(UFDouble ngroupprice) {
		this.setAttributeValue(InvReceivecollBVO.NGROUPPRICE, ngroupprice);
	}

	/**
	 * 获取税额（集团本币）
	 * 
	 * @return 税额（集团本币）
	 */
	public UFDouble getNgrouptaxmny() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGROUPTAXMNY);
	}

	/**
	 * 设置税额（集团本币）
	 * 
	 * @param ngrouptaxmny
	 *            税额（集团本币）
	 */
	public void setNgrouptaxmny(UFDouble ngrouptaxmny) {
		this.setAttributeValue(InvReceivecollBVO.NGROUPTAXMNY, ngrouptaxmny);
	}

	/**
	 * 获取单价（集团本币）（含税）
	 * 
	 * @return 单价（集团本币）（含税）
	 */
	public UFDouble getNgrouptaxprice() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NGROUPTAXPRICE);
	}

	/**
	 * 设置单价（集团本币）（含税）
	 * 
	 * @param ngrouptaxprice
	 *            单价（集团本币）（含税）
	 */
	public void setNgrouptaxprice(UFDouble ngrouptaxprice) {
		this.setAttributeValue(InvReceivecollBVO.NGROUPTAXPRICE, ngrouptaxprice);
	}

	/**
	 * 获取发票金额
	 * 
	 * @return 发票金额
	 */
	public UFDouble getNinvmny() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NINVMNY);
	}

	/**
	 * 设置发票金额
	 * 
	 * @param ninvmny
	 *            发票金额
	 */
	public void setNinvmny(UFDouble ninvmny) {
		this.setAttributeValue(InvReceivecollBVO.NINVMNY, ninvmny);
	}

	/**
	 * 获取发票金额（含税）
	 * 
	 * @return 发票金额（含税）
	 */
	public UFDouble getNinvtaxmny() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NINVTAXMNY);
	}

	/**
	 * 设置发票金额（含税）
	 * 
	 * @param ninvtaxmny
	 *            发票金额（含税）
	 */
	public void setNinvtaxmny(UFDouble ninvtaxmny) {
		this.setAttributeValue(InvReceivecollBVO.NINVTAXMNY, ninvtaxmny);
	}

	/**
	 * 获取数量
	 * 
	 * @return 数量
	 */
	public Integer getNnum() {
		return (Integer) this.getAttributeValue(InvReceivecollBVO.NNUM);
	}

	/**
	 * 设置数量
	 * 
	 * @param nnum
	 *            数量
	 */
	public void setNnum(Integer nnum) {
		this.setAttributeValue(InvReceivecollBVO.NNUM, nnum);
	}

	/**
	 * 获取发票金额（原币）
	 * 
	 * @return 发票金额（原币）
	 */
	public UFDouble getNoriginvmny() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NORIGINVMNY);
	}

	/**
	 * 设置发票金额（原币）
	 * 
	 * @param noriginvmny
	 *            发票金额（原币）
	 */
	public void setNoriginvmny(UFDouble noriginvmny) {
		this.setAttributeValue(InvReceivecollBVO.NORIGINVMNY, noriginvmny);
	}

	/**
	 * 获取发票金额（原币）（含税）
	 * 
	 * @return 发票金额（原币）（含税）
	 */
	public UFDouble getNoriginvtaxmny() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NORIGINVTAXMNY);
	}

	/**
	 * 设置发票金额（原币）（含税）
	 * 
	 * @param noriginvtaxmny
	 *            发票金额（原币）（含税）
	 */
	public void setNoriginvtaxmny(UFDouble noriginvtaxmny) {
		this.setAttributeValue(InvReceivecollBVO.NORIGINVTAXMNY, noriginvtaxmny);
	}

	/**
	 * 获取单价(原币）
	 * 
	 * @return 单价(原币）
	 */
	public UFDouble getNorigprice() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NORIGPRICE);
	}

	/**
	 * 设置单价(原币）
	 * 
	 * @param norigprice
	 *            单价(原币）
	 */
	public void setNorigprice(UFDouble norigprice) {
		this.setAttributeValue(InvReceivecollBVO.NORIGPRICE, norigprice);
	}

	/**
	 * 获取税额（原币）
	 * 
	 * @return 税额（原币）
	 */
	public UFDouble getNorigtaxmny() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NORIGTAXMNY);
	}

	/**
	 * 设置税额（原币）
	 * 
	 * @param norigtaxmny
	 *            税额（原币）
	 */
	public void setNorigtaxmny(UFDouble norigtaxmny) {
		this.setAttributeValue(InvReceivecollBVO.NORIGTAXMNY, norigtaxmny);
	}

	/**
	 * 获取单价（原币）（含税）
	 * 
	 * @return 单价（原币）（含税）
	 */
	public UFDouble getNorigtaxprice() {
		return (UFDouble) this
				.getAttributeValue(InvReceivecollBVO.NORIGTAXPRICE);
	}

	/**
	 * 设置单价（原币）（含税）
	 * 
	 * @param norigtaxprice
	 *            单价（原币）（含税）
	 */
	public void setNorigtaxprice(UFDouble norigtaxprice) {
		this.setAttributeValue(InvReceivecollBVO.NORIGTAXPRICE, norigtaxprice);
	}

	/**
	 * 获取单价
	 * 
	 * @return 单价
	 */
	public UFDouble getNprice() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NPRICE);
	}

	/**
	 * 设置单价
	 * 
	 * @param nprice
	 *            单价
	 */
	public void setNprice(UFDouble nprice) {
		this.setAttributeValue(InvReceivecollBVO.NPRICE, nprice);
	}

	/**
	 * 获取税额
	 * 
	 * @return 税额
	 */
	public UFDouble getNtaxmny() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NTAXMNY);
	}

	/**
	 * 设置税额
	 * 
	 * @param ntaxmny
	 *            税额
	 */
	public void setNtaxmny(UFDouble ntaxmny) {
		this.setAttributeValue(InvReceivecollBVO.NTAXMNY, ntaxmny);
	}

	/**
	 * 获取单价（含税）
	 * 
	 * @return 单价（含税）
	 */
	public UFDouble getNtaxprice() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NTAXPRICE);
	}

	/**
	 * 设置单价（含税）
	 * 
	 * @param ntaxprice
	 *            单价（含税）
	 */
	public void setNtaxprice(UFDouble ntaxprice) {
		this.setAttributeValue(InvReceivecollBVO.NTAXPRICE, ntaxprice);
	}

	/**
	 * 获取税率(%)
	 * 
	 * @return 税率(%)
	 */
	public UFDouble getNtaxrate() {
		return (UFDouble) this.getAttributeValue(InvReceivecollBVO.NTAXRATE);
	}

	/**
	 * 设置税率(%)
	 * 
	 * @param ntaxrate
	 *            税率(%)
	 */
	public void setNtaxrate(UFDouble ntaxrate) {
		this.setAttributeValue(InvReceivecollBVO.NTAXRATE, ntaxrate);
	}

	/**
	 * 获取进项转出原因
	 * 
	 * @return 进项转出原因
	 */
	public String getPk_intranreason() {
		return (String) this
				.getAttributeValue(InvReceivecollBVO.PK_INTRANREASON);
	}

	/**
	 * 设置进项转出原因
	 * 
	 * @param pk_intranreason
	 *            进项转出原因
	 */
	public void setPk_intranreason(String pk_intranreason) {
		this.setAttributeValue(InvReceivecollBVO.PK_INTRANREASON,
				pk_intranreason);
	}

	/**
	 * 获取上层单据主键
	 * 
	 * @return 上层单据主键
	 */
	public String getPk_receive_coll() {
		return (String) this
				.getAttributeValue(InvReceivecollBVO.PK_RECEIVE_COLL);
	}

	/**
	 * 设置上层单据主键
	 * 
	 * @param pk_receive_coll
	 *            上层单据主键
	 */
	public void setPk_receive_coll(String pk_receive_coll) {
		this.setAttributeValue(InvReceivecollBVO.PK_RECEIVE_COLL,
				pk_receive_coll);
	}

	/**
	 * 获取纸质发票内容主表
	 * 
	 * @return 纸质发票内容主表
	 */
	public String getPk_receive_coll_b() {
		return (String) this
				.getAttributeValue(InvReceivecollBVO.PK_RECEIVE_COLL_B);
	}

	/**
	 * 设置纸质发票内容主表
	 * 
	 * @param pk_receive_coll_b
	 *            纸质发票内容主表
	 */
	public void setPk_receive_coll_b(String pk_receive_coll_b) {
		this.setAttributeValue(InvReceivecollBVO.PK_RECEIVE_COLL_B,
				pk_receive_coll_b);
	}

	/**
	 * 获取应税项目
	 * 
	 * @return 应税项目
	 */
	public String getPk_taxdeductlist() {
		return (String) this
				.getAttributeValue(InvReceivecollBVO.PK_TAXDEDUCTLIST);
	}

	/**
	 * 设置应税项目
	 * 
	 * @param pk_taxdeductlist
	 *            应税项目
	 */
	public void setPk_taxdeductlist(String pk_taxdeductlist) {
		this.setAttributeValue(InvReceivecollBVO.PK_TAXDEDUCTLIST,
				pk_taxdeductlist);
	}

	/**
	 * 获取时间戳
	 * 
	 * @return 时间戳
	 */
	public UFDateTime getTs() {
		return (UFDateTime) this.getAttributeValue(InvReceivecollBVO.TS);
	}

	/**
	 * 设置时间戳
	 * 
	 * @param ts
	 *            时间戳
	 */
	public void setTs(UFDateTime ts) {
		this.setAttributeValue(InvReceivecollBVO.TS, ts);
	}

	/**
	 * 获取自定义项1
	 * 
	 * @return 自定义项1
	 */
	public String getVdef1() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF1);
	}

	/**
	 * 设置自定义项1
	 * 
	 * @param vdef1
	 *            自定义项1
	 */
	public void setVdef1(String vdef1) {
		this.setAttributeValue(InvReceivecollBVO.VDEF1, vdef1);
	}

	/**
	 * 获取自定义项10
	 * 
	 * @return 自定义项10
	 */
	public String getVdef10() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF10);
	}

	/**
	 * 设置自定义项10
	 * 
	 * @param vdef10
	 *            自定义项10
	 */
	public void setVdef10(String vdef10) {
		this.setAttributeValue(InvReceivecollBVO.VDEF10, vdef10);
	}

	/**
	 * 获取自定义项11
	 * 
	 * @return 自定义项11
	 */
	public String getVdef11() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF11);
	}

	/**
	 * 设置自定义项11
	 * 
	 * @param vdef11
	 *            自定义项11
	 */
	public void setVdef11(String vdef11) {
		this.setAttributeValue(InvReceivecollBVO.VDEF11, vdef11);
	}

	/**
	 * 获取自定义项12
	 * 
	 * @return 自定义项12
	 */
	public String getVdef12() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF12);
	}

	/**
	 * 设置自定义项12
	 * 
	 * @param vdef12
	 *            自定义项12
	 */
	public void setVdef12(String vdef12) {
		this.setAttributeValue(InvReceivecollBVO.VDEF12, vdef12);
	}

	/**
	 * 获取自定义项13
	 * 
	 * @return 自定义项13
	 */
	public String getVdef13() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF13);
	}

	/**
	 * 设置自定义项13
	 * 
	 * @param vdef13
	 *            自定义项13
	 */
	public void setVdef13(String vdef13) {
		this.setAttributeValue(InvReceivecollBVO.VDEF13, vdef13);
	}

	/**
	 * 获取自定义项14
	 * 
	 * @return 自定义项14
	 */
	public String getVdef14() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF14);
	}

	/**
	 * 设置自定义项14
	 * 
	 * @param vdef14
	 *            自定义项14
	 */
	public void setVdef14(String vdef14) {
		this.setAttributeValue(InvReceivecollBVO.VDEF14, vdef14);
	}

	/**
	 * 获取自定义项15
	 * 
	 * @return 自定义项15
	 */
	public String getVdef15() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF15);
	}

	/**
	 * 设置自定义项15
	 * 
	 * @param vdef15
	 *            自定义项15
	 */
	public void setVdef15(String vdef15) {
		this.setAttributeValue(InvReceivecollBVO.VDEF15, vdef15);
	}

	/**
	 * 获取自定义项16
	 * 
	 * @return 自定义项16
	 */
	public String getVdef16() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF16);
	}

	/**
	 * 设置自定义项16
	 * 
	 * @param vdef16
	 *            自定义项16
	 */
	public void setVdef16(String vdef16) {
		this.setAttributeValue(InvReceivecollBVO.VDEF16, vdef16);
	}

	/**
	 * 获取自定义项17
	 * 
	 * @return 自定义项17
	 */
	public String getVdef17() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF17);
	}

	/**
	 * 设置自定义项17
	 * 
	 * @param vdef17
	 *            自定义项17
	 */
	public void setVdef17(String vdef17) {
		this.setAttributeValue(InvReceivecollBVO.VDEF17, vdef17);
	}

	/**
	 * 获取自定义项18
	 * 
	 * @return 自定义项18
	 */
	public String getVdef18() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF18);
	}

	/**
	 * 设置自定义项18
	 * 
	 * @param vdef18
	 *            自定义项18
	 */
	public void setVdef18(String vdef18) {
		this.setAttributeValue(InvReceivecollBVO.VDEF18, vdef18);
	}

	/**
	 * 获取自定义项19
	 * 
	 * @return 自定义项19
	 */
	public String getVdef19() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF19);
	}

	/**
	 * 设置自定义项19
	 * 
	 * @param vdef19
	 *            自定义项19
	 */
	public void setVdef19(String vdef19) {
		this.setAttributeValue(InvReceivecollBVO.VDEF19, vdef19);
	}

	/**
	 * 获取自定义项2
	 * 
	 * @return 自定义项2
	 */
	public String getVdef2() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF2);
	}

	/**
	 * 设置自定义项2
	 * 
	 * @param vdef2
	 *            自定义项2
	 */
	public void setVdef2(String vdef2) {
		this.setAttributeValue(InvReceivecollBVO.VDEF2, vdef2);
	}

	/**
	 * 获取自定义项20
	 * 
	 * @return 自定义项20
	 */
	public String getVdef20() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF20);
	}

	/**
	 * 设置自定义项20
	 * 
	 * @param vdef20
	 *            自定义项20
	 */
	public void setVdef20(String vdef20) {
		this.setAttributeValue(InvReceivecollBVO.VDEF20, vdef20);
	}

	/**
	 * 获取自定义项3
	 * 
	 * @return 自定义项3
	 */
	public String getVdef3() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF3);
	}

	/**
	 * 设置自定义项3
	 * 
	 * @param vdef3
	 *            自定义项3
	 */
	public void setVdef3(String vdef3) {
		this.setAttributeValue(InvReceivecollBVO.VDEF3, vdef3);
	}

	/**
	 * 获取自定义项4
	 * 
	 * @return 自定义项4
	 */
	public String getVdef4() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF4);
	}

	/**
	 * 设置自定义项4
	 * 
	 * @param vdef4
	 *            自定义项4
	 */
	public void setVdef4(String vdef4) {
		this.setAttributeValue(InvReceivecollBVO.VDEF4, vdef4);
	}

	/**
	 * 获取自定义项5
	 * 
	 * @return 自定义项5
	 */
	public String getVdef5() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF5);
	}

	/**
	 * 设置自定义项5
	 * 
	 * @param vdef5
	 *            自定义项5
	 */
	public void setVdef5(String vdef5) {
		this.setAttributeValue(InvReceivecollBVO.VDEF5, vdef5);
	}

	/**
	 * 获取自定义项6
	 * 
	 * @return 自定义项6
	 */
	public String getVdef6() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF6);
	}

	/**
	 * 设置自定义项6
	 * 
	 * @param vdef6
	 *            自定义项6
	 */
	public void setVdef6(String vdef6) {
		this.setAttributeValue(InvReceivecollBVO.VDEF6, vdef6);
	}

	/**
	 * 获取自定义项7
	 * 
	 * @return 自定义项7
	 */
	public String getVdef7() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF7);
	}

	/**
	 * 设置自定义项7
	 * 
	 * @param vdef7
	 *            自定义项7
	 */
	public void setVdef7(String vdef7) {
		this.setAttributeValue(InvReceivecollBVO.VDEF7, vdef7);
	}

	/**
	 * 获取自定义项8
	 * 
	 * @return 自定义项8
	 */
	public String getVdef8() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF8);
	}

	/**
	 * 设置自定义项8
	 * 
	 * @param vdef8
	 *            自定义项8
	 */
	public void setVdef8(String vdef8) {
		this.setAttributeValue(InvReceivecollBVO.VDEF8, vdef8);
	}

	/**
	 * 获取自定义项9
	 * 
	 * @return 自定义项9
	 */
	public String getVdef9() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VDEF9);
	}

	/**
	 * 设置自定义项9
	 * 
	 * @param vdef9
	 *            自定义项9
	 */
	public void setVdef9(String vdef9) {
		this.setAttributeValue(InvReceivecollBVO.VDEF9, vdef9);
	}

	/**
	 * 获取规格
	 * 
	 * @return 规格
	 */
	public String getVinvspec() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VINVSPEC);
	}

	/**
	 * 设置规格
	 * 
	 * @param vinvspec
	 *            规格
	 */
	public void setVinvspec(String vinvspec) {
		this.setAttributeValue(InvReceivecollBVO.VINVSPEC, vinvspec);
	}

	/**
	 * 获取型号
	 * 
	 * @return 型号
	 */
	public String getVinvtype() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VINVTYPE);
	}

	/**
	 * 设置型号
	 * 
	 * @param vinvtype
	 *            型号
	 */
	public void setVinvtype(String vinvtype) {
		this.setAttributeValue(InvReceivecollBVO.VINVTYPE, vinvtype);
	}

	/**
	 * 获取单位
	 * 
	 * @return 单位
	 */
	public String getVinvunit() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VINVUNIT);
	}

	/**
	 * 设置单位
	 * 
	 * @param vinvunit
	 *            单位
	 */
	public void setVinvunit(String vinvunit) {
		this.setAttributeValue(InvReceivecollBVO.VINVUNIT, vinvunit);
	}

	/**
	 * 获取备注
	 * 
	 * @return 备注
	 */
	public String getVmemo() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VMEMO);
	}

	/**
	 * 设置备注
	 * 
	 * @param vmemo
	 *            备注
	 */
	public void setVmemo(String vmemo) {
		this.setAttributeValue(InvReceivecollBVO.VMEMO, vmemo);
	}

	/**
	 * 获取发票内容
	 * 
	 * @return 发票内容
	 */
	public String getVopencontent() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VOPENCONTENT);
	}

	/**
	 * 设置发票内容
	 * 
	 * @param vopencontent
	 *            发票内容
	 */
	public void setVopencontent(String vopencontent) {
		this.setAttributeValue(InvReceivecollBVO.VOPENCONTENT, vopencontent);
	}

	/**
	 * 获取预留字段1
	 * 
	 * @return 预留字段1
	 */
	public String getVreserve1() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE1);
	}

	/**
	 * 设置预留字段1
	 * 
	 * @param vreserve1
	 *            预留字段1
	 */
	public void setVreserve1(String vreserve1) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE1, vreserve1);
	}

	/**
	 * 获取预留字段10
	 * 
	 * @return 预留字段10
	 */
	public String getVreserve10() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE10);
	}

	/**
	 * 设置预留字段10
	 * 
	 * @param vreserve10
	 *            预留字段10
	 */
	public void setVreserve10(String vreserve10) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE10, vreserve10);
	}

	/**
	 * 获取预留字段2
	 * 
	 * @return 预留字段2
	 */
	public String getVreserve2() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE2);
	}

	/**
	 * 设置预留字段2
	 * 
	 * @param vreserve2
	 *            预留字段2
	 */
	public void setVreserve2(String vreserve2) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE2, vreserve2);
	}

	/**
	 * 获取预留字段3
	 * 
	 * @return 预留字段3
	 */
	public String getVreserve3() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE3);
	}

	/**
	 * 设置预留字段3
	 * 
	 * @param vreserve3
	 *            预留字段3
	 */
	public void setVreserve3(String vreserve3) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE3, vreserve3);
	}

	/**
	 * 获取预留字段4
	 * 
	 * @return 预留字段4
	 */
	public String getVreserve4() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE4);
	}

	/**
	 * 设置预留字段4
	 * 
	 * @param vreserve4
	 *            预留字段4
	 */
	public void setVreserve4(String vreserve4) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE4, vreserve4);
	}

	/**
	 * 获取预留字段5
	 * 
	 * @return 预留字段5
	 */
	public String getVreserve5() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE5);
	}

	/**
	 * 设置预留字段5
	 * 
	 * @param vreserve5
	 *            预留字段5
	 */
	public void setVreserve5(String vreserve5) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE5, vreserve5);
	}

	/**
	 * 获取预留字段6
	 * 
	 * @return 预留字段6
	 */
	public String getVreserve6() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE6);
	}

	/**
	 * 设置预留字段6
	 * 
	 * @param vreserve6
	 *            预留字段6
	 */
	public void setVreserve6(String vreserve6) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE6, vreserve6);
	}

	/**
	 * 获取预留字段7
	 * 
	 * @return 预留字段7
	 */
	public String getVreserve7() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE7);
	}

	/**
	 * 设置预留字段7
	 * 
	 * @param vreserve7
	 *            预留字段7
	 */
	public void setVreserve7(String vreserve7) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE7, vreserve7);
	}

	/**
	 * 获取预留字段8
	 * 
	 * @return 预留字段8
	 */
	public String getVreserve8() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE8);
	}

	/**
	 * 设置预留字段8
	 * 
	 * @param vreserve8
	 *            预留字段8
	 */
	public void setVreserve8(String vreserve8) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE8, vreserve8);
	}

	/**
	 * 获取预留字段9
	 * 
	 * @return 预留字段9
	 */
	public String getVreserve9() {
		return (String) this.getAttributeValue(InvReceivecollBVO.VRESERVE9);
	}

	/**
	 * 设置预留字段9
	 * 
	 * @param vreserve9
	 *            预留字段9
	 */
	public void setVreserve9(String vreserve9) {
		this.setAttributeValue(InvReceivecollBVO.VRESERVE9, vreserve9);
	}

	@Override
	public IVOMeta getMetaData() {
		return VOMetaFactory.getInstance().getVOMeta("jzinv.InvReceivecollBVO");
	}
}