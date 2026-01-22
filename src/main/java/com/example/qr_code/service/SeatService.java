package com.example.qr_code.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.qr_code.entity.Seat;
import com.example.qr_code.entity.Order;
import com.example.qr_code.mapper.SeatMapper;
import com.example.qr_code.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeatService {

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 获取所有座位列表
     */
    public List<Seat> getAllSeats() {
        return seatMapper.selectList(new QueryWrapper<Seat>().orderByAsc("seat_number"));
    }

    /**
     * 根据ID获取座位
     */
    public Seat getSeatById(Long id) {
        return seatMapper.selectById(id);
    }

    /**
     * 根据座位编号获取座位
     */
    public Seat getSeatByNumber(String seatNumber) {
        return seatMapper.selectOne(new QueryWrapper<Seat>().eq("seat_number", seatNumber));
    }

    /**
     * 获取可用座位列表
     */
    public List<Seat> getAvailableSeats() {
        return seatMapper.selectList(new QueryWrapper<Seat>().eq("status", "available"));
    }

    /**
     * 控制座位电源
     * @param seatId 座位ID
     * @param powerOn 电源状态：true-通电，false-断电
     * @return 更新后的座位信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Seat controlPower(Long seatId, boolean powerOn) {
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }

        seat.setPowerOn(powerOn ? 1 : 0);
        seat.setUpdatedAt(LocalDateTime.now());
        seatMapper.updateById(seat);

        return seat;
    }

    /**
     * 更新座位状态
     * @param seatId 座位ID
     * @param status 状态：available-空闲, occupied-占用, reserved-预约, maintenance-维护
     */
    @Transactional(rollbackFor = Exception.class)
    public Seat updateSeatStatus(Long seatId, String status) {
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }

        seat.setStatus(status);
        seat.setUpdatedAt(LocalDateTime.now());
        
        // 如果座位变为空闲，自动断电
        if ("available".equals(status)) {
            seat.setPowerOn(0);
        }
        
        seatMapper.updateById(seat);
        return seat;
    }

    /**
     * 入座操作：更新座位状态为占用，并通电
     * 使用行级锁防止并发冲突
     */
    @Transactional(rollbackFor = Exception.class)
    public Seat occupySeat(Long seatId) {
        // 使用 FOR UPDATE 行级锁，防止并发抢座
        Seat seat = seatMapper.selectByIdForUpdate(seatId);
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }
        
        if (!"available".equals(seat.getStatus())) {
            throw new RuntimeException("座位当前不可用，可能已被他人占用");
        }

        seat.setStatus("occupied");
        seat.setPowerOn(1);
        seat.setUpdatedAt(LocalDateTime.now());
        seatMapper.updateById(seat);

        return seat;
    }

    /**
     * 离座操作：更新座位状态为空闲，并断电
     */
    @Transactional(rollbackFor = Exception.class)
    public Seat releaseSeat(Long seatId) {
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }

        seat.setStatus("available");
        seat.setPowerOn(0);
        seat.setUpdatedAt(LocalDateTime.now());
        seatMapper.updateById(seat);

        return seat;
    }

    /**
     * 获取座位统计信息
     */
    public SeatStats getSeatStats() {
        List<Seat> allSeats = getAllSeats();
        int total = allSeats.size();
        int available = 0;
        int occupied = 0;
        int reserved = 0;
        int maintenance = 0;

        for (Seat seat : allSeats) {
            switch (seat.getStatus()) {
                case "available":
                    available++;
                    break;
                case "occupied":
                    occupied++;
                    break;
                case "reserved":
                    reserved++;
                    break;
                case "maintenance":
                    maintenance++;
                    break;
            }
        }

        return new SeatStats(total, available, occupied, reserved, maintenance);
    }

    /**
     * 座位统计数据类
     */
    public static class SeatStats {
        public int total;
        public int available;
        public int occupied;
        public int reserved;
        public int maintenance;

        public SeatStats(int total, int available, int occupied, int reserved, int maintenance) {
            this.total = total;
            this.available = available;
            this.occupied = occupied;
            this.reserved = reserved;
            this.maintenance = maintenance;
        }
    }
}
