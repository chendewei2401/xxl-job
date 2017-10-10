package com.xxl.job.admin.core.trigger;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxl.job.admin.core.enums.ExecutorFailStrategyEnum;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;
import com.xxl.job.admin.core.route.ExecutorRouteStrategyEnum;
import com.xxl.job.admin.core.schedule.XxlJobDynamicScheduler;
import com.xxl.job.admin.core.thread.JobFailMonitorHelper;

/**
 * xxl-job trigger
 * Created by xuxueli on 17/7/13.
 */
public class XxlJobTrigger {
    private static Logger logger = LoggerFactory.getLogger(XxlJobTrigger.class);

    /**
     * trigger job
     *
     * @param jobId
     */
    public static void trigger(int jobId) {

        // load data
        XxlJobInfo jobInfo = XxlJobDynamicScheduler.xxlJobInfoDao.loadById(jobId);              // job info
        if (jobInfo == null) {
            logger.warn(">>>>>>>>>>>> xxl-job trigger fail, jobId invalid，jobId={}", jobId);
            return;
        }
        XxlJobGroup group = XxlJobDynamicScheduler.xxlJobGroupDao.load(jobInfo.getJobGroup());  // group info

        ExecutorFailStrategyEnum failStrategy = ExecutorFailStrategyEnum.match(jobInfo.getExecutorFailStrategy(), ExecutorFailStrategyEnum.FAIL_ALARM);    // fail strategy
        ExecutorRouteStrategyEnum executorRouteStrategyEnum = ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null);    // route strategy
        ArrayList<String> addressList = (ArrayList<String>) group.getRegistryList();

        // 1、save log-id
        XxlJobLog jobLog = new XxlJobLog();
        jobLog.setJobGroup(jobInfo.getJobGroup());
        jobLog.setJobId(jobInfo.getId());
        XxlJobDynamicScheduler.xxlJobLogDao.save(jobLog);
        logger.debug(">>>>>>>>>>> xxl-job trigger start, jobId:{}", jobLog.getId());

        // 2、prepare trigger-info
        //jobLog.setExecutorAddress(executorAddress);
        jobLog.setGlueType(jobInfo.getGlueType());
        jobLog.setExecutorHandler(jobInfo.getExecutorHandler());
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
        XxlJobDynamicScheduler.xxlJobLogDao.updateTriggerInfo(jobLog);

        // 6、monitor triger
        JobFailMonitorHelper.monitor(jobLog.getId());
        logger.debug(">>>>>>>>>>> xxl-job trigger end, jobId:{}", jobLog.getId());
    }

    /**
     * run executor
     * @param address
     * @return  ReturnT.content: final address
     */
    public static String runExecutor(String address){
//        ReturnT<String> runResult = null;
        try {
//            ExecutorBiz executorBiz = XxlJobDynamicScheduler.getExecutorBiz(address);
//            runResult = executorBiz.run(triggerParam);
            // TODO:curl http://www.baidu.com
            return "200";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "";

    }

}
