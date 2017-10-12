package com.cmcc.pay.admin.controller;

import com.cmcc.pay.admin.core.model.ReturnT;
import com.cmcc.pay.admin.core.model.JobGroup;
import com.cmcc.pay.admin.dao.JobGroupDao;
import com.cmcc.pay.admin.dao.JobInfoDao;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author chendewei@cmhi.chinamobile.com
 * @version V1.0
 * @date 2016年11月13日 下午1:34:02
 * @since JDK1.8
 * <p>
 * 功能说明: 执行器控制入口，表为_JOB_QRTZ_TRIGGER_GROUP
 */
@Controller
@RequestMapping("/jobgroup")
public class JobGroupController {

	@Resource
	public JobInfoDao JobInfoDao;
	@Resource
	public JobGroupDao JobGroupDao;

	@RequestMapping
	public String index(Model model) {

		// job group (executor)
		List<JobGroup> list = JobGroupDao.findAll();

		model.addAttribute("list", list);
		return "jobgroup/jobgroup.index";
	}

	@RequestMapping("/save")
	@ResponseBody
	public ReturnT<String> save(JobGroup JobGroup){

		// valid
		if (JobGroup.getAppName()==null || StringUtils.isBlank(JobGroup.getAppName())) {
			return new ReturnT<String>(500, "请输入AppName");
		}
		if (JobGroup.getAppName().length()>64) {
			return new ReturnT<String>(500, "AppName长度限制为4~64");
		}
		if (JobGroup.getTitle()==null || StringUtils.isBlank(JobGroup.getTitle())) {
			return new ReturnT<String>(500, "请输入名称");
		}
		if (StringUtils.isBlank(JobGroup.getAddressList())) {
			return new ReturnT<String>(500, "机器地址不可为空");
		}
		String[] addresss = JobGroup.getAddressList().split(",");
		for (String item: addresss) {
			if (StringUtils.isBlank(item)) {
				return new ReturnT<String>(500, "机器地址非法");
			}
		}

		int ret = JobGroupDao.save(JobGroup);
		return (ret>0)?ReturnT.SUCCESS:ReturnT.FAIL;
	}

	@RequestMapping("/update")
	@ResponseBody
	public ReturnT<String> update(JobGroup JobGroup){
		// valid
		if (JobGroup.getAppName()==null || StringUtils.isBlank(JobGroup.getAppName())) {
			return new ReturnT<String>(500, "请输入AppName");
		}
		if (JobGroup.getAppName().length()>64) {
			return new ReturnT<String>(500, "AppName长度限制为4~64");
		}
		if (JobGroup.getTitle()==null || StringUtils.isBlank(JobGroup.getTitle())) {
			return new ReturnT<String>(500, "请输入名称");
		}
		if (StringUtils.isBlank(JobGroup.getAddressList())) {
			return new ReturnT<String>(500, "机器地址不可为空");
		}
		String[] addresss = JobGroup.getAddressList().split(",");
		for (String item: addresss) {
			if (StringUtils.isBlank(item)) {
				return new ReturnT<String>(500, "机器地址非法");
			}
		}

		int ret = JobGroupDao.update(JobGroup);
		return (ret>0)?ReturnT.SUCCESS:ReturnT.FAIL;
	}

	@RequestMapping("/remove")
	@ResponseBody
	public ReturnT<String> remove(int id){

		// valid
		int count = JobInfoDao.pageListCount(0, 10, id);
		if (count > 0) {
			return new ReturnT<String>(500, "该分组使用中, 不可删除");
		}

		List<JobGroup> allList = JobGroupDao.findAll();
		if (allList.size() == 1) {
			return new ReturnT<String>(500, "删除失败, 系统需要至少预留一个默认分组");
		}

		int ret = JobGroupDao.remove(id);
		return (ret>0)?ReturnT.SUCCESS:ReturnT.FAIL;
	}

}
