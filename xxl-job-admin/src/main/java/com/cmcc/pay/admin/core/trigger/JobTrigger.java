package com.cmcc.pay.admin.core.trigger;

import java.util.ArrayList;
import java.util.Date;

import com.cmcc.pay.admin.core.thread.JobFailMonitorHelper;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmcc.pay.admin.core.enums.ExecutorFailStrategyEnum;
import com.cmcc.pay.admin.core.model.JobGroup;
import com.cmcc.pay.admin.core.model.JobInfo;
import com.cmcc.pay.admin.core.model.JobLog;
import com.cmcc.pay.admin.core.route.ExecutorRouteStrategyEnum;
import com.cmcc.pay.admin.core.schedule.JobDynamicScheduler;

/**
 * -job trigger
 * Created by xuxueli on 17/7/13.
 */
public class JobTrigger {
    private static Logger logger = LoggerFactory.getLogger(JobTrigger.class);

    /**
     * trigger job
     *
     * @param jobId
     */
    public static void trigger(int jobId) {

        // load data
        JobInfo jobInfo = JobDynamicScheduler.JobInfoDao.loadById(jobId);              // job info
        if (jobInfo == null) {
            logger.warn(">>>>>>>>>>>> -job trigger fail, jobId invalid，jobId={}", jobId);
            return;
        }
        JobGroup group = JobDynamicScheduler.JobGroupDao.load(jobInfo.getJobGroup());  // group info

        ExecutorFailStrategyEnum failStrategy = ExecutorFailStrategyEnum.match(jobInfo.getExecutorFailStrategy(), ExecutorFailStrategyEnum.FAIL_ALARM);    // fail strategy
        ExecutorRouteStrategyEnum executorRouteStrategyEnum = ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null);    // route strategy
        ArrayList<String> addressList = (ArrayList<String>) group.getRegistryList();

        // 1、save log-id
        JobLog jobLog = new JobLog();
        jobLog.setJobGroup(jobInfo.getJobGroup());
        jobLog.setJobId(jobInfo.getId());
        JobDynamicScheduler.JobLogDao.save(jobLog);
        logger.debug(">>>>>>>>>>> -job trigger start, jobId:{}", jobLog.getId());

        // 2、prepare trigger-info
        //jobLog.setExecutorAddress(executorAddress);
        jobLog.setExecutorParam(jobInfo.getExecutorParam());
        jobLog.setTriggerTime(new Date());

        StringBuffer triggerMsgSb = new StringBuffer();
        triggerMsgSb.append("<br>失败处理策略：").append(failStrategy.getTitle());
        triggerMsgSb.append("<br>地址列表：").append(group.getRegistryList());
        triggerMsgSb.append("<br>路由策略：").append(executorRouteStrategyEnum.getTitle());

        String triggerResult = "200";
        // 3、trigger-valid
        if (CollectionUtils.isEmpty(addressList)) {
            triggerResult = "999";
            triggerMsgSb.append("<br>----------------------<br>").append("调度失败：").append("执行器地址为空");
        }

        if (triggerResult == "200") {

            // 4.2、trigger-run (route run / trigger remote executor)
            triggerResult = executorRouteStrategyEnum.getRouter().routeRun(addressList);
            triggerMsgSb.append("<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>").append(triggerResult);

            // 4.3、trigger (fail retry)
            if (triggerResult != "200" && failStrategy == ExecutorFailStrategyEnum.FAIL_RETRY) {
                triggerResult = executorRouteStrategyEnum.getRouter().routeRun(addressList);
                triggerMsgSb.append("<br><br><span style=\"color:#F39C12;\" > >>>>>>>>>>>失败重试<<<<<<<<<<< </span><br>").append(triggerResult);
            }
        }

        // 5、save trigger-info
        jobLog.setExecutorAddress(addressList.get(0));
        jobLog.setTriggerCode(Integer.valueOf(triggerResult));
        jobLog.setTriggerMsg(triggerMsgSb.toString());
        JobDynamicScheduler.JobLogDao.updateTriggerInfo(jobLog);

        // 6、monitor triger
        JobFailMonitorHelper.monitor(jobLog.getId());
        logger.debug(">>>>>>>>>>> -job trigger end, jobId:{}", jobLog.getId());
    }

    /**
     * run executor
     * @param address
     * @return  ReturnT.content: final address
     */
    public static String runExecutor(String address){
//        ReturnT<String> runResult = null;
        try {
//            ExecutorBiz executorBiz = JobDynamicScheduler.getExecutorBiz(address);
//            runResult = executorBiz.run(triggerParam);
            // TODO:curl http://www.baidu.com
            return "200";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "";

    }

}
