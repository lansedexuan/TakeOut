package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 指定时间内的营业额统计
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end){
        // 若果begin>end 抛出异常
        if(begin.isAfter(end)){
            throw new RuntimeException("开始时间不能大于结束时间");
        }

        // 创建一个日期列表，包含begin到end的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();
        for(LocalDate date : dateList){
            // 获取date日期的营业额，状态为已完成的订单金额合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//00:00
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//23:59:59.999999999

            //select sum(amount) from orders where status = 5 and order_time >= ? and order_time <= ?
            //>= &gt=;
            //<= &gt=;
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);

            Double turnover = orderMapper.sumByMap(map);
            turnoverList.add(turnover == null ? 0.0 : turnover);
        }

        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();

        return turnoverReportVO;
    }

    /**
     * 指定时间内的营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 若果begin>end 抛出异常
        if(begin.isAfter(end)){
            throw new RuntimeException("开始时间不能大于结束时间");
        }

        // 创建一个日期列表，包含begin到end的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //select count(id) from user where create_time >= ? and create_time <= ?
        List<Integer> newUserList = new ArrayList<>();
        //select count(id) from user where create_time < ?
        List<Integer> totalUserList = new ArrayList<>();

        for(LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//00:00
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//23:59:59.999999999

            Map map = new HashMap();

            map.put("end", endTime);
            Integer totalUser = userMapper.countByMap(map);
            totalUserList.add(totalUser);

            map.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(map);
            newUserList.add(newUser);
        }

        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();

        return userReportVO;
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // 若果begin>end 抛出异常
        if(begin.isAfter(end)){
            throw new RuntimeException("开始时间不能大于结束时间");
        }

        // 创建一个日期列表，包含begin到end的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        for(LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//00:00
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//23:59:59.999999999

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);

            //每天订单总数 select count(id) from orders where order_time >= ? and order_time <= ?
            Integer orderCount = orderMapper.countByMap(map);
            orderCountList.add(orderCount);

            //每天有效总数 select count(id) from orders where order_time >= ? and order_time <= ? and status = 5
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByMap(map);
            validOrderCountList.add(validOrderCount);
        }

        OrderReportVO orderReportVO = OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(orderCountList.stream().mapToInt(x -> x).sum())
                .validOrderCount(validOrderCountList.stream().mapToInt(x -> x).sum())
                .orderCompletionRate(validOrderCountList.stream().mapToInt(x -> x).sum() * 1.0 / orderCountList.stream().mapToInt(x -> x).sum())
                .build();
        return orderReportVO;
    }

    /**
     * 销量排名top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        // 若果begin>end 抛出异常
        if(begin.isAfter(end)){
            throw new RuntimeException("开始时间不能大于结束时间");
        }

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);//00:00
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);//23:59:59.999999999
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);

        List<String> names =salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");

        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        SalesTop10ReportVO salesTop10ReportVO = SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();

        return salesTop10ReportVO;
    }
}
