package com.cmcc.pay.admin.dao;

import com.cmcc.pay.admin.core.model.JobLog;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * job log
 * @author xuxueli 2016-1-12 18:03:06
 */
public interface JobLogDao {
	
	List<JobLog> pageList(@Param("offset") int offset,
						  @Param("pagesize") int pagesize,
						  @Param("jobGroup") int jobGroup,
						  @Param("jobId") int jobId,
						  @Param("triggerTimeStart") Date triggerTimeStart,
						  @Param("triggerTimeEnd") Date triggerTimeEnd,
						  @Param("logStatus") int logStatus);
	int pageListCount(@Param("offset") int offset,
                      @Param("pagesize") int pagesize,
                      @Param("jobGroup") int jobGroup,
                      @Param("jobId") int jobId,
                      @Param("triggerTimeStart") Date triggerTimeStart,
                      @Param("triggerTimeEnd") Date triggerTimeEnd,
                      @Param("logStatus") int logStatus);
	
	JobLog load(@Param("id") int id);

	int save(JobLog JobLog);

	int updateTriggerInfo(JobLog JobLog);

	int updateHandleInfo(JobLog JobLog);
	
	int delete(@Param("jobId") int jobId);

	int triggerCountByHandleCode(@Param("handleCode") int handleCode);

	List<Map<String, Object>> triggerCountByDay(@Param("from") Date from,
                                                @Param("to") Date to,
                                                @Param("handleCode") int handleCode);

}
