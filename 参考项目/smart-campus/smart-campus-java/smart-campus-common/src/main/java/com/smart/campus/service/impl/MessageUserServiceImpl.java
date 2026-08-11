package com.smart.campus.service.impl;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.po.MessageUser;
import com.smart.campus.entity.query.MessageUserQuery;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.mappers.MessageUserMapper;
import com.smart.campus.service.MessageUserService;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("messageUserService")
public class MessageUserServiceImpl implements MessageUserService {

    @Resource
    private MessageUserMapper<MessageUser, MessageUserQuery> messageUserMapper;

    @Override
    public List<MessageUser> findListByParam(MessageUserQuery param) {
        return this.messageUserMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(MessageUserQuery param) {
        return this.messageUserMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<MessageUser> findListByPage(MessageUserQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<MessageUser> list = this.findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(MessageUser bean) {
        return this.messageUserMapper.insert(bean);
    }

    @Override
    public Integer addBatch(List<MessageUser> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.messageUserMapper.insertBatch(listBean);
    }

    @Override
    public Integer addOrUpdateBatch(List<MessageUser> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.messageUserMapper.insertOrUpdateBatch(listBean);
    }

    @Override
    public Integer updateByParam(MessageUser bean, MessageUserQuery param) {
        StringTools.checkParam(param);
        return this.messageUserMapper.updateByParam(bean, param);
    }

    @Override
    public Integer deleteByParam(MessageUserQuery param) {
        StringTools.checkParam(param);
        return this.messageUserMapper.deleteByParam(param);
    }

    @Override
    public MessageUser getMessageUserById(Long id) {
        return this.messageUserMapper.selectById(id);
    }

    @Override
    public Integer updateMessageUserById(MessageUser bean, Long id) {
        return this.messageUserMapper.updateById(bean, id);
    }

    @Override
    public Integer deleteMessageUserById(Long id) {
        return this.messageUserMapper.deleteById(id);
    }

    @Override
    public MessageUser getMessageUserByMessageIdAndUserId(Long messageId, Integer userId) {
        return this.messageUserMapper.selectByMessageIdAndUserId(messageId, userId);
    }

    @Override
    public Integer updateMessageUserByMessageIdAndUserId(MessageUser bean, Long messageId, Integer userId) {
        return this.messageUserMapper.updateByMessageIdAndUserId(bean, messageId, userId);
    }

    @Override
    public Integer deleteMessageUserByMessageIdAndUserId(Long messageId, Integer userId) {
        return this.messageUserMapper.deleteByMessageIdAndUserId(messageId, userId);
    }
}
