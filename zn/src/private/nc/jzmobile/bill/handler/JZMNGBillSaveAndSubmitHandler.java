package nc.jzmobile.bill.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.mobile.app.IMobilebillExecute;
import nc.jzmobile.bill.util.BillMetaUtil;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.vo.jzbase.pub.tool.ReflectHelper;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.IColumnMeta;
import nc.vo.pub.IVOMeta;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pubapp.pattern.model.entity.bill.IBill;
import nc.vo.pubapp.pattern.model.meta.entity.bill.IBillMeta;
import nc.vo.pubapp.pattern.model.tool.MetaTool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/**
 * ����
 * ��ٵ��ύ������&�ύ��
 */
public class JZMNGBillSaveAndSubmitHandler implements INCMobileServletHandler{

	@Override
	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		Logger.info("---JZMNGBillSaveAndSubmitHandler  start---");
		try {
			String userId = map.get("userid");
			if (userId == null) {
				throw new BusinessException("�û���Ϣ����Ϊ�գ�");
			}
			String billType = map.get("billtype");
			if (billType == null) {
				throw new BusinessException("�������Ͳ���Ϊ�գ�");
			}
			InvocationInfoProxy.getInstance().setGroupId(
					JZMobileAppUtils.getPkGroupByUserId(userId));
			
			
			IMobilebillExecute service = NCLocator.getInstance().lookup(IMobilebillExecute.class);
			//����map��װaggvo
			AggregatedValueObject aggaVO = changeMap2AggVO(map, billType,userId);
			
			AggregatedValueObject resultVO = service.saveBill(aggaVO, billType);
			AggregatedValueObject submitBill = service.submitBill(resultVO, billType);
			if(resultVO.getParentVO()==null)
				throw new BusinessException("���صı�ͷ����Ϊ�գ�����ʧ�ܣ�");
			String primaryKey = resultVO.getParentVO().getPrimaryKey();
			result.success().setData(primaryKey);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}

