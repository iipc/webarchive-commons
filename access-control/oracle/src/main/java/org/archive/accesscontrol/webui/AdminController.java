package org.archive.accesscontrol.webui;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.URIException;
import org.archive.accesscontrol.model.HibernateRuleDao;
import org.archive.accesscontrol.model.Rule;
import org.archive.accesscontrol.model.RuleSet;
import org.archive.surt.SURTTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class AdminController extends AbstractController {
    private HibernateRuleDao ruleDao;
    private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static {
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Autowired
    public AdminController(HibernateRuleDao ruleDao) {
        this.ruleDao = ruleDao;        
    }
    
    protected ModelAndView ruleList(String surt, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Long editingRuleId = null;
        if (request.getParameter("edit") != null) {
            try {
                editingRuleId = Long.decode(request.getParameter("edit"));
            } catch (NumberFormatException e) {
            }
        }
        return ruleList(surt, editingRuleId, request, response);
    }
    
    protected ModelAndView ruleList(String surt, Long editingRuleId, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        RuleSet rules = ruleDao.getRuleTree(surt);
        ArrayList<DisplayRule> ruleList = new ArrayList<DisplayRule>();
        
        for (Rule rule: rules) {
            int comparison = rule.getSurt().compareTo(surt);
            if (comparison <= 0) {
                DisplayRule displayRule = new DisplayRule(rule, comparison != 0);
                displayRule.setEditing(rule.getId().equals(editingRuleId));
                ruleList.add(displayRule);
            } else {
                // lowerRules.add(rule);
            }
        }
        Collections.sort(ruleList);
        
        model.put("rules", ruleList);
        model.put("surt", surt);
        model.put("encodedSurt", URLEncoder.encode(surt, "utf-8"));
        model.put("breadcrumbs", SurtNode.nodesFromSurt(surt));
        model.put("editingRuleId", request.getParameter("edit"));
        return new ModelAndView("list_rules", model);
    }
    
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        if (request.getParameter("saveRule") != null) {
            return saveRule(request, response);
        }
        
        
        String surt = (String) request.getAttribute("id");
        if (surt == null) {
            surt = request.getParameter("surt");
        }
        
        if (surt != null) {
            return ruleList(surt, request, response);
        }
        
        return new ModelAndView("index");
    }

    private ModelAndView saveRule(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String surt = request.getParameter("surt");
        Long ruleId = Long.decode(request.getParameter("edit"));
        Rule rule = ruleDao.getRule(ruleId);
        rule.setSurt(surt);
        rule.setPolicy(request.getParameter("policy"));
        rule.setWho(request.getParameter("who"));
        rule.setCaptureStart(parseDate(request.getParameter("captureStart")));
        rule.setCaptureEnd(parseDate(request.getParameter("captureEnd")));
        rule.setRetrievalStart(parseDate(request.getParameter("retrievalStart")));
        rule.setRetrievalEnd(parseDate(request.getParameter("retrievalEnd")));
        rule.setSecondsSinceCapture(parseInteger(request.getParameter("secondsSinceCapture")));
        ruleDao.saveRule(rule);
        
        response.setHeader("Location", request.getContextPath() + "/admin?surt=" + URLEncoder.encode(surt, "UTF-8"));
        response.setStatus(302);
        return null;
    }
    
    private Date parseDate(String s) {
        try {
            return dateFormatter.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }
    
    private Integer parseInteger(String s) {
        try {
            return Integer.decode(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
