package com.shareniu.flowablemodeler7.resource;


import com.shareniu.flowablemodeler7.service.IHrService;
import org.flowable.ui.common.model.BaseRestActionRepresentation;
import org.flowable.ui.common.security.SecurityUtils;
import org.flowable.ui.common.service.exception.BadRequestException;
import org.flowable.ui.modeler.domain.ModelHistory;
import org.flowable.ui.modeler.model.ReviveModelResultRepresentation;
import org.flowable.ui.modeler.rest.app.AbstractModelHistoryResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hr")
public class HrModelHistoryResource extends AbstractModelHistoryResource {

    @Autowired
    private IHrService iHrService;

    @PostMapping(value = "/rest/models/{modelId}/history/{modelHistoryId}", produces = "application/json")
    public ReviveModelResultRepresentation executeProcessModelHistoryAction(@PathVariable String modelId, @PathVariable String modelHistoryId,
                                                                            @RequestBody(required = true) BaseRestActionRepresentation action) {

        // In order to execute actions on a historic process model, write permission is needed
        ModelHistory modelHistory = modelService.getModelHistory(modelId, modelHistoryId);

        if ("useAsNewVersion".equals(action.getAction())) {
            ReviveModelResultRepresentation reviveModelResultRepresentation = modelService.reviveProcessModelHistory(modelHistory, SecurityUtils.getCurrentUserObject(), action.getComment());
            // 进行部署到Hr
            iHrService.deployToHr(modelHistory.getModelId());
            return reviveModelResultRepresentation;
        } else {
            throw new BadRequestException("Invalid action to execute on model history " + modelHistoryId + ": " + action.getAction());
        }
    }

}
