/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.search.IJsonBuilder;
import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.model.search.VeriniceQuery;
import sernet.verinice.model.search.VeriniceSearchResult;
import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.model.search.VeriniceSearchResultTable;
import sernet.verinice.search.IElementSearchDao;
import sernet.verinice.search.Indexer;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.test.helper.vnaimport.BeforeEachVNAImportHelper;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("unchecked")
@org.springframework.test.context.ContextConfiguration(locations={
        "classpath:/sernet/gs/server/spring/veriniceserver-search-base.xml", //NON-NLS-1$
        "classpath:/sernet/gs/server/spring/veriniceserver-search.xml", //NON-NLS-1$
})
public class ElasticsearchTest extends BeforeEachVNAImportHelper {

    private static final Logger LOG = Logger.getLogger(ElasticsearchTest.class);

    private static final String VNA_FILENAME = "ElasticsearchTest.vna";

    @Resource(name = "searchIndexer")
    protected Indexer searchIndexer;

    @Resource(name = "searchElementDao")
    protected IElementSearchDao searchDao;

    @Resource(name = "searchService")
    protected ISearchService searchService;

    @Resource(name = "jsonBuilder")
    protected IJsonBuilder jsonBuilder;

    final String NEW_TITEL = "SerNet NOT defined yet";
    final String TITEL = "Cryptography";

