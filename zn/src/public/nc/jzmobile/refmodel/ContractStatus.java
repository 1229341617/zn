package nc.jzmobile.refmodel;

/**
 * 供应链、资金、建筑中关于合同单据状态、合同状态的定义各不相同。
 * 在参照中进行了适配 
 * 供合同参照用 
 * @author yangsyc
 *
 */
public interface ContractStatus {

	/**
	 * 自由态
	 */
	public static final int FREE = 0;
	/**
	 * 生效
	 */
	public static final int ACTIVE = 1;
	/**
	 *审批通过
	 */
	public static final int CHECKPASS = 2 ;
	/**
	 * 未生效
	 */
	public static final int UNACTIVE = 3;
	/**
	 * 冻结
	 */
	public static final int FREEZE = 4;
	/**
	 * 关闭
	 */
	public static final int CLOSE = 5;
	/**
	 * 完成
	 */
	public static final int FINISH = 6;
}
