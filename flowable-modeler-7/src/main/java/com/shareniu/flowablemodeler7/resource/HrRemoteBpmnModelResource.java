package com.shareniu.flowablemodeler7.resource;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.ui.common.service.exception.BadRequestException;
import org.flowable.ui.common.service.exception.BaseModelerRestException;
import org.flowable.ui.common.service.exception.InternalServerErrorException;
import org.flowable.ui.modeler.domain.AbstractModel;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.repository.ModelRepository;
import org.flowable.ui.modeler.rest.app.AbstractModelBpmnResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/hr")
public class HrRemoteBpmnModelResource  extends AbstractModelBpmnResource {



    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModelBpmnResource.class);

    @RequestMapping(value = "/rest/models/{processModelId}/bpmn20", method = RequestMethod.GET)
    public void getProcessModelBpmn20Xml(HttpServletResponse response, @PathVariable String processModelId) throws IOException {
        if (processModelId == null) {
            throw new BadRequestException("No process model id provided");
        } else {
            Model model = this.modelService.getModel(processModelId);
            this.generateBpmn20XmlHr(response, model);
        }
    }

    private void generateBpmn20XmlHr(HttpServletResponse response, AbstractModel model) {
//        String name = model.getName().replaceAll(" ", "_") + ".bpmn20.xml";
        String name = model.getKey()+ ".bpmn";
        String encodedName = null;

        try {
            encodedName = "UTF-8''" + URLEncoder.encode(name, "UTF-8");
        } catch (Exception var12) {
            LOGGER.warn("Failed to encode name " + name);
        }

        String contentDispositionValue = "attachment; filename=" + name;
        if (encodedName != null) {
            contentDispositionValue = contentDispositionValue + "; filename*=" + encodedName;
        }

        response.setHeader("Content-Disposition", contentDispositionValue);
        if (model.getModelEditorJson() != null) {
            try {
                ServletOutputStream servletOutputStream = response.getOutputStream();
                response.setContentType("application/xml");
                BpmnModel bpmnModel = this.modelService.getBpmnModel(model);
                byte[] xmlBytes = this.modelService.getBpmnXML(bpmnModel);
                BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(xmlBytes));
                byte[] buffer = new byte[8096];

                while(true) {
                    int count = in.read(buffer);
                    if (count == -1) {
                        servletOutputStream.flush();
                        servletOutputStream.close();
                        break;
                    }

                    servletOutputStream.write(buffer, 0, count);
                }
            } catch (BaseModelerRestException var13) {
                throw var13;
            } catch (Exception var14) {
                LOGGER.error("Could not generate BPMN 2.0 XML", var14);
                throw new InternalServerErrorException("Could not generate BPMN 2.0 xml");
            }
        }

    }

}
