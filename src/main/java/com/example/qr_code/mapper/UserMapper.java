package com.example.qr_code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.qr_code.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends BaseMapper<User> {
    // 继承 BaseMapper 后，已经自动有了 selectOne, insert 等方法
    // 这里不需要写代码
}