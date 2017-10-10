package com.xxl.job.admin.core.route;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xuxueli on 17/3/10.
 */
public abstract class ExecutorRouter {
    protected static Logger logger = LoggerFactory.getLogger(ExecutorRouter.class);

    /**
     * route run executor
     *
     * @param addressList
     * @return  ReturnT.content: final address
     */
    public abstract String routeRun(ArrayList<String> addressList);

}
