package com.cmcc.pay.admin.service;

import com.cmcc.pay.admin.core.enums.ExecutorFailStrategyEnum;
import com.cmcc.pay.admin.core.model.JobGroup;
import com.cmcc.pay.admin.core.model.JobInfo;
import com.cmcc.pay.admin.core.model.ReturnT;
import com.cmcc.pay.admin.core.route.ExecutorRouteStrategyEnum;
import com.cmcc.pay.admin.core.schedule.JobDynamicScheduler;
import com.cmcc.pay.admin.dao.JobGroupDao;
import com.cmcc.pay.admin.dao.JobInfoDao;
import com.cmcc.pay.admin.dao.JobLogDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.quartz.CronExpression;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import com..job.core.enums.ExecutorBlockStrategyEnum;
//import com..job.core.glue.GlueTypeEnum;

/**
 * core job action for -job
 * @author xuxueli 2016-5-28 15:30:33
 */
@Service
public class JobService {
	private static Logger logger = LoggerFactory.getLogger(JobService.class);

	@Resource
	private JobGroupDao JobGroupDao;
	@Resource
	private JobInfoDao JobInfoDao;
	@Resource
	public JobLogDao JobLogDao;

	public Map<String, Object> pageList(int start, int length, int jobGroup, String filterTime) {

		// page list
		List<JobInfo> list = JobInfoDao.pageList(start, length, jobGroup);
		int list_count = JobInfoDao.pageListCount(start, length, jobGroup);

		// fill job info
		if (list!=null && list.size()>0) {
			for (JobInfo jobInfo : list) {
				JobDynamicScheduler.fillJobInfo(jobInfo);
			}
		}

		// package result
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put("recordsTotal", list_count);		// 总记录数
		maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
		maps.put("data", list);  					// 分页列表
		return maps;
	}


