package nc.jzmobile.refmodel;

/**
 * ��Ӧ�����ʽ𡢽����й��ں�ͬ����״̬����ͬ״̬�Ķ��������ͬ��
 * �ڲ����н��������� 
 * ����ͬ������ 
 * @author yangsyc
 *
 */
public interface ContractStatus {

	/**
	 * ����̬
	 */
	public static final int FREE = 0;
	/**
	 * ��Ч
	 */
	public static final int ACTIVE = 1;
	/**
	 *����ͨ��
	 */
	public static final int CHECKPASS = 2 ;
	/**
	 * δ��Ч
	 */
	public static final int UNACTIVE = 3;
	/**
	 * ����
	 */
	public static final int FREEZE = 4;
	/**
	 * �ر�
	 */
	public static final int CLOSE = 5;
	/**
	 * ���
	 */
	public static final int FINISH = 6;
}
