package com.shareniu.flowablemodeler7.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.shareniu.flowablemodeler7.util.UserUtil;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.idm.api.User;
import org.flowable.ui.common.service.exception.BadRequestException;
import org.flowable.ui.common.util.XmlUtil;
import org.flowable.ui.modeler.domain.AbstractModel;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.model.ModelRepresentation;
import org.flowable.ui.modeler.repository.ModelRepository;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStreamReader;
import java.util.List;

@RestController
@RequestMapping("/hr")
public class HrController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HrController.class);

    @Autowired
    protected ModelRepository modelRepository;
    @Autowired
    protected ModelService modelService;

    protected BpmnXMLConverter bpmnXmlConverter = new BpmnXMLConverter();
    protected BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();

    @RequestMapping(value = "/get-id-by-key", method = RequestMethod.GET)
    public String getProcessIdByKey(String key) {
        if (key == null) {
            return null;
        }
        // 获取ID
        List<Model> list = modelRepository.findByKeyAndType(key, AbstractModel.MODEL_TYPE_BPMN);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0).getId();
    }

    @PostMapping(value = "/rest/import-process-model", produces = "application/json")
    public ModelRepresentation importProcessModel(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName != null && (fileName.endsWith(".bpmn") || fileName.endsWith(".bpmn20.xml"))) {
            try {
                XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();
                InputStreamReader xmlIn = new InputStreamReader(file.getInputStream(), "UTF-8");
                XMLStreamReader xtr = xif.createXMLStreamReader(xmlIn);
                BpmnModel bpmnModel = bpmnXmlConverter.convertToBpmnModel(xtr);
                if (org.flowable.editor.language.json.converter.util.CollectionUtils.isEmpty(bpmnModel.getProcesses())) {
                    throw new BadRequestException("No process found in definition " + fileName);
                }

                if (bpmnModel.getLocationMap().size() == 0) {
                    BpmnAutoLayout bpmnLayout = new BpmnAutoLayout(bpmnModel);
                    bpmnLayout.execute();
                }

                ObjectNode modelNode = bpmnJsonConverter.convertToJson(bpmnModel);


                org.flowable.bpmn.model.Process process = bpmnModel.getMainProcess();
                String name = process.getId();
                if (StringUtils.isNotEmpty(process.getName())) {
                    name = process.getName();
                }
                String description = process.getDocumentation();

                ModelRepresentation model = new ModelRepresentation();
                model.setKey(process.getId());
                model.setName(name);
                model.setDescription(description);
                model.setModelType(AbstractModel.MODEL_TYPE_BPMN);

                // 内置用户
                User user = UserUtil.getUser();
                Model saveModel;
                // 根据key是否在数据库中存在 判断是更新还是新增
                List<Model> list = modelRepository.findByKeyAndType(model.getKey(), AbstractModel.MODEL_TYPE_BPMN);
                if (CollectionUtils.isEmpty(list)) {
                    // 新增
                    saveModel = modelService.createModel(model, modelNode.toString(), user);
                } else {
                    // 更新
                    saveModel = modelService.saveModel(list.get(0).getId(), model.getName(), model.getKey(), model.getDescription(), modelNode.toString(), true,
                            null, UserUtil.getUser());
                }
                return new ModelRepresentation(saveModel);

            } catch (BadRequestException e) {
                throw e;

            } catch (Exception e) {
                LOGGER.error("Import failed for {}", fileName, e);
                throw new BadRequestException("Import failed for " + fileName + ", error message " + e.getMessage());
            }
        } else {
            throw new BadRequestException("Invalid file name, only .bpmn and .bpmn20.xml files are supported not " + fileName);
        }
    }

}
