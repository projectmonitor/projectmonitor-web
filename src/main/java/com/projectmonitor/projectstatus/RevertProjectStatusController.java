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
        // TODO:
        // display the errors we are saving in the revert job
            // allow clearing of them how?
        // on server boot reset production revert flag
            // or determine state based on running revert job?
        return "redirect:/";
    }
}
