package org.freejava.controller;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.freejava.manager.LocationManager;
import org.freejava.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/locations")
public class LocationController {

    @Autowired
    private LocationManager manager;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Location create(ServletRequest request) {
        String bundleId = request.getParameter("bundleId");
        String url = request.getParameter("url");
        Location location = new Location();
        location.setBundleId(Long.parseLong(bundleId));
        location.setUrl(url);
        location = manager.add(location);
        return location;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<Location> index(ServletRequest request) throws Exception {
        Map<String, Object[]> criteriaValues = new Hashtable<String, Object[]>();

        for (String paramName : new String[] {"id", "bundleId"})
        	ControllerUtils.addLongParams(request, paramName, criteriaValues);

        for (String paramName : new String[] {"url"})
            ControllerUtils.addStringParams(request, paramName, criteriaValues);

        int startPosition = ControllerUtils.getIntParameter(request, "start", 0);

        int maxResult = ControllerUtils.getIntParameter(request, "limit", 10);

        return manager.findByConditions(criteriaValues, startPosition, maxResult);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Location findLocation(@PathVariable("id") long id) throws Exception {
        return manager.findById(id);
    }

}
