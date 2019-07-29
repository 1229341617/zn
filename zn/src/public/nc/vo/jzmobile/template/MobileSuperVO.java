package nc.vo.jzmobile.template;

import java.util.HashMap;
import java.util.Map;

import nc.jzmobile.utils.BeanUtils;
import nc.vo.pub.SuperVO;

@SuppressWarnings("serial")
public abstract class MobileSuperVO extends SuperVO{

protected Map<String,Object> valuemap = new HashMap<String,Object>();
	
	@Override
	public void setAttributeValue(String attributeName, Object value) {
		//重写setAttributeValue方法,将vo中没有定义的值域存入map中
		if(null==BeanUtils.getFieldType(this.getClass(), attributeName)){
			valuemap.put(attributeName, value);
		}else{
			super.setAttributeValue(attributeName, value);
		}
	}
	
	@Override
	public Object getAttributeValue(String attributeName) {
		//重写getAttributeValue方法,vo中没有定义的值域对应的值从map中提取
		if(null==BeanUtils.getFieldType(this.getClass(), attributeName)){
			return valuemap.get(attributeName);
		}else{
			return super.getAttributeValue(attributeName);
		}
	}
}
