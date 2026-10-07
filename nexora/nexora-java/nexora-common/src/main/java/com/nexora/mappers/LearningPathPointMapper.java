package com.nexora.mappers;

import com.nexora.entity.vo.PathPointRowVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学习路径积分归集（二期 PATH，手写精简 mapper）。
 *
 * 分两条查询把「路径已得积分」一次算出来（调用方在内存里合并），
 * 避免按路径逐条查库（仓库规则：禁止在循环里调用数据库）。
 */
public interface LearningPathPointMapper {

    /** 节点类流水（PATH_TEST 节点小测 / PATH_NODE 节点完成）按所属路径归集 */
    List<PathPointRowVO> selectNodePointsByPath(@Param("userId") String userId);

    /** 整条完成流水（PATH_DONE，bizId 即路径 ID）按路径归集 */
    List<PathPointRowVO> selectPathDonePoints(@Param("userId") String userId);
}
