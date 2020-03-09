package com.shareniu.flowablemodeler7.service.impl;

import com.shareniu.flowablemodeler7.service.IHrService;
import com.shareniu.flowablemodeler7.util.HttpIOUtil;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.ui.common.service.exception.BadRequestException;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.repository.ModelRepository;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HrServiceImpl implements IHrService {
    @Value("${hr.deploy.url}")
    private String deployUrl;

    @Autowired
    private ModelService modelService;

    @Autowired
    ModelRepository modelRepository;

    @Override
    public void deployToHr(Model model) {
        if (deployUrl == null || StringUtils.isEmpty(deployUrl)) {
            throw new BadRequestException("部署到Hr++的Url不存在");
        }
        BpmnModel bpmnModel = this.modelService.getBpmnModel(model);
        byte[] xmlBytes = this.modelService.getBpmnXML(bpmnModel);
        try {
            HttpIOUtil.sendByte(deployUrl, xmlBytes, model.getKey() + ".bpmn");
        } catch (Exception e) {
            throw new BadRequestException("部署到Hr++时出错，出错信息：", e.getMessage());
        }
    }

    @Override
    public void deployToHr(String modelId) {
        Model latestModel = modelRepository.get(modelId);
        deployToHr(latestModel);
    }
}
