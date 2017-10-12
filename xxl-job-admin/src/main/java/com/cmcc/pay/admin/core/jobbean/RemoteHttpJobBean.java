package com.cmcc.pay.admin.core.jobbean;

import com.cmcc.pay.admin.core.trigger.JobTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * http job bean
 * “@DisallowConcurrentExecution” diable concurrent, thread size can not be only one, better given more
 * @author xuxueli 2015-12-17 18:20:34
 */
//@DisallowConcurrentExecution
public class RemoteHttpJobBean extends QuartzJobBean {

	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {

		// load jobId
		JobKey jobKey = context.getTrigger().getJobKey();
		Integer jobId = Integer.valueOf(jobKey.getName());

		// trigger
		JobTrigger.trigger(jobId);
	}

}