package com.projectmonitor.projectstatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RevertProjectStatusController {

    private ProductionRevertFlag productionRevertFlag;

    @Autowired
    public RevertProjectStatusController(ProductionRevertFlag productionRevertFlag) {
        this.productionRevertFlag = productionRevertFlag;
    }

    @PostMapping("/")
    public String execute() {
        productionRevertFlag.set();

        // TODO:
        // build the prod deploy queue on production deploys - start here!!!!!
        // mark a production revert flag so other background job doesn't try shit - done
            // (also can disable button) - done
            // (also used to mark background red as can be)
        // enqueue a revert job (async) that ->
            // determine production sha/story (prod1)
                // (what if prod is down yo?) (our own list of deploys)?
            // determine SA sha/story (SA1)
            // get previous production story (not stored anywhere yet) (prod2)

            // reject production story (prod1)
            // deploy previous prod to prod (prod2)
            // mark the SA story back to finished (sa1)
            // put the SA story at the top (fornt) of the build queue (sa1)
            // deploy production (prod1) to story acceptance
            // finally clear the production revert flag so things can go back to normal


        // redirect holmes
        return "redirect:/";
    }
}
