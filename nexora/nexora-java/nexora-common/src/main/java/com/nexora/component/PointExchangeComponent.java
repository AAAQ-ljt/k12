package com.nexora.component;

import com.nexora.entity.po.StudentPointAccount;
import com.nexora.entity.po.StudentPointExchange;
import com.nexora.entity.vo.PointExchangeItemVO;
import com.nexora.entity.vo.PointExchangeListVO;
import com.nexora.entity.vo.PointExchangeResultVO;
import com.nexora.exception.BusinessException;
import com.nexora.mappers.StudentPointAccountMapper;
import com.nexora.mappers.StudentPointExchangeMapper;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 积分兑换（二期 A-7）：学生用**可用积分**换取两类「零履约成本」的权益。
 *
 * 商品（价格/上限/额度都可在管理端 GAME 组调整，改完即生效）：
 * 1) UPLOAD_QUOTA —— 个人知识库容量扩容（学生上传额度的用户级加成，读取见 StudentQuotaComponent）；
 * 2) VOICE_&lt;音色码&gt; —— 解锁朗读音色（宽定义：一份「已解锁音色集合」，
 *    绘本朗读现在就用，支柱 B 的 AI 助教朗读上线后自动继承同一份解锁状态）。
 *
 * 扣分只走 {@link PointAwardComponent#spend}：只减可用积分、累计积分不变、写负分流水（幂等键=兑换单号）。
 * 本类不做缓存：单个学生的兑换记录只有几条，多一次小查询换「不会读到过期缓存」是划算的。
 */
@Slf4j
@Component
public class PointExchangeComponent {

    /** 商品码：上传额度扩容 */
    public static final String ITEM_UPLOAD_QUOTA = "UPLOAD_QUOTA";

    /** 音色商品码前缀：VOICE_<音色码> */
    public static final String ITEM_PREFIX_VOICE = "VOICE_";

    /** 商品分类 */
    public static final String CATEGORY_QUOTA = "UPLOAD_QUOTA";
    public static final String CATEGORY_VOICE = "VOICE";

    /** GAME 组配置键 */
    private static final String KEY_QUOTA_COST = "EXCHANGE_UPLOAD_QUOTA_COST";
    private static final String KEY_QUOTA_MB = "EXCHANGE_UPLOAD_QUOTA_MB";
    private static final String KEY_QUOTA_MAX = "EXCHANGE_UPLOAD_QUOTA_MAX";
    private static final String KEY_VOICE_COST = "EXCHANGE_VOICE_COST";
    private static final String KEY_VOICE_FREE = "VOICE_FREE_CODES";
    private static final String KEY_VOICE_UNLOCK = "VOICE_UNLOCK_CODES";

    /** 默认值（配置缺失时回落，与 20261008 增量脚本一致） */
    private static final int DEFAULT_QUOTA_COST = 200;
    private static final int DEFAULT_QUOTA_MB = 200;
    private static final int DEFAULT_QUOTA_MAX = 3;
    private static final int DEFAULT_VOICE_COST = 150;
    private static final String DEFAULT_VOICE_FREE = "mimo_default,冰糖,白桦";
    private static final String DEFAULT_VOICE_UNLOCK = "茉莉,苏打,Mia,Chloe,Milo,Dean";

    /** 兑换记录展示条数 */
    private static final int RECORD_LIMIT = 20;

    @Resource
    private SystemConfigComponent systemConfigComponent;

    @Resource
    private PointAwardComponent pointAwardComponent;

    @Resource
    private StudentPointExchangeMapper exchangeMapper;

    @Resource
    private StudentPointAccountMapper accountMapper;

    /** 学生端兑换页：可用积分 + 商品 + 我的兑换记录 */
    public PointExchangeListVO catalog(String userId, String stage) {
        PointExchangeListVO vo = new PointExchangeListVO();
        if (StringTools.isEmpty(userId)) {
            return vo;
        }
        StudentPointAccount account = accountMapper.selectByUserId(userId);
        int available = account == null || account.getAvailablePoints() == null ? 0 : account.getAvailablePoints();
        vo.setAvailablePoints(available);
        List<String> owned = exchangeMapper.selectItemCodesByUser(userId);
        Set<String> ownedSet = new HashSet<>(owned == null ? List.of() : owned);

        List<PointExchangeItemVO> items = new ArrayList<>();
        // ① 上传额度扩容
        int quotaUsed = exchangeMapper.countByUserAndItem(userId, ITEM_UPLOAD_QUOTA);
        items.add(buildItem(ITEM_UPLOAD_QUOTA, "上传额度 +" + intConfig(KEY_QUOTA_MB, DEFAULT_QUOTA_MB) + "MB",
                "扩容个人知识库存储空间，可多次兑换；已扩容的容量永久有效",
                intConfig(KEY_QUOTA_COST, DEFAULT_QUOTA_COST), intConfig(KEY_QUOTA_MAX, DEFAULT_QUOTA_MAX),
                quotaUsed, available, false, CATEGORY_QUOTA));
        // ② 音色解锁（每个音色只能解锁一次，解锁后永久可用）
        int voiceCost = intConfig(KEY_VOICE_COST, DEFAULT_VOICE_COST);
        for (String voice : csvConfig(KEY_VOICE_UNLOCK, DEFAULT_VOICE_UNLOCK)) {
            String code = ITEM_PREFIX_VOICE + voice;
            boolean unlocked = ownedSet.contains(code);
            items.add(buildItem(code, "朗读音色 · " + voice,
                    "解锁后可在绘本朗读里选用这个音色（AI 助教朗读上线后同样可用）",
                    voiceCost, 1, unlocked ? 1 : 0, available, unlocked, CATEGORY_VOICE));
        }
        vo.setItems(items);
        List<StudentPointExchange> records = exchangeMapper.selectByUser(userId, RECORD_LIMIT);
        vo.setRecords(records == null ? List.of() : records);
        return vo;
    }

    /**
     * 兑换（幂等 + 事务）：校验商品与次数 → 扣可用积分（唯一扣分入口）→ 落兑换记录。
     */
    @Transactional(rollbackFor = Exception.class)
    public PointExchangeResultVO exchange(String userId, String stage, String itemCode) {
        if (StringTools.isEmpty(userId)) {
            throw new BusinessException("请先登录");
        }
        if (StringTools.isEmpty(itemCode)) {
            throw new BusinessException("请选择要兑换的权益");
        }
        PointExchangeListVO list = catalog(userId, stage);
        PointExchangeItemVO item = list.getItems().stream()
                .filter(each -> each.getItemCode().equals(itemCode))
                .findFirst()
                .orElseThrow(() -> new BusinessException("该权益不存在或已下架"));
        if (Boolean.TRUE.equals(item.getUnlocked())) {
            throw new BusinessException("该权益已经解锁过了");
        }
        if (item.getMaxTimes() != null && item.getMaxTimes() > 0 && item.getUsedTimes() >= item.getMaxTimes()) {
            throw new BusinessException("该权益最多兑换 " + item.getMaxTimes() + " 次，你已经用完了");
        }
        if (!Boolean.TRUE.equals(item.getAffordable())) {
            throw new BusinessException("可用积分不足，本次需要 " + item.getCostPoints() + " 分");
        }
        String exchangeId = UUID.randomUUID().toString().replace("-", "");
        // 扣分（只减可用积分；余额不足会在这里抛业务异常）
        int availableAfter = pointAwardComponent.spend(userId, stage, exchangeId, item.getCostPoints(),
                "兑换：" + item.getItemName());

        StudentPointExchange record = new StudentPointExchange();
        record.setExchangeId(exchangeId);
        record.setUserId(userId);
        record.setStage(stage);
        record.setItemCode(item.getItemCode());
        record.setItemName(item.getItemName());
        record.setCostPoints(item.getCostPoints());
        record.setBalanceAfter(availableAfter);
        if (exchangeMapper.insert(record) == 0) {
            throw new BusinessException("兑换单重复，请重试");
        }
        log.info("积分兑换成功 userId={} item={} cost={} availableAfter={}",
                userId, item.getItemCode(), item.getCostPoints(), availableAfter);

        PointExchangeResultVO result = new PointExchangeResultVO();
        result.setExchangeId(exchangeId);
        result.setItemName(item.getItemName());
        result.setCostPoints(item.getCostPoints());
        result.setAvailablePoints(availableAfter);
        result.setTip(ITEM_UPLOAD_QUOTA.equals(item.getItemCode())
                ? "已扩容 " + intConfig(KEY_QUOTA_MB, DEFAULT_QUOTA_MB) + "MB，去知识中心上传吧"
                : "已解锁，去绘本朗读里试试这个音色吧");
        return result;
    }

    /** 上传额度的用户级加成（MB）：已兑换次数 × 每次扩容额度 */
    public int uploadQuotaBonusMb(String userId) {
        if (StringTools.isEmpty(userId)) {
            return 0;
        }
        Integer times = exchangeMapper.countByUserAndItem(userId, ITEM_UPLOAD_QUOTA);
        if (times == null || times <= 0) {
            return 0;
        }
        return times * intConfig(KEY_QUOTA_MB, DEFAULT_QUOTA_MB);
    }

    /** 免费音色（系统默认 + 学段默认） */
    public Set<String> freeVoices() {
        return new HashSet<>(csvConfig(KEY_VOICE_FREE, DEFAULT_VOICE_FREE));
    }

    /** 我解锁的音色（不含前缀） */
    public Set<String> unlockedVoices(String userId) {
        Set<String> result = new HashSet<>();
        if (StringTools.isEmpty(userId)) {
            return result;
        }
        List<String> codes = exchangeMapper.selectItemCodesByUser(userId);
        if (codes == null) {
            return result;
        }
        for (String code : codes) {
            if (code != null && code.startsWith(ITEM_PREFIX_VOICE)) {
                result.add(code.substring(ITEM_PREFIX_VOICE.length()));
            }
        }
        return result;
    }

    /** 该学生是否可以使用这个音色（免费或已解锁）——朗读链路用它做服务端兜底 */
    public boolean voiceAllowed(String userId, String voice) {
        if (StringTools.isEmpty(voice)) {
            return false;
        }
        return freeVoices().contains(voice) || unlockedVoices(userId).contains(voice);
    }

    private PointExchangeItemVO buildItem(String code, String name, String effect, int cost, int maxTimes,
                                          int usedTimes, int available, boolean unlocked, String category) {
        PointExchangeItemVO item = new PointExchangeItemVO();
        item.setItemCode(code);
        item.setItemName(name);
        item.setEffect(effect);
        item.setCostPoints(cost);
        item.setMaxTimes(maxTimes);
        item.setUsedTimes(usedTimes);
        item.setRemainingTimes(maxTimes > 0 ? Math.max(0, maxTimes - usedTimes) : -1);
        item.setAffordable(available >= cost);
        item.setUnlocked(unlocked);
        item.setCategory(category);
        return item;
    }

    private int intConfig(String key, int defaultValue) {
        String raw = systemConfigComponent.getValue(SystemConfigComponent.GROUP_GAME, key, null);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            log.warn("GAME.{} 解析失败，回落默认值 {}：{}", key, defaultValue, raw);
            return defaultValue;
        }
    }

    private List<String> csvConfig(String key, String defaultValue) {
        String raw = systemConfigComponent.getValue(SystemConfigComponent.GROUP_GAME, key, null);
        String value = raw == null || raw.isBlank() ? defaultValue : raw;
        List<String> result = new ArrayList<>();
        for (String part : value.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
