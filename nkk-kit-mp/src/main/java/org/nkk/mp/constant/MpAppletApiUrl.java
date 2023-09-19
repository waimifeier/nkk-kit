package org.nkk.mp.constant;

import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

/**
 * <p>小程序api常量</p>
 * @author Nkk
 * @time 2023-8-25 11:11:24
 */
@UtilityClass
public class MpAppletApiUrl {

    /**
     * 接口域名
     */
    private static final String BASE_URL = "https://api.weixin.qq.com";


    public interface Base{
        /**
         * 获取访问凭证
         */
        String GET_ACCESS_TOKEN = "https://api.weixin.qq.com/cgi-bin/stable_token";
    }

    /**
     * <p>小程序用户相关接口</p>
     * @author Nkk
     */
    public interface User {

        /**
         * 获取手机号
         */
        String GET_PHONE_NUMBER_URL = StrUtil.concat(true,BASE_URL,"/wxa/business/getuserphonenumber");
    }


    /**
     * <p>小程序二维码</p>
     * @author Nkk
     *
     */
    public interface Qrcode {

        /**
         * 创建小程序二维码
         */
        String CREATE_QRCODE_URL = StrUtil.concat(true,BASE_URL,"/cgi-bin/wxaapp/createwxaqrcode");

        /**
         * 获取小程序二维码
         */
        String GET_WXACODE_URL =  StrUtil.concat(true,BASE_URL,"/wxa/getwxacode") ;

        /**
         * todo.....
         */
        String GET_WXACODE_UNLIMIT_URL = StrUtil.concat(true,BASE_URL,"/wxa/getwxacodeunlimit") ;

    }

    /**
     * <p>小程序支付相关</p>
     * @author Nkk
     */
    public interface Pay {
        /**
         * 创建预付订单
         */
        String CREATE_ORDER = StrUtil.concat(true,BASE_URL,"/shop/pay/createorder");

        /**
         * 获取订单
         */
        String GET_ORDER = StrUtil.concat(true,BASE_URL,"/shop/pay/getorder");

        /**
         * 发起退款
         */
        String REFUND_ORDER = StrUtil.concat(true,BASE_URL,"/shop/pay/refundorder") ;
    }

    /**
     * <p>小程序即时配送</p>
     * @author Nkk
     */
    public interface InstantDelivery {

        interface Base {

            /**
             * 拉取已绑定账号. <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/immediate-delivery/by-business/immediateDelivery.getBindAccount.html">文档地址</a>
             *
             */
            String GET_BIND_ACCOUNT =  StrUtil.concat(true,BASE_URL,"/cgi-bin/express/local/business/shop/get") ;

            /**
             * 拉取配送单信息.  <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/immediate-delivery/by-business/immediateDelivery.getOrder.html">文档地址</a>
             *
             */
            String GET_ORDER = StrUtil.concat(true,BASE_URL,"/cgi-bin/express/local/business/order/get") ;

            /**
             * 模拟配送公司更新配送单状态. <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/immediate-delivery/by-business/immediateDelivery.mockUpdateOrder.html">文档地址</a>
             *
             */
            String MOCK_UPDATE_ORDER =  StrUtil.concat(true,BASE_URL,"/cgi-bin/express/local/business/test_update_order");

            /**
             * 物流服务-查询组件-跟踪物流面单
             * 商户使用此接口向微信提供某交易单号对应的运单号。微信后台会跟踪运单的状态变化
             */
            String TRACE_WAYBILL_URL = StrUtil.concat(true,BASE_URL,"/cgi-bin/express/delivery/open_msg/trace_waybill") ;


            /**
             * 物流服务-查询组件-查询运单接口 query_trace
             * 商户在调用完trace_waybill接口后，可以使用本接口查询到对应运单的详情信息
             */
            String QUERY_WAYBILL_TRACE_URL = StrUtil.concat(true,BASE_URL,"/cgi-bin/express/delivery/open_msg/query_trace") ;

            /**
             * 物流服务-消息组件-传运单接口(订阅消息) follow_waybill
             * 商户在调用完trace_waybill接口后，可以使用本接口查询到对应运单的详情信息
             */
            String FOLLOW_WAYBILL_URL =  StrUtil.concat(true,BASE_URL,"/cgi-bin/express/delivery/open_msg/follow_waybill");

            /**
             * 物流服务-消息组件-查运单接口(订阅消息) query_follow_trace
             * 商户在调用完trace_waybill接口后，可以使用本接口查询到对应运单的详情信息
             */
            String QUERY_FOLLOW_TRACE_URL = StrUtil.concat(true,BASE_URL,"/cgi-bin/express/delivery/open_msg/query_follow_trace") ;

        }

        /**
         * 下单接口.
         */
        interface PlaceAnOrder {

            /**
             * 获取已支持的配送公司列表接口
             * <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/immediate-delivery/by-business/immediateDelivery.getAllImmeDelivery.html">文档地址</a>
             */
            String GET_ALL_IMME_DELIVERY = StrUtil.concat(true,BASE_URL,"/cgi-bin/express/local/business/delivery/getall") ;

            /**
             * 预下配送单接口
             * <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/immediate-delivery/by-business/immediateDelivery.preAddOrder.html">文档地址</a>
             */
            String PRE_ADD_ORDER =  StrUtil.concat(true,BASE_URL,"/cgi-bin/express/local/business/order/pre_add");

            /**
             * 下配送单接口.
             * <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/immediate-delivery/by-business/immediateDelivery.addOrder.html">文档地址</a>
             */
            String ADD_ORDER =  StrUtil.concat(true,BASE_URL,"/cgi-bin/express/local/business/order/add");

            /**
             * 重新下单
             * <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/immediate-delivery/by-business/immediateDelivery.reOrder.html">文档地址</a>
             */
            String RE_ORDER = StrUtil.concat(true,BASE_URL,"//cgi-bin/express/local/business/order/readd") ;

            /**
             * 增加小费  <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/immediate-delivery/by-business/immediateDelivery.addTip.html">文档地址</a>
             * <p>可以对待接单状态的订单增加小费。需要注意：订单的小费，以最新一次加小费动作的金额为准，故下一次增加小费额必须大于上一次小费额.</p>
             *
             */
            String ADD_TIP =  StrUtil.concat(true,BASE_URL,"/cgi-bin/express/local/business/order/addtips");

        }

        /**
         * 取消接口.
         */
        interface Cancel {

            /**
             * 预取消配送单接口
             * <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/immediate-delivery/by-business/immediateDelivery.preCancelOrder.html">文档地址</a>
             */
            String PRE_CANCEL_ORDER =  StrUtil.concat(true,BASE_URL,"/cgi-bin/express/local/business/order/precancel");

            /**
             * 取消配送单接口
             * <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/immediate-delivery/by-business/immediateDelivery.cancelOrder.html">文档地址</a>
             */
            String CANCEL_ORDER =  StrUtil.concat(true,BASE_URL,"/cgi-bin/express/local/business/order/cancel");

            /**
             * 异常件退回商家商家确认收货接口
             * <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/immediate-delivery/by-business/immediateDelivery.abnormalConfirm.html">文档地址</a>
             */
            String ABNORMAL_CONFIRM = StrUtil.concat(true,BASE_URL,"/cgi-bin/express/local/business/order/confirm_return") ;

        }


        /**
         * 安全风控
         */
        interface SafetyRiskControl {
            /**
             * 获取用户的安全等级，无需用户授权
             */
            String GET_USER_RISK_RANK =  StrUtil.concat(true,BASE_URL,"/wxa/getuserriskrank");
        }

    }

}
