/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.interfaces.bpm;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public interface ITask {

    /**
     * @return
     */
    String getId();
    
    /**
     * @return
     */
    String getName();
    
    /**
     * @return
     */
    String getControlTitle();

    /**
     * @return
     */
    Date getCreateDate();
    
    /**
     * @return
     */
    String getControlUuid();

    /**
     * Returns a map with outcomes of this task.
     * Key is the id of the outcome, value the translated title.
     * 
     * @return a map with outcomes
     */
    List<KeyValue> getOutcomes();
    
    /**
     * Sets the map of outcomes of this task.
     * Key is the id of the outcome, value the translated title.
     * 
     * @param outcomeMap a map with outcomes
     */
    void setOutcomes(List<KeyValue> outcomes);
    
    public class KeyValue {
            
            String key;
            
            String value;
    
            public KeyValue(String key, String value) {
                super();
                this.key = key;
                this.value = value;
            }
    
            public String getKey() {
                return key;
            }
    
            public void setKey(String key) {
                this.key = key;
            }
    
            public String getValue() {
                return value;
            }
    
            public void setValue(String value) {
                this.value = value;
            }
            
            
        }

}