    @Test
    public void testIndexAndClear() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Running testClear()...");
        }
        searchIndexer.blockingIndexing();
        findAllElementsFromVna(true);
        searchDao.clear();
        findAllElementsFromVna(false);

    }

    @Test
    public void findLongWord() {
        searchIndexer.blockingIndexing();
        String longWord = "automatically";

        VeriniceSearchResult result = searchService
                .query(new VeriniceQuery(longWord, VeriniceQuery.MAX_LIMIT));
        VeriniceSearchResultTable entity = result.getVeriniceSearchObject(SamtTopic.TYPE_ID);
        assertNotNull("Token \"" + longWord + "\" not found in " + VNA_FILENAME, entity);

        VeriniceSearchResultRow element = result.getVeriniceSearchObject(SamtTopic.TYPE_ID)
                .getRows().iterator().next();
        assertNotNull(element.getValueFromResultString(SamtTopic.PROP_DESC),
                "Token \"" + longWord + "\" is not in the right column " + SamtTopic.PROP_DESC);

        String propertyId = element.getOccurence().getColumnIds().first();
        assertThat("Token \"" + longWord + "\" is not in the right column " + propertyId,
                element.getValueFromResultString(propertyId),
                JUnitMatchers.containsString(longWord));
    }

    @Test
    public void testUpdate() {
        searchIndexer.blockingIndexing();

        VeriniceSearchResult result = findByTitle(NEW_TITEL);
        assertEquals("Element found with string: " + NEW_TITEL, 0, result.getHits());

        result = findByTitle(TITEL);
        assertThat("No element found with ' " + TITEL + "' in title", result.getHits(),
                CoreMatchers.not(CoreMatchers.equalTo(0)));

        VeriniceSearchResultRow row = result.getAllVeriniceSearchTables().iterator().next()
                .getRows().iterator().next();
        String uuid = getUuid(row);
        CnATreeElement element = elementDao.findByUuid(uuid,
                RetrieveInfo.getPropertyInstance().setPermissions(true));
        assertNotNull("No element found with uuid: " + uuid, element);

        element.setTitel(NEW_TITEL);
        String json = jsonBuilder.getJson(element);
        assertThat("JSON does not contain " + NEW_TITEL + ":VNA_FILENAME " + json, json,
                JUnitMatchers.containsString(NEW_TITEL));

        searchDao.update(uuid, json);
        result = findByTitle(NEW_TITEL);
        assertThat("No element found with string: " + NEW_TITEL, result.getHits(),
                CoreMatchers.not(CoreMatchers.equalTo(0)));
    }

    @Test
    public void testDelete() {
        searchIndexer.blockingIndexing();
        VeriniceSearchResult result = findByTitle(TITEL);
        assertThat("No element found with ' " + TITEL + "' in title", result.getHits(),
                CoreMatchers.not(CoreMatchers.equalTo(0)));
        delete(result);
        result = findByTitle(NEW_TITEL);
        assertEquals("Element found with ' " + TITEL + "' in title", 0, result.getHits());
    }

    @Test
    public void findPhrases() {
        searchIndexer.blockingIndexing();
        String phrase = "Protection from malware";

        VeriniceSearchResult result = searchService
                .query(new VeriniceQuery(phrase, VeriniceQuery.MAX_LIMIT));
        VeriniceSearchResultTable entity = result.getVeriniceSearchObject(SamtTopic.TYPE_ID);
        assertNotNull("Phrase \"" + phrase + "\" not found in " + VNA_FILENAME, entity);

        Set<VeriniceSearchResultRow> entities = result.getVeriniceSearchObject(SamtTopic.TYPE_ID)
                .getRows();
        assertEquals("Phrase \"" + phrase + "\" should only match one time in " + VNA_FILENAME, 1,
                entities.size());

        VeriniceSearchResultRow element = result.getVeriniceSearchObject(SamtTopic.TYPE_ID)
                .getRows().iterator().next();
        String propertyId = element.getOccurence().getColumnIds().first();

        assertNotNull(element.getValueFromResultString(propertyId),
                "Phrase \"" + phrase + "\" is not in the right column " + propertyId);
        assertThat("Phrase \"" + phrase + "\" is not in the right column " + propertyId,
                element.getValueFromResultString(propertyId), JUnitMatchers.containsString(phrase));
    }

    @After
    public void tearDown() throws CommandException {
        searchDao.clear();
        super.tearDown();
    }

    private void findAllElementsFromVna(boolean expectedResult) {
        List<Object> elementList = elementDao.findByQuery(
                "select e.uuid from CnATreeElement e where e.sourceId = '1460b5'", new String[] {});
        if (LOG.isInfoEnabled()) {
            LOG.info("Number of elements to test: " + elementList.size());
        }
        for (Object uuid : elementList) {
            findElement((String) uuid, expectedResult);
        }
    }

    private void delete(VeriniceSearchResult result) {
        Set<VeriniceSearchResultTable> resultList = result.getAllVeriniceSearchTables();
        for (VeriniceSearchResultTable resultObject : resultList) {
            Set<VeriniceSearchResultRow> rows = resultObject.getAllResults();
            for (VeriniceSearchResultRow row : rows) {
                searchDao.delete(getUuid(row));
            }
        }
    }

    private void findElement(String uuid, boolean expectedResult) {
        CnATreeElement element = elementDao.findByUuid(uuid, RetrieveInfo.getPropertyInstance());
        testFindByUuid(element, expectedResult);
        if (!(element instanceof Group)) {
            testFindByTitle(element, expectedResult);
        }
    }

    private void testFindByUuid(CnATreeElement element, boolean expectedResult) {
        VeriniceQuery query = new VeriniceQuery(element.getUuid(), 1);
        VeriniceSearchResult result = searchService.query(query, element.getTypeId());
        LOG.debug(element.getUuid() + ", hits: " + result.getHits());
        boolean found = isElementInResult(result, element);
        String message = (expectedResult) ? "Element not found" : "Element found";
        assertEquals(message + ", title: " + element.getTitle() + ", uuid: " + element.getUuid(),
                expectedResult, found);
    }

    private void testFindByTitle(CnATreeElement element, boolean expectedResult) {
        VeriniceQuery query = new VeriniceQuery(element.getTitle(), 200);
        VeriniceSearchResult result = searchService.query(query, element.getTypeId());
        LOG.debug(element.getTitle() + ", hits: " + result.getHits());
        boolean found = isElementInResult(result, element);
        String message = (expectedResult) ? "Element not found" : "Element found";
        assertEquals(message + ", title: " + element.getTitle() + ", uuid: " + element.getUuid(),
                expectedResult, found);
    }

    private boolean isElementInResult(VeriniceSearchResult result, CnATreeElement element) {
        String uuid = element.getUuid();
        boolean found = (getResultRow(result, uuid) != null);
        if (found) {
            LOG.debug("Element found, title: " + element.getTitle() + ", uuid: " + uuid);
        } else {
            LOG.debug("Element not found, title: " + element.getTitle() + ", uuid: " + uuid);
        }
        return found;
    }

    private VeriniceSearchResultRow getResultRow(VeriniceSearchResult result, String uuid) {
        for (VeriniceSearchResultTable resultTable : result.getAllVeriniceSearchTables()) {
            Set<VeriniceSearchResultRow> resultRows = resultTable.getRows();
            for (VeriniceSearchResultRow resultRow : resultRows) {
                String uuidFromResult = resultRow
                        .getValueFromResultString(ISearchService.ES_FIELD_UUID);
                if (uuid.equals(uuidFromResult)) {
                    return resultRow;
                }
            }
        }
        return null;
    }

    private String getUuid(VeriniceSearchResultRow row) {
        return (String) row.getValueFromResultString(ISearchService.ES_FIELD_UUID);
    }

    private VeriniceSearchResult findByTitle(String title) {
        VeriniceQuery veriniceQuery = new VeriniceQuery(title, 200);
        VeriniceSearchResult result = searchService.query(veriniceQuery);
        return result;
    }

    @Override
    protected String getFilePath() {
        return this.getClass().getResource(VNA_FILENAME).getPath();
    }

    @Override
    protected SyncParameter getSyncParameter() throws SyncParameterException {
        return new SyncParameter(true, true, true, false,
                SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
    }

}
