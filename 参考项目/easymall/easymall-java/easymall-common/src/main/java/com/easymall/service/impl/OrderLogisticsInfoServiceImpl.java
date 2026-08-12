package com.easymall.service.impl;

import com.easymall.component.RedisComponent;
import com.easymall.constants.Constants;
import com.easymall.entity.config.AppConfig;
import com.easymall.entity.enums.LogisticsStatusEnum;
import com.easymall.entity.enums.OrderStatusEnum;
import com.easymall.entity.enums.PageSize;
import com.easymall.entity.po.OrderInfo;
import com.easymall.entity.po.OrderLogisticsInfo;
import com.easymall.entity.po.OrderLogisticsInfoRecord;
import com.easymall.entity.query.OrderInfoQuery;
import com.easymall.entity.query.OrderLogisticsInfoQuery;
import com.easymall.entity.query.OrderLogisticsInfoRecordQuery;
import com.easymall.entity.query.SimplePage;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.exception.BusinessException;
import com.easymall.mappers.OrderInfoMapper;
import com.easymall.mappers.OrderLogisticsInfoMapper;
import com.easymall.mappers.OrderLogisticsInfoRecordMapper;
import com.easymall.service.OrderLogisticsInfoService;
import com.easymall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 * 物流信息表 业务接口实现
 */
@Service("orderLogisticsInfoService")
public class OrderLogisticsInfoServiceImpl implements OrderLogisticsInfoService {

    @Resource
    private OrderLogisticsInfoMapper<OrderLogisticsInfo, OrderLogisticsInfoQuery> orderLogisticsInfoMapper;

    @Resource
    private OrderInfoMapper<OrderInfo, OrderInfoQuery> orderInfoMapper;

