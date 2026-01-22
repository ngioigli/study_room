package com.example.qr_code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.qr_code.entity.Seat;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatMapper extends BaseMapper<Seat> {
    
    /**
     * 使用行级锁查询座位（用于并发控制）
     * SELECT ... FOR UPDATE 会锁定该行，直到事务结束
     */
    @Select("SELECT * FROM seats WHERE id = #{id} FOR UPDATE")
    Seat selectByIdForUpdate(Long id);
}
