package nc.jzmobile.bill.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.uap.billtemplate.IBillTemplateQry;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.jzmobile.bill.data.access.PubBillTempletBModel;
import nc.jzmobile.bill.data.access.PubBillTempletModel;
import nc.jzmobile.bill.data.access.PubBillTempletTModel;
import nc.ui.pub.bill.IBillItem;
import nc.vo.jzmobile.template.comparator.BillTabVOComparator;
import nc.vo.jzmobile.template.comparator.BillTempletBodyVOComparator;
import nc.vo.pub.BusinessException;
import nc.vo.pub.bill.BillTabVO;
import nc.vo.pub.bill.BillTempletVO;

public class BillTempletUtil {
	// 排序
	public static final BillTabVOComparator TABVO_COMPARATOR = new BillTabVOComparator();
	public static final BillTempletBodyVOComparator BODYVO_COMPARATOR = new BillTempletBodyVOComparator();
	private static BillTempletVO billTempletVO = null;

	
	public static PubBillTempletModel templatInfo(String billtype)
			throws BusinessException {
		try {
			
			PubBillTempletModel templet = getTemplateID(billtype);
			
			if (templet == null || "".equals(templet.getBillTempletId())) {
				throw new BusinessException("没有找到单据:" + billtype + "的模版！");
			}
			loadTemplate(templet.getBillTempletId());
			templet.setBillTempletTList(getTabVO());
			templet.setBillTempletBList(getBillTempletBodyVO(templet));
			return templet;
		} catch (Exception e) {
			Logger.error(e);
			throw new BusinessException(e);
		}
	}
	
	
	public static PubBillTempletModel getTemplateID(String billtype)
			throws BusinessException {

		// 查询数据库
		PubBillTempletModel templet = null;
		try {
			BaseDAO dao = new BaseDAO();
			templet = (PubBillTempletModel) dao
					.executeQuery(
							"select pk_billtemplet,bill_templetcaption,bill_templetname,pk_billtypecode from pub_billtemplet where isnull(dr,0)=0 and bill_templetcaption = 'MOBILE"
									+billtype + "' ",
							new ResultSetProcessor() {

								private static final long serialVersionUID = 1L;

								public Object handleResultSet(ResultSet rs)
										throws SQLException {
									while (rs.next()) {
										PubBillTempletModel templet = new PubBillTempletModel();
										templet.setBillTempletCaption(rs
												.getString("bill_templetcaption"));
										templet.setBillTempletName(rs
												.getString("bill_templetname"));
										templet.setBillTypeCode(rs
												.getString("pk_billtypecode"));
										templet.setBillTempletId(rs
												.getString("pk_billtemplet"));
										return templet;
									}
									return null;
								}
							});
		} catch (DAOException e) {
			Logger.error(e);
			throw new BusinessException(e);
		}

		return templet;
	}
	
	
	public static PubBillTempletModel templatInfo(String billtype,String preffx,String column)
			throws BusinessException {
		try {
			
			
			PubBillTempletModel templet = getTemplateID(billtype,preffx,column);
			
			if (templet == null || "".equals(templet.getBillTempletId())) {
				throw new BusinessException("没有找到单据:" + billtype + "的模版！");
			}
			loadTemplate(templet.getBillTempletId());
			templet.setBillTempletTList(getTabVO());
			templet.setBillTempletBList(getBillTempletBodyVO(templet));
			return templet;
		} catch (Exception e) {
			Logger.error(e);
			throw new BusinessException(e);
		}
	}

	private static void loadTemplate(String pk_billTemplet) {
		billTempletVO = getBillTempletVO(pk_billTemplet);

	}
	
