package com.example.qr_code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.qr_code.entity.EncouragementCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * EncouragementCardMapper 鼓励卡片数据访问层
 */
@Mapper
public interface EncouragementCardMapper extends BaseMapper<EncouragementCard> {
    
    /**
     * 获取随机可见的鼓励卡片
     */
    @Select("SELECT * FROM encouragement_card WHERE is_hidden = 0 ORDER BY RAND() LIMIT #{limit}")
    List<EncouragementCard> selectRandomCards(int limit);
    
    /**
     * 增加点赞数
     */
    @Update("UPDATE encouragement_card SET likes = likes + 1 WHERE id = #{id}")
    int incrementLikes(Long id);
}
