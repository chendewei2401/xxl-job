package com.xxl.job.admin.core.route.strategy;

import java.util.ArrayList;

import com.xxl.job.admin.core.route.ExecutorRouter;
import com.xxl.job.admin.core.trigger.XxlJobTrigger;

/**
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteFirst extends ExecutorRouter {

    public String route(ArrayList<String> addressList) {
        return addressList.get(0);
    }

    @Override
    public String routeRun(ArrayList<String> addressList) {

        // address
        String address = route(addressList);

        // run executor
        return XxlJobTrigger.runExecutor(address);
    }
}
