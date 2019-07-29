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
		//��дsetAttributeValue����,��vo��û�ж����ֵ�����map��
		if(null==BeanUtils.getFieldType(this.getClass(), attributeName)){
			valuemap.put(attributeName, value);
		}else{
			super.setAttributeValue(attributeName, value);
		}
	}
	
	@Override
	public Object getAttributeValue(String attributeName) {
		//��дgetAttributeValue����,vo��û�ж����ֵ���Ӧ��ֵ��map����ȡ
		if(null==BeanUtils.getFieldType(this.getClass(), attributeName)){
			return valuemap.get(attributeName);
		}else{
			return super.getAttributeValue(attributeName);
		}
	}
}
