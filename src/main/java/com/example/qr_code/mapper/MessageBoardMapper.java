package com.example.qr_code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.qr_code.entity.MessageBoard;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MessageBoardMapper 留言板数据访问接口
 */
@Repository
public interface MessageBoardMapper extends BaseMapper<MessageBoard> {
    
    /**
     * 查询留言列表（带用户信息）
     */
    @Select("SELECT m.*, u.nickname, u.avatar, u.today_status " +
            "FROM message_board m " +
            "LEFT JOIN users u ON m.user_id = u.id " +
            "WHERE m.status = 1 " +
            "ORDER BY m.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    List<MessageBoard> selectMessagesWithUser(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计正常状态的留言数量
     */
    @Select("SELECT COUNT(*) FROM message_board WHERE status = 1")
    int countActiveMessages();
    
    /**
     * 更新回复数量
     */
    @Update("UPDATE message_board SET reply_count = reply_count + #{delta} WHERE id = #{messageId}")
    int updateReplyCount(@Param("messageId") Long messageId, @Param("delta") int delta);
    
    /**
     * 删除过期留言（用于定时清理）
     */
    @Select("DELETE FROM message_board WHERE created_at < DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    int deleteExpiredMessages(@Param("days") int days);
}
