package com.cmcc.pay.admin.dao;

import com.cmcc.pay.admin.core.model.JobGroup;
import com.cmcc.pay.admin.dao.JobGroupDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationcontext-*.xml")
public class JobGroupDaoTest {

    @Resource
    private JobGroupDao JobGroupDao;

    @Test
    public void test(){
        List<JobGroup> list = JobGroupDao.findAll();


        JobGroup group = new JobGroup();
        group.setAppName("setAppName");
        group.setTitle("setTitle");
        group.setOrder(1);
        group.setAddressList("setAddressList");

        int ret = JobGroupDao.save(group);

        JobGroup group2 = JobGroupDao.load(group.getId());
        group2.setAppName("setAppName2");
        group2.setTitle("setTitle2");
        group2.setOrder(2);
        group2.setAddressList("setAddressList2");

        int ret2 = JobGroupDao.update(group2);

        int ret3 = JobGroupDao.remove(group.getId());
    }

}
