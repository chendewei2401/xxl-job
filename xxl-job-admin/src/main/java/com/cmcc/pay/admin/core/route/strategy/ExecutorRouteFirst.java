package com.cmcc.pay.admin.core.route.strategy;

import java.util.ArrayList;

import com.cmcc.pay.admin.core.trigger.JobTrigger;
import com.cmcc.pay.admin.core.route.ExecutorRouter;

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
        return JobTrigger.runExecutor(address);
    }
}
