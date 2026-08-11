package com.smart.campus.service.impl;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.po.MessageInfo;
import com.smart.campus.entity.query.MessageInfoQuery;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.mappers.MessageInfoMapper;
import com.smart.campus.service.MessageInfoService;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("messageInfoService")
public class MessageInfoServiceImpl implements MessageInfoService {

    @Resource
    private MessageInfoMapper<MessageInfo, MessageInfoQuery> messageInfoMapper;

    @Override
    public List<MessageInfo> findListByParam(MessageInfoQuery param) {
        return this.messageInfoMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(MessageInfoQuery param) {
        return this.messageInfoMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<MessageInfo> findListByPage(MessageInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<MessageInfo> list = this.findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(MessageInfo bean) {
        return this.messageInfoMapper.insert(bean);
    }

    @Override
    public Integer addBatch(List<MessageInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.messageInfoMapper.insertBatch(listBean);
    }

    @Override
    public Integer addOrUpdateBatch(List<MessageInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.messageInfoMapper.insertOrUpdateBatch(listBean);
    }

    @Override
    public Integer updateByParam(MessageInfo bean, MessageInfoQuery param) {
        StringTools.checkParam(param);
        return this.messageInfoMapper.updateByParam(bean, param);
    }

    @Override
    public Integer deleteByParam(MessageInfoQuery param) {
        StringTools.checkParam(param);
        return this.messageInfoMapper.deleteByParam(param);
    }

    @Override
    public MessageInfo getMessageInfoByMessageId(Long messageId) {
        return this.messageInfoMapper.selectByMessageId(messageId);
    }

    @Override
    public Integer updateMessageInfoByMessageId(MessageInfo bean, Long messageId) {
        return this.messageInfoMapper.updateByMessageId(bean, messageId);
    }

    @Override
    public Integer deleteMessageInfoByMessageId(Long messageId) {
        return this.messageInfoMapper.deleteByMessageId(messageId);
    }
}