	private static List<PubBillTempletBModel> getBillTempletBodyVO(
			PubBillTempletModel templet) throws BusinessException {
		List<PubBillTempletBModel> btbList = null;
		if (billTempletVO.getBodyVO() != null
				&& billTempletVO.getBodyVO().length > 0) {
			btbList = new ArrayList<PubBillTempletBModel>();
			Map<String, String> meteDate = getMeteDate(templet.getBillTypeCode());

			for (int i = 0; i < billTempletVO.getBodyVO().length; i++) {
				// 只有卡片界面显示的内容才会继续
				// if
				// (!billTempletVO.getBodyVO()[i].getShowflag().booleanValue())
				// continue;
				PubBillTempletBModel btb = new PubBillTempletBModel();
				btb.setShowFlag(billTempletVO.getBodyVO()[i].getShowflag()
						.booleanValue());
				btb.setDataType(billTempletVO.getBodyVO()[i].getDatatype());
				btb.setItemKey(billTempletVO.getBodyVO()[i].getItemkey());
				String showName = billTempletVO.getBodyVO()[i]
						.getDefaultshowname();
				if (showName == null || "".equals(showName)) {
					showName = meteDate.get(btb.getItemKey());
				}
				btb.setDefaultShowName(showName);
				btb.setDefaultValue(billTempletVO.getBodyVO()[i]
						.getDefaultvalue());
				// btb.setHyperLinkFlag(billTempletVO.getBodyVO()[i].getHyperlinkflag());
				btb.setInputLength(billTempletVO.getBodyVO()[i]
						.getInputlength());
				btb.setItemType(billTempletVO.getBodyVO()[i].getItemtype());
				btb.setListShowFlag(billTempletVO.getBodyVO()[i]
						.getListshowflag());
				btb.setNullFlag(billTempletVO.getBodyVO()[i].getNullflag());
				btb.setOptions(billTempletVO.getBodyVO()[i].getOptions());
				btb.setPos(billTempletVO.getBodyVO()[i].getPos());
				btb.setShowFlag(billTempletVO.getBodyVO()[i].getShowflag());
				btb.setTableCode(billTempletVO.getBodyVO()[i].getTable_code());
				btb.setTableName(billTempletVO.getBodyVO()[i].getTableName());
				
				
				if(btb.getDataType()==IBillItem.COMBO){
					btb.setComboValues(getEnumType(btb.getItemKey(),templet.getBillTempletId()));
				}
				
				btbList.add(btb);

			}
		}
		return btbList;
	}

	// 页签
	private static List<PubBillTempletTModel> getTabVO() {
		BillTabVO[] bvos = billTempletVO.getHeadVO().getStructvo()
				.getBillTabVOs();
		List<PubBillTempletTModel> bttList = new ArrayList<PubBillTempletTModel>();
		for (BillTabVO headTabVo : bvos) {
			PubBillTempletTModel btt = new PubBillTempletTModel();
			btt.setPos(headTabVo.getPos());
			btt.setBaseTab(headTabVo.getBasetab());
			btt.setTabCode(headTabVo.getTabcode());
			btt.setTabIndex(headTabVo.getTabindex());
			btt.setTabName(headTabVo.getTabname());
			bttList.add(btt);
		}
		return bttList;
	}

	private static BillTempletVO getBillTempletVO(String pk_billTemplet) {
		Logger.error("getBillTempletVO()");
		BillTempletVO vo = null;
		Logger.error("qry==null");
		IBillTemplateQry qry = (IBillTemplateQry) NCLocator.getInstance()
				.lookup(IBillTemplateQry.class.getName());
		Logger.error("qry==null"+(qry==null));
		try {
			vo = qry.findTempletData(pk_billTemplet);
		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
		}
		return vo;
	}

	private static Map<String,String> getEnumType(String itemKey,String billTypeCode) throws BusinessException{
		Map<String, String> enumType = null;

		try {
			BaseDAO dao = new BaseDAO();
			enumType = (Map<String, String>) dao
					.executeQuery(
							"select * from md_enumvalue where id=" +
							"(select DATATYPE from MD_PROPERTY where name='"+itemKey+"' and CLASSID= " +
							"(select MDID from sm_funcregister where FUNCODE='"+billTypeCode+"'))",
							new ResultSetProcessor() {

								private static final long serialVersionUID = 1L;
								Map<String, String> enumType = new HashMap<String, String>();

								public Object handleResultSet(ResultSet rs)
										throws SQLException {
									while (rs.next()) {
										enumType.put(rs.getString("value"),
												rs.getString("name"));
									}
									return enumType;
								}
							});
		} catch (DAOException e) {
			Logger.error(e);
			throw new BusinessException(e);
		}
		return enumType;
	}
	
