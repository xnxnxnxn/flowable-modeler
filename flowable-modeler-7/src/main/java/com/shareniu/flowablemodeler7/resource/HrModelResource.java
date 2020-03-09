package com.shareniu.flowablemodeler7.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.shareniu.flowablemodeler7.service.IHrService;
import com.shareniu.flowablemodeler7.util.UserUtil;
import org.apache.commons.lang3.StringUtils;
import org.flowable.ui.common.security.SecurityUtils;
import org.flowable.ui.common.service.exception.BadRequestException;
import org.flowable.ui.common.service.exception.ConflictingRequestException;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.model.ModelKeyRepresentation;
import org.flowable.ui.modeler.model.ModelRepresentation;
import org.flowable.ui.modeler.rest.app.ModelResource;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Date;

@RestController
@RequestMapping("/hr")
public class HrModelResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelResource.class);

    private static final String RESOLVE_ACTION_OVERWRITE = "overwrite";
    private static final String RESOLVE_ACTION_SAVE_AS = "saveAs";
    private static final String RESOLVE_ACTION_NEW_VERSION = "newVersion";


    @Autowired
    protected ModelService modelService;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private IHrService iHrService;


    /**
     * POST /rest/models/{modelId}/editor/json -> save the JSON model
     */
    @PostMapping(value = "/rest/models/{modelId}/editor/json")
    public ModelRepresentation saveModel(@PathVariable String modelId, @RequestBody MultiValueMap<String, String> values) {
        // Validation: see if there was another update in the meantime
        long lastUpdated = -1L;
        String lastUpdatedString = values.getFirst("lastUpdated");
        if (lastUpdatedString == null) {
            throw new BadRequestException("Missing lastUpdated date");
        }
        try {
            Date readValue = objectMapper.getDeserializationConfig().getDateFormat().parse(lastUpdatedString);
            lastUpdated = readValue.getTime();
        } catch (ParseException e) {
            throw new BadRequestException("Invalid lastUpdated date: '" + lastUpdatedString + "'");
        }

        Model model = modelService.getModel(modelId);
//        User currentUser = SecurityUtils.getCurrentUserObject();
//        boolean currentUserIsOwner = model.getLastUpdatedBy().equals(currentUser.getId());
        String resolveAction = values.getFirst("conflictResolveAction");

        // If timestamps differ, there is a conflict or a conflict has been resolved by the user
        if (model.getLastUpdated().getTime() != lastUpdated) {

            if (RESOLVE_ACTION_SAVE_AS.equals(resolveAction)) {

                String saveAs = values.getFirst("saveAs");
                String json = values.getFirst("json_xml");
                return createNewModel(saveAs, model.getDescription(), model.getModelType(), json);

            } else if (RESOLVE_ACTION_OVERWRITE.equals(resolveAction)) {
                return updateModel(model, values, false);
            } else if (RESOLVE_ACTION_NEW_VERSION.equals(resolveAction)) {
                return updateModel(model, values, true);
            } else {

                // Exception case: the user is the owner and selected to create a new version
                String isNewVersionString = values.getFirst("newversion");
                if ("true".equals(isNewVersionString)) {
                    return updateModel(model, values, true);
                } else {
                    // Tried everything, this is really a conflict, return 409
                    ConflictingRequestException exception = new ConflictingRequestException("Process model was updated in the meantime");
                    exception.addCustomData("userFullName", model.getLastUpdatedBy());
//                    exception.addCustomData("newVersionAllowed", currentUserIsOwner);
                    throw exception;
                }
            }

        } else {
            // Actual, regular, update
            return updateModel(model, values, false);

        }
    }

    private ModelRepresentation updateModel(Model model, MultiValueMap<String, String> values, boolean forceNewVersion) {

        String name = values.getFirst("name");
        String key = values.getFirst("key").replaceAll(" ", "");
        String description = values.getFirst("description");
        String isNewVersionString = values.getFirst("newversion");
        String newVersionComment = null;

        ModelKeyRepresentation modelKeyInfo = modelService.validateModelKey(model, model.getModelType(), key);
        if (modelKeyInfo.isKeyAlreadyExists()) {
            throw new BadRequestException("Model with provided key already exists " + key);
        }

        boolean newVersion = false;
        if (forceNewVersion) {
            newVersion = true;
            newVersionComment = values.getFirst("comment");
        } else {
            if (isNewVersionString != null) {
                newVersion = "true".equals(isNewVersionString);
                newVersionComment = values.getFirst("comment");
            }
        }

        String json = values.getFirst("json_xml");

        try {
            ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(json);

            ObjectNode propertiesNode = (ObjectNode) editorJsonNode.get("properties");
            String processId = key;
            propertiesNode.put("process_id", processId);
            propertiesNode.put("name", name);
            if (StringUtils.isNotEmpty(description)) {
                propertiesNode.put("documentation", description);
            }
            editorJsonNode.set("properties", propertiesNode);
            model = modelService.saveModel(model.getId(), name, key, description, editorJsonNode.toString(), newVersion,
                    newVersionComment, UserUtil.getUser());
            // 更新完成后直接部署到HR系统
            iHrService.deployToHr(model);
            return new ModelRepresentation(model);

        } catch (Exception e) {
            LOGGER.error("Error saving model {}", model.getId(), e);
            throw new BadRequestException("Process model could not be saved " + model.getId());
        }
    }

    private ModelRepresentation createNewModel(String name, String description, Integer modelType, String editorJson) {
        ModelRepresentation model = new ModelRepresentation();
        model.setName(name);
        model.setDescription(description);
        model.setModelType(modelType);
        Model newModel = modelService.createModel(model, editorJson, SecurityUtils.getCurrentUserObject());
        return new ModelRepresentation(newModel);
    }

}