	public ReturnT<String> add(JobInfo jobInfo) {
		// valid
		JobGroup group = JobGroupDao.load(jobInfo.getJobGroup());
		if (group == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "请选择“执行器”");
		}
		if (!CronExpression.isValidExpression(jobInfo.getJobCron())) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "请输入格式正确的“Cron”");
		}
		if (StringUtils.isBlank(jobInfo.getJobDesc())) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "请输入“任务描述”");
		}
		if (StringUtils.isBlank(jobInfo.getAuthor())) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "请输入“负责人”");
		}
		if (ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "路由策略非法");
		}

		if (ExecutorFailStrategyEnum.match(jobInfo.getExecutorFailStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "失败处理策略非法");
		}


		// childJobKey valid
		if (StringUtils.isNotBlank(jobInfo.getChildJobKey())) {
			String[] childJobKeys = jobInfo.getChildJobKey().split(",");
			for (String childJobKeyItem: childJobKeys) {
				String[] childJobKeyArr = childJobKeyItem.split("_");
				if (childJobKeyArr.length!=2) {
					return new ReturnT<String>(ReturnT.FAIL_CODE, MessageFormat.format("子任务Key({0})格式错误", childJobKeyItem));
				}
				JobInfo childJobInfo = JobInfoDao.loadById(Integer.valueOf(childJobKeyArr[1]));
				if (childJobInfo==null) {
					return new ReturnT<String>(ReturnT.FAIL_CODE, MessageFormat.format("子任务Key({0})无效", childJobKeyItem));
				}
			}
		}

		// add in db
		JobInfoDao.save(jobInfo);
		if (jobInfo.getId() < 1) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "新增任务失败");
		}

		// add in quartz
		String qz_group = String.valueOf(jobInfo.getJobGroup());
		String qz_name = String.valueOf(jobInfo.getId());
		try {
			JobDynamicScheduler.addJob(qz_name, qz_group, jobInfo.getJobCron());
			//JobDynamicScheduler.pauseJob(qz_name, qz_group);
			return ReturnT.SUCCESS;
		} catch (SchedulerException e) {
			logger.error(e.getMessage(), e);
			try {
				JobInfoDao.delete(jobInfo.getId());
				JobDynamicScheduler.removeJob(qz_name, qz_group);
			} catch (SchedulerException e1) {
				logger.error(e.getMessage(), e1);
			}
			return new ReturnT<String>(ReturnT.FAIL_CODE, "新增任务失败:" + e.getMessage());
		}
	}


	public ReturnT<String> reschedule(JobInfo jobInfo) {

		// valid
		if (!CronExpression.isValidExpression(jobInfo.getJobCron())) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "请输入格式正确的“Cron”");
		}
		if (StringUtils.isBlank(jobInfo.getJobDesc())) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "请输入“任务描述”");
		}
		if (StringUtils.isBlank(jobInfo.getAuthor())) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "请输入“负责人”");
		}
		if (ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "路由策略非法");
		}

		if (ExecutorFailStrategyEnum.match(jobInfo.getExecutorFailStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "失败处理策略非法");
		}

		// childJobKey valid
		if (StringUtils.isNotBlank(jobInfo.getChildJobKey())) {
			String[] childJobKeys = jobInfo.getChildJobKey().split(",");
			for (String childJobKeyItem: childJobKeys) {
				String[] childJobKeyArr = childJobKeyItem.split("_");
				if (childJobKeyArr.length!=2) {
					return new ReturnT<String>(ReturnT.FAIL_CODE, MessageFormat.format("子任务Key({0})格式错误", childJobKeyItem));
				}
				JobInfo childJobInfo = JobInfoDao.loadById(Integer.valueOf(childJobKeyArr[1]));
				if (childJobInfo==null) {
					return new ReturnT<String>(ReturnT.FAIL_CODE, MessageFormat.format("子任务Key({0})无效", childJobKeyItem));
				}
			}
		}

		// stage job info
		JobInfo exists_jobInfo = JobInfoDao.loadById(jobInfo.getId());
		if (exists_jobInfo == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "参数异常");
		}
		//String old_cron = exists_jobInfo.getJobCron();

		exists_jobInfo.setJobCron(jobInfo.getJobCron());
		exists_jobInfo.setJobDesc(jobInfo.getJobDesc());
		exists_jobInfo.setAuthor(jobInfo.getAuthor());
		exists_jobInfo.setAlarmEmail(jobInfo.getAlarmEmail());
		exists_jobInfo.setExecutorRouteStrategy(jobInfo.getExecutorRouteStrategy());
		exists_jobInfo.setExecutorParam(jobInfo.getExecutorParam());
		exists_jobInfo.setExecutorBlockStrategy(jobInfo.getExecutorBlockStrategy());
		exists_jobInfo.setExecutorFailStrategy(jobInfo.getExecutorFailStrategy());
		exists_jobInfo.setChildJobKey(jobInfo.getChildJobKey());
		JobInfoDao.update(exists_jobInfo);

		// fresh quartz
		String qz_group = String.valueOf(exists_jobInfo.getJobGroup());
		String qz_name = String.valueOf(exists_jobInfo.getId());
		try {
			boolean ret = JobDynamicScheduler.rescheduleJob(qz_group, qz_name, exists_jobInfo.getJobCron());
			return ret?ReturnT.SUCCESS:ReturnT.FAIL;
		} catch (SchedulerException e) {
			logger.error(e.getMessage(), e);
		}

		return ReturnT.FAIL;
	}


	public ReturnT<String> remove(int id) {
		JobInfo JobInfo = JobInfoDao.loadById(id);
		String group = String.valueOf(JobInfo.getJobGroup());
		String name = String.valueOf(JobInfo.getId());

		try {
			JobDynamicScheduler.removeJob(name, group);
			JobInfoDao.delete(id);
			JobLogDao.delete(id);
			return ReturnT.SUCCESS;
		} catch (SchedulerException e) {
			logger.error(e.getMessage(), e);
		}
		return ReturnT.FAIL;
	}


	public ReturnT<String> pause(int id) {
		JobInfo JobInfo = JobInfoDao.loadById(id);
		String group = String.valueOf(JobInfo.getJobGroup());
		String name = String.valueOf(JobInfo.getId());

		try {
			boolean ret = JobDynamicScheduler.pauseJob(name, group);	// jobStatus do not store
			return ret?ReturnT.SUCCESS:ReturnT.FAIL;
		} catch (SchedulerException e) {
			logger.error(e.getMessage(), e);
			return ReturnT.FAIL;
		}
	}


	public ReturnT<String> resume(int id) {
		JobInfo JobInfo = JobInfoDao.loadById(id);
		String group = String.valueOf(JobInfo.getJobGroup());
		String name = String.valueOf(JobInfo.getId());

		try {
			boolean ret = JobDynamicScheduler.resumeJob(name, group);
			return ret?ReturnT.SUCCESS:ReturnT.FAIL;
		} catch (SchedulerException e) {
			logger.error(e.getMessage(), e);
			return ReturnT.FAIL;
		}
	}


	public ReturnT<String> triggerJob(int id) {
		JobInfo JobInfo = JobInfoDao.loadById(id);
		if (JobInfo == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "任务ID非法");
		}

		String group = String.valueOf(JobInfo.getJobGroup());
		String name = String.valueOf(JobInfo.getId());

		try {
			JobDynamicScheduler.triggerJob(name, group);
			return ReturnT.SUCCESS;
		} catch (SchedulerException e) {
			logger.error(e.getMessage(), e);
			return new ReturnT<String>(ReturnT.FAIL_CODE, e.getMessage());
		}
	}


	public Map<String, Object> dashboardInfo() {

		int jobInfoCount = JobInfoDao.findAllCount();
		int jobLogCount = JobLogDao.triggerCountByHandleCode(-1);
		int jobLogSuccessCount = JobLogDao.triggerCountByHandleCode(ReturnT.SUCCESS_CODE);

		// executor count
		Set<String> executerAddressSet = new HashSet<String>();
		List<JobGroup> groupList = JobGroupDao.findAll();

		if (CollectionUtils.isNotEmpty(groupList)) {
			for (JobGroup group: groupList) {
				if (CollectionUtils.isNotEmpty(group.getRegistryList())) {
					executerAddressSet.addAll(group.getRegistryList());
				}
			}
		}

		int executorCount = executerAddressSet.size();

		Map<String, Object> dashboardMap = new HashMap<String, Object>();
		dashboardMap.put("jobInfoCount", jobInfoCount);
		dashboardMap.put("jobLogCount", jobLogCount);
		dashboardMap.put("jobLogSuccessCount", jobLogSuccessCount);
		dashboardMap.put("executorCount", executorCount);
		return dashboardMap;
	}


	public ReturnT<Map<String, Object>> triggerChartDate() {
		Date from = DateUtils.addDays(new Date(), -30);
		Date to = new Date();

		List<String> triggerDayList = new ArrayList<String>();
		List<Integer> triggerDayCountSucList = new ArrayList<Integer>();
		List<Integer> triggerDayCountFailList = new ArrayList<Integer>();
		int triggerCountSucTotal = 0;
		int triggerCountFailTotal = 0;

		List<Map<String, Object>> triggerCountMapAll = JobLogDao.triggerCountByDay(from, to, -1);
		List<Map<String, Object>> triggerCountMapSuc = JobLogDao.triggerCountByDay(from, to, ReturnT.SUCCESS_CODE);
		if (CollectionUtils.isNotEmpty(triggerCountMapAll)) {
			for (Map<String, Object> item: triggerCountMapAll) {
				String day = String.valueOf(item.get("triggerDay"));
				int dayAllCount = Integer.valueOf(String.valueOf(item.get("triggerCount")));
				int daySucCount = 0;
				int dayFailCount = dayAllCount - daySucCount;

				if (CollectionUtils.isNotEmpty(triggerCountMapSuc)) {
					for (Map<String, Object> sucItem: triggerCountMapSuc) {
						String daySuc = String.valueOf(sucItem.get("triggerDay"));
						if (day.equals(daySuc)) {
							daySucCount = Integer.valueOf(String.valueOf(sucItem.get("triggerCount")));
							dayFailCount = dayAllCount - daySucCount;
						}
					}
				}

				triggerDayList.add(day);
				triggerDayCountSucList.add(daySucCount);
				triggerDayCountFailList.add(dayFailCount);
				triggerCountSucTotal += daySucCount;
				triggerCountFailTotal += dayFailCount;
			}
		} else {
			for (int i = 4; i > -1; i--) {
				triggerDayList.add(FastDateFormat.getInstance("yyyy-MM-dd").format(DateUtils.addDays(new Date(), -i)));
				triggerDayCountSucList.add(0);
				triggerDayCountFailList.add(0);
			}
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("triggerDayList", triggerDayList);
		result.put("triggerDayCountSucList", triggerDayCountSucList);
		result.put("triggerDayCountFailList", triggerDayCountFailList);
		result.put("triggerCountSucTotal", triggerCountSucTotal);
		result.put("triggerCountFailTotal", triggerCountFailTotal);
		return new ReturnT<Map<String, Object>>(result);
	}

}
