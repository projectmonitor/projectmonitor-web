package com.projectmonitor.projectstatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RevertProjectStatusController {

    private ProductionRevertFlag productionRevertFlag;
    private ProductionRevertTask productionRevertTask;

    @Autowired
    public RevertProjectStatusController(ProductionRevertFlag productionRevertFlag,
                                         ProductionRevertTask productionRevertTask) {
        this.productionRevertFlag = productionRevertFlag;
        this.productionRevertTask = productionRevertTask;
    }

    @PostMapping("/")
    public String execute() {
        productionRevertFlag.set();
        productionRevertTask.start();



        // build the prod deploy queue on production deploys - done
        // on server boot reset production revert flag
            // or determine state based on running revert job?
        // mark a production revert flag so other background job doesn't try shit - done
            // (also can disable button) - done
            // (also used to mark background red as can be)
        // enqueue a revert job (async) that ->



        // redirect holmes
        return "redirect:/";
    }
}
