//package com.hytx.bobo.config.even;
//
///**
// * 事件处理对象
// *
// * @author lixiaodong
// */
//public enum EventTypeHandler {
//
//    /**
//     * 下载对账单
//     */
//    EVENT_DOWNLOAD_STATEMENT_HANDLER(EventType.EVENT_DOWNLOAD_BILL),
//
//    /**
//     * 下载资金账单
//     */
//    EVENT_DOWNLOAD_FINANCIAL_BILL_HANDLER(EventType.EVENT_DOWNLOAD_BILL),
//
//    /**
//     * 订单转正计算收益监听
//     */
//    OUTLET_EVENT_CUSTOMER_ORDER_FINISHED_CALCULATE_REVENUE(EventType.OUTLET_EVENT_CUSTOMER_ORDER_FINISHED);
//
//
//    private EventType eventType;
//
//    EventTypeHandler(EventType eventType) {
//        this.eventType = eventType;
//    }
//
//    public EventType getEventType() {
//        return eventType;
//    }
//}