		return result;
	}
	/**
     * ��map�еı�ͷ��������ת��Ϊ��Ӧ��AggVO
     * @param map
     * @return
     * @throws Exception
     */
    public  AggregatedValueObject changeMap2AggVO(Map<String, String> map,String billType,String userId)throws Exception{
    	AggregatedValueObject billVO = (AggregatedValueObject) ((AggregatedValueObject) Class
				.forName(BillMetaUtil.getAggVOFullClassName(billType)).newInstance());
    	IBillMeta billMeta = MetaTool.getBillMeta((Class<? extends IBill>) Class.forName(BillMetaUtil.getAggVOFullClassName(billType)));
    	IVOMeta[] metas = billMeta.getChildren();
		
    	billVO.setParentVO(getJson2ParentVO(map,billMeta,userId));
    	for(IVOMeta meta:metas){
    	  billVO.setChildrenVO(getJson2ChildrenVOs(map,billMeta, meta,billType));
    	}
    	return billVO;
    	
    }
    
    /**
     * ��װ��ͷ
     * @param parentStr
     * @return
   * @throws Exception 
     */
    private  SuperVO getJson2ParentVO(Map<String, String> map,IBillMeta billMeta,String userId)throws Exception{
  	  SuperVO parentVO = (SuperVO) ReflectHelper.newInstance(billMeta.getVOClass(billMeta.getParent()));
  	  IColumnMeta[] columns = billMeta.getParent().getStatisticInfo().getTables()[0].getColumns();
  	  JSONObject jsonObject = JSON.parseObject(map.get("headdata"));
  	  if(jsonObject==null)
  		  throw new BusinessException("������keΪheaddata��valueֵ����Ϊ��");
  	  Set<String> sets = jsonObject.keySet(); 
  	  for(String str:sets){
  		  boolean checkColumnIsExist = checkColumnIsExist(columns,str);
  		  if(checkColumnIsExist){
  			parentVO.setAttributeValue(str, "".equals(jsonObject.get(str))?null:jsonObject.get(str));
  		  }
  	  }
  	  parentVO.setAttributeValue("ts",new UFDateTime());
  	  parentVO.setAttributeValue("billmaker",map.get("userid"));
  	  parentVO.setAttributeValue("creator",map.get("userid"));
  	  parentVO.setAttributeValue("creationtime",new UFDateTime());
  	   
  	  parentVO.setAttributeValue("pk_group", JZMobileAppUtils.getPkGroupByUserId(userId));
  	  parentVO.setAttributeValue("pk_org", JZMobileAppUtils.getPkOrgByUserId(userId));
  	  return parentVO;
    }
    /**
     * ��װ����
     * @param map
     * @param billMeta
     * @return
     * @throws BusinessException
     */
    private  SuperVO[] getJson2ChildrenVOs(Map<String, String> map,IBillMeta billMeta,IVOMeta meta,String billType)throws BusinessException{
  			  
  	  JSONObject jsonObject = JSON.parseObject(map.get("bodydata"));
  	  if(jsonObject==null||jsonObject.equals(""))
  		  return null;
  	  List<SuperVO> list = new ArrayList<SuperVO>();
  	  Set<String> sets = jsonObject.keySet(); 
  	  
  	  IColumnMeta[] columns = meta.getStatisticInfo().getTables()[0].getColumns();
  	  //У��ҳǩ���Ƿ��NC��һ��
  	  checkTabCode(BillMetaUtil.findBillPos(map.get("billtype"),1),sets);
  	  JSONArray array =  (JSONArray) jsonObject.get(BillMetaUtil.getTabCode(meta,billType));
  	  if(array==null)
  		  return null;
  	  for(int j=0;j<array.size();j++){
  		  SuperVO vo = (SuperVO) ReflectHelper.newInstance(billMeta.getVOClass(meta));
  		  JSONObject json = (JSONObject) array.get(j);
  		  Set<String> set = json.keySet();
  		  //У���ֶ��Ƿ�һ��
  		  checkColumnName(columns,set);
  		  for(String s :set){
  			      vo.setAttributeValue(s, "".equals(json.get(s))?null:json.get(s));//�����""����null
  		  }
  		  list.add(vo);
  		  }
  		 
  	  if(list.size()==0)
  		  return null;
  	  SuperVO[] vos = new SuperVO[list.size()];
  	  for(int i=0;i<list.size();i++){
  		  vos[i] = list.get(i);
  	  }
  	  return vos;
    }
    /*
     * У��ҳǩ���Ƿ��NC��һ��
     */
    private  void checkTabCode(List<String> list,Set<String> sets) throws BusinessException{
  	  for(String s:sets){
  		  if(!list.contains(s))
  			  throw new BusinessException("ҳǩ��Ϊ"+s+"�����ݿ����ҳǩ����һ�£�");
  	  }
    }
    /**
     * У���ֶ��Ƿ�һ��,���в�һ���ֶ������set���Ƴ�
     * @param columns
     * @param sets
     * @throws BusinessException
     */
    private  void checkColumnName(IColumnMeta[] columns,Set<String> sets) throws BusinessException{
  	  List<String> list = new ArrayList<String>();
  	  for(int i=0;i<columns.length;i++){
  		  list.add(columns[i].getName());
  	  }
  	  Iterator<String> it = sets.iterator();
  	  while(it.hasNext())
  		  if(!list.contains(it.next())){
  			  it.remove();
  		  }
  	  }
    
    private boolean checkColumnIsExist(IColumnMeta[] columns,String column) throws BusinessException{
    	  List<String> list = new ArrayList<String>();
    	  for(int i=0;i<columns.length;i++){
    		  list.add(columns[i].getName());
    	  }
    	  
		  if(list.contains(column)){
			  return true;
		  }
    	 return false;
     }
}