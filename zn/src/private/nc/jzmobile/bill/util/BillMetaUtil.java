package nc.jzmobile.bill.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.ml.NCLangResOnserver;
import nc.itf.uap.pf.IPFConfig;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.jzmobile.bill.data.access.NCBillAccessBillTemplate;
import nc.jzmobile.bill.data.access.PubBillTempletBModel;
import nc.jzmobile.bill.data.access.PubBillTempletModel;
import nc.md.model.IBusinessEntity;
import nc.md.model.access.javamap.AggVOStyle;
import nc.md.model.access.javamap.IBeanStyle;
import nc.uap.pf.metadata.PfMetadataTools;
import nc.vo.jzmobile.app.MobileBillData;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.IVOMeta;
import nc.vo.pubapp.pattern.model.entity.bill.AbstractBill;
import nc.vo.uap.pf.PFBusinessException;

/**
 * 该工具类专门处理和元数据有关以及aggVO和map互换的操作
 * @Created by wangruin on 2017/8/18
 *
 */
public class BillMetaUtil {
	/**
	 * 获取所有字段名称
	 * @param billType
	 * @return
	 * @throws Exception
	 */
	public static List<String> getAttrNames(String billType) throws Exception {

		IBusinessEntity be = PfMetadataTools.queryMetaOfBilltype(billType);
	    List<String> attrs = new ArrayList<String>();
	    if (be.getAttributes() == null){
			throw new BusinessException("根据billType查询对应的属性为空");
		}
		for(int i =0;i<be.getAttributes().size();i++){
			attrs.add(be.getAttributes().get(i).getName());
		}
		return attrs;
	}
	
	
	