    @Resource
    private OrderLogisticsInfoRecordMapper<OrderLogisticsInfoRecord, OrderLogisticsInfoRecordQuery> orderLogisticsInfoRecordMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<OrderLogisticsInfo> findListByParam(OrderLogisticsInfoQuery param) {
        return this.orderLogisticsInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(OrderLogisticsInfoQuery param) {
        return this.orderLogisticsInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<OrderLogisticsInfo> findListByPage(OrderLogisticsInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<OrderLogisticsInfo> list = this.findListByParam(param);
        PaginationResultVO<OrderLogisticsInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(OrderLogisticsInfo bean) {
        return this.orderLogisticsInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<OrderLogisticsInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.orderLogisticsInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<OrderLogisticsInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.orderLogisticsInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(OrderLogisticsInfo bean, OrderLogisticsInfoQuery param) {
        StringTools.checkParam(param);
        return this.orderLogisticsInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(OrderLogisticsInfoQuery param) {
        StringTools.checkParam(param);
        return this.orderLogisticsInfoMapper.deleteByParam(param);
    }

    /**
     * 根据OrderId获取对象
     */
    @Override
    public OrderLogisticsInfo getOrderLogisticsInfoByOrderId(String orderId) {
        return this.orderLogisticsInfoMapper.selectByOrderId(orderId);
    }

    /**
     * 根据OrderId修改
     */
    @Override
    public Integer updateOrderLogisticsInfoByOrderId(OrderLogisticsInfo bean, String orderId) {
        return this.orderLogisticsInfoMapper.updateByOrderId(bean, orderId);
    }

    /**
     * 根据OrderId删除
     */
    @Override
    public Integer deleteOrderLogisticsInfoByOrderId(String orderId) {
        return this.orderLogisticsInfoMapper.deleteByOrderId(orderId);
    }

    @Override
    public void delivery(OrderLogisticsInfo logisticsInfo) {
        OrderInfo orderInfo = orderInfoMapper.selectByOrderId(logisticsInfo.getOrderId());
        if (orderInfo == null) {
            throw new BusinessException("订单不存在");
        }

        if (!OrderStatusEnum.PAID.getStatus().equals(orderInfo.getOrderStatus())) {
            throw new BusinessException("订单不是待发货状态");
        }

        OrderInfo updateOrderInfo = new OrderInfo();
        updateOrderInfo.setOrderStatus(OrderStatusEnum.SHIPPED.getStatus());

        OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
        orderInfoQuery.setOrderId(logisticsInfo.getOrderId());
        orderInfoQuery.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderInfoMapper.updateByParam(updateOrderInfo, orderInfoQuery);


        logisticsInfo.setLogisticsStatus(LogisticsStatusEnum.IN_TRANSIT.getStatus());
        this.orderLogisticsInfoMapper.updateByOrderId(logisticsInfo, logisticsInfo.getOrderId());

        OrderLogisticsInfo dbInfo = this.getOrderLogisticsInfoByOrderId(logisticsInfo.getOrderId());
        OrderLogisticsInfoRecord record = new OrderLogisticsInfoRecord();
        record.setRecordAddress(dbInfo.getSenderAddress());
        record.setRecordTime(new Date());
        record.setOrderId(logisticsInfo.getOrderId());
        this.orderLogisticsInfoRecordMapper.insert(record);


        //加入队列，模拟物流
        redisComponent.addOrder2LogisticsQueue(10, logisticsInfo.getOrderId());

        //发货后自动确认收货
        redisComponent.addOrder2DelayQueue(Constants.REDIS_KEY_ORDER_DELAY_QUEUE_CONFIRM, appConfig.getOrderConfirmMinute(), logisticsInfo.getOrderId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void mockOrderlogistics(String orderId) {
        String[] logisticsCenters = {
                "北京市大兴区礼贤镇礼贤物流园3号中转站",
                "上海市浦东新区祝桥镇空港六路256号中通航空中转中心",
                "广州市花都区花东镇联邦快递亚太转运中心",
                "深圳市宝安区福永街道兴围顺丰华南航空枢纽",
                "成都市双流区西航港街道物流大道中通川渝分拨中心",
                "重庆市渝北区木耳镇空港大道圆通西南航空枢纽",
                "武汉市黄陂区横店街道临空产业园韵达华中转运中心",
                "郑州市新郑市薛店镇华南城二路三志物流华中枢纽",
                "南京市江宁区禄口街道机场路1号邮政航空速递物流中心",
                "杭州市萧山区瓜沥镇杭州保税物流中心D区",
                "西安市西咸新区空港新城自贸大道京东亚洲一号西安智能物流园",
                "长沙市长沙县黄花镇机场口社区邮政速递长沙处理中心",
                "天津市武清区崔黄口镇电商物流园百世快递华北转运中心",
                "青岛市胶州市胶东街道青岛保税物流园",
                "厦门市湖里区高崎北三路航空港物流园",
                "哈尔滨市道里区太平镇空港物流园极兔速递黑龙江转运中心",
                "昆明市官渡区大板桥街道长水机场物流园区",
                "乌鲁木齐市新市区地窝堡乡国际机场物流园",
                "南宁市江南区吴圩镇机场高速路安能物流广西分拨中心",
                "兰州市安宁区沙井驿街道西北物资物流园",
                "南昌市南昌县向塘镇铁路物流基地",
                "合肥市肥西县桃花工业园顺丰合肥陆运中转场",
                "太原市小店区北格镇山西综改示范区物流园",
                "石家庄市正定县新城铺镇石家庄综合保税区物流中心",
                "长春市宽城区兴隆山镇长春铁路物流基地",
                "沈阳市苏家屯区佟沟街道沈阳桃仙国际机场物流园区",
                "海口市美兰区灵山镇海口美兰国际机场物流中心",
                "呼和浩特市赛罕区巴彦镇白塔机场物流园",
                "贵阳市龙洞堡机场物流园区",
                "宁波市江北区洪塘街道宁波电商物流中心",
                "佛山市南海区狮山镇佛山机场物流园区",
                "无锡市新吴区硕放街道苏南国际机场物流中心",
                "大连市甘井子区大连湾街道大连港集装箱物流园",
                "济南市历城区荷花路街道济南国际机场物流园区",
                "温州市龙湾区机场大道温州航空物流园",
                "福州市长乐区漳港街道福州保税区物流园",
                "泉州市晋江市磁灶镇泉州陆地港",
                "长沙市浏阳市永安镇长沙黄花综合保税区",
                "青岛市即墨区蓝村镇青岛即墨国际陆港",
                "成都市青白江区城厢镇成都铁路集装箱中心站",
                "武汉市东西湖区走马岭街道武汉铁路集装箱中心站",
                "郑州市管城回族区航海东路郑州铁路集装箱中心站",
                "苏州市姑苏区金阊新城苏州铁路西站物流园",
                "南京市浦口区桥林街道南京铁路集装箱中心站",
                "东莞市麻涌镇新沙港后方物流园",
                "中山市火炬开发区中山港货运联营有限公司",
                "珠海市香洲区洪湾港珠海保税区物流园",
                "江门市蓬江区荷塘镇江门物流中心站",
                "惠州市惠阳区淡水街道惠州物流集散中心",
                "湛江市霞山区临港工业园湛江港物流中心",
                "漳州市龙海市角美镇漳州台商投资区物流园"
        };

        OrderInfo orderInfo = orderInfoMapper.selectByOrderId(orderId);
        if (orderInfo == null) {
            return;
        }
        if (OrderStatusEnum.REFUNDED.getStatus().equals(orderInfo.getOrderStatus())) {
            OrderLogisticsInfo logisticsInfo = new OrderLogisticsInfo();
            logisticsInfo.setLogisticsStatus(LogisticsStatusEnum.CANCELLED.getStatus());
            this.orderLogisticsInfoMapper.updateByOrderId(logisticsInfo, orderId);
            return;
        }

        OrderLogisticsInfoRecordQuery query = new OrderLogisticsInfoRecordQuery();
        query.setOrderId(orderId);
        Integer recordCount = this.orderLogisticsInfoRecordMapper.selectCount(query);
        String address = null;
        //模拟5个中转地址
        if (recordCount >= 5) {
            OrderLogisticsInfo orderLogisticsInfo = this.orderLogisticsInfoMapper.selectByOrderId(orderId);
            address = orderLogisticsInfo.getReceiverAddress();
        } else {
            Integer randomIndex = StringTools.getRandomNumberRange(0, logisticsCenters.length);
            address = logisticsCenters[randomIndex];
            //继续加入队列，模拟物流
            redisComponent.addOrder2LogisticsQueue(StringTools.getRandomNumberRange(1, 5), orderId);
        }
        OrderLogisticsInfoRecord record = new OrderLogisticsInfoRecord();
        record.setRecordAddress(address);
        record.setRecordTime(new Date());
        record.setOrderId(orderId);
        this.orderLogisticsInfoRecordMapper.insert(record);

        if (recordCount >= 5) {
            OrderLogisticsInfo logisticsInfo = new OrderLogisticsInfo();
            logisticsInfo.setLogisticsStatus(LogisticsStatusEnum.DELIVERED.getStatus());
            this.orderLogisticsInfoMapper.updateByOrderId(logisticsInfo, orderId);
        }
    }

    @Override
    public OrderLogisticsInfo getOrderLogisticsRecords(String userId, String orderId) {
        OrderLogisticsInfo orderLogisticsInfo = this.orderLogisticsInfoMapper.selectByOrderId(orderId);
        if (orderLogisticsInfo == null || !orderLogisticsInfo.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        OrderLogisticsInfoRecordQuery query = new OrderLogisticsInfoRecordQuery();
        query.setOrderId(orderId);
        query.setOrderBy("record_time desc");
        List<OrderLogisticsInfoRecord> recordList = this.orderLogisticsInfoRecordMapper.selectList(query);
        orderLogisticsInfo.setRecordList(recordList);
        return orderLogisticsInfo;
    }
}