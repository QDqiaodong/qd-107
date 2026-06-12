package com.sport.checkin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.checkin.entity.CheckinRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Mapper
public interface CheckinRecordMapper extends BaseMapper<CheckinRecord> {

    @Select("SELECT COUNT(*) FROM checkin_record WHERE user_id = #{userId} AND checkin_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    Integer countByDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(duration), 0) FROM checkin_record WHERE user_id = #{userId} AND checkin_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    Integer sumDurationByDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(calorie), 0) FROM checkin_record WHERE user_id = #{userId} AND checkin_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    java.math.BigDecimal sumCalorieByDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT sport_type_id, COUNT(*) as count FROM checkin_record " +
            "WHERE checkin_time >= #{startTime} AND deleted = 0 " +
            "GROUP BY sport_type_id")
    java.util.List<Map<String, Object>> countCheckinBySportTypeSince(@Param("startTime") LocalDateTime startTime);

    @Select("SELECT intensity, COUNT(*) as count FROM checkin_record " +
            "WHERE user_id = #{userId} AND checkin_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0 " +
            "GROUP BY intensity")
    java.util.List<Map<String, Object>> countByIntensityAndDateRange(@Param("userId") Long userId,
                                                                      @Param("startDate") LocalDate startDate,
                                                                      @Param("endDate") LocalDate endDate);

    @Select("SELECT COUNT(*) FROM checkin_record WHERE plan_id = #{planId} AND checkin_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    Integer countByPlanIdAndDateRange(@Param("planId") Long planId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(duration), 0) FROM checkin_record WHERE plan_id = #{planId} AND checkin_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    Integer sumDurationByPlanIdAndDateRange(@Param("planId") Long planId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(calorie), 0) FROM checkin_record WHERE plan_id = #{planId} AND checkin_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    java.math.BigDecimal sumCalorieByPlanIdAndDateRange(@Param("planId") Long planId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COUNT(*) FROM checkin_record WHERE sport_type_id = #{sportTypeId} AND user_id = #{userId} AND checkin_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    Integer countBySportTypeAndDateRange(@Param("userId") Long userId, @Param("sportTypeId") Long sportTypeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(duration), 0) FROM checkin_record WHERE sport_type_id = #{sportTypeId} AND user_id = #{userId} AND checkin_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    Integer sumDurationBySportTypeAndDateRange(@Param("userId") Long userId, @Param("sportTypeId") Long sportTypeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(calorie), 0) FROM checkin_record WHERE sport_type_id = #{sportTypeId} AND user_id = #{userId} AND checkin_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    java.math.BigDecimal sumCalorieBySportTypeAndDateRange(@Param("userId") Long userId, @Param("sportTypeId") Long sportTypeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT sport_type_id, COUNT(*) as count, COALESCE(SUM(duration), 0) as total_duration " +
            "FROM checkin_record WHERE user_id = #{userId} AND deleted = 0 " +
            "GROUP BY sport_type_id ORDER BY count DESC")
    java.util.List<Map<String, Object>> countAndDurationBySportType(@Param("userId") Long userId);

    @Select("SELECT HOUR(checkin_time) as hour, COUNT(*) as count " +
            "FROM checkin_record WHERE user_id = #{userId} AND deleted = 0 AND checkin_time IS NOT NULL " +
            "GROUP BY HOUR(checkin_time) ORDER BY hour")
    java.util.List<Map<String, Object>> countByHour(@Param("userId") Long userId);

    @Select("SELECT DAYOFWEEK(checkin_date) as weekday, COUNT(*) as count " +
            "FROM checkin_record WHERE user_id = #{userId} AND deleted = 0 AND checkin_date IS NOT NULL " +
            "GROUP BY DAYOFWEEK(checkin_date) ORDER BY weekday")
    java.util.List<Map<String, Object>> countByWeekday(@Param("userId") Long userId);

    @Select("SELECT COALESCE(AVG(duration), 0) FROM checkin_record WHERE user_id = #{userId} AND deleted = 0 AND duration > 0")
    BigDecimal getAverageDuration(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM checkin_record WHERE user_id = #{userId} AND deleted = 0")
    Integer getTotalCheckinCount(@Param("userId") Long userId);
}