	/**
	 * 根据billType查询对应的主表表名
	 * 
	 * @param billType
	 * @return
	 * @throws Exception
	 */
	public static String getTableName(String billType) throws Exception {

		IBusinessEntity be = PfMetadataTools.queryMetaOfBilltype(billType);
	    
		String tableName = be.getTable().getName();
		BillTempletUtil.templatInfo(billType);
		if (tableName == null){
			throw new BusinessException("根据billType查询对应的主表表名为空");
		}
		return tableName;
	}
	
	
	/**
	 * 根据billType查询对应的主表表名
	 * 
	 * @param billType
	 * @return
	 * @throws Exception
	 */
	public static String getTableName(String billType,String preffx,String column) throws Exception {

		IBusinessEntity be = PfMetadataTools.queryMetaOfBilltype(billType);
	    
		String tableName = be.getTable().getName();
		BillTempletUtil.templatInfo(billType,preffx,column);
		if (tableName == null){
			throw new BusinessException("根据billType查询对应的主表表名为空");
		}
		return tableName;
	}
	/**
	 * 根据billType查询对应的AggVO的全类名
	 * @param billType
	 * @return
	 * @throws BusinessException 
	 */
    public static String getAggVOFullClassName(String billType)throws BusinessException{
    	IBusinessEntity be = PfMetadataTools.queryMetaOfBilltype(billType);
    	IBeanStyle bs = be.getBeanStyle();
    	String aggVoClassName =null;
    	if (bs instanceof AggVOStyle) 
    		aggVoClassName = ((AggVOStyle)bs).getAggVOClassName();
		else {
			throw new PFBusinessException(NCLangResOnserver.getInstance()
					.getStrByID("busitype", "busitypehint-000009"));
			}
		return aggVoClassName;
    }
    /**
     * 将AggVO数据转化为map
     * @param aggVO
     * @param billType
     * @param userId
     * @return
     * @throws BusinessException
     */
    public static List<MobileBillData> changeAggVO2Map(AbstractBill aggVO,String billType,String userId)throws BusinessException{
    	String pk_templet = BillMetaUtil.getTemplateIDBill(billType);
    	if(pk_templet==null)
    		throw new BusinessException("查询不到单据类型为"+billType+"的单据模板！");
    	NCBillAccessBillTemplate ba = new NCBillAccessBillTemplate(pk_templet);
		ba.loadTemplate();
		List<MobileBillData> dataList = new ArrayList<MobileBillData>();
		ba.setBillVO(aggVO);
		MobileBillData billData = null;
		billData = ba.billVO2Map(null, userId);
		billData.setBilltypename(ba.getBillTempletVO().getHeadVO().getBillTempletCaption());
		billData.setTs(aggVO.getParentVO().getAttributeValue("ts").toString());
		billData.setId(aggVO.getPrimaryKey());
		dataList.add(billData);
    	return dataList;
    }
    /**
     * 根据单据类型和单据主键查询对应的aggVO
     * @param billtype
     * @param billid
     * @return
     * @throws BusinessException
     */
    public static AggregatedValueObject queryAggVO(String billtype,String billid)throws BusinessException {
		AggregatedValueObject aggvo = null;
		try {
//			
			IPFConfig bsConfig = (IPFConfig) NCLocator.getInstance().lookup(IPFConfig.class.getName());
			aggvo = bsConfig.queryBillDataVO(billtype, billid);
		} catch (Exception e) {
			Logger.error(e);
			e.printStackTrace();
			throw new BusinessException(e);
		}
		return aggvo;
	}
    /**
	 * 移动 制单 templateid
	 * ***/
	public static String getTemplateIDBill(String billtype)
			throws BusinessException {
		String pk_billtemplet = null; 
		try {
			BaseDAO dao = new BaseDAO();
			pk_billtemplet = (String) dao
					.executeQuery(
							"select pk_billtemplet from pub_billtemplet where isnull(dr,0)=0 and bill_templetcaption='"
									+"MOBILE"+ billtype + "'",
							new ResultSetProcessor() {
								private static final long serialVersionUID = 1L;

								public Object handleResultSet(ResultSet rs)
										throws SQLException {
									while (rs.next()) {
										return rs.getString("pk_billtemplet");
									}
									return null;
								}
							});
		} catch (DAOException e) {
			Logger.error(e);
			throw new BusinessException(e);
		}

		return pk_billtemplet;
	}
     /* 该方法根据pos找出对应的表头或表体的tableCode
     * pos为0表示表头，1为表体，2为表尾
     * @param billType
     * @param pos
     * @return
     * @throws BusinessException
     */
	public static List<String>  findBillPos(String billType,Integer pos,String preffx,String column) throws BusinessException{
		  if(pos==null)
			  throw new BusinessException("pos不能为空");
		  if(pos!=0&&pos!=1&&pos!=2)
			  throw new BusinessException("pos只能为0(表头)，1(表体)，2(表尾)");
		  
		  PubBillTempletModel model = BillTempletUtil.templatInfo(billType,preffx,column);
		  List<String> list = new ArrayList<String>();
		  List<PubBillTempletBModel> modelList = model.getBillTempletBList();
		  for(PubBillTempletBModel b:modelList){
			  if(pos.equals(b.getPos())){
				 list.add(b.getTableCode());
			  }
		  }
		  if(list.size()==0)
			  throw new BusinessException("pos为"+pos+"的页签编码为空");
	  return list;  }
	
	
	 /* 该方法根据pos找出对应的表头或表体的tableCode
     * pos为0表示表头，1为表体，2为表尾
     * @param billType
     * @param pos
     * @return
     * @throws BusinessException
     */
	public static List<String>  findBillPos(String billType,Integer pos) throws BusinessException{
		  if(pos==null)
			  throw new BusinessException("pos不能为空");
		  if(pos!=0&&pos!=1&&pos!=2)
			  throw new BusinessException("pos只能为0(表头)，1(表体)，2(表尾)");
		  
		  PubBillTempletModel model = BillTempletUtil.templatInfo(billType);
		  List<String> list = new ArrayList<String>();
		  List<PubBillTempletBModel> modelList = model.getBillTempletBList();
		  for(PubBillTempletBModel b:modelList){
			  if(pos.equals(b.getPos())){
				 list.add(b.getTableCode());
			  }
		  }
		  if(list.size()==0)
			  throw new BusinessException("pos为"+pos+"的页签编码为空");
	  return list;  }
	
