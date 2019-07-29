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
 * �ù�����ר�Ŵ����Ԫ�����й��Լ�aggVO��map�����Ĳ���
 * @Created by wangruin on 2017/8/18
 *
 */
public class BillMetaUtil {
	/**
	 * ��ȡ�����ֶ�����
	 * @param billType
	 * @return
	 * @throws Exception
	 */
	public static List<String> getAttrNames(String billType) throws Exception {

		IBusinessEntity be = PfMetadataTools.queryMetaOfBilltype(billType);
	    List<String> attrs = new ArrayList<String>();
	    if (be.getAttributes() == null){
			throw new BusinessException("����billType��ѯ��Ӧ������Ϊ��");
		}
		for(int i =0;i<be.getAttributes().size();i++){
			attrs.add(be.getAttributes().get(i).getName());
		}
		return attrs;
	}
	
	
	
	/**
	 * ����billType��ѯ��Ӧ���������
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
			throw new BusinessException("����billType��ѯ��Ӧ���������Ϊ��");
		}
		return tableName;
	}
	
	
	/**
	 * ����billType��ѯ��Ӧ���������
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
			throw new BusinessException("����billType��ѯ��Ӧ���������Ϊ��");
		}
		return tableName;
	}
	/**
	 * ����billType��ѯ��Ӧ��AggVO��ȫ����
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
     * ��AggVO����ת��Ϊmap
     * @param aggVO
     * @param billType
     * @param userId
     * @return
     * @throws BusinessException
     */
    public static List<MobileBillData> changeAggVO2Map(AbstractBill aggVO,String billType,String userId)throws BusinessException{
    	String pk_templet = BillMetaUtil.getTemplateIDBill(billType);
    	if(pk_templet==null)
    		throw new BusinessException("��ѯ������������Ϊ"+billType+"�ĵ���ģ�壡");
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
     * ���ݵ������ͺ͵���������ѯ��Ӧ��aggVO
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
	 * �ƶ� �Ƶ� templateid
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
     /* �÷�������pos�ҳ���Ӧ�ı�ͷ������tableCode
     * posΪ0��ʾ��ͷ��1Ϊ���壬2Ϊ��β
     * @param billType
     * @param pos
     * @return
     * @throws BusinessException
     */
	public static List<String>  findBillPos(String billType,Integer pos,String preffx,String column) throws BusinessException{
		  if(pos==null)
			  throw new BusinessException("pos����Ϊ��");
		  if(pos!=0&&pos!=1&&pos!=2)
			  throw new BusinessException("posֻ��Ϊ0(��ͷ)��1(����)��2(��β)");
		  
		  PubBillTempletModel model = BillTempletUtil.templatInfo(billType,preffx,column);
		  List<String> list = new ArrayList<String>();
		  List<PubBillTempletBModel> modelList = model.getBillTempletBList();
		  for(PubBillTempletBModel b:modelList){
			  if(pos.equals(b.getPos())){
				 list.add(b.getTableCode());
			  }
		  }
		  if(list.size()==0)
			  throw new BusinessException("posΪ"+pos+"��ҳǩ����Ϊ��");
	  return list;  }
	
	
	 /* �÷�������pos�ҳ���Ӧ�ı�ͷ������tableCode
     * posΪ0��ʾ��ͷ��1Ϊ���壬2Ϊ��β
     * @param billType
     * @param pos
     * @return
     * @throws BusinessException
     */
	public static List<String>  findBillPos(String billType,Integer pos) throws BusinessException{
		  if(pos==null)
			  throw new BusinessException("pos����Ϊ��");
		  if(pos!=0&&pos!=1&&pos!=2)
			  throw new BusinessException("posֻ��Ϊ0(��ͷ)��1(����)��2(��β)");
		  
		  PubBillTempletModel model = BillTempletUtil.templatInfo(billType);
		  List<String> list = new ArrayList<String>();
		  List<PubBillTempletBModel> modelList = model.getBillTempletBList();
		  for(PubBillTempletBModel b:modelList){
			  if(pos.equals(b.getPos())){
				 list.add(b.getTableCode());
			  }
		  }
		  if(list.size()==0)
			  throw new BusinessException("posΪ"+pos+"��ҳǩ����Ϊ��");
	  return list;  }
	
	public static List<String>  findBillPosByBillType(String billtype,Integer pos) throws BusinessException{
		  if(pos==null)
			  throw new BusinessException("pos����Ϊ��");
		  if(pos!=0&&pos!=1&&pos!=2)
			  throw new BusinessException("posֻ��Ϊ0(��ͷ)��1(����)��2(��β)");
		  
		  PubBillTempletModel model = BillTempletUtil.templatInfoByBillType(billtype);
		  List<String> list = new ArrayList<String>();
		  List<PubBillTempletBModel> modelList = model.getBillTempletBList();
		  for(PubBillTempletBModel b:modelList){
			  if(pos.equals(b.getPos())){
				 list.add(b.getTableCode());
			  }
		  }
		  if(list.size()==0)
			  throw new BusinessException("posΪ"+pos+"��ҳǩ����Ϊ��");
	  return list;  }
  
  /**
   * ����pk_billtemplet��metadataclass��ѯtabcode
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
		  throw new BusinessException("pk_billtempletΪ"+model.getBillTempletId()+"��metadataclassΪ"+meta.getEntityName()+"�ڱ�pub_billtemplet_t������Ϊ��");
      return (String) list.get(0).get("tabcode");
  }
	
	/**
	   * ����pk_billtemplet��metadataclass��ѯtabcode
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
			  throw new BusinessException("pk_billtempletΪ"+model.getBillTempletId()+"��metadataclassΪ"+meta.getEntityName()+"�ڱ�pub_billtemplet_t������Ϊ��");
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
			  throw new BusinessException("pk_billtempletΪ"+model.getBillTempletId()+"��metadataclassΪ"+meta.getEntityName()+"�ڱ�pub_billtemplet_t������Ϊ��");
	      return (String) list.get(0).get("tabcode");
	  }
	
	
  
 }  
