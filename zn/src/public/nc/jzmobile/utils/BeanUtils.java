package nc.jzmobile.utils;

import java.lang.reflect.Field;

import nc.bs.logging.Logger;
import nc.vo.jzmobile.template.MobileSuperVO;

/**
 * �Ա�׼Bean(POJO)����Ĳ�������
 * 
 */

public class BeanUtils {


	/**
	 * ���propNameָ���ֶε����͡� ע��:��ָ���������������ֶ�
	 * 
	 * @param clazz ָ�����Ķ�������
	 * @param propName ָ���������͵��ֶ���
	 * @return �ֶε����Ͷ���
	 */
	@SuppressWarnings({ "rawtypes" })
	public static Class getFieldType(Class clazz, String propName) {

		if(clazz == null || propName == null)
			return null;
		
		//������ΪMobileSuperVOʱ����null,�����в������κ�ֵ��
		if(clazz.equals(MobileSuperVO.class))
			return null;

		Field field = null;

		try {
			field = clazz.getDeclaredField(propName);

		} catch (NoSuchFieldException e) {
			//class��û���ҵ�propName����ʱ���׳�NoSuchFieldException�쳣,�ʲ�����
		} catch(Exception e){
			Logger.error(e.getMessage());
		}

		if (field == null) {
			
			//�ݹ����getFieldType����,��clazz�ĳ�����Ϊ��������
			Class classfiled = getFieldType(clazz.getSuperclass(),propName);
			 if(null == classfiled){
				 return null;
			 }else{
				 return classfiled;
			 }

		}

		return field.getClass();

	}


}
