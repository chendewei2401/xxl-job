package com.cmcc.pay.admin.core.thread;

import com.cmcc.pay.admin.core.model.JobGroup;
import com.cmcc.pay.admin.core.model.JobInfo;
import com.cmcc.pay.admin.core.model.JobLog;
import com.cmcc.pay.admin.core.schedule.JobDynamicScheduler;
import com.cmcc.pay.admin.core.util.MailUtil;
import com.cmcc.pay.admin.core.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * job monitor instance
 * @author xuxueli 2015-9-1 18:05:56
 */
public class JobFailMonitorHelper {
	private static Logger logger = LoggerFactory.getLogger(JobFailMonitorHelper.class);
	
	private static JobFailMonitorHelper instance = new JobFailMonitorHelper();
	public static JobFailMonitorHelper getInstance(){
		return instance;
	}

	private LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>(0xfff8);

	private Thread monitorThread;
	private volatile boolean toStop = false;
	public void start(){
		monitorThread = new Thread(new Runnable() {

			@Override
			public void run() {

				// monitor
				while (!toStop) {
					try {
						Integer jobLogId = JobFailMonitorHelper.instance.queue.take();
						if (jobLogId != null && jobLogId > 0) {
							JobLog log = JobDynamicScheduler.JobLogDao.load(jobLogId);
							if (log!=null) {
								if (ReturnT.SUCCESS_CODE==log.getTriggerCode() && log.getHandleCode()==0) {
									// job running, wait + again monitor
									TimeUnit.SECONDS.sleep(10);

									JobFailMonitorHelper.monitor(jobLogId);
									logger.info(">>>>>>>>>>> job monitor, job running, JobLogId:{}", jobLogId);
								}
								if (ReturnT.SUCCESS_CODE==log.getTriggerCode() && ReturnT.SUCCESS_CODE==log.getHandleCode()) {
									// job success, pass
									logger.info(">>>>>>>>>>> job monitor, job success, JobLogId:{}", jobLogId);
								}

								if (ReturnT.FAIL_CODE == log.getTriggerCode()|| ReturnT.FAIL_CODE==log.getHandleCode()) {
									// job fail,
									sendMonitorEmail(log);
									logger.info(">>>>>>>>>>> job monitor, job fail, JobLogId:{}", jobLogId);
								}
							}
						}
					} catch (Exception e) {
						logger.error("job monitor error:{}", e);
					}
				}

				// monitor all clear
				List<Integer> jobLogIdList = new ArrayList<Integer>();
				int drainToNum = getInstance().queue.drainTo(jobLogIdList);
				if (jobLogIdList!=null && jobLogIdList.size()>0) {
					for (Integer jobLogId: jobLogIdList) {
						JobLog log = JobDynamicScheduler.JobLogDao.load(jobLogId);
						if (ReturnT.FAIL_CODE == log.getTriggerCode()|| ReturnT.FAIL_CODE==log.getHandleCode()) {
							// job fail,
							sendMonitorEmail(log);
							logger.info(">>>>>>>>>>> job monitor last, job fail, JobLogId:{}", jobLogId);
						}
					}
				}

			}
		});
		monitorThread.setDaemon(true);
		monitorThread.start();
	}

	/**
	 * send monitor email
	 * @param jobLog
	 */
	private void sendMonitorEmail(JobLog jobLog){
		JobInfo info = JobDynamicScheduler.JobInfoDao.loadById(jobLog.getJobId());
		if (info!=null && info.getAlarmEmail()!=null && info.getAlarmEmail().trim().length()>0) {

			Set<String> emailSet = new HashSet<String>(Arrays.asList(info.getAlarmEmail().split(",")));
			for (String email: emailSet) {
				String title = "定时任务任务调度中心报警";
				JobGroup group = JobDynamicScheduler.JobGroupDao.load(Integer.valueOf(info.getJobGroup()));
				String content = MessageFormat.format("任务调度失败, 执行器名称:{0}, 任务描述:{1}.", group!=null?group.getTitle():"null", info.getJobDesc());
				MailUtil.sendMail(email, title, content, false, null);
			}
		}
	}

	public void toStop(){
		toStop = true;
		// interrupt and wait
		monitorThread.interrupt();
		try {
			monitorThread.join();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	// producer
	public static void monitor(int jobLogId){
		getInstance().queue.offer(jobLogId);
	}
	
}
