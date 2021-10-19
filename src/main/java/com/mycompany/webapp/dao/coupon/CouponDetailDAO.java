package com.mycompany.webapp.dao.coupon;

import org.apache.ibatis.annotations.Mapper;

import com.mycompany.webapp.dto.coupon.CouponDTO;
import com.mycompany.webapp.dto.coupon.CouponDetailDTO;

@Mapper
public interface CouponDetailDAO {
	public CouponDTO selectCouponDetailByCoupon(CouponDTO couponDTO);
	public void insertCouponDetail(CouponDTO couponDTO);
	public void insertCouponDetailNoValidity(CouponDTO couponDTO);
	public int updateStateByCouponDetail(CouponDetailDTO couponDetailDTO);
	public int updateStateByOrderDetailNo(String orderDetailNo);
	int insertCouponDetailByCouponDetail(CouponDetailDTO couponDetailDTO);
	int selectCountByCouponDetail(CouponDetailDTO coupon);
}
