package org.freejava.controller;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.commons.lang.StringUtils;
import org.freejava.manager.BundleManager;
import org.freejava.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/bundles")
public class BundleController {

    @Autowired
    private BundleManager manager;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Bundle create(ServletRequest request) {
        String origin = request.getParameter("origin");
        String md5 = request.getParameter("md5");
        String sha1 = request.getParameter("sha1");
        String sourceId = request.getParameter("sourceId");

        Bundle bundle = new Bundle();
        bundle.setOrigin(origin);
        bundle.setMd5(md5);
        bundle.setSha1(sha1);
        if (StringUtils.isNotBlank(sourceId))
            bundle.setSourceId(Long.parseLong(sourceId));

        bundle = manager.add(bundle);
        return bundle;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<Bundle> index(ServletRequest request) throws Exception {
        Map<String, Object[]> criteriaValues = new Hashtable<String, Object[]>();

        for (String paramName : new String[] {"id", "sourceId"})
        	ControllerUtils.addLongParams(request, paramName, criteriaValues);

        for (String paramName : new String[] {"md5", "sha1", "origin"})
            ControllerUtils.addStringParams(request, paramName, criteriaValues);

        int startPosition = ControllerUtils.getIntParameter(request, "start", 0);

        int maxResult = ControllerUtils.getIntParameter(request, "limit", 10);

        return manager.findByConditions(criteriaValues, startPosition, maxResult);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Bundle id(@PathVariable("id") long id) throws Exception {
        return manager.findById(id);
    }

    @RequestMapping(value = "/max", method = RequestMethod.GET)
    @ResponseBody
    public Long max() throws Exception {
        return manager.findMaxId();
    }
}
