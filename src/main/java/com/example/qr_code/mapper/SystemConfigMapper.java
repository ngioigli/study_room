package com.example.qr_code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.qr_code.entity.SystemConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * SystemConfigMapper 系统配置数据访问接口
 */
@Repository
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
    
    /**
     * 根据键获取配置值
     */
    @Select("SELECT config_value FROM system_config WHERE config_key = #{key}")
    String getConfigValue(@Param("key") String key);
}