	public static List<String>  findBillPosByBillType(String billtype,Integer pos) throws BusinessException{
		  if(pos==null)
			  throw new BusinessException("pos不能为空");
		  if(pos!=0&&pos!=1&&pos!=2)
			  throw new BusinessException("pos只能为0(表头)，1(表体)，2(表尾)");
		  
		  PubBillTempletModel model = BillTempletUtil.templatInfoByBillType(billtype);
		  List<String> list = new ArrayList<String>();
		  List<PubBillTempletBModel> modelList = model.getBillTempletBList();
		  for(PubBillTempletBModel b:modelList){
			  if(pos.equals(b.getPos())){
				 list.add(b.getTableCode());
			  }
		  }
		  if(list.size()==0)
			  throw new BusinessException("pos为"+pos+"的页签编码为空");
	  return list;  }
  
  /**
   * 根据pk_billtemplet和metadataclass查询tabcode
   * @param meta
   * @param billType
   * @return
   * @throws BusinessException
   */
	public static String getTabCode(IVOMeta meta,String billType,String preffx,String column) throws BusinessException{
	  PubBillTempletModel model = BillTempletUtil.templatInfo(billType,preffx,column);
	  
	  StringBuffer buffer = new StringBuffer();
	  buffer.append("select tabcode from pub_billtemplet_t where ");
	  buffer.append(" pk_billtemplet='"+model.getBillTempletId()+"'");
	  buffer.append(" and metadataclass='"+meta.getEntityName()+"'");
	  
	  @SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>) new BaseDAO()
				.executeQuery(buffer.toString(), new MapListProcessor());
	  
	  if(list==null||list.size()==0)
		  throw new BusinessException("pk_billtemplet为"+model.getBillTempletId()+"且metadataclass为"+meta.getEntityName()+"在表pub_billtemplet_t中数据为空");
      return (String) list.get(0).get("tabcode");
  }
	
	/**
	   * 根据pk_billtemplet和metadataclass查询tabcode
	   * @param meta
	   * @param billType
	   * @return
	   * @throws BusinessException
	   */
		public static String getTabCode(IVOMeta meta,String billType) throws BusinessException{
		  PubBillTempletModel model = BillTempletUtil.templatInfo(billType);
		  
		  StringBuffer buffer = new StringBuffer();
		  buffer.append("select tabcode from pub_billtemplet_t where ");
		  buffer.append(" pk_billtemplet='"+model.getBillTempletId()+"'");
		  buffer.append(" and metadataclass='"+meta.getEntityName()+"'");
		  
		  @SuppressWarnings("unchecked")
			List<Map<String, Object>> list = (List<Map<String, Object>>) new BaseDAO()
					.executeQuery(buffer.toString(), new MapListProcessor());
		  
		  if(list==null||list.size()==0)
			  throw new BusinessException("pk_billtemplet为"+model.getBillTempletId()+"且metadataclass为"+meta.getEntityName()+"在表pub_billtemplet_t中数据为空");
	      return (String) list.get(0).get("tabcode");
	  }
	
	public static String getTabCodeByBillType(IVOMeta meta,String billtype) throws BusinessException{
		  PubBillTempletModel model = BillTempletUtil.templatInfoByBillType(billtype);
		  
		  StringBuffer buffer = new StringBuffer();
		  buffer.append("select tabcode from pub_billtemplet_t where ");
		  buffer.append(" pk_billtemplet='"+model.getBillTempletId()+"'");
		  buffer.append(" and metadataclass='"+meta.getEntityName()+"'");
		  
		  @SuppressWarnings("unchecked")
			List<Map<String, Object>> list = (List<Map<String, Object>>) new BaseDAO()
					.executeQuery(buffer.toString(), new MapListProcessor());
		  
		  if(list==null||list.size()==0)
			  throw new BusinessException("pk_billtemplet为"+model.getBillTempletId()+"且metadataclass为"+meta.getEntityName()+"在表pub_billtemplet_t中数据为空");
	      return (String) list.get(0).get("tabcode");
	  }
	
	
  
 }  
