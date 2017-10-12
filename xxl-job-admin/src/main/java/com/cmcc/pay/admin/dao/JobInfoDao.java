package com.cmcc.pay.admin.dao;

import com.cmcc.pay.admin.core.model.JobInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * job info
 * @author xuxueli 2016-1-12 18:03:45
 */
public interface JobInfoDao {

	List<JobInfo> pageList(@Param("offset") int offset, @Param("pagesize") int pagesize, @Param("jobGroup") int jobGroup);

	int pageListCount(@Param("offset") int offset, @Param("pagesize") int pagesize, @Param("jobGroup") int jobGroup);
	
	int save(JobInfo info);

	JobInfo loadById(@Param("id") int id);
	
	int update(JobInfo item);
	
	int delete(@Param("id") int id);

	List<JobInfo> getJobsByGroup(@Param("jobGroup") int jobGroup);

	int findAllCount();

}
