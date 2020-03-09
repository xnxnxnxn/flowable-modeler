package com.shareniu.flowablemodeler7.service;

import org.flowable.ui.modeler.domain.Model;

public interface IHrService {

    /**
     * 部署到Hr 通过model
     *
     * @param model model
     */
    void deployToHr(Model model);

    /**
     * 部署到Hr modelId
     *
     * @param modelId modelId
     */
    void deployToHr(String modelId);

}
