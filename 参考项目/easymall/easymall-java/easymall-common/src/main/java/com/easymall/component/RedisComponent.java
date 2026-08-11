package com.easymall.component;

import com.easymall.constants.Constants;
import com.easymall.entity.dto.LogisticsSendDTO;
import com.easymall.entity.dto.RagDataDTO;
import com.easymall.entity.dto.TokenUserInfoDTO;
import com.easymall.entity.po.SysCategory;
import com.easymall.redis.RedisUtils;
import com.easymall.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
public class RedisComponent {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private RedissonClient redissonClient;


    public String saveTokenInfo4Admin(String account) {
        String token = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_ADMIN + token, account, Constants.REDIS_KEY_EXPIRES_DAY);
        return token;
    }

    public void cleanToken4Admin(String token) {
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_ADMIN + token);
    }

    public String getLoginInfo4Admin(String token) {
        return (String) redisUtils.get(Constants.REDIS_KEY_TOKEN_ADMIN + token);
    }

    public String saveCheckCode(String code) {
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, 60 * 10);
        return checkCodeKey;
    }

    public String getCheckCode(String checkCodeKey) {
        return (String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    public void cleanCheckCode(String checkCodeKey) {
        redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    public void saveTokenInfo(TokenUserInfoDTO tokenUserInfoDto) {
        cleanUserTokenInfo(tokenUserInfoDto.getUserId());
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenUserInfoDto.setToken(token);
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_WEB + token, tokenUserInfoDto, Constants.REDIS_KEY_EXPIRES_DAY * 7);

        redisUtils.setex(Constants.REDIS_KEY_TOKEN_USERID_WEB + tokenUserInfoDto.getUserId(), token, Constants.REDIS_KEY_EXPIRES_DAY * 7);
    }

    public void cleanUserTokenInfo(String userId) {
        String token = (String) redisUtils.get(Constants.REDIS_KEY_TOKEN_USERID_WEB + userId);
        if (StringTools.isEmpty(token)) {
            return;
        }
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_WEB + token);
    }

    public void updateTokenInfo(TokenUserInfoDTO tokenUserInfoDto) {
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_WEB + tokenUserInfoDto.getToken(), tokenUserInfoDto, Constants.REDIS_KEY_EXPIRES_DAY * 7);
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_USERID_WEB + tokenUserInfoDto.getUserId(), tokenUserInfoDto.getToken(), Constants.REDIS_KEY_EXPIRES_DAY * 7);
    }

    public TokenUserInfoDTO getTokenInfo(String token) {
        return (TokenUserInfoDTO) redisUtils.get(Constants.REDIS_KEY_TOKEN_WEB + token);
    }

    public void cleanToken(String token) {
        if (StringTools.isEmpty(token)) {
            return;
        }
        TokenUserInfoDTO tokenUserInfoDTO = getTokenInfo(token);
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_WEB + token);
        if (tokenUserInfoDTO != null) {
            redisUtils.delete(Constants.REDIS_KEY_TOKEN_USERID_WEB + tokenUserInfoDTO.getUserId());
        }
    }

    public void forceLogout(String userId) {
        String token = (String) redisUtils.get(Constants.REDIS_KEY_TOKEN_USERID_WEB + userId);
        if (!StringTools.isEmpty(token)) {
            redisUtils.delete(Constants.REDIS_KEY_TOKEN_WEB + token);
        }
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_USERID_WEB + userId);
    }

    public void saveUserHeartBeat(String userId) {
        redisUtils.setex(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId, System.currentTimeMillis(), Constants.REDIS_KEY_EXPIRES_HEART_BEAT);
    }

/*    public void saveTokenUserInfoDto(TokenUserInfoDTO tokenUserInfoDto) {
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN + tokenUserInfoDto.getToken(), tokenUserInfoDto, Constants.REDIS_KEY_EXPIRES_DAY * 2);
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN_USERID + tokenUserInfoDto.getUserId(), tokenUserInfoDto.getToken(), Constants.REDIS_KEY_EXPIRES_DAY * 2);
    }*/


    public void saveCategoryList(List<SysCategory> categoryInfoList) {
        redisUtils.set(Constants.REDIS_KEY_CATEGORY_LIST, categoryInfoList);
    }

    public List<SysCategory> getCategoryList() {
        List<SysCategory> categoryInfoList = (List<SysCategory>) redisUtils.get(Constants.REDIS_KEY_CATEGORY_LIST);
        return categoryInfoList == null ? new ArrayList<>() : categoryInfoList;
    }


    public void addOrder2DelayQueue(String queueName, Integer delayMin, String orderId) {
        long executeTime = System.currentTimeMillis() + delayMin * 60 * 1000;
        redisUtils.zsetAdd(queueName, orderId, executeTime);
    }

    public Set<String> getTimeOutOrder(String queueName) {
        return redisUtils.zsetRangeByScore(queueName, 0, System.currentTimeMillis());
    }

    public Long removeTimeOutOrder(String queueName, String orderId) {
        return redisUtils.zsetAddRemove(queueName, orderId);
    }


    public void saveLogisticsInfo(LogisticsSendDTO logisticsSendDTO) {
        redisUtils.set(Constants.REDIS_KEY_SETTING_LOGISTICS, logisticsSendDTO);
    }

    public LogisticsSendDTO getLogisticsInfo() {
        return (LogisticsSendDTO) redisUtils.get(Constants.REDIS_KEY_SETTING_LOGISTICS);
    }

    public void addOrder2LogisticsQueue(Integer delaySeconds, String orderId) {
        long executeTime = System.currentTimeMillis() + delaySeconds * 1000;
        redisUtils.zsetAdd(Constants.REDIS_KEY_ORDER_LOGISTICS_QUEUE, orderId, executeTime);
    }

    public Set<String> getTimeOuterLogistics() {
        return redisUtils.zsetRangeByScore(Constants.REDIS_KEY_ORDER_LOGISTICS_QUEUE, 0, System.currentTimeMillis());
    }

    public Long removeTimeOuterLogistics(String orderId) {
        return redisUtils.zsetAddRemove(Constants.REDIS_KEY_ORDER_LOGISTICS_QUEUE, orderId);
    }

    //提示词
    public void savePrompt(String key, String value) {
        redisUtils.set(Constants.REDIS_KEY_PROMPT + key, value);
    }

    public String getPrompt(String key) {
        return (String) redisUtils.get(Constants.REDIS_KEY_PROMPT + key);
    }

    public void cleanPrompt(String key) {
        redisUtils.delete(Constants.REDIS_KEY_PROMPT + key);
    }

    //消息队列
    public void sendMessage(String queueName, RagDataDTO data) {
        RBlockingQueue<RagDataDTO> queue = redissonClient.getBlockingQueue(queueName, JsonJacksonCodec.INSTANCE);
        try {
            log.info("开始发送消息");
            queue.put(data);
            log.info("发送消息成功");
        } catch (InterruptedException e) {
            log.error("消息发送失败", e);
        }
    }

    public void cancelMessage(String userId, Integer messageId) {
        redisUtils.setex(Constants.REDIS_KEY_CANCEL_AGENT_MESSAGE + userId + messageId, messageId, Constants.REDIS_KEY_EXPIRES_ONE_MIN);
    }

    public Boolean hasCancelMessage(String userId, Integer messageId) {
        return redisUtils.get(Constants.REDIS_KEY_CANCEL_AGENT_MESSAGE + userId + messageId) != null;
    }
}