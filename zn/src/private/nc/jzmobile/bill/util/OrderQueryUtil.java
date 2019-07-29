package nc.jzmobile.bill.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nc.impl.pubapp.pattern.data.vo.VOQuery;
import nc.impl.pubapp.pattern.database.DataAccessUtils;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pu.m21.entity.OrderHeaderVO;
import nc.vo.pu.m21.entity.OrderItemVO;
import nc.vo.pu.m21.entity.OrderReceivePlanVO;
import nc.vo.pu.m21.entity.OrderVO;
import nc.vo.pu.m21.entity.StatusOnWayItemVO;
import nc.vo.pu.m21.pub.SplitOrderVOUtil;
import nc.vo.pu.m21transtype.enumeration.OnwayStatus;
import nc.vo.pu.pub.util.CirVOUtil;
import nc.vo.pu.pub.util.VOSortUtils;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pubapp.pattern.data.IRowSet;
import nc.vo.pubapp.pattern.model.tool.BillComposite;
import nc.vo.pubapp.pattern.pub.MathTool;

public class OrderQueryUtil {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String[] queryFor45_23(String sql, UFBoolean isLazy) {

		DataAccessUtils utils = new DataAccessUtils();
		IRowSet rowset = utils.query(sql);
		Set<String> headids = new HashSet();
		Set<String> itemids = new HashSet();
		Set<String> bb1ids = new HashSet();

		Set<String> onwayPks = new HashSet();
		while (rowset.next()) {
			if (!StringUtil.isEmptyWithTrim(rowset.getString(0))) {
				headids.add(rowset.getString(0));
			}
			if (!StringUtil.isEmptyWithTrim(rowset.getString(1))) {
				itemids.add(rowset.getString(1));
			}
			if (!StringUtil.isEmptyWithTrim(rowset.getString(2))) {
				bb1ids.add(rowset.getString(2));
			}
			if (!StringUtil.isEmptyWithTrim(rowset.getString(3))) {
				onwayPks.add(rowset.getString(3));
			}
		}
		if ((0 == headids.size()) || (0 == itemids.size())) {
			return null;
		}

		if (isLazy.booleanValue()) {
		}

		OrderHeaderVO[] headers = (OrderHeaderVO[]) new VOQuery(
				OrderHeaderVO.class).query((String[]) headids
				.toArray(new String[headids.size()]));

		OrderItemVO[] items = (OrderItemVO[]) new VOQuery(OrderItemVO.class)
				.query((String[]) itemids.toArray(new String[itemids.size()]));

		StatusOnWayItemVO[] onwayVOs = (StatusOnWayItemVO[]) new VOQuery(
				StatusOnWayItemVO.class).query((String[]) onwayPks
				.toArray(new String[onwayPks.size()]));
		Map<String, OrderItemVO> map = CirVOUtil.createKeyVOMap(items);
		for (StatusOnWayItemVO onwayVO : onwayVOs) {
			String pk_order_b = onwayVO.getPk_order_b();
			OrderItemVO itemVO = (OrderItemVO) map.get(pk_order_b);
			if (null != itemVO) {

				UFDouble nonwaynum = onwayVO.getNonwaynum();
				if (OnwayStatus.STATUS_ARRIVE.toInt() == onwayVO
						.getFonwaystatus().intValue()) {
					itemVO.setNcanarrivenum(MathTool.sub(nonwaynum,
							itemVO.getNaccumarrvnum()));
				}

				if (OnwayStatus.STATUS_STORE.toInt() == onwayVO
						.getFonwaystatus().intValue()) {
					itemVO.setNcaninnum(MathTool.sub(nonwaynum,
							itemVO.getNaccumstorenum()));
				}
			}
		}

		BillComposite<OrderVO> bc = new BillComposite(OrderVO.class);
		OrderVO tempVO = new OrderVO();
		bc.append(tempVO.getMetaData().getParent(), headers);
		bc.append(tempVO.getMetaData().getVOMeta(OrderItemVO.class), items);
		OrderVO[] orderVOs = (OrderVO[]) bc.composite();
		if (bb1ids.isEmpty()) {
			sort(orderVOs);
			String[] pk = new String[orderVOs.length];
			for (int i = 0; i < pk.length; i++) {
				pk[i] = orderVOs[i].getPrimaryKey();
			}
			return pk;
		}

