package org.intermine.web.results;

/*
 * Copyright (C) 2002-2004 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import servletunit.struts.MockStrutsTestCase;
import org.apache.struts.action.ActionMapping;

import org.intermine.objectstore.dummy.ObjectStoreDummyImpl;
import org.intermine.objectstore.query.iql.IqlQuery;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;

import org.intermine.model.testmodel.*;
import org.intermine.util.DynamicUtil;
import org.intermine.web.Constants;

public class ChangeResultsSizeActionTest extends MockStrutsTestCase
{
    public ChangeResultsSizeActionTest(String arg1) {
        super(arg1);
    }

    private PagedResults pr;
    private Results results;

    private Company company1, company2, company3;
    private Department department1, department2, department3;

    public void setUp() throws Exception {
        super.setUp();
        ObjectStoreDummyImpl os = new ObjectStoreDummyImpl();
        os.setResultsSize(15);
        IqlQuery fq = new IqlQuery("select c, d from Company as c, Department as d", "org.intermine.model.testmodel");
        results = os.execute(fq.toQuery());
        pr = new PagedResults(results);
        pr.setPageSize(5);

        // Set up some known objects in the first 3 results rows
        company1 = (Company) DynamicUtil.createObject(Collections.singleton(Company.class));
        company1.setName("Company1");
        company1.setId(new Integer(1));
        company2 = (Company) DynamicUtil.createObject(Collections.singleton(Company.class));
        company2.setName("Company2");
        company2.setId(new Integer(2));
        company3 = (Company) DynamicUtil.createObject(Collections.singleton(Company.class));
        company3.setName("Company3");
        company3.setId(new Integer(3));
        department1 = new Department();
        department1.setName("Department1");
        department1.setId(new Integer(4));
        department2 = new Department();
        department2.setName("Department2");
        department2.setId(new Integer(5));
        department3 = new Department();
        department3.setName("Department3");
        department3.setId(new Integer(6));

        ResultsRow row = new ResultsRow();
        row.add(company1);
        row.add(department1);
        os.addRow(row);
        row = new ResultsRow();
        row.add(company2);
        row.add(department2);
        os.addRow(row);
        row = new ResultsRow();
        row.add(company3);
        row.add(department3);
        os.addRow(row);
    }

    public void testChangePageSize1() throws Exception {
        setRequestPathInfo("/changeResultsSize");
        addRequestParameter("buttons(changePageSize)", "");

        ChangeResultsForm form = new MockChangeResultsForm();
        form.setPageSize("25");
        setActionForm(form);

        pr.setStart(0);
        getSession().setAttribute(Constants.RESULTS_TABLE, pr);

        actionPerform();

        verifyForward("results");
        verifyNoActionErrors();
        assertEquals(0, pr.getStart());
        assertEquals(25, pr.getPageSize());
    }

    public void testChangePageSize2() throws Exception {
        setRequestPathInfo("/changeResultsSize");
        addRequestParameter("buttons(changePageSize)", "");

        ChangeResultsForm form = new ChangeResultsForm();
        form.setPageSize("10");
        setActionForm(form);

        pr.setStart(12);
        getSession().setAttribute(Constants.RESULTS_TABLE, pr);

        actionPerform();

        verifyForward("results");
        verifyNoActionErrors();
        assertEquals(10, pr.getStart());
        assertEquals(10, pr.getPageSize());
    }

    public void testSaveNewBag() throws Exception {
        setRequestPathInfo("/changeResultsSize");
        addRequestParameter("buttons(saveNewBag)", "");
        getSession().setAttribute(Constants.RESULTS_TABLE, new PagedResults(results));

        ChangeResultsForm form = new MockChangeResultsForm();
        form.setSelectedObjects(new String[] {"0,0", "1,2"});
        form.setNewBagName("testBag1");
        setActionForm(form);

        actionPerform();

        verifyForward("results");
        verifyNoActionErrors();
        
        Map savedBags = (Map) getSession().getAttribute(Constants.SAVED_BAGS);
        Collection objs = (Collection) savedBags.get("testBag1");
        assertEquals(2, objs.size());
        Iterator iter = objs.iterator();
        assertEquals(company1, iter.next());
        assertEquals(department3, iter.next());
    }

    public void testAddToExistingBag() throws Exception {
        setRequestPathInfo("/changeResultsSize");
        addRequestParameter("buttons(addToExistingBag)", "");
        getSession().setAttribute(Constants.RESULTS_TABLE, new PagedResults(results));

        ChangeResultsForm form = new MockChangeResultsForm();
        form.setSelectedObjects(new String[] {"0,1", "1,1"});
        form.setBagName("testBag1");
        setActionForm(form);

        Collection objs = new LinkedHashSet();
        objs.add(company1);
        objs.add(department3);

        Map savedBags = new HashMap();
        savedBags.put("testBag1", objs);
        getSession().setAttribute(Constants.SAVED_BAGS, savedBags);

        actionPerform();

        verifyForward("results");
        verifyNoActionErrors();

        savedBags = (Map) getSession().getAttribute(Constants.SAVED_BAGS);
        objs = (Collection) savedBags.get("testBag1");

        assertEquals(4, objs.size());
        Iterator iter = objs.iterator();
        assertEquals(company1, iter.next());
        assertEquals(department3, iter.next());
        assertEquals(company2, iter.next());
        assertEquals(department2, iter.next());
    }

    public void testAddSameToExistingBag() throws Exception {
        setRequestPathInfo("/changeResultsSize");
        addRequestParameter("buttons(addToExistingBag)", "");
        getSession().setAttribute(Constants.RESULTS_TABLE, new PagedResults(results));

        ChangeResultsForm form = new MockChangeResultsForm();
        form.setSelectedObjects(new String[] {"0,1", "1,1"});
        form.setBagName("testBag1");
        setActionForm(form);

        Collection objs = new LinkedHashSet();
        objs.add(company1);
        objs.add(department2);

        Map savedBags = new HashMap();
        getSession().setAttribute(Constants.SAVED_BAGS, savedBags);
        savedBags.put("testBag1", objs);

        actionPerform();

        verifyForward("results");
        verifyNoActionErrors();

        savedBags = (Map) getSession().getAttribute(Constants.SAVED_BAGS);
        objs = (Collection) savedBags.get("testBag1");

        assertEquals(3, objs.size());
        Iterator iter = objs.iterator();
        assertEquals(company1, iter.next());
        assertEquals(department2, iter.next());
        assertEquals(company2, iter.next());
    }

    class MockChangeResultsForm extends ChangeResultsForm
    {
        public void reset(ActionMapping mapping, HttpServletRequest request) {
        }
    }
}
