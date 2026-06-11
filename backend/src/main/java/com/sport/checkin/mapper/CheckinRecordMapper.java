package com.sport.checkin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sport.checkin.entity.CheckinRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
