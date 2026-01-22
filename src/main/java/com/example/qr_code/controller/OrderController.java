package com.example.qr_code.controller;

import com.example.qr_code.entity.Order;
import com.example.qr_code.entity.User;
import com.example.qr_code.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单（入座）
     * POST /api/orders
     * Body: { "seatId": 1, "plannedDuration": 120 }
     */
    @PostMapping
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> body, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Object seatIdObj = body.get("seatId");
            if (seatIdObj == null) {
                result.put("success", false);
                result.put("message", "请选择座位");
                return result;
            }

            Long seatId = Long.valueOf(seatIdObj.toString());
            Integer plannedDuration = null;
            if (body.get("plannedDuration") != null) {
                plannedDuration = Integer.valueOf(body.get("plannedDuration").toString());
            }

            Order order = orderService.createOrder(user.getId(), seatId, plannedDuration);

            result.put("success", true);
            result.put("order", order);
            result.put("message", "入座成功，座位已通电");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 结束订单（离座）
     * PUT /api/orders/{id}/end
     */
    @PutMapping("/{id}/end")
    public Map<String, Object> endOrder(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Order order = orderService.endOrder(id);
            double fee = orderService.calculateFee(order.getActualDuration());

            result.put("success", true);
            result.put("order", order);
            result.put("actualDuration", order.getActualDuration());
            result.put("fee", fee);
            result.put("message", "离座成功，座位已断电");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 取消订单
     * PUT /api/orders/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public Map<String, Object> cancelOrder(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Order order = orderService.cancelOrder(id);

            result.put("success", true);
            result.put("order", order);
            result.put("message", "订单已取消");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 获取当前进行中的订单
     * GET /api/orders/current
     */
    @GetMapping("/current")
    public Map<String, Object> getCurrentOrder(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Order order = orderService.getActiveOrderByUserId(user.getId());
            if (order != null) {
                OrderService.OrderDetail detail = orderService.getOrderDetail(order.getId());
                result.put("success", true);
                result.put("hasActiveOrder", true);
                result.put("order", detail.order);
                result.put("seat", detail.seat);
            } else {
                result.put("success", true);
                result.put("hasActiveOrder", false);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取当前订单失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取订单详情
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public Map<String, Object> getOrderDetail(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            OrderService.OrderDetail detail = orderService.getOrderDetail(id);
            if (detail != null) {
                result.put("success", true);
                result.put("order", detail.order);
                result.put("seat", detail.seat);
                
                // 计算费用
                if (detail.order.getActualDuration() != null) {
                    result.put("fee", orderService.calculateFee(detail.order.getActualDuration()));
                }
            } else {
                result.put("success", false);
                result.put("message", "订单不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取订单详情失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取订单历史
     * GET /api/orders/history?limit=10
     */
    @GetMapping("/history")
    public Map<String, Object> getOrderHistory(@RequestParam(defaultValue = "10") int limit, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            List<Order> orders = orderService.getOrderHistory(user.getId(), limit);
            result.put("success", true);
            result.put("orders", orders);
            result.put("count", orders.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取订单历史失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取所有进行中的订单（管理员）
     * GET /api/orders/active
     */
    @GetMapping("/active")
    public Map<String, Object> getAllActiveOrders(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        // 验证管理员权限
        if (!"admin".equals(user.getRole())) {
            result.put("success", false);
            result.put("message", "权限不足，需要管理员权限");
            return result;
        }

        try {
            List<Order> orders = orderService.getAllActiveOrders();
            result.put("success", true);
            result.put("orders", orders);
            result.put("count", orders.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取进行中订单失败: " + e.getMessage());
        }
        return result;
    }
}