	@SuppressWarnings("rawtypes")
	private static Map<String, String> getMeteDate(String billTypeCode)
			throws BusinessException {
		Map<String, String> meteDate = null;

		try {
			BaseDAO dao = new BaseDAO();
			meteDate = (Map<String, String>) dao
					.executeQuery(
							"select DISPLAYNAME,name from md_property where CLASSID= (select MDID from sm_funcregister where FUNCODE='"
									+ billTypeCode + "')",
							new ResultSetProcessor() {

								private static final long serialVersionUID = 1L;
								Map<String, String> meteDate = new HashMap<String, String>();

								public Object handleResultSet(ResultSet rs)
										throws SQLException {
									while (rs.next()) {
										meteDate.put(rs.getString("name"),
												rs.getString("DISPLAYNAME"));
									}
									return meteDate;
								}
							});
		} catch (DAOException e) {
			Logger.error(e);
			throw new BusinessException(e);
		}
		return meteDate;
	}

	/**
	 * 根据表名查询该表的主键字段名
	 * 
	 * @param tableName
	 *            Create by wangruin on 2017/8/10
	 * @throws BusinessException
	 */
	public static String getPkName(String tableName) throws BusinessException {
		String sql = "select name from md_column where tableid='" + tableName
				+ "' and pkey='Y'";
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> conList = (List<Map<String, Object>>) new BaseDAO()
				.executeQuery(sql, new MapListProcessor());
		String pkName = (String) conList.get(0).get("name");
		if (pkName == null)
			throw new BusinessException("根据表名查询不到该表的主键字段名");
		return pkName;
	}

	public static PubBillTempletModel getTemplateID(String billtype,String preffx,String column)
			throws BusinessException {

		// 查询数据库
		PubBillTempletModel templet = null;
		try {
			BaseDAO dao = new BaseDAO();
			templet = (PubBillTempletModel) dao
					.executeQuery(
							"select pk_billtemplet,bill_templetcaption,bill_templetname,pk_billtypecode from pub_billtemplet where isnull(dr,0)=0 and "+column+"='"
									+ preffx + billtype + "' ",
							new ResultSetProcessor() {

								private static final long serialVersionUID = 1L;

								public Object handleResultSet(ResultSet rs)
										throws SQLException {
									while (rs.next()) {
										PubBillTempletModel templet = new PubBillTempletModel();
										templet.setBillTempletCaption(rs
												.getString("bill_templetcaption"));
										templet.setBillTempletName(rs
												.getString("bill_templetname"));
										templet.setBillTypeCode(rs
												.getString("pk_billtypecode"));
										templet.setBillTempletId(rs
												.getString("pk_billtemplet"));
										return templet;
									}
									return null;
								}
							});
		} catch (DAOException e) {
			Logger.error(e);
			throw new BusinessException(e);
		}

		return templet;
	}

	public static PubBillTempletModel templatInfoByBillType(String billtype) throws BusinessException {
	try {
			
			
			PubBillTempletModel templet = getTemplateIDByBillType(billtype);
			
			if (templet == null || "".equals(templet.getBillTempletId())) {
				throw new BusinessException("没有找到单据: 节点为" + billtype + "的模版！");
			}
			loadTemplate(templet.getBillTempletId());
			templet.setBillTempletTList(getTabVO());
			templet.setBillTempletBList(getBillTempletBodyVO(templet));
			return templet;
		} catch (Exception e) {
			Logger.error(e);
			throw new BusinessException(e);
		}
	}
	

	public static PubBillTempletModel getTemplateIDByBillType(String billtype)
			throws BusinessException {

		// 查询数据库
		PubBillTempletModel templet = null;
		try {
			BaseDAO dao = new BaseDAO();
			templet = (PubBillTempletModel) dao
					.executeQuery(
							"select pk_billtemplet,bill_templetcaption,bill_templetname,pk_billtypecode from pub_billtemplet where isnull(dr,0)=0 and pk_billtypecode=(select nodecode from bd_billtype where pk_billtypecode = '"
									+  billtype + "') ",
							new ResultSetProcessor() {

								private static final long serialVersionUID = 1L;

								public Object handleResultSet(ResultSet rs)
										throws SQLException {
									while (rs.next()) {
										PubBillTempletModel templet = new PubBillTempletModel();
										templet.setBillTempletCaption(rs
												.getString("bill_templetcaption"));
										templet.setBillTempletName(rs
												.getString("bill_templetname"));
										templet.setBillTypeCode(rs
												.getString("pk_billtypecode"));
										templet.setBillTempletId(rs
												.getString("pk_billtemplet"));
										return templet;
									}
									return null;
								}
							});
		} catch (DAOException e) {
			Logger.error(e);
			throw new BusinessException(e);
		}

		return templet;
	}

}
