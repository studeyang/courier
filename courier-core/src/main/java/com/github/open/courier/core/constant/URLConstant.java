package com.github.open.courier.core.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * URL常量
 *
 * @author Courier
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class URLConstant {

    public static final String HTTP_PREFIX = "http://";

    // ------------------------------ Client -----------------------------------------

    public static final String CLIENT_RECEIVE = "/courier/message/receive";

    public static final String CLIENT_RECEIVES = "/courier/messages/receive";

    public static final String CLIENT_METRICS = "/courier/client/metrics";

    // ------------------------------ Producer ---------------------------------------

    public static final String PRODUCER_SEND = "/courier/message/send";

    public static final String PRODUCER_SENDS = "/courier/messages/send";

    public static final String PRODUCER_SUBSCRIBE = "/courier/messages/subscribe";

    public static final String PRODUCER_UNSUBSCRIBE = "/courier/messages/unsubscribe";

    public static final String PRODUCER_REFRESH = "/courier/messages/refresh";

    public static final String PRODUCER_PAUSE_PUSH = "/courier/messages/push/pause";

    public static final String PRODUCER_RESUME_PUSH = "/courier/messages/push/resume";


    // ------------------------------ delay ---------------------------------------

    public static final String DELAY_SEND = "/courier/delaymessage/send";

    public static final String DELAY_SENDS = "/courier/delaymessages/send";

    public static final String DELAY_PROPERTIES = "/courier/delay/properties";

    public static final String DELAY_RACESIGN = "/courier/delay/racesign";

    public static final String DELAY_TIMEWHEEL_METRICS = "/courier/timewheel/metrics";


    // ------------------------------ Consumer ---------------------------------------

    public static final String CONSUMER_RECORD = "/courier/messages/record";

    public static final String CONSUMER_INSERT = "/courier/messages/insert";

    public static final String CONSUMER_BROADCAST = "/courier/messages/broadcast";

    public static final String CONSUMER_METRICS = "/courier/consumer/metrics";

    public static final String CONSUMER_PODS = "/courier/consumer/podsMap";

    public static final String CONSUMER_ALARM = "/courier/alarm";

    public static final String CONSUMER_ALARM_RECOVERY = "/courier/alarm/recovery";

    // ------------------------------ Management -------------------------------------

    public static final String MANAGEMENT_SEND_FAIL = "/courier/failmessage/send";

    public static final String MANAGEMENT_SEND_FAILS = "/courier/failmessages/send";

    public static final String MANAGEMENT_HANDLE_SUCCESS = "/courier/successmessages/handle";

    public static final String MANAGEMENT_HANDLE_FAIL = "/courier/failmessages/handle";

    public static final String MANAGEMENT_RESEND = "/courier/messages/resend";

    public static final String MANAGEMENT_RECONSUME = "/courier/messages/reconsume";

    public static final String MANAGEMENT_METRICS = "/courier/management/metrics";

    public static final String MANAGEMENT_QUERY_SEND_SUCCESS = "/courier/query/send/success";

    public static final String MANAGEMENT_QUERY_SEND_FAIL = "/courier/query/send/fail";

    public static final String MANAGEMENT_QUERY_CONSUME_SUCCESS = "/courier/query/consume/success";

    public static final String MANAGEMENT_QUERY_CONSUME_FAIL = "/courier/query/consume/fail";

    public static final String MANAGEMENT_QUERY_PAGE_SEND_SUCCESS = "/courier/query/pageHelper/send/success";

    public static final String MANAGEMENT_QUERY_PAGE_SEND_FAIL = "/courier/query/pageHelper/send/fail";

    public static final String MANAGEMENT_QUERY_PAGE_CONSUME_SUCCESS = "/courier/query/pageHelper/consume/success";

    public static final String MANAGEMENT_QUERY_PAGE_CONSUME_FAIL = "/courier/query/pageHelper/consume/fail";

    public static final String MANAGEMENT_SUBSCRIBE_BIND = "/courier/management/subscribeBind";

    public static final String MANAGEMENT_QUERY_ALL_SUBSCRIBE_MANAGE = "/courier/query/allSubscribeManage";

    public static final String MANAGEMENT_QUERY_SUBSCRIBE_MANAGE = "/courier/query/oneSubscribeManage";

    public static final String MANAGEMENT_TABLE = "/courier/table";

    public static final String MANAGEMENT_SUBSCRIBE_METADATAS_PAGE= "/courier/subscribe/metadatas/page";

    public static final String MANAGEMENT_SUBSCRIBE_ENVTAG_ASSIGN = "/courier/subscribe/envtag/assign";

    public static final String MANAGEMENT_SUBSCRIBE_ENVTAG_CLEAR = "/courier/subscribe/envtag/clear";

    public static final String MANAGEMENT_SUBSCRIBECLUSTERS_METADATA_CLUSTERS = "/courier/subscribecluster/metadata/clusters";

    public static final String MANAGEMENT_SUBSCRIBECLUSTERS_METADATA_ENVS = "/courier/subscribecluster/metadata/envs";

    // alarm

    public static final String MANAGEMENT_ALARM_LIST = "/courier/alarm/list";

    public static final String MANAGEMENT_ALARM = "/courier/alarm";

}
