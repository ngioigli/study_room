package com.example.qr_code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.qr_code.entity.MessageReply;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MessageReplyMapper 留言回复数据访问接口
 */
@Repository
public interface MessageReplyMapper extends BaseMapper<MessageReply> {
    
    /**
     * 查询留言的回复列表（带用户信息）
     */
    @Select("SELECT r.*, u.nickname, u.avatar " +
            "FROM message_replies r " +
            "LEFT JOIN users u ON r.user_id = u.id " +
            "WHERE r.message_id = #{messageId} AND r.status = 1 " +
            "ORDER BY r.created_at ASC")
    List<MessageReply> selectRepliesWithUser(@Param("messageId") Long messageId);
    
    /**
     * 统计留言的回复数量
     */
    @Select("SELECT COUNT(*) FROM message_replies WHERE message_id = #{messageId} AND status = 1")
    int countByMessageId(@Param("messageId") Long messageId);
}