		OrderReceivePlanVO[] rpVOs = (OrderReceivePlanVO[]) new VOQuery(
				OrderReceivePlanVO.class).query((String[]) bb1ids
				.toArray(new String[bb1ids.size()]));
		orderVOs = SplitOrderVOUtil.getInstance().splitOrderVOByRPVOs(orderVOs,
				rpVOs);

		sort(orderVOs);

		String[] pk = new String[orderVOs.length];
		for (int i = 0; i < pk.length; i++) {
			pk[i] = orderVOs[i].getPrimaryKey();
		}
		return pk;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static OrderVO[] queryFor45_23Order(String sql, UFBoolean isLazy) {

		DataAccessUtils utils = new DataAccessUtils();
		IRowSet rowset = utils.query(sql);
		Set<String> headids = new HashSet();
		Set<String> itemids = new HashSet();
		Set<String> bb1ids = new HashSet();

		Set<String> onwayPks = new HashSet();
		while (rowset.next()) {
			if (!StringUtil.isEmptyWithTrim(rowset.getString(0))) {
				headids.add(rowset.getString(0));
			}
			if (!StringUtil.isEmptyWithTrim(rowset.getString(1))) {
				itemids.add(rowset.getString(1));
			}
			if (!StringUtil.isEmptyWithTrim(rowset.getString(2))) {
				bb1ids.add(rowset.getString(2));
			}
			if (!StringUtil.isEmptyWithTrim(rowset.getString(3))) {
				onwayPks.add(rowset.getString(3));
			}
		}
		if ((0 == headids.size()) || (0 == itemids.size())) {
			return null;
		}

		if (isLazy.booleanValue()) {
		}

		OrderHeaderVO[] headers = (OrderHeaderVO[]) new VOQuery(
				OrderHeaderVO.class).query((String[]) headids
				.toArray(new String[headids.size()]));

		OrderItemVO[] items = (OrderItemVO[]) new VOQuery(OrderItemVO.class)
				.query((String[]) itemids.toArray(new String[itemids.size()]));

		StatusOnWayItemVO[] onwayVOs = (StatusOnWayItemVO[]) new VOQuery(
				StatusOnWayItemVO.class).query((String[]) onwayPks
				.toArray(new String[onwayPks.size()]));
		Map<String, OrderItemVO> map = CirVOUtil.createKeyVOMap(items);
		for (StatusOnWayItemVO onwayVO : onwayVOs) {
			String pk_order_b = onwayVO.getPk_order_b();
			OrderItemVO itemVO = (OrderItemVO) map.get(pk_order_b);
			if (null != itemVO) {

				UFDouble nonwaynum = onwayVO.getNonwaynum();
				if (OnwayStatus.STATUS_ARRIVE.toInt() == onwayVO
						.getFonwaystatus().intValue()) {
					itemVO.setNcanarrivenum(MathTool.sub(nonwaynum,
							itemVO.getNaccumarrvnum()));
				}

				if (OnwayStatus.STATUS_STORE.toInt() == onwayVO
						.getFonwaystatus().intValue()) {
					itemVO.setNcaninnum(MathTool.sub(nonwaynum,
							itemVO.getNaccumstorenum()));
				}
			}
		}

		BillComposite<OrderVO> bc = new BillComposite(OrderVO.class);
		OrderVO tempVO = new OrderVO();
		bc.append(tempVO.getMetaData().getParent(), headers);
		bc.append(tempVO.getMetaData().getVOMeta(OrderItemVO.class), items);
		OrderVO[] orderVOs = (OrderVO[]) bc.composite();
		if (bb1ids.isEmpty()) {
			sort(orderVOs);
			return orderVOs;

		}

		OrderReceivePlanVO[] rpVOs = (OrderReceivePlanVO[]) new VOQuery(
				OrderReceivePlanVO.class).query((String[]) bb1ids
				.toArray(new String[bb1ids.size()]));
		orderVOs = SplitOrderVOUtil.getInstance().splitOrderVOByRPVOs(orderVOs,
				rpVOs);

		sort(orderVOs);
		return orderVOs;
	}

	private static void sort(OrderVO[] orderVOs) {
		for (OrderVO orderVO : orderVOs) {
			OrderItemVO[] itemVOs = orderVO.getBVO();
			VOSortUtils.ascSort(itemVOs, "crowno");
			orderVO.setBVO(itemVOs);
		}
	}

}
