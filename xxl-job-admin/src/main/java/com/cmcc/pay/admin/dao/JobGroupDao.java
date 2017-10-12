package com.cmcc.pay.admin.dao;

import com.cmcc.pay.admin.core.model.JobGroup;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by xuxueli on 16/9/30.
 */
public interface JobGroupDao {

    List<JobGroup> findAll();

    int save(JobGroup JobGroup);

    int update(JobGroup JobGroup);

    int remove(@Param("id") int id);

    JobGroup load(@Param("id") int id);
}
