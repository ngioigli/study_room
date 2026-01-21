package com.example.qr_code.controller;

import com.example.qr_code.entity.Seat;
import com.example.qr_code.entity.SeatReservation;
import com.example.qr_code.entity.User;
import com.example.qr_code.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReservationController 座位预约控制器
 * 处理座位预约相关功能
 */
@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    /**
     * 获取我的预约列表
     * GET /api/reservation/my
     */
    @GetMapping("/my")
    public Map<String, Object> getMyReservations(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }
        
        List<SeatReservation> reservations = reservationService.getUserReservations(user.getId());
        
        response.put("success", true);
        response.put("reservations", reservations);
        return response;
    }

    /**
     * 获取所有座位列表
     * GET /api/reservation/seats
     */
    @GetMapping("/seats")
    public Map<String, Object> getSeats() {
        Map<String, Object> response = new HashMap<>();
        
        List<Seat> seats = reservationService.getAllSeats();
        
        response.put("success", true);
        response.put("seats", seats);
        return response;
    }

    /**
     * 获取座位在指定日期的预约情况
     * GET /api/reservation/seat/{seatId}?date=2026-01-21
     */
    @GetMapping("/seat/{seatId}")
    public Map<String, Object> getSeatReservations(
            @PathVariable Long seatId,
            @RequestParam String date) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<SeatReservation> reservations = reservationService.getSeatReservations(seatId, localDate);
            
            response.put("success", true);
            response.put("reservations", reservations);
        } catch (DateTimeParseException e) {
            response.put("success", false);
            response.put("message", "日期格式错误");
        }
        return response;
    }

    /**
     * 创建预约
     * POST /api/reservation/create
     */
    @PostMapping("/create")
    public Map<String, Object> createReservation(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }
        
        try {
            Long seatId = Long.parseLong(data.get("seatId"));
            LocalDate date = LocalDate.parse(data.get("date"));
            LocalTime startTime = LocalTime.parse(data.get("startTime"));
            LocalTime endTime = LocalTime.parse(data.get("endTime"));
            
            String error = reservationService.createReservation(user.getId(), seatId, date, startTime, endTime);
            
            if (error == null) {
                response.put("success", true);
                response.put("message", "预约成功");
            } else {
                response.put("success", false);
                response.put("message", error);
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            response.put("success", false);
            response.put("message", "参数格式错误");
        }
        return response;
    }

    /**
     * 取消预约
     * POST /api/reservation/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public Map<String, Object> cancelReservation(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }
        
        String error = reservationService.cancelReservation(id, user.getId());
        
        if (error == null) {
            response.put("success", true);
            response.put("message", "取消成功");
        } else {
            response.put("success", false);
            response.put("message", error);
        }
        return response;
    }

    /**
     * 签到
     * POST /api/reservation/{id}/checkin
     */
    @PostMapping("/{id}/checkin")
    public Map<String, Object> checkIn(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "请先登录");
            return response;
        }
        
        String error = reservationService.checkIn(id, user.getId());
        
        if (error == null) {
            response.put("success", true);
            response.put("message", "签到成功");
        } else {
            response.put("success", false);
            response.put("message", error);
        }
        return response;
    }

    /**
     * 获取可预约的时间段
     * GET /api/reservation/timeslots
     */
    @GetMapping("/timeslots")
    public Map<String, Object> getTimeSlots() {
        Map<String, Object> response = new HashMap<>();
        
        // 预设时间段（每2小时一个）
        String[][] timeSlots = {
            {"08:00", "10:00"},
            {"10:00", "12:00"},
            {"12:00", "14:00"},
            {"14:00", "16:00"},
            {"16:00", "18:00"},
            {"18:00", "20:00"},
            {"20:00", "22:00"}
        };
        
        response.put("success", true);
        response.put("timeSlots", timeSlots);
        return response;
    }
}
