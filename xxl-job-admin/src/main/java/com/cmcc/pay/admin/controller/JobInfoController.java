package com.cmcc.pay.admin.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.cmcc.pay.admin.core.enums.ExecutorFailStrategyEnum;
import com.cmcc.pay.admin.core.model.ReturnT;
import com.cmcc.pay.admin.core.model.JobGroup;
import com.cmcc.pay.admin.core.model.JobInfo;
import com.cmcc.pay.admin.service.JobService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cmcc.pay.admin.core.route.ExecutorRouteStrategyEnum;
import com.cmcc.pay.admin.dao.JobGroupDao;
//import com..job.core.enums.ExecutorBlockStrategyEnum;

/**
 * @author chendewei@cmhi.chinamobile.com
 * @version V1.0
 * @date 2016年11月13日 下午1:34:02
 * @since JDK1.8
 * <p>
 * 功能说明: 任务管理控制器入口，表为_JOB_QRTZ_TRIGGER_INFO
 */
@Controller
@RequestMapping("/jobinfo")
public class JobInfoController {

	@Resource
	private JobGroupDao JobGroupDao;
	@Resource
	private JobService JobService;
	
	@RequestMapping
	public String index(Model model, @RequestParam(required = false, defaultValue = "-1") int jobGroup) {

		// 枚举-字典
		model.addAttribute("ExecutorRouteStrategyEnum", ExecutorRouteStrategyEnum.values());	// 路由策略-列表
//		model.addAttribute("GlueTypeEnum", GlueTypeEnum.values());								// Glue类型-字典
//		model.addAttribute("ExecutorBlockStrategyEnum", ExecutorBlockStrategyEnum.values());	// 阻塞处理策略-字典
		model.addAttribute("ExecutorFailStrategyEnum", ExecutorFailStrategyEnum.values());		// 失败处理策略-字典

		// 任务组
		List<JobGroup> jobGroupList =  JobGroupDao.findAll();
		model.addAttribute("JobGroupList", jobGroupList);
		model.addAttribute("jobGroup", jobGroup);

		return "jobinfo/jobinfo.index";
	}
	
	@RequestMapping("/pageList")
	@ResponseBody
	public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,  
			@RequestParam(required = false, defaultValue = "10") int length,
			int jobGroup, String executorHandler, String filterTime) {
		
		return JobService.pageList(start, length, jobGroup, filterTime);
	}
	
	@RequestMapping("/add")
	@ResponseBody
	public ReturnT<String> add(JobInfo jobInfo) {
		return JobService.add(jobInfo);
	}
	
	@RequestMapping("/reschedule")
	@ResponseBody
	public ReturnT<String> reschedule(JobInfo jobInfo) {
		return JobService.reschedule(jobInfo);
	}
	
	@RequestMapping("/remove")
	@ResponseBody
	public ReturnT<String> remove(int id) {
		return JobService.remove(id);
	}
	
	@RequestMapping("/pause")
	@ResponseBody
	public ReturnT<String> pause(int id) {
		return JobService.pause(id);
	}
	
	@RequestMapping("/resume")
	@ResponseBody
	public ReturnT<String> resume(int id) {
		return JobService.resume(id);
	}
	
	@RequestMapping("/trigger")
	@ResponseBody
	public ReturnT<String> triggerJob(int id) {
		return JobService.triggerJob(id);
	}
	
}
