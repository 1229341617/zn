package nc.jzmobile.bill.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.mobile.app.IMobilebillExecute;
import nc.jzmobile.bill.util.BillMetaUtil;
import nc.jzmobile.bill.util.JudgeExistPushBillUtil;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.vo.am.common.util.StringUtils;
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
 * 保存操作Handler
 * @author wangruin on 2017/8/21
 *
 */
public class BillSaveHandler implements INCMobileServletHandler{

	@Override
	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		Logger.info("---BillSaveHandler  start---");
		
		
		
		
		try {
			String userId = map.get("userid");
			if (userId == null) {
				throw new BusinessException("用户信息不能为空！");
			}
			String billType = map.get("billtype");
			if (billType == null) {
				throw new BusinessException("单据类型不能为空！");
			}
			
			/**用于查看附件*/
			String taskId = map.get("taskid");
			
			
			
			InvocationInfoProxy.getInstance().setGroupId(
					JZMobileAppUtils.getPkGroupByUserId(userId));
			IMobilebillExecute service = NCLocator.getInstance().lookup(IMobilebillExecute.class);
			
			/**存在推多条情况*/
			String pushmore = map.get("pushmore");
			if(pushmore!= null && "1".equals(pushmore)){
				AggregatedValueObject[] aggvos = changeMap2AggVOPushMore(map, billType,userId);
				/**推单情况下,不允许存在重复推单*/
				for(int i = 0 ;i <= aggvos.length ;i++){
					boolean isExist = JudgeExistPushBillUtil.isExistQS(billType,aggvos[i].getParentVO().getAttributeValue("csrcid").toString());
				    if(isExist){
				    	throw new BusinessException("该单据已经生成下游单据，不允许重复推单！");
				    }
				}
				AggregatedValueObject[] resultVOs = service.saveBatch(aggvos, billType);
				
				if(resultVOs==null)
					throw new BusinessException("返回的表头数据为空，保存失败！");
				String[] taskids = new String[aggvos.length];
				for(int i = 0 ; i<resultVOs.length;i++){
					taskids[i] = resultVOs[i].getParentVO().getPrimaryKey();
				}
				result.success().setData(taskids);
				return result;
			}
			
			
			/**单据处于修改状态*/
			String isEdit = map.get("isEdit");
			AggregatedValueObject aggaVO = null;
			if(isEdit != null && "1".equals(isEdit)){
				aggaVO = changeMap2AggVOE(map, billType,userId);
			
			/**单据处于新增状态*/
			}else{
				aggaVO = changeMap2AggVO(map, billType,userId);
				
				boolean isExist = JudgeExistPushBillUtil.isExistQS(billType,aggaVO.getParentVO().getAttributeValue("csrcid").toString());
			    
				if(isExist){
			    	throw new BusinessException("该单据已经生成下游单据，不允许重复推单！");
			    }
			}
			
			
			//根据map组装aggvo
			AggregatedValueObject resultVO = service.saveBill(aggaVO, billType); 
			if(resultVO.getParentVO()==null)
				throw new BusinessException("返回的表头数据为空，保存失败！");
			
			if(!StringUtils.isEmpty(taskId)){
				updateImage(taskId,resultVO.getParentVO().getPrimaryKey());
		    }
			result.success().setData(resultVO.getParentVO().getPrimaryKey());
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}

