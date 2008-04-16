package org.archive.accesscontrol.webui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.accesscontrol.model.HibernateRuleDao;
import org.archive.accesscontrol.model.Rule;
import org.archive.accesscontrol.model.RuleSet;
import org.archive.surt.NewSurtTokenizer;
import org.archive.util.ArchiveUtils;
import org.archive.util.SURT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class AdminController extends AbstractController {
    private HibernateRuleDao ruleDao;
    private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final long NEW_RULE = -1L;
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
            if (request.getParameter("edit").equals("new")) {
                editingRuleId = NEW_RULE;
            } else {
                try {
                    editingRuleId = Long.decode(request.getParameter("edit"));
                } catch (NumberFormatException e) {
                }
            }
            
        }
        return ruleList(surt, editingRuleId, request, response);
    }

    /**
     * Return true if the given string appears to be a SURT.
     * @param s
     * @return
     */
    protected boolean isSurt(String s) {
        return s.charAt(0) == '(' || s.indexOf("://") == s.indexOf("://(");
    }
    
    /**
     * Perform a several cleanups on the given surt:
     *   * Convert a URL to a SURT
     *   * Add a trailing slash to SURTs of the form: http://(...)
     * @param surt
     * @return
     */
    protected String cleanSurt(String surt) {
        if (!isSurt(surt)) {
            surt = ArchiveUtils.addImpliedHttpIfNecessary(surt);
            surt = SURT.fromURI(surt);
        }
        
        if (surt.endsWith(",)") && surt.indexOf(")") == surt.length()-1) {
            surt = surt + "/";
        }
        
        return surt;
    }
    
    protected ModelAndView ruleList(String surt, Long editingRuleId, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        
        surt = cleanSurt(surt);        
        int surtSegments = new NewSurtTokenizer(surt).toList().size();
        Map<String, Object> model = new HashMap<String, Object>();
        RuleSet rules = ruleDao.getRuleTree(surt);
        ArrayList<DisplayRule> ruleList = new ArrayList<DisplayRule>();
        ArrayList<String> childSurts = new ArrayList<String>();
        
        for (Rule rule: rules) {
            int comparison = rule.getSurt().compareTo(surt);
            if (comparison <= 0) {
                DisplayRule displayRule = new DisplayRule(rule, comparison != 0);
                displayRule.setEditing(rule.getId().equals(editingRuleId));
                ruleList.add(displayRule);
            } else {
                try {
                String segment = new NewSurtTokenizer(rule.getSurt())
                            .toList().get(surtSegments);
                    if (!childSurts.contains(segment)) {
                        childSurts.add(segment);
                    }
                } catch (IndexOutOfBoundsException e) {
                }
            }
        }
        Collections.sort(ruleList);
        
        if (editingRuleId != null && editingRuleId == NEW_RULE) {
            Rule rule = new Rule();
            rule.setId(NEW_RULE);
            rule.setSurt(surt);
            
            DisplayRule newRule = new DisplayRule(rule, false);
            newRule.setEditing(true);
            ruleList.add(newRule);
        }
        
        ArrayList<String> childSurtsList = new ArrayList<String>(childSurts);
        Collections.sort(childSurtsList);
        
        model.put("rules", ruleList);
        model.put("surt", surt);
        model.put("childSurts", childSurtsList);
        model.put("encodedSurt", URLEncoder.encode(surt, "utf-8"));
        model.put("breadcrumbs", SurtNode.nodesFromSurt(surt));
        model.put("editingRuleId", request.getParameter("edit"));
        return new ModelAndView("list_rules", model);
    }
    
    protected ModelAndView redirectToSurt(HttpServletRequest request, HttpServletResponse response, String surt) throws UnsupportedEncodingException {
        response.setHeader("Location", request.getContextPath() + "/admin?surt=" + URLEncoder.encode(surt, "UTF-8"));
        response.setStatus(302);
        return null;
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
        
        if (request.getParameter("cancel") != null) {
            return redirectToSurt(request, response, surt);
        }
        
        if (request.getParameter("delete") != null) {
            return deleteRule(request, response);
        }
        
        if (surt != null) {
            return ruleList(surt, request, response);
        }
        
        return new ModelAndView("index");
    }

    private ModelAndView deleteRule(HttpServletRequest request,
            HttpServletResponse response) throws UnsupportedEncodingException {
        Long ruleId = Long.decode(request.getParameter("edit"));
        ruleDao.deleteRule(ruleId);
        return redirectToSurt(request, response, request.getParameter("surt"));
    }

    private ModelAndView saveRule(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String surt = request.getParameter("surt");
        
        Rule rule;
        Long ruleId = Long.decode(request.getParameter("edit"));
        if (ruleId == NEW_RULE) {
            rule = new Rule();
        } else {
            rule = ruleDao.getRule(ruleId);
        }
        rule.setSurt(surt);
        rule.setPolicy(request.getParameter("policy"));
        rule.setWho(request.getParameter("who"));
        rule.setCaptureStart(parseDate(request.getParameter("captureStart")));
        rule.setCaptureEnd(parseDate(request.getParameter("captureEnd")));
        rule.setRetrievalStart(parseDate(request.getParameter("retrievalStart")));
        rule.setRetrievalEnd(parseDate(request.getParameter("retrievalEnd")));
        rule.setSecondsSinceCapture(parseInteger(request.getParameter("secondsSinceCapture")));
        rule.setPrivateComment(request.getParameter("privateComment"));
        rule.setPublicComment(request.getParameter("publicComment"));
        ruleDao.saveRule(rule);
        
        return redirectToSurt(request, response, surt);
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
