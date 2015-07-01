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
package sernet.verinice.search;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyOption;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.IElementTitleCache;
import sernet.verinice.interfaces.search.IJsonBuilder;
import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class JsonBuilder implements IJsonBuilder {
    
    private static final Logger LOG = Logger.getLogger(JsonBuilder.class);
    
    private IElementTitleCache titleCache;
    
    public String getJson(CnATreeElement element) {
        if(!isIndexableElement(element)){
            return null;
        }
        String title = null;    
        if(getTitleCache()!=null && element.getScopeId()!=null) {
            title = getScopeTitle(element);
        }
        if(title==null && element.getScopeId()!=null) {
            title = element.getScopeId().toString();
        }
        return getJson(element, title);
    }

    private String getScopeTitle(CnATreeElement element) {
        String title = null;
        if(isScope(element)) {
            title = element.getTitle();
        } else {
            title = getTitleCache().get(element.getScopeId());
        }
        if(title==null) {
            LOG.warn("Scope title not found in cache for element: " + element.getUuid() + ", type: " + element.getTypeId() + ". Loading all scope titles now...");
            getTitleCache().load(new String[] {ITVerbund.TYPE_ID_HIBERNATE, Organization.TYPE_ID});
            title = getTitleCache().get(element.getScopeId());
        }
        return title;
    }

    private boolean isScope(CnATreeElement element) {
        return element instanceof ITVerbund || element instanceof Organization;
    }
    
    public static final String getJson(CnATreeElement element, String scopeTitle) {       
        try {
            String json = null;
            if(isIndexableElement(element)){
                json = doGetJson(element, scopeTitle);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(json);
            }
            return json;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }  
    }

    private static String doGetJson(CnATreeElement element, String scopeTitle) throws IOException {
        XContentBuilder builder;
        builder = XContentFactory.jsonBuilder().startObject();      
        builder.field(ISearchService.ES_FIELD_UUID, element.getUuid());
        builder.field(ISearchService.ES_FIELD_DBID, element.getDbId());
        builder.field(ISearchService.ES_FIELD_TITLE, element.getTitle());
        builder.field(ISearchService.ES_FIELD_ELEMENT_TYPE, element.getTypeId());
        builder.field(ISearchService.ES_FIELD_EXT_ID, element.getExtId());
        builder.field(ISearchService.ES_FIELD_SOURCE_ID, element.getSourceId());
        builder.field(ISearchService.ES_FIELD_SCOPE_ID, element.getScopeId());
        if(scopeTitle!=null) {
            builder.field(ISearchService.ES_FIELD_SCOPE_TITLE, scopeTitle);
        }
        builder.field(ISearchService.ES_FIELD_PARENT_ID, element.getParentId());
        builder.field(ISearchService.ES_FIELD_ICON_PATH, element.getIconPath());
       
        addPermissions(builder, element);
        
        if(element.getEntity()!=null && element.getEntityType()!=null && element.getEntityType().getAllPropertyTypeIds()!=null) {
            builder = addProperties(builder, element.getEntityType().getAllPropertyTypeIds(), element.getEntity());
        }
        return builder.endObject().string();
    }

    /**
     * @param element
     * @return
     * @throws IOException 
     */
    private static void addPermissions(XContentBuilder builder, CnATreeElement element) throws IOException {
        if(element.getPermissions()==null || element.getPermissions().isEmpty()) {
            return;
        }
        builder.startArray(ISearchService.ES_FIELD_PERMISSION_ROLES);
        for (Permission p : element.getPermissions()) {          
            int value = 0;
            if(p.isReadAllowed()) {
                value = 1;
            }
            if(p.isWriteAllowed()) {
                value = 2;
            }
            if(value > 0) {
                builder.startObject();
                builder.field( ISearchService.ES_FIELD_PERMISSION_NAME, p.getRole());
                builder.field( ISearchService.ES_FIELD_PERMISSION_VALUE, value);
                builder.endObject();
            }
        }
        builder.endArray();
    }

    private static XContentBuilder addProperties(XContentBuilder builder, String[] propertyTypeIds, Entity e) throws IOException {
        HUITypeFactory factory = HUITypeFactory.getInstance();
        for(String propertyTypeId : propertyTypeIds){
            builder.field(propertyTypeId, mapPropertyString(e, factory.getPropertyType(e.getEntityType(), propertyTypeId)));

        }
        return builder;
    }
    
    
    private static String mapPropertyString(Entity e, PropertyType pType){
        String value = e.getSimpleValue(pType.getId());
        String mappedValue = "";
        if(StringUtils.isEmpty(value)){
            mappedValue = getNullValue();
        } else if(pType.isDate()){
            mappedValue = mapDateProperty(value);
        } else if(pType.isSingleSelect() || pType.isMultiselect()){
            mappedValue = mapMultiSelectProperty(value, pType);
        } else if(pType.isNumericSelect()){
            mappedValue = mapNumericSelectProperty(value, pType);
        } else {
            mappedValue = value;
        }
        return mappedValue;
    }
    
    private static String getNullValue(){
        if(Locale.GERMAN.equals(Locale.getDefault()) || Locale.GERMANY.equals(Locale.getDefault())){
            return "unbearbeitet";
        } else {
            return "unedited";
        }
    }
    
    
    
    private static String mapNumericSelectProperty(String value, PropertyType type){
        return type.getNameForValue(Integer.parseInt(value));
    }
    
    private static String mapMultiSelectProperty(String value, PropertyType type){
        PropertyOption o = type.getOption(value);
        if(value == null || type == null || o == null){
            if(LOG.isDebugEnabled()){
                LOG.debug("No mapping for:\t" + value + "\t on <" + type.getId() + "> found, returning value");
            }
            return value;
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("Mapping for:\t" + value + "\t on <" + type.getId() + ">:\t" + o.getName());
        }
        return type.getOption(value).getName();
    }
    
    private static String mapDateProperty(String value){
        if(StringUtils.isNotEmpty(value)){
            try{
                return tryParseDate(value);
            } catch (Exception e){
                LOG.error("Error on mapping date property", e);
            }
        }
        return value;
    }
    
    private static String tryParseDate(String value){

        String[] patterns = new String[]{"EEE MMM dd HH:mm:ss zzzz yyyy",
                "dd.MM.yy",
                "EE, dd.MM.yyyy",
                "dd.MM.yyyy",
                "dd.MM.yyyy HH:mm",
                "MMM dd, yyyy hh:mm aa",
                "MMM dd, yyyy hh:mm aa",
                "yyyy-MM-dd",
        "EEE MMM dd HH:mm:ss z yyyy"};

        
        Locale[] locales = new Locale[]{null, Locale.GERMAN, Locale.GERMANY, Locale.ENGLISH, Locale.US, Locale.UK, Locale.FRANCE, Locale.FRENCH, Locale.ITALIAN, Locale.ITALY};
        for(int i = 0; i < patterns.length; i++){
            for(int j = 0; j < locales.length; j++){
                try{
                    SimpleDateFormat formatter = null;
                    if(locales[j] == null){
                        formatter = new SimpleDateFormat(patterns[i]);
                    } else {
                        formatter = new SimpleDateFormat(patterns[i], locales[j]);
                    }
                    formatter.setLenient(true);
                    Date parsedDate = formatter.parse(value);
                    return getLocalizedDatePattern().format(parsedDate);
                } catch (Exception e) {
                    // do nothing and go on with next locale
                }
            }
        }
        LOG.error("Date not parseable:\t" + value);
        return "unparseable Date:\t" + value;
    }
    
    private static DateFormat getLocalizedDatePattern(){
        return SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, Locale.getDefault());
    }
    
    private static boolean isIndexableElement(CnATreeElement element){
        if(element instanceof ImportIsoGroup || element instanceof ImportBsiGroup){
            return false;
        } else {
            return true;
        }
    }

    public IElementTitleCache getTitleCache() {
        return titleCache;
    }

    public void setTitleCache(IElementTitleCache titleCache) {
        this.titleCache = titleCache;
    }
}
