//package com.hytx.bobo.config.even;
//
//import java.util.Objects;
//
//import com.winhxd.b2c.common.domain.order.model.OrderInfo;
//import com.winhxd.b2c.common.domain.order.vo.OrderRebateRecordVO;
//import com.winhxd.b2c.common.domain.pay.condition.DownloadStatementCondition;
//
///**
// * 事件类型
// *
// * @author lixiaodong
// */
//public enum EventType {
//
//    /**
//     * 用户订单完成事件
//     */
//    OUTLET_EVENT_CUSTOMER_ORDER_FINISHED(OrderRebateRecordVO.class),
//
//
//    /**
//     * 下载账单事件
//     */
//    EVENT_DOWNLOAD_BILL(DownloadStatementCondition.class);
//
//    private Class<?> eventObjectClass;
//
//    EventType(Class<?> eventObjectClass) {
//        Objects.requireNonNull(eventObjectClass);
//        this.eventObjectClass = eventObjectClass;
//    }
//
//    public Class<?> getEventObjectClass() {
//        return eventObjectClass;
//    }
//}