		return result;
	}
	
     private void updateImage(String tempid,String pk){
		
		BaseDAO dao = new BaseDAO();
		String sql = "UPDATE sm_pub_filesystem SET filepath=REPLACE(filepath, '"+tempid+"', '"+pk+"'); ";
		
		try{
			dao.executeUpdate(sql);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
	public  AggregatedValueObject[] changeMap2AggVOPushMore(Map<String, String> map,String billType,String userId)throws Exception{
    	
		//String aggvo = map.get("aggvos");
		JSONArray jsonArray = JSON.parseArray(map.get("aggvos").toString());
	
	  	if(jsonArray==null||jsonArray.equals("")||jsonArray.size()==0)
	  		return null;
	  	AggregatedValueObject[] aggvos = new AggregatedValueObject[jsonArray.size()];
	  	
    	IBillMeta billMeta = MetaTool.getBillMeta((Class<? extends IBill>) Class.forName(BillMetaUtil.getAggVOFullClassName(billType)));
    	IVOMeta[] metas = billMeta.getChildren();
	  	
	  	for(int i=0 ;i<jsonArray.size();i++){
	  		AggregatedValueObject billVO = (AggregatedValueObject) ((AggregatedValueObject) Class
					.forName(BillMetaUtil.getAggVOFullClassName(billType)).newInstance());
	  		JSONObject jsonObject = jsonArray.getJSONObject(i);
	  		billVO.setParentVO(getJson2ParentVOPushMore(map,jsonObject.getJSONObject("headdata"),billMeta,userId));
	  		for(IVOMeta meta:metas){
	      	  billVO.setChildrenVO(getJson2ChildrenVOsPushMore(map,jsonObject.getJSONObject("bodydata"),billMeta, meta,billType));
	      	}
	  		aggvos[i] = billVO;	
	  	}
	  	
	  	return aggvos;
    }
	
	
	
	/**
     * 将map中的表头表体数据转化为对应的AggVO
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
    
    @SuppressWarnings("unchecked")
	public  AggregatedValueObject changeMap2AggVOE(Map<String, String> map,String billType,String userId)throws Exception{
    	AggregatedValueObject billVO = (AggregatedValueObject) ((AggregatedValueObject) Class
				.forName(BillMetaUtil.getAggVOFullClassName(billType)).newInstance());
    	IBillMeta billMeta = MetaTool.getBillMeta((Class<? extends IBill>) Class.forName(BillMetaUtil.getAggVOFullClassName(billType)));
    	IVOMeta[] metas = billMeta.getChildren();
    	billVO.setParentVO(getJson2ParentVOE(map,billMeta,userId));
    	for(IVOMeta meta:metas){
    	  billVO.setChildrenVO(getJson2ChildrenVOsE(map,billMeta, meta,billType));
    	}
    	return billVO;
    	
    }
    
    /**
     * 组装表头
     * @param parentStr
     * @return
   * @throws Exception 
     */
    private  SuperVO getJson2ParentVOPushMore(Map<String, String> map,JSONObject jsonObject,IBillMeta billMeta,String userId)throws Exception{
  	  SuperVO parentVO = (SuperVO) ReflectHelper.newInstance(billMeta.getVOClass(billMeta.getParent()));
  	  IColumnMeta[] columns = billMeta.getParent().getStatisticInfo().getTables()[0].getColumns();
  	  //JSONObject jsonObject = JSON.parseObject(map.get("headdata"));
  	  if(jsonObject==null)
  		  throw new BusinessException("参数中ke为headdata的value值不能为空");
  	  Set<String> sets = jsonObject.keySet(); 
  	  for(String str:sets){
  		  JSONObject json = (JSONObject) jsonObject.get(str);
  		  Set<String> set = json.keySet();
  		  checkColumnName(columns,set);
  		  for(String s :set){
  			      parentVO.setAttributeValue(s, "".equals(json.get(s))?null:json.get(s));
  		  }
  	  }
  	  parentVO.setAttributeValue("billmaker",map.get("userid"));
  	  parentVO.setAttributeValue("creator",map.get("userid"));
  	  parentVO.setAttributeValue("creationtime",new UFDateTime());
  	  parentVO.setAttributeValue("fstatusflag",-1);
  	  parentVO.setAttributeValue("pk_group", JZMobileAppUtils.getPkGroupByUserId(userId));
  	  return parentVO;
    }
    
    /**
     * 组装表头
     * @param parentStr
     * @return
   * @throws Exception 
     */
    private  SuperVO getJson2ParentVO(Map<String, String> map,IBillMeta billMeta,String userId)throws Exception{
  	  SuperVO parentVO = (SuperVO) ReflectHelper.newInstance(billMeta.getVOClass(billMeta.getParent()));
  	  IColumnMeta[] columns = billMeta.getParent().getStatisticInfo().getTables()[0].getColumns();
  	  JSONObject jsonObject = JSON.parseObject(map.get("headdata"));
  	  if(jsonObject==null)
  		  throw new BusinessException("参数中ke为headdata的value值不能为空");
  	  Set<String> sets = jsonObject.keySet(); 
  	  for(String str:sets){
  		  JSONObject json = (JSONObject) jsonObject.get(str);
  		  Set<String> set = json.keySet();
  		  checkColumnName(columns,set);
  		  for(String s :set){
  			      parentVO.setAttributeValue(s, "".equals(json.get(s))?null:json.get(s));
  		  }
  	  }
  	  parentVO.setAttributeValue("billmaker",map.get("userid"));
  	  parentVO.setAttributeValue("creator",map.get("userid"));
  	  parentVO.setAttributeValue("creationtime",new UFDateTime());
  	  parentVO.setAttributeValue("fstatusflag",-1);
  	  parentVO.setAttributeValue("pk_group", JZMobileAppUtils.getPkGroupByUserId(userId));
  	  return parentVO;
    }
    
    private  SuperVO getJson2ParentVOE(Map<String, String> map,IBillMeta billMeta,String userId)throws Exception{
    	  SuperVO parentVO = (SuperVO) ReflectHelper.newInstance(billMeta.getVOClass(billMeta.getParent()));
    	  //IColumnMeta[] columns = billMeta.getParent().getStatisticInfo().getTables()[0].getColumns();
    	  JSONObject jsonObject = JSON.parseObject(map.get("headdata"));
    	  if(jsonObject==null)
    		  throw new BusinessException("参数中ke为headdata的value值不能为空");
    	  Set<String> sets = jsonObject.keySet(); 
    	  for(String str:sets){
    		  JSONObject json = (JSONObject) jsonObject.get(str);
    		  Set<String> set = json.keySet();
    		  //checkColumnName(columns,set);
    		  for(String s :set){
    			  if(!"fstatusflag".equals(s)){
    				  parentVO.setAttributeValue(s, "".equals(json.get(s))?null:json.get(s));
    	  		  } 
    			      
    		  }
    	  }
    	  parentVO.setStatus(1);
    	  parentVO.setAttributeValue("billmaker",map.get("userid"));
    	  parentVO.setAttributeValue("creator",map.get("userid"));
    	  parentVO.setAttributeValue("creationtime",new UFDateTime());
    	  parentVO.setAttributeValue("fstatusflag",-1);
    	  parentVO.setAttributeValue("pk_group", JZMobileAppUtils.getPkGroupByUserId(userId));
    	  return parentVO;
      }
    
    private  SuperVO[] getJson2ChildrenVOsPushMore(Map<String, String> map,JSONObject jsonObject,IBillMeta billMeta,IVOMeta meta,String billType)throws Exception{
		  
    	  //JSONObject jsonObject = JSON.parseObject(map.get("bodydata"));
    	  if(jsonObject==null||jsonObject.equals(""))
    		  return null;
    	  List<SuperVO> list = new ArrayList<SuperVO>();
    	  Set<String> sets = jsonObject.keySet(); 
    	  
    	  IColumnMeta[] columns = meta.getStatisticInfo().getTables()[0].getColumns();
    	  //校验页签名是否和NC中一致
    	  checkTabCode(BillMetaUtil.findBillPos(map.get("billtype"),1),sets);
    	  JSONArray array =  (JSONArray) jsonObject.get(BillMetaUtil.getTabCode(meta,billType));
    	  if(array==null)
    		  return null;
    	  for(int j=0;j<array.size();j++){
    		  SuperVO vo = (SuperVO) ReflectHelper.newInstance(billMeta.getVOClass(meta));
    		  JSONObject json = (JSONObject) array.get(j);
    		  Set<String> set = json.keySet();
    		  //校验字段是否一致
    		  checkColumnName(columns,set);
    		  for(String s :set){
    			vo.setAttributeValue(s, "".equals(json.get(s))?null:json.get(s));//如果是""则置null 
    		  }
    		  vo.setAttributeValue("pk_group", JZMobileAppUtils.getPkGroupByUserId(map.get("userid")));
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
    /**
     * 组装表体
     * @param map
     * @param billMeta
     * @return
     * @throws Exception 
     */
    private  SuperVO[] getJson2ChildrenVOs(Map<String, String> map,IBillMeta billMeta,IVOMeta meta,String billType)throws Exception{
  			  
  	  JSONObject jsonObject = JSON.parseObject(map.get("bodydata"));
  	  if(jsonObject==null||jsonObject.equals(""))
  		  return null;
  	  List<SuperVO> list = new ArrayList<SuperVO>();
  	  Set<String> sets = jsonObject.keySet(); 
  	  
  	  IColumnMeta[] columns = meta.getStatisticInfo().getTables()[0].getColumns();
  	  //校验页签名是否和NC中一致
  	  checkTabCode(BillMetaUtil.findBillPos(map.get("billtype"),1),sets);
  	  JSONArray array =  (JSONArray) jsonObject.get(BillMetaUtil.getTabCode(meta,billType));
  	  if(array==null)
  		  return null;
  	  for(int j=0;j<array.size();j++){
  		  SuperVO vo = (SuperVO) ReflectHelper.newInstance(billMeta.getVOClass(meta));
  		  JSONObject json = (JSONObject) array.get(j);
  		  Set<String> set = json.keySet();
  		  //校验字段是否一致
  		  checkColumnName(columns,set);
  		  for(String s :set){
  			vo.setAttributeValue(s, "".equals(json.get(s))?null:json.get(s));//如果是""则置null 
  		  }
  		  vo.setAttributeValue("pk_group", JZMobileAppUtils.getPkGroupByUserId(map.get("userid")));
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
    
    private  SuperVO[] getJson2ChildrenVOsE(Map<String, String> map,IBillMeta billMeta,IVOMeta meta,String billType)throws Exception{
		  
    	  JSONObject jsonObject = JSON.parseObject(map.get("bodydata"));
    	  if(jsonObject==null||jsonObject.equals(""))
    		  return null;
    	  List<SuperVO> list = new ArrayList<SuperVO>();
    	  Set<String> sets = jsonObject.keySet(); 
    	  
    	  //IColumnMeta[] columns = meta.getStatisticInfo().getTables()[0].getColumns();
    	  //校验页签名是否和NC中一致
    	  checkTabCode(BillMetaUtil.findBillPos(map.get("billtype"),1),sets);
    	  JSONArray array =  (JSONArray) jsonObject.get(BillMetaUtil.getTabCode(meta,billType));
    	  if(array==null)
    		  return null;
    	  for(int j=0;j<array.size();j++){
    		  SuperVO vo = (SuperVO) ReflectHelper.newInstance(billMeta.getVOClass(meta));
    		  JSONObject json = (JSONObject) array.get(j);
    		  Set<String> set = json.keySet();
    		  //校验字段是否一致
    		  //checkColumnName(columns,set);
    		  for(String s :set){
    			      vo.setAttributeValue(s, "".equals(json.get(s))?null:json.get(s));//如果是""则置null
    		  }
    		  Object status = json.get("status");
    		  if(status != null){
    			  vo.setStatus(Integer.parseInt(status.toString()));
    		  }else{
    			  vo.setStatus(1);
    		  }
    		  
    		  vo.setAttributeValue("pk_group", JZMobileAppUtils.getPkGroupByUserId(map.get("userid")));
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
     * 校验页签名是否和NC中一致
     */
    private  void checkTabCode(List<String> list,Set<String> sets) throws BusinessException{
  	  for(String s:sets){
  		  if(!list.contains(s))
  			  throw new BusinessException("页签名为"+s+"与数据库里的页签名不一致！");
  	  }
    }
    /**
     * 校验字段是否一致,若有不一致字段则将其从set中移除
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
}
