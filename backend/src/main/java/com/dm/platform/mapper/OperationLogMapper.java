package com.dm.platform.mapper;

import com.dm.platform.entity.OperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作日志表数据访问
 */
@Mapper
public interface OperationLogMapper {

    @Insert("INSERT INTO operation_log(manuscript_id, operator_id, action, from_status, to_status, detail) " +
            "VALUES(#{manuscriptId}, #{operatorId}, #{action}, #{fromStatus}, #{toStatus}, #{detail})")
    int insert(OperationLog log);

    @Select("SELECT * FROM operation_log WHERE manuscript_id = #{manuscriptId} ORDER BY create_time ASC")
    List<OperationLog> findByManuscriptId(Long manuscriptId);
}
